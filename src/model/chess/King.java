package model.chess;

import java.util.HashSet;
import java.util.Set;
import model.Move;
import model.Position;
import model.Side;

public class King  extends ChessPiece {

    public King(Side side) {
        super(ChessPieceType.KING, side);
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    int destCol = curCol + dx;
                    int destRow = curRow + dy;
                    if (ChessUtils.posBoundsTest(destRow, destCol)) {
                        Position destPos = new Position(destRow, destCol);
                        moves.add(new Move(curPos, destPos));
                    }
                }
            }
        }
        return moves;
    }
}
