package algoscope.pathfinding;

/**
 * What a cell is made of. The cost is paid when a path <em>enters</em> the cell.
 * <p>
 * Mud is what makes the algorithms different: BFS ignores costs and walks straight
 * through it (fewest steps), Dijkstra and A* go around it when that is cheaper.
 */
public enum Terrain {

    OPEN("Open", 1),
    MUD("Mud", 5),
    WALL("Wall", 0);

    private final String label;
    private final int cost;

    Terrain(String label, int cost) {
        this.label = label;
        this.cost = cost;
    }

    public String label() {
        return label;
    }

    public boolean passable() {
        return this != WALL;
    }

    /** Cost of stepping into a cell of this terrain. */
    public int cost() {
        if (!passable()) {
            throw new IllegalStateException("A wall cannot be entered");
        }
        return cost;
    }
}
