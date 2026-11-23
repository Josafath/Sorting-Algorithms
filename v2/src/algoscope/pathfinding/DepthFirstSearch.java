package algoscope.pathfinding;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Goes as deep as it can in one direction and only backs up when it is stuck.
 * A stack (last in, first out) gives that order. It reaches the goal, but usually
 * through a long, winding path: it does not look for the best one.
 */
public final class DepthFirstSearch implements PathfindingAlgorithm {

    @Override
    public String name() {
        return "Depth-First Search (DFS)";
    }

    @Override
    public String description() {
        return "Follows one direction as far as it can with a stack, backing up only when stuck. The path is rarely the best.";
    }

    @Override
    public boolean usesCosts() {
        return false;
    }

    @Override
    public boolean findsBestPath() {
        return false;
    }

    @Override
    public void search(SearchTracker tracker) {
        Grid grid = tracker.grid();
        ParentLinks parents = new ParentLinks(grid);
        Deque<Cell> stack = new ArrayDeque<>();

        tracker.discover(grid.start());
        stack.push(grid.start());
        while (!stack.isEmpty()) {
            Cell cell = stack.pop();
            // A cell can be on the stack more than once; explore it only the first time
            if (tracker.isVisited(cell)) {
                continue;
            }
            tracker.visit(cell);
            if (cell.equals(grid.goal())) {
                tracker.foundPath(parents.pathTo(cell));
                return;
            }
            List<Cell> next = grid.neighbors(cell);
            // Pushed in reverse so the first direction (up) is the first one popped
            for (int k = next.size() - 1; k >= 0; k--) {
                Cell neighbor = next.get(k);
                if (!tracker.isVisited(neighbor)) {
                    // The last push wins: it is the one that will be popped first
                    parents.link(neighbor, cell);
                    tracker.discover(neighbor);
                    stack.push(neighbor);
                }
            }
        }
    }
}
