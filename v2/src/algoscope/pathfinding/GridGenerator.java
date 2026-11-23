package algoscope.pathfinding;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Ready-made maps: a maze or random walls and mud. The same seed always gives the same map. */
public final class GridGenerator {

    private GridGenerator() {
    }

    /**
     * A maze carved with depth-first search (the "recursive backtracker").
     * <p>
     * Rooms are the cells with both coordinates even; the cells between two rooms are
     * walls until the carver opens them. The carver walks from room to random unvisited
     * room, opening the wall in between, and backs up when it is stuck: the same idea as
     * DFS, used to build instead of to search. The result has exactly one route between
     * any two rooms. Odd sizes fit best (with an even size the last row or column stays
     * a wall).
     *
     * @param loops fraction (0 to 1) of the remaining inner walls to knock down afterwards.
     *              0 gives a perfect maze; more loops give several routes, which is when
     *              BFS, DFS, Dijkstra and A* start to choose differently.
     */
    public static Grid maze(int rows, int cols, double loops, long seed) {
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("A maze needs at least 3 x 3 cells, got " + rows + " x " + cols);
        }
        if (loops < 0 || loops > 1) {
            throw new IllegalArgumentException("loops must be between 0 and 1: " + loops);
        }
        Random random = new Random(seed);
        boolean[][] open = new boolean[rows][cols];

        // Iterative DFS with our own stack: a recursive one could overflow on big mazes
        Deque<Cell> stack = new ArrayDeque<>();
        open[0][0] = true;
        stack.push(new Cell(0, 0));
        while (!stack.isEmpty()) {
            Cell room = stack.peek();
            List<Cell> unvisited = new ArrayList<>(4);
            for (int[] d : new int[][]{{-2, 0}, {0, 2}, {2, 0}, {0, -2}}) {
                int r = room.row() + d[0];
                int c = room.col() + d[1];
                if (r >= 0 && r < rows && c >= 0 && c < cols && !open[r][c]) {
                    unvisited.add(new Cell(r, c));
                }
            }
            if (unvisited.isEmpty()) {
                // Dead end: back up to the previous room
                stack.pop();
                continue;
            }
            Cell next = unvisited.get(random.nextInt(unvisited.size()));
            // Open the wall halfway between the two rooms, and the new room
            open[(room.row() + next.row()) / 2][(room.col() + next.col()) / 2] = true;
            open[next.row()][next.col()] = true;
            stack.push(next);
        }

        // Knock down some walls that separate two rooms, to create loops
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean betweenRows = r % 2 == 1 && c % 2 == 0 && r + 1 < rows;
                boolean betweenCols = r % 2 == 0 && c % 2 == 1 && c + 1 < cols;
                if (!open[r][c] && (betweenRows || betweenCols) && random.nextDouble() < loops) {
                    open[r][c] = true;
                }
            }
        }

        Grid grid = new Grid(rows, cols);
        // The goal goes in the last room (bottom right); set it before adding walls
        Cell lastRoom = new Cell(evenAtMost(rows - 1), evenAtMost(cols - 1));
        grid.setGoal(lastRoom);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!open[r][c]) {
                    grid.setTerrain(new Cell(r, c), Terrain.WALL);
                }
            }
        }
        return grid;
    }

    /**
     * Random walls and mud on an open grid. The start and the goal are kept open.
     * There may be no route to the goal if there are many walls.
     */
    public static Grid scatter(int rows, int cols, double wallChance, double mudChance, long seed) {
        if (wallChance < 0 || mudChance < 0 || wallChance + mudChance > 1) {
            throw new IllegalArgumentException("Chances must be positive and add up to 1 at most");
        }
        Random random = new Random(seed);
        Grid grid = new Grid(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = new Cell(r, c);
                double roll = random.nextDouble();
                if (cell.equals(grid.start()) || cell.equals(grid.goal())) {
                    continue;
                }
                if (roll < wallChance) {
                    grid.setTerrain(cell, Terrain.WALL);
                } else if (roll < wallChance + mudChance) {
                    grid.setTerrain(cell, Terrain.MUD);
                }
            }
        }
        return grid;
    }

    private static int evenAtMost(int n) {
        return n % 2 == 0 ? n : n - 1;
    }
}
