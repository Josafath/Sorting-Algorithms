package algoscope.pathfinding;

/**
 * Explores the cheapest cell of the frontier first, so cells are reached in order of
 * cost from the start. The first time the goal is explored, its path is the cheapest.
 */
public final class Dijkstra extends CostSearch {

    @Override
    public String name() {
        return "Dijkstra";
    }

    @Override
    public String description() {
        return "Always explores the cheapest cell reached so far, so it finds the cheapest path, going around mud when it pays.";
    }

    /** No guess at all: Dijkstra only looks at the cost already paid. */
    @Override
    int estimate(Cell cell, Cell goal) {
        return 0;
    }
}
