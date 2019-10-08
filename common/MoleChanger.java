package common;

import java.util.TimerTask;

/**
 * A task class which is called after each interval of time passes that
 * changes the mole's up/down state.
 *
 * @author Allan Sun (as4536@rit.edu)
 * @author Aryan Jha (axj2613@rit.edu)
 */
public class MoleChanger extends TimerTask {
    /** The mole that called this task */
    private Mole mole;

    /**
     * Generate a new status changer task for the mole.
     * @param mole The mole instance this is connected to.
     */
    public MoleChanger(Mole mole){
        this.mole = mole;
    }

    @Override
    public void run() {
        mole.changeStatus();
    }
}
