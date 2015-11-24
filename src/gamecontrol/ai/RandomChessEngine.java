package gamecontrol.ai;

import model.Board;
import model.Move;
import model.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Chooses a random move. Is very dumb.
 *
 * @author Joe
 */
public class RandomChessEngine implements AIChessEngine {
    @Override
    public Move chooseNextMove(Map<Piece, Set<Move>> pieceMoves, Board board) {
        Piece selected = null;

        if (pieceMoves == null) {
            return null;
        }

        List<Piece> shuffled = new ArrayList<>(pieceMoves.keySet());
        Collections.shuffle(shuffled);
        Iterator<Piece> iterator = shuffled.iterator();
        while (iterator.hasNext()
                &&
                (pieceMoves.get(selected) == null
                || pieceMoves.get(selected).isEmpty())) {
            selected = iterator.next();
        }

        List<Move> shuffledMoves = new ArrayList<>(pieceMoves.get(selected));
        Collections.shuffle(shuffledMoves);

        return shuffledMoves.get(0);
    }
}
