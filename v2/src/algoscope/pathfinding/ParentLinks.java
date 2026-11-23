package algoscope.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Remembers, for each cell, the cell it was reached from. Following those links
 * backwards from the goal gives the path. All four algorithms use it.
 */
final class ParentLinks {

    private final Grid grid;
    // parent[i] = flat index of the cell that reached cell i, or -1
    private final int[] parent;

    ParentLinks(Grid grid) {
        this.grid = grid;
        this.parent = new int[grid.size()];
        Arrays.fill(parent, -1);
    }

    /** Says that {@code child} was reached from {@code from} (replacing any older link). */
    void link(Cell child, Cell from) {
        parent[grid.index(child)] = grid.index(from);
    }

    /** The path from the start to {@code goal}, following the links backwards. */
    List<Cell> pathTo(Cell goal) {
        List<Cell> path = new ArrayList<>();
        int i = grid.index(goal);
        int start = grid.index(grid.start());
        while (i != start) {
            path.add(grid.cellAt(i));
            i = parent[i];
            if (i == -1) {
                throw new IllegalStateException("No link back to the start from " + goal);
            }
        }
        path.add(grid.start());
        // It was built from the goal backwards
        Collections.reverse(path);
        return path;
    }
}
