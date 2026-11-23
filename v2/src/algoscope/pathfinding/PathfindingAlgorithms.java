package algoscope.pathfinding;

import java.util.List;

/** The list of search algorithms the app offers, in the order of the menu. */
public final class PathfindingAlgorithms {

    public static final List<PathfindingAlgorithm> ALL = List.of(
            new BreadthFirstSearch(),
            new DepthFirstSearch(),
            new Dijkstra(),
            new AStar());

    private PathfindingAlgorithms() {
    }

    /** Finds an algorithm by its name, like "A*". */
    public static PathfindingAlgorithm byName(String name) {
        for (PathfindingAlgorithm algorithm : ALL) {
            if (algorithm.name().equals(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Unknown pathfinding algorithm: " + name);
    }
}
