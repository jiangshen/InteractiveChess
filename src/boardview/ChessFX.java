package boardview;

//Chess Model
import model.Side;

//Game Controllers
import gamecontrol.GameController;
import gamecontrol.ChessController;
import gamecontrol.AIChessController;
import gamecontrol.NetworkedChessController;

//Exceptions
import java.io.IOException;
import java.net.UnknownHostException;

import java.net.InetAddress;

//JavaFX
import javafx.application.Application;
import javafx.event.EventHandler;

//JavaFX UI
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
//Layout and Bounds
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
//Shapes
import javafx.scene.shape.Rectangle;
//Text and Font
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
//Menubar and related items
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
//Menubar input
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
//Toolbar and related controls
import javafx.scene.control.ToolBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
//Titled Pane and related items
import javafx.scene.control.TitledPane;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
//Alert Dialogs
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * Main class for the chess application
 * Sets up the top level of the GUI
 * @author Gustavo
 * @version 1.0
 */
public class ChessFX extends Application {

    private GameController controller;

    private MenuBar menuBar;
    private RadioMenuItem playerGameMenuItem;
    private RadioMenuItem aiGameMenuItem;
    private RadioMenuItem networkGameMenuItem;
    private RadioMenuItem whiteSideMenuItem;
    private RadioMenuItem blackSideMenuItem;

    //main VBox elements
    private VBox subRoot;
    private BoardView board;
    private ToolBar tool;
    private Text state;
    private Text sideStatus;
    private Rectangle sideRect;
    private Text tType;

    //side pane elements
    private VBox sidePane;
    private TitledPane lvlHistoryHolder;
    private ListView<String> lvHistory;
    private TitledPane whiteEats;
    private Label blackPiecesEaten;
    private TitledPane blackEats;
    private Label whitePiecesEaten;
    private TitledPane lblCurrentPieceHolder;
    private Label lblCurrentPiece;

    private HBox root;
    private VBox masterRoot;

    private Side currentGameSide;
    private GameType currentGameType;
    private String ip;
    private ObservableList<String> moveHistory;

