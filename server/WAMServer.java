package server;

import common.Mole;
import common.WAMException;
import common.WAMGame;
import common.WAMProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


/**
 * The {@link WAMServer} waits for incoming client connections and
 * pairs them off to play {@link WAMGame} games.
 *
 * @author Aryan Jha (axj2613@rit.edu)
 * @author Allan Sun (as4536@rit.edu)
 */
public class WAMServer implements Runnable{
    /** The {@link ServerSocket} used to wait for incoming client connections. */
    private ServerSocket server;
    /** Number of rows on board. */
    private int rowCount;
    /** Number of columns on board. */
    private int colCount;
    /** Number of players. */
    private int playerCount;
    /** Game running time. */
    private int gameTime;
    /** All the player connections to the clients */
    private WAMPlayer[] players;
    /** The game that is currently running */
    private WAMGame game;

    /**
     * Creates a new {@link WAMServer} that listens for incoming
     * connections on the specified port.
     * @param port Port number the game will be on.
     * @param rowCount The amount of hole rows to have.
     * @param colCount The amount of hole columns to have.
     * @param playerCount The amount of players to have.
     * @param gameTime The amount of time the game should run for (in seconds).
     * @throws WAMException If an error occurs in accepting connections.
     */
    public WAMServer(int port, int rowCount, int colCount, int playerCount, int gameTime) throws WAMException {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new WAMException(e);
        }
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.playerCount = playerCount;
        this.gameTime = gameTime;
        this.players = new WAMPlayer[playerCount];
    }

    /**
     * Message sent from the server to all clients to inform
     * that a mole has changed its status to either MOLE_UP or
     * MOLE_DOWN.
     * @param moleNum The mole number that changed.
     * @param upStatus True if the mole is now up, false if it is now down.
     */
    public void moleChanged(int moleNum, boolean upStatus) {
        for (WAMPlayer player : players) {
            if (upStatus) {
                player.send(WAMProtocol.MOLE_UP + " " + moleNum);
            } else {
                player.send(WAMProtocol.MOLE_DOWN + " " + moleNum);
            }
        }
    }

    /**
     * Called when a mole is whacked to make changes to score.
     * @param moleNumber The mole number that was whacked.
     * @return score change
     */
    public int moleWhacked(int moleNumber) {
        Mole mole = game.getAllMoles()[moleNumber];
        if(mole.getUpStatus() && !mole.getClicked()) { // Mole is up and hasn't been clicked already
            mole.moleClicked();
            return 2;
        } else {
            return -1;
        }
    }

    /**
     * Get scores.
     * @return all scores as a String
     */
    public String getScores(){
        String message = "";
        for(WAMPlayer player : players) {
            message += " " + player.getScore();
        }
        return message;
    }

    /**
     * Called when gameTime has expired to check for winners and losers,
     * and to close the players and the server
     */
    public void endGame() {
        int hiScore = players[0].getScore();
        //Start off with first player score by default

        for (WAMPlayer player : players) {
            if (player.getScore() > hiScore) {
                hiScore = player.getScore();
            }
        }

        ArrayList<WAMPlayer> winners = new ArrayList<>();
        ArrayList<WAMPlayer> losers = new ArrayList<>();
        for (WAMPlayer player : players) {
            if (player.getScore() == hiScore) {
                winners.add(player);
            } else {
                losers.add(player);
            }
        }

        if (winners.size() == 1) {
            winners.get(0).send(WAMProtocol.GAME_WON);
        } else {
            for (WAMPlayer winner : winners) {
                winner.send(WAMProtocol.GAME_TIED);
            }
        }

        for (WAMPlayer loser : losers) {
            loser.send(WAMProtocol.GAME_LOST);
        }

        //Game has now ended, close all WAMPlayer connections, clients will close upon GUI exit
        for (WAMPlayer player : players) {
            try {
                player.close();
            } catch (WAMException e) {
                //Squash
            }
        }
        try {
            server.close();
        } catch (IOException e) {
            //Squash
        }
        for (Mole mole : game.getAllMoles()) {
            mole.finished();
        }
    }

    /**
     * Waits for playerCount number of clients to connect. Creates a thread
     * for each and then pairs an array of them off in a {@link WAMGame}.<P>
     */
    @Override
    public void run() {
        try {
            for(int p = 1; p <= playerCount; p++) {
                System.out.println("Waiting for player " + p + "...");
                WAMPlayer playerSocket = new WAMPlayer(server.accept(), this);
                playerSocket.send(WAMProtocol.WELCOME + " " + rowCount + " " +
                        colCount + " " + playerCount + " " + (p-1));
                players[p-1] = playerSocket;
                Thread player = new Thread(playerSocket);
                player.start();
                System.out.println("Player " + p + " connected!");
            }

            System.out.println("Starting game!");
            this.game = new WAMGame(rowCount, colCount, gameTime, this);
            new Thread(game).run();
        } catch (IOException e) {
            System.err.println("Something has gone horribly wrong!");
            for(WAMPlayer player : players) {
                player.send(WAMProtocol.ERROR);
            }
            e.printStackTrace();
        } catch (WAMException e) {
            System.err.println("Failed to create players!");
            for(WAMPlayer player : players) {
                player.send(WAMProtocol.ERROR);
            }
            e.printStackTrace();
        }

    }

    /**
     * Starts a new {@link WAMServer}. Simply creates the server and
     * calls {@link #run()} in the main thread.
     * @param args Command line arguments.
     * @throws WAMException If the game fails to start.
     */
    public static void main(String[] args) throws WAMException {
        if (args.length != 5) {
            System.out.println("Usage: java WAMServer <port> <number of rows> " +
                    "<number of columns> <number of players> <game running time>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int rowCount = Integer.parseInt(args[1]);
        int colCount = Integer.parseInt(args[2]);
        int playerCount = Integer.parseInt(args[3]);
        int gameTime = Integer.parseInt(args[4]);
        WAMServer server = new WAMServer(port, rowCount, colCount, playerCount, gameTime);

        server.run();
    }
}
