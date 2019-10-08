package common;

import server.WAMServer;

/**
 * A class which serves as the driver for starting and ending a game.
 *
 * @author Allan Sun (as4536@rit.edu)
 * @author Aryan Jha (axj2613@rit.edu)
 */
public class WAMGame implements Runnable{
    /* The holes, which all contain moles */
    private Mole[] holes;
    /* The amount of hole rows */
    private int rowCount;
    /* The amount of hole columns */
    private int columnCount;
    /* The amount of time the game will run for, in seconds */
    private int gameTime;
    /* The server this game is associated with */
    private WAMServer server;

    /**
     * Create a new Whack-A-Mole game.
     * @param rows The amount of hole rows to have.
     * @param columns The amount of hole columns to have.
     * @param gameTime The duration of the game (in seconds).
     */
    public WAMGame(int rows, int columns, int gameTime, WAMServer server){
        rowCount = rows;
        columnCount = columns;
        holes = new Mole[rows * columns];
        createMoles();

        this.gameTime = gameTime;
        this.server = server;
    }

    /**
     * Fill each hole with a mole.
     */
    private void createMoles(){
        for (int i = 0; (i < (rowCount * columnCount)); i++){
            holes[i] = new Mole(this, i);
        }
    }

    /**
     * Start all the moles (threads).
     */
    private void startMoles(){
        Thread[] moleThreads = new Thread[holes.length];

        for (int i = 0; i < holes.length; i++){
            moleThreads[i] = new Thread(holes[i]);
        }

        for (Thread thread: moleThreads){
            thread.start();
        }
    }

    /**
     * Access all the moles.
     * @return The array containing all the mole threads.
     */
    public Mole[] getAllMoles(){
        return this.holes;
    }

    /**
     * Called by the mole whenever its status changes, so the server can let
     * all the players know the mole changed.
     * @param moleNum The number of the mole that changed.
     * @param isUp True if the mole is now up, false if it is now down.
     */
    public void moleStatusChanged(int moleNum, boolean isUp){
        server.moleChanged(moleNum, isUp);
    }

    /**
     * Starts the game once connections are established.
     */
    @Override
    public void run() {
        startMoles();

        try {
            Thread.sleep(gameTime * 1000);
        } catch (InterruptedException e) {
            //Squash
        }

        server.endGame();
        for (Mole mole: holes){
            mole.finished();
        }

        System.out.println("Game finished!");
    }
}
