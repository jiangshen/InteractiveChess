package model;

/**
 * represents a 2D coordinate pair
 *
 * @author Joe
 */
public class Position {
    private int row;
    private int col;

    /**
     * Constructs a 2D pair
     *
     * @param row
     * @param col
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     *
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     *
     * @return the column
     */
    public int getCol() {
        return col;
    }

    @Override
    public int hashCode() {
        return 8 * col + row;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position p = (Position) o;
        return p.row == this.row && p.col == this.col;
    }

    @Override
    //Do not touch this method
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(row);
        sb.append(", ");
        sb.append(col);
        sb.append(")");
        return sb.toString();
    }

}
