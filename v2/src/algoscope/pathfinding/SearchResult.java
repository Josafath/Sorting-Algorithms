package algoscope.pathfinding;

import java.util.List;

/**
 * The outcome of a search.
 *
 * @param path    the cells from start to goal, both included; empty if the goal cannot be reached
 * @param cost    the sum of the costs of the cells entered (the start is not paid); 0 if no path
 * @param visited how many cells were explored before stopping
 */
public record SearchResult(List<Cell> path, int cost, int visited) {

    public SearchResult {
        path = List.copyOf(path);
    }

    public boolean found() {
        return !path.isEmpty();
    }

    /** Number of moves: one less than the cells of the path. */
    public int steps() {
        return Math.max(0, path.size() - 1);
    }
}
