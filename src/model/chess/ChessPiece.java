package model.chess;

import java.util.Set;
import model.Move;
import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;

public abstract class ChessPiece implements Piece {

    private static int refCount;

    private int id;
    private Side side;
    private ChessPieceType type;


    public ChessPiece(ChessPieceType type, Side side) {
        id = refCount++;
        this.side = side;
        this.type = type;
    }

    @Override
    public abstract Set<Move> generateMoves(Position curPos);

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public PieceType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nChessPiece Type: ");
        sb.append(type.toString());
        sb.append("\n Side: ");
        sb.append(side.toString());
        sb.append("\n ID: ");
        sb.append(id);

        return sb.toString();
    }

    public enum ChessPieceType implements PieceType {
        PAWN("♟", "♙"), ROOK("♜", "♖"), KNIGHT("♞", "♘"),
        BISHOP("♝", "♗"), QUEEN("♛", "♕"), KING("♚", "♔");

        private String black;
        private String white;

        @Override
        public String getSymbol(Side pieceSide) {
            return pieceSide == Side.WHITE ? white : black;
        }

        ChessPieceType(String black, String white) {
            this.black = black;
            this.white = white;
        }
    }


}
