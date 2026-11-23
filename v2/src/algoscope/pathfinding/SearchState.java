package algoscope.pathfinding;

import java.util.Arrays;

/**
 * What the screen shows while a search is replayed: the status of every cell and
 * how many cells have been visited and marked as path so far.
 * <p>
 * Only the steps of this package can change it; everybody else can only read.
 */
public final class SearchState {

    private final Grid grid;
    private final CellStatus[] status;
    private int visited;
    private int pathCells;

    SearchState(Grid grid) {
        this.grid = grid;
        this.status = new CellStatus[grid.size()];
        Arrays.fill(status, CellStatus.UNSEEN);
    }

    /** The grid that was searched (a copy: editing the map does not change a recording). */
    public Grid grid() {
        return grid;
    }

    public CellStatus status(Cell cell) {
        return status[grid.index(cell)];
    }

    /** Cells explored up to the current step. */
    public int visited() {
        return visited;
    }

    /** Cells of the path shown up to the current step. */
    public int pathCells() {
        return pathCells;
    }

    // ---- Used by SearchStep to apply and undo ----

    void setStatus(Cell cell, CellStatus newStatus) {
        status[grid.index(cell)] = newStatus;
    }

    void countVisit(int delta) {
        visited += delta;
    }

    void countPathCell(int delta) {
        pathCells += delta;
    }
}
