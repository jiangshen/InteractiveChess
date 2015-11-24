package boardview;

import java.util.List;
import java.util.Map;
import gamecontrol.GameController;
import gamecontrol.GameState;
import gamecontrol.NetworkedChessController;
import gamecontrol.ChessController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import model.Move;
import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;
import model.chess.ChessPiece;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Set;
import java.util.ArrayList;
import javafx.scene.control.ChoiceDialog;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import model.IllegalMoveException;
import javafx.scene.control.Label;
import javafx.collections.ObservableList;

/**
 * A class for a view for a chess board. This class must have a reference
 * to a GameController for chess playing chess
 * @author Gustavo
 * @date Oct 20, 2015
 */
public class BoardView {

    /* You may add more instance data if you need it */
    protected GameController controller;
    private GridPane gridPane;
    private Tile[][] tiles;
    private Rectangle sideRect;
    private Text sideStatus;
    private Text state;
    private boolean isRotated;

    private boolean isFirstClick;
    private Set<Move> possibleMoves;
    private Position start;
    private Move lastMove;
    private boolean hasStarted;
    private String currentPlayingPiece;
    private String promotedPiece;
    private boolean promotionEventHappened;

    //from ChessFX
    private RadioMenuItem playerGameMenuItem;
    private Text tType;
    private ChessFX.GameType currentGameType;

    //Pieces Eaten
    private String blacksEaten;
    private String whitesEaten;
    private Label whitePiecesEaten;
    private Label blackPiecesEaten;

    //Moves History List
    private ObservableList<String> moveHistory;

    //Piece Preview
    private Label lblCurrentPiece;

    /**
     * Construct a BoardView with an instance of a GameController
     * and a couple of Text object for displaying info to the user
     * @param controller The controller for the chess game
     * @param state A Text object used to display state to the user
     * @param sideRect A Rectangle displaying the color of the current side
     * @param sideStatus A Text object used to display whose turn it is
     * @param playerGameMenuItem The MenuItem to start a player game
     * @param tType A Text object to display the current type of game
     * @param whitePiecesEaten A Label for displaying the white pieces eaten
     * @param blackPiecesEaten A Label for displaying the black pieces eaten
     * @param moveHistory An ObservableList to record the move history
     * @param lblCurrentPiece A Label for displaying the current piece clicked
     */
    public BoardView(GameController controller, Text state, Rectangle sideRect,
        Text sideStatus, RadioMenuItem playerGameMenuItem, Text tType,
        Label whitePiecesEaten, Label blackPiecesEaten,
        ObservableList<String> moveHistory, Label lblCurrentPiece) {

        this.controller = controller;
        this.state = state;
        this.sideRect = sideRect;
        this.sideStatus = sideStatus;

        this.playerGameMenuItem = playerGameMenuItem;

        this.tType = tType;

        this.whitePiecesEaten = whitePiecesEaten;
        this.blackPiecesEaten = blackPiecesEaten;

        this.moveHistory = moveHistory;
        this.lblCurrentPiece = lblCurrentPiece;

        tiles = new Tile[8][8];
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color : darkcyan;");
        reset(controller);

        isFirstClick = true;
        hasStarted = false;
        promotionEventHappened = false;
    }

    /**
     * Listener for clicks on a tile
     *
     * @param tile The tile attached to this listener
     * @return The event handler for all tiles.
     */
    private EventHandler<? super MouseEvent> tileListener(Tile tile) {
        return event -> {
            if (controller instanceof NetworkedChessController
                    && controller.getCurrentSide()
                    != ((NetworkedChessController) controller).getLocalSide()) {
                //not your turn!
                return;
            }

            // Don't change the code above this :)
            if (isFirstClick) {
                firstClick(tile);
            } else {
                secondClick(tile);
                isFirstClick = true;
            }
        };
    }

    /**
     * Perform the first click functions, like displaying
     * which are the valid moves for the piece you clicked.
     * @param tile The TileView that was clicked
     */
    private void firstClick(Tile tile) {
        possibleMoves = controller.getMovesForPieceAt(tile.getPosition());
        if (possibleMoves.size() > 0) {
            tile.highlight(Color.rgb(173, 216, 230, 0.6));
            for (Move m : possibleMoves) {
                getTileAt(m.getDestination()).highlight(
                    Color.rgb(60, 179, 113, 0.6));
            }
            isFirstClick = false;
            start = tile.getPosition();
            currentPlayingPiece = controller.getSymbolForPieceAt(start);
            lblCurrentPiece.setText(currentPlayingPiece.toString());
        }
    }

