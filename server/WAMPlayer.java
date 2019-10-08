package server;

import common.Duplexer;
import common.WAMException;
import common.WAMProtocol;

import java.net.Socket;
import java.util.NoSuchElementException;

/**
 * A class that manages the requests and responses to a single client.
 *
 * @author Aryan Jha (axj2613@rit.edu)
 * @author Allan Sun (as4536@rit.edu)
 */
public class WAMPlayer extends Duplexer implements Runnable {
    /** The server this player is connected to */
    private WAMServer server;
    /** The player's score */
    private int score;

    /**
     * Creates a new {@link WAMPlayer} that will use the specified
     * {@link Socket} to communicate with the client.
     * @param playerSocket The socket connected to the client.
     * @throws WAMException If an error occurs.
     */
    public WAMPlayer(Socket playerSocket, WAMServer server) throws WAMException {
        super(playerSocket);
        this.server = server;
        this.score = 0;
    }

    /**
     * Get score.
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * Runs the communication to an individual client from the server side
     */
    @Override
    public void run() {
        boolean keepGoing = true;

        while (keepGoing) {
            String request = "";

            try {
                request = receive();
            } catch (NoSuchElementException nsee){
                //Sudden close
                keepGoing = false;
            }
            String[] tokens = request.split(" ");

            switch (tokens[0]) {
                case WAMProtocol.WHACK:
                    score += server.moleWhacked(Integer.parseInt(tokens[1]));
                    send(WAMProtocol.SCORE + server.getScores());
                    break;
                default:
                    send(WAMProtocol.ERROR);
            }
        }
    }
}
