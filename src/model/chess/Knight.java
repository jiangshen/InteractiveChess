package model.chess;

import java.util.HashSet;
import java.util.Set;
import model.Move;
import model.Position;
import model.Side;

public class Knight extends ChessPiece {

    public Knight(Side side) {
        super(ChessPieceType.KNIGHT, side);
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<Move>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                if (x != y && x != 0 && y != 0 && x + y != 0) {
                    int destCol = curCol + x;
                    int destRow = curRow + y;
                    if (ChessUtils.posBoundsTest(destRow, destCol)) {
                        Position destPos = new Position(destRow, destCol);
                        moves.add(new Move(curPos, destPos, true));
                    }
                }

            }
        }

        return moves;
    }
}
