package algoscope.pathfinding;

/**
 * A position on the grid. Rows go down from 0, columns go right from 0.
 * Being a record, two cells with the same row and column are equal.
 */
public record Cell(int row, int col) {

    /** Steps between two cells moving only up, down, left or right. */
    public int manhattanDistance(Cell other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