    /**
     * Perform the second click functions, like
     * sending moves to the controller but also
     * checking that the user clicked on a valid position.
     * If they click on the same piece they clicked on for the first click
     * then you should reset to click state back to the first click and clear
     * the highlighting effected on the board.
     *
     * @param tile the TileView at which the second click occurred
     */
    private void secondClick(Tile tile) {
        try {
            //make new move
            controller.makeMove(new Move(start, tile.getPosition()));

            //updates the pieces eaten
            if (controller.getCurrentSide() == Side.WHITE) {
                blacksEaten += tile.getSymbol().toString();
            } else {
                whitesEaten += tile.getSymbol().toString();
            }
            //updates the corresponding UI
            if (!blacksEaten.equals("")) {
                blackPiecesEaten.setText(blacksEaten);
            }
            if (!whitesEaten.equals("")) {
                whitePiecesEaten.setText(whitesEaten);
            }

            addToHistoryAndUpdate(start, tile.getPosition(),
                tile.getSymbol().toString());

            controller.endTurn();
            //start next turn
            if (!controller.getCurrentState().isGameOver()) {
                controller.beginTurn();
                hasStarted = true;
            }
        } catch (IllegalMoveException e) {
            //remove highlighting first
            clearHighlightAfterMove();
        }
    }

    /**
     * This method should be called any time a move is made on the back end.
     * It should update the tiles' highlighting and symbols to reflect the
     * change in the board state.
     *
     * @param moveMade the move to show on the view
     * @param capturedPositions a list of positions where pieces were captured
     *
     */
    public void updateView(Move moveMade, List<Position> capturedPositions) {

        //update Toolbar UI
        state.setText(controller.getCurrentState().toString());
        sideStatus.setText(controller.getCurrentSide().toString()
            .toUpperCase());
        updateSideRectColor();

        //unhighlight the last highlight
        if (lastMove != null) {
            getTileAt(lastMove.getStart()).clear();
            getTileAt(lastMove.getDestination()).clear();
        }
        this.lastMove = moveMade;
        clearHighlightAfterMove();
        //remove captured positions
        for (Position p : capturedPositions) {
            getTileAt(p).setSymbol("");
        }
        //move the piece to new position
        getTileAt(moveMade.getDestination())
            .setSymbol(controller.getSymbolForPieceAt(moveMade
                .getDestination()));
        getTileAt(moveMade.getStart()).setSymbol("");
        //highlight last move made
        getTileAt(moveMade.getStart()).highlight(Color.rgb(173, 216, 230, 0.6));
        getTileAt(moveMade.getDestination()).highlight(
            Color.rgb(173, 216, 230, 0.6));
    }

