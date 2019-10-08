package common;

import java.util.Random;
import java.util.Timer;

/**
 * Represents a mole which goes up and down and can be whacked by players.
 *
 * @author Allan Sun (as4536@rit.edu)
 * @author Aryan Jha (axj2613@rit.edu)
 */
public class Mole implements Runnable{
    /* The mole number */
    private int number;
    /* The game the mole is associated with */
    private WAMGame game;
    /* If true, the mole is up; if false, the mole is down */
    private boolean upStatus;
    /* Used to see if the mole has been clicked already when it's up, in
     * case of multiple clicks */
    private boolean clicked;
    /* The game is still running if its countdown timer has not finished yet */
    private boolean gameRunning;
    /* A timer which takes tasks that changes the mole after a set amount of time */
    private Timer changeTimer;


    /**
     * Create a new mole.
     * @param game The instance of the game that created the mole.
     * @param number The mole number in the game.
     */
    public Mole(WAMGame game, int number){
        this.number = number;
        this.game = game;
        this.upStatus = false; //Down by default
        this.clicked = false;
        this.gameRunning = false;
        this.changeTimer = new Timer();
    }

    /**
     * Make the mole go up if it's down, and down if it's up.
     */
    public void changeStatus(){
        if (this.upStatus){
            this.upStatus = false;
            game.moleStatusChanged(this.number, false);
        }else{
            this.upStatus = true;
            clicked = false;
            game.moleStatusChanged(this.number, true);
        }
    }

    /**
     * Check the mole's up/down status.
     * @return True if the mole is up, false if the mole is down.
     */
    public boolean getUpStatus(){
        return this.upStatus;
    }

    /**
     * Check whether this mole has been clicked yet or not.
     * @return True if it has been clicked, false if not.
     */
    public boolean getClicked(){
        return this.clicked;
    }

    /**
     * This method is called when the mole gets clicked for the first time
     * while it's up. It forcibly changes the mole's status.
     */
    public void moleClicked(){
        this.clicked = true;
        this.changeStatus();
    }

    /**
     * Stop the mole when the game has finished.
     */
    public void finished(){
        if (upStatus){
            changeStatus();
        }
        changeTimer.cancel();
        gameRunning = false;
    }

    /**
     * Have this mole start going up and down randomly.
     */
    @Override
    public void run() {
        Random RNG = new Random();
        this.gameRunning = true;

        while (gameRunning){
            int downSeconds = (RNG.nextInt(6 - 2) + 2) * 1000; //Stays down between 2 and 6 seconds

            try {
                changeTimer.schedule(new MoleChanger(this), downSeconds);
            }catch(IllegalStateException ise){
                //When the game ends but the loop has yet to break
            }
            try {
                Thread.sleep(downSeconds);
            } catch (InterruptedException e) {
                //Squash
            }

            int upSeconds = (RNG.nextInt(2 - 1) + 1) * 1000; //Stays up between 1 and 2 seconds
            try{
                changeTimer.schedule(new MoleChanger(this), upSeconds);
            }catch(IllegalStateException ise){
                //Squash
            }
            try {
                Thread.sleep(upSeconds);
            } catch (InterruptedException e) {
                //Squash
            }
        }
    }
}
