package gamecontrol.ai;

import model.Board;
import model.Move;
import model.Piece;

import java.util.Map;
import java.util.Set;

/**
 * An interface that allows you to swap out more and more sophisticated AI
 *
 * @author Joe
 */
public interface AIChessEngine {

    /**
     * Choose the next move that the computer will make
     * @param pieceMoves
     * @param board
     * @return the AI's move
     */
    Move chooseNextMove(Map<Piece, Set<Move>> pieceMoves, Board board);
}
