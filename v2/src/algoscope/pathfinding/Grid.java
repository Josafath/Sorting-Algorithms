package algoscope.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The map the search algorithms walk on: a rectangle of cells, each one open, mud
 * or wall, with a start and a goal.
 * <p>
 * It is a graph in disguise: every cell is a node connected to its open neighbours
 * up, right, down and left. The start and the goal can never be walls.
 */
public final class Grid {

    // The four directions, always in this order so every run gives the same result
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    private final int rows;
    private final int cols;
    private final Terrain[] terrain;
    private Cell start;
    private Cell goal;

    /** An open grid with the start at the top left and the goal at the bottom right. */
    public Grid(int rows, int cols) {
        if (rows < 1 || cols < 1 || rows * cols < 2) {
            throw new IllegalArgumentException("A grid needs at least 2 cells, got " + rows + " x " + cols);
        }
        this.rows = rows;
        this.cols = cols;
        this.terrain = new Terrain[rows * cols];
        Arrays.fill(terrain, Terrain.OPEN);
        this.start = new Cell(0, 0);
        this.goal = new Cell(rows - 1, cols - 1);
    }

    /** An independent copy: changing one does not change the other. */
    public Grid copy() {
        Grid copy = new Grid(rows, cols);
        System.arraycopy(terrain, 0, copy.terrain, 0, terrain.length);
        copy.start = start;
        copy.goal = goal;
        return copy;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    /** Number of cells. */
    public int size() {
        return rows * cols;
    }

    public boolean inside(Cell cell) {
        return cell.row() >= 0 && cell.row() < rows && cell.col() >= 0 && cell.col() < cols;
    }

    /** Position of a cell in a flat array of {@link #size()} elements (row by row). */
    public int index(Cell cell) {
        check(cell);
        return cell.row() * cols + cell.col();
    }

    /** The cell at a flat index, the opposite of {@link #index(Cell)}. */
    public Cell cellAt(int index) {
        return new Cell(index / cols, index % cols);
    }

    public Terrain terrain(Cell cell) {
        return terrain[index(cell)];
    }

    public boolean passable(Cell cell) {
        return terrain(cell).passable();
    }

    /** Cost of stepping into the cell (1 for open, 5 for mud). */
    public int cost(Cell cell) {
        return terrain(cell).cost();
    }

    /** Changes a cell. The start and the goal cannot become walls. */
    public void setTerrain(Cell cell, Terrain newTerrain) {
        if (newTerrain == Terrain.WALL && (cell.equals(start) || cell.equals(goal))) {
            throw new IllegalArgumentException("The start and the goal cannot be walls: " + cell);
        }
        terrain[index(cell)] = newTerrain;
    }

    public Cell start() {
        return start;
    }

    public Cell goal() {
        return goal;
    }

    /** Moves the start. A wall under it becomes open ground. */
    public void setStart(Cell cell) {
        moveEndpoint(cell, goal);
        start = cell;
    }

    /** Moves the goal. A wall under it becomes open ground. */
    public void setGoal(Cell cell) {
        moveEndpoint(cell, start);
        goal = cell;
    }

    private void moveEndpoint(Cell cell, Cell other) {
        check(cell);
        if (cell.equals(other)) {
            throw new IllegalArgumentException("The start and the goal must be different cells: " + cell);
        }
        if (terrain(cell) == Terrain.WALL) {
            terrain[index(cell)] = Terrain.OPEN;
        }
    }

    /** The cells a path can move to from {@code cell}: up, right, down, left, skipping walls and edges. */
    public List<Cell> neighbors(Cell cell) {
        check(cell);
        List<Cell> result = new ArrayList<>(4);
        for (int[] d : DIRECTIONS) {
            Cell next = new Cell(cell.row() + d[0], cell.col() + d[1]);
            if (inside(next) && passable(next)) {
                result.add(next);
            }
        }
        return result;
    }

    private void check(Cell cell) {
        if (!inside(cell)) {
            throw new IndexOutOfBoundsException(cell + " is outside the " + rows + " x " + cols + " grid");
        }
    }
}