    @Override
    public void start(Stage primaryStage) {

        subRoot = new VBox();
        sidePane = new VBox(20);
        root = new HBox();
        masterRoot = new VBox();
        currentGameSide = Side.WHITE;

        //Menu Bar
        menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        Menu gameMenu = new Menu("Game");
        Menu newGameMenu = new Menu("New Game");
        ToggleGroup newGameMenuGrp = new ToggleGroup();
        playerGameMenuItem = new RadioMenuItem("Player");
        playerGameMenuItem.setOnAction(actionEvent ->
            newGameWithConfirmation(GameType.Player));
        playerGameMenuItem.setToggleGroup(newGameMenuGrp);
        aiGameMenuItem = new RadioMenuItem("Computer");
        aiGameMenuItem.setToggleGroup(newGameMenuGrp);
        aiGameMenuItem.setOnAction(actionEvent ->
            newGameWithConfirmation(GameType.Computer));
        networkGameMenuItem = new RadioMenuItem("Network...");
        networkGameMenuItem.setToggleGroup(newGameMenuGrp);
        networkGameMenuItem.setOnAction(actionEvent ->
            newGameWithConfirmation(GameType.Network));
        newGameMenu.getItems().addAll(playerGameMenuItem, aiGameMenuItem,
            networkGameMenuItem);
        MenuItem resetGameMenuItem = new MenuItem("Reset Game");
        resetGameMenuItem.setOnAction(actionEvent -> resetGame());
        MenuItem exitGameMenuItem = new MenuItem("Exit Window");
        exitGameMenuItem.setOnAction(actionEvent -> exitGame());
        gameMenu.getItems().addAll(newGameMenu, resetGameMenuItem,
            new SeparatorMenuItem(), exitGameMenuItem);

        //wanted to set player starts on which side
        //for now since only AIChessController has the relevant constructor
        //it only works on a new AI Game
        Menu playerMenu = new Menu("Player");
        ToggleGroup playerGameMenuGrp = new ToggleGroup();
        whiteSideMenuItem = new RadioMenuItem("Start Computer Match on White");
        whiteSideMenuItem.setToggleGroup(playerGameMenuGrp);
        whiteSideMenuItem.setOnAction(actionEvent ->
            setCurrentPlayerSide("White"));
        whiteSideMenuItem.setSelected(true);
        blackSideMenuItem = new RadioMenuItem("Start Computer Match on Black");
        blackSideMenuItem.setToggleGroup(playerGameMenuGrp);
        blackSideMenuItem.setOnAction(actionEvent ->
            setCurrentPlayerSide("Black"));
        playerMenu.getItems().addAll(whiteSideMenuItem, blackSideMenuItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutMenuItem = new MenuItem("About Interactive Chess");
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Interactive Chess");
        alert.setHeaderText("About Interactive Chess\nVersion 1.0");
        alert.setContentText("CS1331 Homework 5 with JavaFX UI Styling :)\n\n");
        aboutMenuItem.setOnAction(actionEvent -> alert.showAndWait());
        helpMenu.getItems().addAll(aboutMenuItem);
        menuBar.getMenus().addAll(gameMenu, playerMenu, helpMenu);
        //Display menu and keyboard shortcuts based on different OS
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            //OS X
            menuBar.useSystemMenuBarProperty().set(true);
            resetGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R,
                KeyCombination.META_DOWN));
            exitGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.W,
                KeyCombination.META_DOWN));
        } else {
            //Windows || Linux
            resetGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R,
                KeyCombination.CONTROL_DOWN));
            exitGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.X,
                KeyCombination.CONTROL_DOWN));
        }

        //Main Code Initialization
        currentGameSide = Side.WHITE;
        tType = new Text("");
        tType.setFill(Color.INDIGO);
        controller = newGame(GameType.Player);
        state = new Text("Ready");
        state.setFont(Font.font(null, FontWeight.BOLD, 14));
        sideRect = new Rectangle(14, 14);
        sideRect.setFill(Color.WHITE);
        sideRect.setStroke(Color.LIGHTGRAY);
        sideStatus = new Text("");

        //Side Pane
        moveHistory = FXCollections.observableArrayList();
        lvHistory = new ListView<String>(moveHistory);
        lvHistory.setPrefWidth(10);
        lvHistory.setPrefHeight(200);
        lvlHistoryHolder = new TitledPane("Moves History", lvHistory);
        lvlHistoryHolder.setCollapsible(false);
        whitePiecesEaten = new Label("None");
        whitePiecesEaten.setFont(Font.font(null, FontWeight.BOLD, 22));
        whitePiecesEaten.setPadding(new Insets(5, 5, 5, 5));
        whitePiecesEaten.setWrapText(true);
        blackPiecesEaten = new Label("None");
        blackPiecesEaten.setPadding(new Insets(5, 5, 5, 5));
        blackPiecesEaten.setWrapText(true);
        blackPiecesEaten.setFont(Font.font(null, FontWeight.BOLD, 22));
        whiteEats = new TitledPane(
            "White Chess Pieces Lost", blackPiecesEaten);
        whiteEats.setCollapsible(false);
        blackEats = new TitledPane(
            "Black Chess Pieces Lost", whitePiecesEaten);
        blackEats.setCollapsible(false);
        lblCurrentPiece = new Label("");
        lblCurrentPiece.setFont(Font.font(null, FontWeight.BOLD, 70));
        lblCurrentPieceHolder = new TitledPane("Current Piece",
            lblCurrentPiece);
        lblCurrentPieceHolder.setCollapsible(false);

        sidePane.setPadding(new Insets(20, 20, 20, 20));

        board = new BoardView(controller, state, sideRect, sideStatus,
            playerGameMenuItem, tType, blackPiecesEaten, whitePiecesEaten,
            moveHistory, lblCurrentPiece);

        //ToolBar
        tool = new ToolBar();
        tool.getItems().addAll(state, new Separator(), tType, new Separator(),
            sideRect, sideStatus);

        //Populate Subroot VBox
        subRoot.getChildren().addAll(board.getView());

        //Populate Side Pane
        sidePane.setStyle("-fx-background: rgb(255, 255, 255);");
        sidePane.getChildren().addAll(lvlHistoryHolder, whiteEats, blackEats,
            lblCurrentPieceHolder);

        //Populate Root HBox
        root.getChildren().addAll(subRoot, sidePane);

        //Populate Master Root VBox
        masterRoot.setStyle("-fx-background: rgb(229, 229, 229);");
        masterRoot.getChildren().addAll(menuBar, root, tool);

        //Populate Stage
        primaryStage.setScene(new Scene(masterRoot));
        primaryStage.setTitle("Interactive Chess");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private EventHandler<? super MouseEvent> makeHostListener() {
        return event -> {
            board.reset(new NetworkedChessController());
        };
    }

    private EventHandler<? super MouseEvent> makeJoinListener(TextField input) {
        return event -> {
            try {
                InetAddress addr = InetAddress.getByName(input.getText());
                GameController newController
                    = new NetworkedChessController(addr);
                board.reset(newController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * Resets to a new game while checking whether current game is in progress
     * @param type the current type of the game
     */
    private void newGameWithConfirmation(GameType type) {
        if (board.isInGame()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Interactive Chess");
            alert.setHeaderText("♟ New " + type.toString() + " Game");
            alert.setContentText("Game has started, creating a new game will "
                + "lose current progress.\n\nContinue?\n\n");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                board.reset(newGame(type));
            }
        } else {
            board.reset(newGame(type));
        }
        board.setCurrentGameType(type);
    }

    /**
     * creates a new game
     * @param  type the game type
     * @return      the new Game Controller
     */
    private GameController newGame(GameType type) {
        GameController newGameController = null;

        if (type == GameType.Player) {
            newGameController = new ChessController();
            playerGameMenuItem.setSelected(true);
        } else if (type == GameType.Computer) {
            newGameController = new AIChessController(currentGameSide);
            aiGameMenuItem.setSelected(true);
        } else {
            newGameController = newNetworkGame();
            networkGameMenuItem.setSelected(true);
        }

        tType.setText(type.toString() + " Match");
        currentGameType = type;

        return newGameController;
    }

    /**
     * Creates a new networked game
     * @return the new Network Game Controller
     */
    private GameController newNetworkGame() {

        GameController newNetworkGameController = null;

        try {
            ip = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            ip = "[Error] Unable to retrieve your IP address";
            e.printStackTrace();
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Interactive Chess");
        alert.setHeaderText("♞ New network game");
        alert.setContentText("Your IP is: " + ip);

        ButtonType btnHost = new ButtonType("Host");
        ButtonType btnJoin = new ButtonType("Join...");

        alert.getButtonTypes().setAll(btnHost, btnJoin);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == btnHost) {
            newNetworkGameController = new NetworkedChessController();
        } else if (result.get() == btnJoin) {
            TextInputDialog dialog = new TextInputDialog("");
            TextField inputText = dialog.getEditor();
            inputText.setPromptText("a.b.c.x");

            dialog.setTitle("Interactive Chess");
            dialog.setHeaderText("Enter opponent IP address");
            dialog.setContentText(null);

            Optional<String> res = dialog.showAndWait();
            if (res.isPresent()) {
                try {
                    newNetworkGameController = new NetworkedChessController(
                        InetAddress.getByName(inputText.getText()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

        //update MenuItem and code
        networkGameMenuItem.setSelected(true);
        currentGameType = GameType.Network;
        board.setCurrentGameType(GameType.Network);

        return newNetworkGameController;
    }

    /**
     * Set the current playing side
     * @param s the current side
     */
    private void setCurrentPlayerSide(String s) {
        if (s.equals("White")) {
            currentGameSide = Side.WHITE;
        } else {
            currentGameSide = Side.BLACK;
        }
    }

    /**
     * Resets the current game
     */
    private void resetGame() {
        refreshCurrentGameType();
        if (board.isInGame()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Interactive Chess");
            alert.setHeaderText("♜ Reset Game");
            alert.setContentText("Game has started, resetting will lose all "
                + "all progress.\n\nContinue?\n\n");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                if (currentGameType == GameType.Network) {
                    //reset to new network game
                    newNetworkGame();
                } else {
                    //reset to normal game
                    board.reset(newGame(currentGameType));
                }
            }
        } else {
            if (currentGameType == GameType.Computer) {
                board.reset(newGame(currentGameType));
            } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Interactive Chess");
                alert.setHeaderText(null);
                alert.setContentText("Game has not started, nothing to reset");
                alert.showAndWait();
            }
        }
    }

    /**
     * Exits the game
     */
    private void exitGame() {
        if (board.isInGame()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Interactive Chess");
            alert.setHeaderText("♝ Exit Chess");
            alert.setContentText("Game has started, exiting will lose all "
                + "progress.\n\nAre you sure?\n\n");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Platform.exit();
            }
        } else {
            Platform.exit();
        }
    }

    /**
     * Updates the currentGameType enum variable
     */
    private void refreshCurrentGameType() {
        if (tType.getText().equals("Player Match")) {
            currentGameType = GameType.Player;
        } else if (tType.getText().equals("Computer Match")) {
            currentGameType = GameType.Computer;
        } else {
            currentGameType = GameType.Network;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Enum displaying the possible game types
     */
    public enum GameType {
        Player, Computer, Network;
    }
}
