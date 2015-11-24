package model.chess;

import model.Move;
import model.Position;

/**
 * Created by joe on 10/27/15.
 */
public class PawnCaptureMove extends Move {

    private boolean isEnPassant;
    private Position enPassantCapturePosition;

    public PawnCaptureMove(Position start, Position destination, boolean isJump,
            boolean cannotCapture, boolean mustCapture) {
        super(start, destination, isJump, cannotCapture, mustCapture);
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public void setIsEnPassant(boolean isEnPassant) {
        this.isEnPassant = isEnPassant;
    }

    public Position getEnPassantCapturePosition() {
        return enPassantCapturePosition;
    }

    public void setEnPassantCapturePosition(Position enPassantCapturePosition) {
        this.enPassantCapturePosition = enPassantCapturePosition;
    }

    @Override
    public String toString() {
        if (isEnPassant) {
            return "PASS:" + enPassantCapturePosition + ":" + super.toString();
        }
        return super.toString();
    }
}
