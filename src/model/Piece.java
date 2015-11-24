package model;

import java.util.Set;

/**
 * Represents a playing piece on an 8x8 board
 *
 * @author Joe
 */
public interface Piece {

    /**
     * Get the moves based on this piece's rules for movement
     *
     * @param curPos the start position of the piece
     * @return all possible moves from curPos, ignoring other pieces
     */
    Set<Move> generateMoves(Position curPos);

    /**
     *
     * @return the side this piece is on
     */
    Side getSide();

    /**
     *
     * @return the type of this piece
     */
    PieceType getType();
}
