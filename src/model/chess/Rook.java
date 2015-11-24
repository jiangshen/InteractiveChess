package model.chess;

import java.util.HashSet;
import java.util.Set;
import model.Move;
import model.Position;
import model.Side;

/**
 * Rook piece implementation
 * @author Joe
 * @date Oct 28, 2015
 */
public class Rook  extends ChessPiece {

    /**
     * Creates a rook on a particular side
     * @param side
     */
    public Rook(Side side) {
        super(ChessPieceType.ROOK, side);
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<Move>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dx) != Math.abs(dy)) {
                    int destCol = curCol + dx;
                    int destRow = curRow + dy;
                    while (ChessUtils.posBoundsTest(destRow, destCol)) {
                        Position destPos = new Position(destRow, destCol);
                        moves.add(new Move(curPos, destPos));

                        destCol = destCol + dx;
                        destRow = destRow + dy;
                    }
                }
            }
        }
        return moves;
    }
}
