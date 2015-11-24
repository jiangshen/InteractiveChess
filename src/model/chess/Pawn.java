package model.chess;

import java.util.HashSet;
import java.util.Set;
import model.Move;
import model.Position;
import model.Side;


public class Pawn  extends ChessPiece {

    private int initialRow;
    private int dy;

    public Pawn(Side side) {
        super(ChessPieceType.PAWN, side);

        if (side.equals(Side.BLACK)) {
            dy = 1;
            initialRow = 1;
        } else {
            dy = -1;
            initialRow = 6;
        }
    }

    @Override
    public Set<Move> generateMoves(Position curPos) {
        Set<Move> moves = new HashSet<>();
        int curCol = curPos.getCol();
        int curRow = curPos.getRow();

        for (int i = -1; i <= 1; i++) {
            int destCol = curCol + i;
            int destRow = curRow + dy;
            if (ChessUtils.posBoundsTest(destRow, destCol)) {
                boolean mustCapture = destCol != curCol;
                boolean cannotCapture = destCol == curCol;
                Move moveToAdd;
                if (destRow == 0 || destRow == 7) {
                    moveToAdd = new PromotionMove(curPos,
                            new Position(destRow, destCol), false,
                            cannotCapture, mustCapture);
                } else if (mustCapture) {
                    moveToAdd = new PawnCaptureMove(curPos,
                            new Position(destRow, destCol), false,
                            cannotCapture, mustCapture);
                } else {
                    moveToAdd = new Move(curPos, new Position(destRow, destCol),
                            false, cannotCapture, mustCapture);
                }
                moves.add(moveToAdd);
            }
        }

        if (curRow == initialRow) {
            moves.add(new Move(curPos, new Position(curRow + dy * 2, curCol),
                        false, true, false));
        }

        return moves;
    }

}
