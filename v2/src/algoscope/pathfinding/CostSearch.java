package algoscope.pathfinding;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The common part of Dijkstra and A*: always explore the frontier cell with the
 * lowest priority, where priority = cost so far + {@link #estimate} of the cost left.
 * <p>
 * Dijkstra estimates 0, so it explores purely by cost so far. A* adds a guess of the
 * distance to the goal, which pulls the search towards it.
 */
abstract class CostSearch implements PathfindingAlgorithm {

    /**
     * An entry of the priority queue.
     *
     * @param cost     cost from the start to this cell along the best path known when queued
     * @param estimate guess of the cost from this cell to the goal
     * @param order    when it was queued, to break ties in a fixed way
     */
    private record Entry(Cell cell, int cost, int estimate, long order) {
        int priority() {
            return cost + estimate;
        }
    }

    // Lowest priority first; on a tie, the one closer to the goal; then the oldest
    private static final Comparator<Entry> ORDER = Comparator
            .comparingInt(Entry::priority)
            .thenComparingInt(Entry::estimate)
            .thenComparingLong(Entry::order);

    /** Guess of the cost from {@code cell} to {@code goal}. Must never be more than the real cost. */
    abstract int estimate(Cell cell, Cell goal);

    @Override
    public boolean usesCosts() {
        return true;
    }

    @Override
    public boolean findsBestPath() {
        return true;
    }

    @Override
    public void search(SearchTracker tracker) {
        Grid grid = tracker.grid();
        Cell goal = grid.goal();
        ParentLinks parents = new ParentLinks(grid);
        // best[i] = cheapest known cost from the start to cell i
        int[] best = new int[grid.size()];
        Arrays.fill(best, Integer.MAX_VALUE);
        PriorityQueue<Entry> frontier = new PriorityQueue<>(ORDER);
        long order = 0;

        best[grid.index(grid.start())] = 0;
        tracker.discover(grid.start());
        frontier.add(new Entry(grid.start(), 0, estimate(grid.start(), goal), order++));

        while (!frontier.isEmpty()) {
            Entry entry = frontier.poll();
            Cell cell = entry.cell();
            // A cell can be queued again with a cheaper cost; the older, dearer entries are skipped
            if (tracker.isVisited(cell) || entry.cost() > best[grid.index(cell)]) {
                continue;
            }
            tracker.visit(cell);
            if (cell.equals(goal)) {
                tracker.foundPath(parents.pathTo(cell));
                return;
            }
            for (Cell next : grid.neighbors(cell)) {
                int cost = entry.cost() + grid.cost(next);
                int i = grid.index(next);
                // Found a cheaper way to reach "next": remember it and queue it
                if (cost < best[i] && !tracker.isVisited(next)) {
                    best[i] = cost;
                    parents.link(next, cell);
                    tracker.discover(next);
                    frontier.add(new Entry(next, cost, estimate(next, goal), order++));
                }
            }
        }
    }
}
