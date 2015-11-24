package model.chess;

import model.Move;
import model.Position;

/**
 * Created by joe on 10/22/15.
 */
public class CastlingMove extends Move {
    public enum CastlingType {
        QUEEN_SIDE(0, 3), KING_SIDE(7, 5);

        private int startCol, destCol;

        private CastlingType(int startCol, int destCol) {
            this.startCol = startCol;
            this.destCol = destCol;
        }

        public int getStartCol() {
            return startCol;
        }

        public int getDestCol() {
            return destCol;
        }
    }

    private CastlingType castlingType;

    public CastlingMove(Position start, Position destination, CastlingType t) {
        super(start, destination);
        this.castlingType = t;
    }

    public CastlingType getCastlingType() {
        return castlingType;
    }


}
