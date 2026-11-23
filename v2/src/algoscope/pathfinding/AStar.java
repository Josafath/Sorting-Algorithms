package algoscope.pathfinding;

/**
 * Dijkstra plus a guess: the priority is cost so far + steps left to the goal in a
 * straight line (Manhattan distance). The guess pulls the search towards the goal, so
 * it explores fewer cells, and it still finds the cheapest path.
 */
public final class AStar extends CostSearch {

    @Override
    public String name() {
        return "A*";
    }

    @Override
    public String description() {
        return "Like Dijkstra, but adds the distance left to the goal, so it heads straight there and explores fewer cells.";
    }

    /**
     * Manhattan distance times the cheapest terrain cost (1). It never overestimates:
     * every step costs at least 1 and at least that many steps are needed. That is
     * what keeps the path A* finds the cheapest one.
     */
    @Override
    int estimate(Cell cell, Cell goal) {
        return cell.manhattanDistance(goal) * Terrain.OPEN.cost();
    }
}
