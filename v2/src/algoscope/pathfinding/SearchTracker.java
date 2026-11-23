package algoscope.pathfinding;

import java.util.ArrayList;
import java.util.List;

/**
 * What the search algorithms use to mark cells. Like TrackedArray for sorting:
 * every discover, visit and path cell goes through here, so it is counted and
 * recorded as a {@link SearchStep}, and the algorithms stay short.
 */
public final class SearchTracker {

    private final Grid grid;
    private final CellStatus[] status;
    private final List<SearchStep> steps = new ArrayList<>();
    private int visited;
    private List<Cell> path = List.of();

    SearchTracker(Grid grid) {
        this.grid = grid;
        this.status = new CellStatus[grid.size()];
        java.util.Arrays.fill(status, CellStatus.UNSEEN);
    }

    public Grid grid() {
        return grid;
    }

    /** True once the cell has been discovered (it may also be visited already). */
    public boolean isDiscovered(Cell cell) {
        return status[grid.index(cell)] != CellStatus.UNSEEN;
    }

    public boolean isVisited(Cell cell) {
        return status[grid.index(cell)] == CellStatus.VISITED;
    }

    /** Puts an unseen cell on the frontier. Does nothing if it was already discovered. */
    public void discover(Cell cell) {
        int i = grid.index(cell);
        if (status[i] != CellStatus.UNSEEN) {
            return;
        }
        steps.add(new SearchStep.Discover(cell, status[i]));
        status[i] = CellStatus.FRONTIER;
    }

    /** Explores a cell. Each cell can be visited only once. */
    public void visit(Cell cell) {
        int i = grid.index(cell);
        if (status[i] == CellStatus.VISITED) {
            throw new IllegalStateException(cell + " was already visited");
        }
        steps.add(new SearchStep.Visit(cell, status[i]));
        status[i] = CellStatus.VISITED;
        visited++;
    }

    /**
     * Records the path found, from the start to the goal. Checks that it really is a
     * path: it must start and end in the right places and only move to open neighbours.
     */
    public void foundPath(List<Cell> cells) {
        if (cells.isEmpty() || !cells.get(0).equals(grid.start()) || !cells.get(cells.size() - 1).equals(grid.goal())) {
            throw new IllegalArgumentException("A path must go from the start to the goal: " + cells);
        }
        for (int k = 1; k < cells.size(); k++) {
            Cell from = cells.get(k - 1);
            Cell to = cells.get(k);
            if (from.manhattanDistance(to) != 1 || !grid.passable(to)) {
                throw new IllegalArgumentException("Invalid move in the path: " + from + " -> " + to);
            }
        }
        for (Cell cell : cells) {
            int i = grid.index(cell);
            steps.add(new SearchStep.PathCell(cell, status[i]));
            status[i] = CellStatus.PATH;
        }
        path = List.copyOf(cells);
    }

    /** The outcome so far: the path (if any), its cost and the cells visited. */
    SearchResult result() {
        int cost = 0;
        // The start is where we already are: only the cells entered are paid
        for (int k = 1; k < path.size(); k++) {
            cost += grid.cost(path.get(k));
        }
        return new SearchResult(path, cost, visited);
    }

    List<SearchStep> steps() {
        return steps;
    }
}
