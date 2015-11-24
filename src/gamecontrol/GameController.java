package gamecontrol;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import model.IllegalMoveException;
import model.Move;
import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;

/**
 * Definition of a game controller interface for an arbitrary board game.
 * This interface allows for registering callbacks for updating GUIs.
 * Make sure that any callbacks you register on this interface update the UI
 * on the UI thread.
 *
 * @author Gustavo
 * @date Oct 28, 2015
 */
public interface GameController {
    /**
     * Get the current state of the game
     * @return A GameState instance representing the current state
     */
    GameState getCurrentState();

    /**
     * Starts the game. Do not call this more than once after instantiating
     * the controller.
     */
    void startGame();

    /**
     * Begins a turn.
     * Call this method after calling start game and after a call to end turn.
     * This should generate the valid moves for the side that began the turn.
     * Begin turn must be followed by an end turn.
     */
    void beginTurn();

    /**
     * Ends a turn.
     * End turn must be followed by a begin turn.
     */
    void endTurn();

    /**
     * Gets the set of possible moves for a piece at a particular position.
     * Should return an empty set if the piece has no valid moves.
     *
     * @param p Position in question
     * @return A set of moves or an empty set.
     */
    Set<Move> getMovesForPieceAt(Position p);

    /**
     * Attempts to make a move on the board
     *
     * @param possibleMove The move
     * @throws IllegalMoveException if the move is not in the set of legal moves
     */
    void makeMove(Move possibleMove) throws IllegalMoveException;

    /**
     * Generates the association of pieces to positions currently on the board.
     * This can be useful for getting the board state at any point in time.
     * It's really most useful in setting up the board initially
     *
     * @return a map from Piece to Position for each Piece active on the board
     */
    Map<Piece, Position> getAllActivePiecesPositions();

    /**
     * Register callback for changes in board state / moves made on the board
     *
     * @param moveListener  a BiConsumer is a functional interface whose
     *                      single abstract method takes in two parameters
     *                      and returns void: in this case the two params
     *                      are type Move, and List<Position> (which represents
     *                      the positions at which pieces have been captured by
     *                      the Move)
     */
    void addMoveListener(BiConsumer<Move, List<Position>> moveListener);

    /**
     * Register callback for changes in the game state.
     * @param listener a Consumer takes in one param and returns void
     *                 this one consumes the new game state.
     *                 Every gameStateChangeListener will be called in setState
     */
    void addGameStateChangeListener(Consumer<GameState> listener);

    /**
     * Register callback for changes in the current Side
     * @param sideListener a Consumer, takes in param of type Side and returns
     *                     void. Every currentSideListener will be called in
     *                     setGameState
     */
    void addCurrentSideListener(Consumer<Side> sideListener);

    /**
     * Register promotion listener to be called when promotion occurs
     *
     * @param supplier a Supplier takes no arguments, and returns something of
     *                 whatever type it's parametrized to. In this case, it
     *                 returns a PieceType, which represents the PieceType
     *                 that the promoted piece should become
     */
    void setPromotionListener(Supplier<PieceType> supplier);

    /**
     * Returns the possible pieces you can promote a piece to when a piece
     * reaches a promotion.
     *
     * @return a list of PieceTypes that you can promote to in this game
     */
    List<PieceType> getPromotionTypes();

    /**
     * Returns the default promotion piece if thats useful
     *
     * @return the default promotion piece type...
     */
    PieceType getDefaultPromotionType();

    /**
     * Gets the currently playing side.
     * @return
     */
    Side getCurrentSide();

    /**
     * Gets the symbol at a particular position. This should be called any time
     * the view of the board is updated - ie when a Piece moves to some new
     * position, call this to get the symbol for the piece at that position.
     *
     * This is useful for promotion
     *
     * @param pos Position
     * @return Returns a String symbol or empty string if no piece at pos
     */
    String getSymbolForPieceAt(Position pos);

    /**
     * Answers if a particular move would result in a capture. This is useful
     * for determining what color to highlight a UI element for instance...
     *
     * @param m a move that could result in capture
     * @return  whether or not it really does
     */
    boolean moveResultsInCapture(Move m);

    /**
     * Gets a new instance
     *
     * @return
     */
    GameController getNewInstance();
}
