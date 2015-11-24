package model.chess;

import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;

public class ChessUtils {
    public static boolean posBoundsTest(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public static boolean posBoundsTest(Position p) {
        return posBoundsTest(p.getRow(), p.getCol());
    }

    public static Piece getPieceOfType(PieceType pieceType, Side s) {
        if (pieceType.equals(ChessPiece.ChessPieceType.BISHOP)) {
            return new Bishop(s);
        } else if (pieceType.equals(ChessPiece.ChessPieceType.KING)) {
            return new King(s);
        } else if (pieceType.equals(ChessPiece.ChessPieceType.KNIGHT)) {
            return new Knight(s);
        } else if (pieceType.equals(ChessPiece.ChessPieceType.PAWN)) {
            return new Pawn(s);
        } else if (pieceType.equals(ChessPiece.ChessPieceType.QUEEN)) {
            return new Queen(s);
        } else if (pieceType.equals(ChessPiece.ChessPieceType.ROOK)) {
            return new Rook(s);
        }
        return null;
    }

    public static PieceType getPieceTypeFromString(String s) {
        if (s.equals("PAWN")) {
            return ChessPiece.ChessPieceType.PAWN;
        } else if (s.equals("ROOK")) {
            return ChessPiece.ChessPieceType.ROOK;
        } else if (s.equals("KNIGHT")) {
            return ChessPiece.ChessPieceType.KNIGHT;
        } else if (s.equals("BISHOP")) {
            return ChessPiece.ChessPieceType.BISHOP;
        } else if (s.equals("QUEEN")) {
            return ChessPiece.ChessPieceType.QUEEN;
        } else if (s.equals("KING")) {
            return ChessPiece.ChessPieceType.KING;
        }
        return null;
    }
}
