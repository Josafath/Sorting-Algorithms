package algoscope.ui.pathfinding;

import algoscope.engine.Timeline;
import algoscope.pathfinding.Cell;
import algoscope.pathfinding.Grid;
import algoscope.pathfinding.GridGenerator;
import algoscope.pathfinding.PathfindingAlgorithm;
import algoscope.pathfinding.PathfindingAlgorithms;
import algoscope.pathfinding.SearchRun;
import algoscope.pathfinding.SearchState;
import algoscope.pathfinding.SearchStep;
import algoscope.pathfinding.Terrain;

/**
 * What the user has in the Pathfinding tab: the map they are editing, the chosen
 * algorithm and the recording of that algorithm on that map. No Swing code, so it can
 * be tested on its own.
 * <p>
 * Every change (another algorithm, a painted cell, a moved start) records the search
 * again; on these map sizes that takes a few milliseconds.
 */
public final class PathfindingSession {

    /** Map sizes offered in the menu. Odd numbers, so mazes fill the whole map. */
    public enum MapSize {
        SMALL("Small", 15, 25),
        MEDIUM("Medium", 25, 41),
        LARGE("Large", 41, 71);

        private final String label;
        private final int rows;
        private final int cols;

        MapSize(String label, int rows, int cols) {
            this.label = label;
            this.rows = rows;
            this.cols = cols;
        }

        public String label() {
            return label;
        }

        public int rows() {
            return rows;
        }

        public int cols() {
            return cols;
        }
    }

    // A few loops so there are several routes and the algorithms choose differently
    static final double MAZE_LOOPS = 0.12;
    static final double RANDOM_WALLS = 0.25;
    static final double RANDOM_MUD = 0.20;

    private PathfindingAlgorithm algorithm = PathfindingAlgorithms.ALL.get(0);
    private MapSize mapSize = MapSize.MEDIUM;
    // Changes with every new map, so each one is different
    private long seed = 1;
    private Grid grid;
    private SearchRun run;

    public PathfindingSession() {
        grid = GridGenerator.maze(mapSize.rows(), mapSize.cols(), MAZE_LOOPS, seed);
        record();
    }

    public PathfindingAlgorithm algorithm() {
        return algorithm;
    }

    public MapSize mapSize() {
        return mapSize;
    }

    /** A copy of the map being edited. */
    public Grid grid() {
        return grid.copy();
    }

    /** The recording of the current algorithm on the current map, and its result. */
    public SearchRun run() {
        return run;
    }

    public Timeline<SearchState, SearchStep> timeline() {
        return run.timeline();
    }

    /** Another algorithm on the same map. */
    public void setAlgorithm(PathfindingAlgorithm algorithm) {
        this.algorithm = algorithm;
        record();
    }

    /** Another size: starts with a new maze of that size. */
    public void setMapSize(MapSize mapSize) {
        this.mapSize = mapSize;
        newMaze();
    }

    public void newMaze() {
        seed++;
        grid = GridGenerator.maze(mapSize.rows(), mapSize.cols(), MAZE_LOOPS, seed);
        record();
    }

    public void newRandomMap() {
        seed++;
        grid = GridGenerator.scatter(mapSize.rows(), mapSize.cols(), RANDOM_WALLS, RANDOM_MUD, seed);
        record();
    }

    /** An empty map of the same size, to draw on. */
    public void clear() {
        grid = new Grid(mapSize.rows(), mapSize.cols());
        record();
    }

    /**
     * Paints one cell. The start and the goal are never painted over.
     *
     * @return true if the map changed (so the screen must be updated)
     */
    public boolean paint(Cell cell, Terrain terrain) {
        if (!grid.inside(cell) || cell.equals(grid.start()) || cell.equals(grid.goal())
                || grid.terrain(cell) == terrain) {
            return false;
        }
        grid.setTerrain(cell, terrain);
        record();
        return true;
    }

    /**
     * Moves the start to another cell (a wall there is cleared).
     *
     * @return true if it moved; false outside the map, onto the goal or onto itself
     */
    public boolean moveStart(Cell cell) {
        if (!grid.inside(cell) || cell.equals(grid.start()) || cell.equals(grid.goal())) {
            return false;
        }
        grid.setStart(cell);
        record();
        return true;
    }

    /** Like {@link #moveStart}, for the goal. */
    public boolean moveGoal(Cell cell) {
        if (!grid.inside(cell) || cell.equals(grid.goal()) || cell.equals(grid.start())) {
            return false;
        }
        grid.setGoal(cell);
        record();
        return true;
    }

    private void record() {
        run = SearchRun.record(algorithm, grid);
    }
}
