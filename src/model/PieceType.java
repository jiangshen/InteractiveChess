package model;

/**
 * Represents a type of playing piece, and holds its representation symbol
 *
 * @author Joe
 */
public interface PieceType {
    /**
     *
     * @param s the side of this piece
     * @return the symbol for a Piece of this type on Side s
     */
    String getSymbol(Side s);
}
