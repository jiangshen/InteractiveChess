package model.chess;

import java.util.HashSet;
import java.util.Set;
import model.Move;
import model.Position;
import model.Side;


public class Bishop extends ChessPiece {

    public Bishop(Side side) {
        super(ChessPieceType.BISHOP, side);
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<Move>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dy = -1; dy <= 1; dy += 2) {
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
        return moves;
    }
}
