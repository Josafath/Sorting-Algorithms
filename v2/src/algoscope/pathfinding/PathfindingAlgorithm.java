package algoscope.pathfinding;

/**
 * A search from the start to the goal of a grid. Implementations mark cells only
 * through the {@link SearchTracker}, and call {@link SearchTracker#foundPath} if they
 * reach the goal.
 */
public interface PathfindingAlgorithm {

    /** The name shown in the window, like "Dijkstra". */
    String name();

    /** One sentence that explains the idea, shown under the menu. */
    String description();

    /** True if mud makes a difference to the route it picks. */
    boolean usesCosts();

    /** True if the path it finds is always the best one (cheapest, or shortest if it ignores costs). */
    boolean findsBestPath();

    void search(SearchTracker tracker);
}