    /**
     * Asks the user which PieceType they want to promote to
     * (suggest using Alert). Then it returns the Piecetype user selected.
     *
     * @return  the PieceType that the user wants to promote their piece to
     */
    private PieceType handlePromotion() {
        List<String> choices = new ArrayList<>();
        choices.add("Queen");
        choices.add("Knight");
        choices.add("Rook");
        choices.add("Bishop");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Queen", choices);
        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("♟ Your pawn has been promoted!");
        dialog.setContentText("Please choose a type to promote to:");

        PieceType returnPiece;
        Side currentSide = controller.getCurrentSide();
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals("Queen")) {
                returnPiece = ChessPiece.ChessPieceType.QUEEN;
            } else if (result.get().equals("Knight")) {
                returnPiece = ChessPiece.ChessPieceType.KNIGHT;
            } else if (result.get().equals("Rook")) {
                returnPiece = ChessPiece.ChessPieceType.ROOK;
            } else {
                returnPiece = ChessPiece.ChessPieceType.BISHOP;
            }
            promotedPiece = result.get();
        } else {
            returnPiece = ChessPiece.ChessPieceType.QUEEN;
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Interactive Chess");
            alert.setHeaderText("You did not make a choice");
            alert.setContentText("The default choice QUEEN will be used");
            alert.showAndWait();
            promotedPiece = "Queen";
        }
        promotionEventHappened = true;

        return returnPiece;
    }

    /**
     * Handles a change in the GameState (ie someone in check or stalemate).
     * If the game is over, it should open an Alert and ask to keep
     * playing or exit.
     *
     * @param s The new Game State
     */
    public void handleGameStateChange(GameState s) {
        if (s.isGameOver()) {
            state.setText(s.toString().toUpperCase());
            moveHistory.add(s.toString());
            //Alerts user that game has ended.
            Alert gameInfo = new Alert(AlertType.INFORMATION);
            gameInfo.setTitle("Interactive Chess");
            gameInfo.setHeaderText("Game Over");
            gameInfo.setContentText(s.toString() + "!");
            gameInfo.showAndWait();
            //Second Alert informing user game has resetted to new player game
            Alert gameNew = new Alert(AlertType.INFORMATION);
            gameNew.setTitle("Interactive Chess");
            gameNew.setHeaderText(null);
            gameNew.setContentText("New default game mode (Player Game) will "
                + "now start. Check the \"Game\" Menu for more game options");
            gameNew.showAndWait();

            //Update UI and code
            tType.setText("Player Match");
            playerGameMenuItem.setSelected(true);
            reset(new ChessController());
        }
    }

    /**
     * Updates UI that depends upon which Side's turn it is
     *
     * @param s The new Side whose turn it currently is
     */
    public void handleSideChange(Side s) {
        if (s.equals(Side.WHITE)) {
            setBoardRotation(0);
        } else {
            setBoardRotation(180);
        }
    }

    /**
     * Resets this BoardView with a new controller.
     * This moves the chess pieces back to their original configuration
     * and calls startGame() at the end of the method
     * @param newController The new controller for this BoardView
     */
    public void reset(GameController newController) {
        if (controller instanceof NetworkedChessController) {
            ((NetworkedChessController) controller).close();
        }
        controller = newController;
        isRotated = false;
        if (controller instanceof NetworkedChessController) {
            Side mySide
                = ((NetworkedChessController) controller).getLocalSide();
            if (mySide == Side.BLACK) {
                isRotated = true;
            }
        }

        //Refreshes History
        refreshHistory();

        //update UI
        sideStatus.setText(controller.getCurrentSide().toString()
            .toUpperCase());
        updateSideRectColor();

        // controller event handlers
        // We must force all of these to run on the UI thread
        controller.addMoveListener(
                (Move move, List<Position> capturePositions) ->
                Platform.runLater(
                    () -> updateView(move, capturePositions)));

        controller.addCurrentSideListener(
                (Side side) -> Platform.runLater(
                    () -> handleSideChange(side)));

        controller.addGameStateChangeListener(
                (GameState state) -> Platform.runLater(
                    () -> handleGameStateChange(state)));

        controller.setPromotionListener(() -> handlePromotion());

        addPieces();
        controller.startGame();
        if (isRotated) {
            setBoardRotation(180);
        } else {
            setBoardRotation(0);
        }

        this.hasStarted = false;
        state.setText("Ready");
    }

    /**
     * Initializes the gridPane object with the pieces from the GameController.
     * This method should only be called once before starting the game.
     */
    private void addPieces() {
        gridPane.getChildren().clear();
        Map<Piece, Position> pieces = controller.getAllActivePiecesPositions();
        /* Add the tiles */
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Tile tile = new TileView(new Position(row, col));
                gridPane.add(tile.getRootNode(),
                        1 + tile.getPosition().getCol(),
                        1 + tile.getPosition().getRow());
                GridPane.setHgrow(tile.getRootNode(), Priority.ALWAYS);
                GridPane.setVgrow(tile.getRootNode(), Priority.ALWAYS);
                getTiles()[row][col] = tile;
                tile.getRootNode().setOnMouseClicked(
                        tileListener(tile));
                tile.clear();
                tile.setSymbol("");
            }
        }
        /* Add the pieces */
        for (Piece p : pieces.keySet()) {
            Position placeAt = pieces.get(p);
            getTileAt(placeAt).setSymbol(p.getType().getSymbol(p.getSide()));
        }
        /* Add the coordinates around the perimeter */
        for (int i = 1; i <= 8; i++) {
            Text coord1 = new Text((char) (64 + i) + "");
            coord1.setFont(Font.font(null, FontWeight.BOLD, 14));
            coord1.setFill(Color.WHITE);
            GridPane.setHalignment(coord1, HPos.CENTER);
            gridPane.add(coord1, i, 0);

            Text coord2 = new Text((char) (64 + i) + "");
            coord2.setFont(Font.font(null, FontWeight.BOLD, 14));
            coord2.setFill(Color.WHITE);
            GridPane.setHalignment(coord2, HPos.CENTER);
            gridPane.add(coord2, i, 9);

            Text coord3 = new Text(" " + (9 - i) + " ");
            coord3.setFont(Font.font(null, FontWeight.BOLD, 14));
            coord3.setFill(Color.WHITE);
            GridPane.setHalignment(coord3, HPos.CENTER);
            gridPane.add(coord3, 0, i);

            Text coord4 = new Text(" " + (9 - i) + " ");
            coord4.setFont(Font.font(null, FontWeight.BOLD, 14));
            coord4.setFill(Color.WHITE);
            GridPane.setHalignment(coord4, HPos.CENTER);
            gridPane.add(coord4, 9, i);
        }
    }

    /**
     * Sets the rotation of the chessboard
     * @param degrees the degree of rotation
     */
    private void setBoardRotation(int degrees) {
        gridPane.setRotate(degrees);
        for (Node n : gridPane.getChildren()) {
            n.setRotate(degrees);
        }
    }

    /**
     * Gets the view to add to the scene graph
     * @return A pane that is the node for the chess board
     */
    public Pane getView() {
        return gridPane;
    }

    /**
     * Gets the tiles that belong to this board view
     * @return A 2d array of TileView objects
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * Gets the tiles at a particular position
     * @param   row the row number of the tile
     * @param   col the column number of the tile
     * @return  A tile at the specified position
     */
    private Tile getTileAt(int row, int col) {
        return getTiles()[row][col];
    }

    /**
     * Gets the tiles at a particular position
     * @param  p the position of the tile
     * @return   A tile at the specified position
     */
    private Tile getTileAt(Position p) {
        return getTileAt(p.getRow(), p.getCol());
    }

    /**
     * Clears the highlighting after a move has been made
     */
    private void clearHighlightAfterMove() {
        //clear start
        getTileAt(start).clear();
        for (Move m : possibleMoves) {
            getTileAt(m.getDestination()).clear();
        }
    }

    /**
     * Updates the color of the side rectangle accounding to which side the game
     * is on
     */
    private void updateSideRectColor() {
        this.sideRect.setFill(controller.getCurrentSide()
            .toString().equals("White") ? Color.WHITE : Color.BLACK);
    }

    /**
     * Checks whether game is ongoing
     * @return a boolean determining whether its in game
     */
    public boolean isInGame() {
        return this.hasStarted;
    }

    /**
     * Sets the current game type
     * @param t GameType enum
     */
    public void setCurrentGameType(ChessFX.GameType t) {
        this.currentGameType = t;
    }

    /**
     * Refreshes the side pane's contents
     */
    public void refreshHistory() {
        whitesEaten = "";
        blacksEaten = "";
        whitePiecesEaten.setText("None");
        blackPiecesEaten.setText("None");

        moveHistory.clear();
    }

    /**
     * Addes the moves to history and updates the list
     * @param start The start position
     * @param end The end position
     * @param k The string k
     */
    public void addToHistoryAndUpdate(Position start, Position end, String k) {
        String moveRecord = "";

        moveRecord = String.format("%s %s%d → %s%d", currentPlayingPiece,
            formatPosition(start.getCol()), 8 - start.getRow(),
            formatPosition(end.getCol()), 8 - end.getRow());

        if (!k.equals("")) {
            moveRecord += " | Killed " + k;
        }

        moveHistory.add(moveRecord);

        if (promotionEventHappened) {
            moveHistory.add(String.format("%s %s%d > %s", currentPlayingPiece,
                formatPosition(end.getCol()), 8 - end.getRow(), promotedPiece));
            promotionEventHappened = false;
        }
    }

    /**
     * Formats the chesspiece moves correctly
     * @param  s the relative position of the chess piece
     * @return   the corresponding alphebetical columns
     */
    public String formatPosition(int s) {
        String result = "";
        if (s == 0) {
            result = "A";
        } else if (s == 1) {
            result = "B";
        } else if (s == 2) {
            result = "C";
        } else if (s == 3) {
            result = "D";
        } else if (s == 4) {
            result = "E";
        } else if (s == 5) {
            result = "F";
        } else if (s == 6) {
            result = "G";
        } else {
            result = "H";
        }
        return result;
    }
}
