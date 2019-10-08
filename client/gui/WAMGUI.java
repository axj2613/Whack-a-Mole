package client.gui;

import common.WAMProtocol;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

/**
 * A visual interface which will be used for the player to play the game.
 * @author Allan Sun (as4536@rit.edu)
 * @author Aryan Jha (axj2613@rit.edu)
 */
public class WAMGUI extends Application {
    /* Stores all the images displaying moles */
    private ImageView[] allMoles;
    /* Visual display for all the moles */
    private GridPane holes;
    /* The image shown when a mole is up */
    private Image moleUp;
    /* The image shown when a mole is down */
    private Image moleDown;
    /* The amount of rows in the game being played */
    private int rowCount;
    /* The amount of columns in the game being played */
    private int colCount;
    /* Text displaying the amount of points of each player */
    private Label scoresText;
    /* Test displaying the status of the game */
    private Label statusText;
    /* connection to network interface to server */
    private WAMNetworkClient serverConn;

    @Override
    public void init(){
        try {
            List<String> args = getParameters().getRaw();

            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            this.serverConn = new WAMNetworkClient(host, port);
            serverConn.registerGUI(this);

            this.rowCount = serverConn.getRowCount();
            this.colCount = serverConn.getColCount();
        } catch (Exception e){
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate the visual display for the moles.
     */
    private void makeMoles(){
        int row = 0;
        int col = 0;

        for (int i = 0; i < allMoles.length; i++){
            ImageView mole = new ImageView(moleDown);
            allMoles[i] = mole;

            if (col == colCount){
                row++;
                col = 0;
            }

            int moleNumber = i;
            mole.setOnMouseClicked(e -> serverConn.whackMade(moleNumber));
            holes.add(mole, col, row);
            col++;
        }
    }


    /**
     * Sets up the GUI display for the game.
     * @param stage The window where the GUI will be rendered.
     * @throws Exception If an error occurs.
     */
    @Override
    public void start(Stage stage) throws Exception {
        Label titleText = new Label("Whack A Monty");
        titleText.setTextFill(Color.web("#d65604"));
        titleText.setFont(Font.font("Magneto",colCount * 8.44));
        titleText.widthProperty().asObject();
        Image titleBG = new Image(getClass().getResourceAsStream("grass.png"));
        titleText.setBackground(new Background(new BackgroundImage(titleBG,
                null, null, null, BackgroundSize.DEFAULT)));
        titleText.setAlignment(Pos.CENTER);
        titleText.setPadding(new Insets(0, 0, 5, 0));

        String scoreDisplayText = "";
        for (int i = 0; i < serverConn.getPlayerCount(); i++){
            if (i == serverConn.getPlayerNum()){
                scoreDisplayText += "Your score: 0\n";
            } else{
                scoreDisplayText += "Player " + (i + 1) + " score: 0\n";
            }
        }
        scoresText = new Label(scoreDisplayText);
        scoresText.setPrefWidth(colCount * 75);
        scoresText.setBackground(new Background(new BackgroundFill(Color.SANDYBROWN, null, null)));
        scoresText.setAlignment(Pos.CENTER);
        scoresText.setFont(new Font("Arial Black", 15));

        this.allMoles = new ImageView[rowCount * colCount];
        moleUp = new Image(getClass().getResourceAsStream("moleup.png"));
        moleDown = new Image(getClass().getResourceAsStream("moledown.png"));
        holes = new GridPane();
        makeMoles();


        statusText = new Label("You are playing the game.");
        statusText.setPrefWidth(colCount * 75);
        statusText.setBackground(new Background(new BackgroundFill(Color.SANDYBROWN, null, null)));
        statusText.setAlignment(Pos.CENTER);


        VBox mainLayout = new VBox(titleText, scoresText, holes, statusText);

        Scene mainScene = new Scene(mainLayout);
        stage.setScene(mainScene);
        stage.show();
        stage.setResizable(false);

        serverConn.startListener();
    }

    /**
     * Close the connection once the GUI is closed.
     */
    @Override
    public void stop(){
        serverConn.close();
    }

    /**
     * Change a mole to be up or down.
     * @param num The mole number.
     */
    private void changeMole(int num, String status){
        ImageView theMole = allMoles[num];

        if (status.equals(WAMProtocol.MOLE_UP)) {
            theMole.setImage(moleUp);
        } else if (status.equals(WAMProtocol.MOLE_DOWN)){
            theMole.setImage(moleDown);
        }
    }

    /**
     * Called by the client (controller) when it receives a MOLE_UP or MOLE_DOWN message
     * from the server.
     * @param num The mole number that changed. (Starts from 0)
     */
    public void moleChanged(int num, String status){
        if (Platform.isFxApplicationThread()){
            this.changeMole(num, status);
        }else{
            Platform.runLater(() -> this.changeMole(num, status));
        }
    }

    /**
     * Update the scores being displayed to the user.
     * @param scores The scores of each player playing the game.
     */
    private void updateScore(String[] scores){
        String scoreDisplayText = "";

        for (int i = 0; i < serverConn.getPlayerCount(); i++){
            if (i == serverConn.getPlayerNum()){
                scoreDisplayText += "Your score: " + scores[i] + "\n";
            }else{
                scoreDisplayText += "Player " + (i + 1) + " score: " + scores[i] + "\n";
            }
        }

        scoresText.setText(scoreDisplayText);
    }

    /**
     * Called by the client (controller) when it receives a SCORE message.
     * @param scores The scores of each player playing the game.
     */
    public void scoreChanged(String[] scores){
        if (Platform.isFxApplicationThread()){
            updateScore(scores);
        }else{
            Platform.runLater(() -> updateScore(scores));
        }
    }

    /**
     * Update the status text being shown to the user.
     * @param status The status of the game (GAME_WON, GAME_LOST, GAME_TIED, etc).
     */
    private void updateStatus(String status){
        if(status.equals(WAMProtocol.GAME_WON)) {
            statusText.setText("Congratulations! You won. #BraggingRights");
        } if(status.equals(WAMProtocol.GAME_LOST)) {
            statusText.setText("BOO! You have lost the game.");
        } if(status.equals(WAMProtocol.GAME_TIED)) {
            statusText.setText("You guys were neck and neck.");
        }
    }

    /**
     * Called by the client (controller) when the status of the game
     * changes.
     * @param status The status of the game now for the player (win, lose, tie, error).
     */
    public void statusChanged(String status){
        if (Platform.isFxApplicationThread()){
            updateStatus(status);
        }else{
            Platform.runLater(() -> updateStatus(status));
        }
    }

    /**
     * Initiate this player with a connection to the host and port where the game
     * will be run.
     * @param args Command line arguments
     */
    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("Usage: java WAMGUI <hostname> <port>");
            System.exit(-1);
        }else{
            Application.launch(args);
        }
    }
}
