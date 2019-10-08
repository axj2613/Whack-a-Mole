package client.gui;

import common.Duplexer;
import common.WAMException;
import common.WAMProtocol;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * The client side network interface to a WAM game server.
 * Each of the players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Aryan Jha (axj2613@rit.edu)
 * @author Allan Sun (as4536@rit.edu)
 */
public class WAMNetworkClient extends Duplexer implements Runnable{

    /** number of rows on board */
    private int rowCount;
    /** number of columns on board */
    private int colCount;
    /** number of players (fixed for the game) */
    private int playerCount;
    /** the player number */
    private int playerNum;
    /** GUI */
    private WAMGUI observer;
    /** boolean argument for client thread */
    private boolean keepGoing;

    /**
     * Hook up with a WAM game server already running and waiting for
     * players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message (WELCOME) from the server. Afterwards a thread that listens for
     * server messages and forwards them to the game object is started.
     * @param host
     * @param port
     * @throws WAMException
     * @throws IOException
     */
    public WAMNetworkClient(String host, int port) throws WAMException, IOException {
            super(new Socket(host, port));

            // Block waiting for the WELCOME message from the server.
            String request = receive();
            String[] arguments = request.split(" ");
            if (!arguments[0].equals(WAMProtocol.WELCOME )) {
                throw new WAMException("Expected WELCOME from server");
            }
            rowCount = Integer.parseInt(arguments[1]);
            colCount = Integer.parseInt(arguments[2]);
            playerCount = Integer.parseInt(arguments[3]);
            playerNum = Integer.parseInt(arguments[4]);

            this.keepGoing = true;
    }

    /**
     * Get row count.
     * @return rowCount
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Get column count.
     * @return colCount
     */
    public int getColCount() {
        return colCount;
    }

    /**
     * Get player count.
     * @return playerCount
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Get player number.
     * @return playerNum
     */
    public int getPlayerNum() {
        return playerNum;
    }

    /**
     * Register a GUI instance to observe changes that happen on this client.
     * @param gui The GUI associated with this client.
     */
    public void registerGUI(WAMGUI gui){
        this.observer = gui;
    }

    /**
     * Begin listening for server messages once the GUI is set up.
     */
    public void startListener(){
        new Thread(() -> this.run()).start();
    }

    /**
     * From client: inform server that it has whacked a mole.
     * @param moleNum
     */
    public void whackMade(int moleNum) {
        send(WAMProtocol.WHACK + " " + moleNum + " " + playerNum);
    }

    /**
     * Close the connection.
     */
    public void close(){
        try {
            keepGoing = false;
            super.close();
        } catch (WAMException e) {
            //Squash
        }
    }

    /**
     * Runs the main client loop that listens for
     * server messages and forwards them to the game object
     */
    @Override
    public void run() {
        while(keepGoing){
            String receive = "";

            try {
                receive = receive();
            } catch (NoSuchElementException nsee){
                //Getting this error means the GUI was closed forcibly
                close();
            }

            String[] tokens = receive.split(" ");

            switch (tokens[0]) {
                case WAMProtocol.MOLE_UP:
                    observer.moleChanged(Integer.parseInt(tokens[1]), WAMProtocol.MOLE_UP);
                    break;
                case WAMProtocol.MOLE_DOWN:
                    observer.moleChanged(Integer.parseInt(tokens[1]), WAMProtocol.MOLE_DOWN);
                    break;
                case WAMProtocol.SCORE:
                    observer.scoreChanged(Arrays.copyOfRange(tokens, 1, tokens.length));
                    break;
                case WAMProtocol.GAME_WON:
                    observer.statusChanged(WAMProtocol.GAME_WON);
                    this.close();
                    break;
                case WAMProtocol.GAME_LOST:
                    observer.statusChanged(WAMProtocol.GAME_LOST);
                    this.close();
                    break;
                case WAMProtocol.GAME_TIED:
                    observer.statusChanged(WAMProtocol.GAME_TIED);
                    this.close();
                    break;
                case WAMProtocol.ERROR:
                    System.err.println("Server error");
                    this.close();
                    break;
            }
        }
    }
}
