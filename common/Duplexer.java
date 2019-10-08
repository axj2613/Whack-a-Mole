package common;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A helper class which is used to send messages between the players on
 * the server and the clients.
 *
 * @author Aryan Jha (axj2613@rit.edu)
 * @author Allan Sun (as4536@rit.edu)
 */
public class Duplexer implements AutoCloseable{
    /** The socket connection this is on */
    private final Socket socket;
    /** Receives messages */
    private final Scanner scanner;
    /** Sends messages */
    private final PrintWriter writer;

    /**
     * Create a new duplexer, from either the server or client side.
     * @param socket The connection being established.
     * @throws WAMException If an error occurs when creating the connection.
     */
    public Duplexer(Socket socket) throws WAMException {
        try {
            this.socket = socket;
            scanner = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new WAMException(e);
        }
    }

    /**
     * Send a message to the other end of the socket.
     * @param message The message to be sent.
     */
    public void send(String message){
        writer.println(message);
        writer.flush();
    }

    /**
     * Receive any incoming messages from the other end of the socket.
     * @return The message, as a string.
     */
    public String receive(){
        String received = scanner.nextLine();
        //Debug: System.out.println("<< " + received);
        return received;
    }

    /** Close the connection. */
    @Override
    public void close() throws WAMException {
        try {
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
        } catch (IOException e) {
            throw new WAMException(e);
        }
    }
}
