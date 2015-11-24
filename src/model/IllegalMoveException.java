package model;

/**
 * Thrown if a player attempts a move that the board deems illegal
 *
 * @author Joe
 */
public class IllegalMoveException extends Exception {

    /**
     *
     * @param m The illegal move
     */
    public IllegalMoveException(Move m) {
        super("Illegal move: " + m.toString());
    }
}