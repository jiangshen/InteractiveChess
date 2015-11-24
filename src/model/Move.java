package model;

/**
 * A 2D translation object. Contains information about whether the move is
 * continuous, or represents a jump
 *
 * @author Joe
 */
public class Move {
    private Position start;
    private Position destination;
    private boolean isJump;
    private boolean cannotCapture;
    private boolean mustCapture;

    /**
     *
     * @param start
     * @param destination
     */
    public Move(Position start, Position destination) {
        this(start, destination, false, false, false);
    }

    /**
     *
     * @param start
     * @param destination
     * @param isJump
     */
    public Move(Position start, Position destination, boolean isJump) {
        this(start, destination, isJump, false, false);
    }

    /**
     *
     * @param start
     * @param destination
     * @param isJump
     * @param cannotCapture
     * @param mustCapture
     */
    public Move(Position start, Position destination, boolean isJump,
            boolean cannotCapture, boolean mustCapture) {
        this.start = start;
        this.destination = destination;
        this.isJump = isJump;
        this.cannotCapture = cannotCapture;
        this.mustCapture = mustCapture;
    }

    /**
     *
     * @return the start position of this move
     */
    public Position getStart() {
        return start;
    }

    /**
     *
     * @return the destination position of this move
     */
    public Position getDestination() {
        return destination;
    }

    /**
     *
     * @return whether or not the move is a jump (ie not continuous)
     */
    public boolean isJump() {
        return isJump;
    }

    /**
     *
     * @return whether or not this move can cause capture
     */
    public boolean cannotCapture() {
        return cannotCapture;
    }

    /**
     *
     * @return whether or not this move must cause capture
     */
    public boolean mustCapture() {
        return mustCapture;
    }

    @Override
    public int hashCode() {
        return start.hashCode() - destination.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move)) {
            return false;
        }
        Move m = (Move) o;
        return start.equals(m.start) && destination.equals(m.destination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        sb.append("->");
        sb.append(destination);
        return sb.toString();
    }

}
