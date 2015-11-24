package gamecontrol;

public enum ChessState implements GameState {
    WHITE_WINS("White Wins"),
    BLACK_WINS("Black Wins"),
    STALEMATE("Stalemate"),
    WHITE_IN_CHECK("White is in Check"),
    BLACK_IN_CHECK("Black is in Check"),
    ONGOING("Ongoing");

    private String message;

    ChessState(String message) {
        this.message = message;
    }

    public boolean isGameOver() {
        return equals(WHITE_WINS) || equals(BLACK_WINS) || equals(STALEMATE);
    }

    @Override
    public String toString() {
        return message;
    }
}
