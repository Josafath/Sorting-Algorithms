package algoscope.pathfinding;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Explores in rings: first every cell 1 step away, then every cell 2 steps away...
 * A queue (first in, first out) keeps that order, so the first time the goal is
 * reached it is through the path with the fewest steps. Mud is ignored.
 */
public final class BreadthFirstSearch implements PathfindingAlgorithm {

    @Override
    public String name() {
        return "Breadth-First Search (BFS)";
    }

    @Override
    public String description() {
        return "Spreads out evenly in rings using a queue. Finds the path with the fewest steps, but ignores mud.";
    }

    @Override
    public boolean usesCosts() {
        return false;
    }

    @Override
    public boolean findsBestPath() {
        // The fewest steps, which is what it measures
        return true;
    }

    @Override
    public void search(SearchTracker tracker) {
        Grid grid = tracker.grid();
        ParentLinks parents = new ParentLinks(grid);
        Queue<Cell> queue = new ArrayDeque<>();

        tracker.discover(grid.start());
        queue.add(grid.start());
        while (!queue.isEmpty()) {
            // The oldest cell in the queue is the closest one not explored yet
            Cell cell = queue.poll();
            tracker.visit(cell);
            if (cell.equals(grid.goal())) {
                tracker.foundPath(parents.pathTo(cell));
                return;
            }
            for (Cell next : grid.neighbors(cell)) {
                // Discovered cells are already queued with a path at least as short
                if (!tracker.isDiscovered(next)) {
                    parents.link(next, cell);
                    tracker.discover(next);
                    queue.add(next);
                }
            }
        }
        // The queue ran out: the goal cannot be reached
    }
}
