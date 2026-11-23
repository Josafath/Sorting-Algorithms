package algoscope.pathfinding;

import algoscope.Check;
import algoscope.engine.Timeline;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests the grid, the four search algorithms, the recording and the map generators.
 * <p>
 * The algorithms are checked against {@link #reference}, a deliberately simple solver
 * that shares no code with them.
 */
public final class PathfindingTests {

    private static final BreadthFirstSearch BFS = new BreadthFirstSearch();
    private static final DepthFirstSearch DFS = new DepthFirstSearch();
    private static final Dijkstra DIJKSTRA = new Dijkstra();
    private static final AStar A_STAR = new AStar();

    private PathfindingTests() {
    }

    public static void run() {
        Check.group("Grid", PathfindingTests::grid);
        Check.group("Algorithm list", PathfindingTests::algorithmList);
        Check.group("Paths on an open grid", PathfindingTests::openGrid);
        Check.group("Small and impossible cases", PathfindingTests::edgeCases);
        Check.group("Against the reference solver (random maps)", PathfindingTests::againstReference);
        Check.group("How the algorithms differ", PathfindingTests::differences);
        Check.group("Recording and replay", PathfindingTests::replay);
        Check.group("Tracker checks", PathfindingTests::trackerChecks);
        Check.group("Maze generator", PathfindingTests::mazes);
        Check.group("Scatter generator", PathfindingTests::scatter);
    }

    // ---- Grid ----

    private static void grid() {
        Grid grid = new Grid(3, 4);
        Check.isTrue("a new grid starts top left and ends bottom right",
                grid.start().equals(new Cell(0, 0)) && grid.goal().equals(new Cell(2, 3)));
        Check.equal("neighbours of a middle cell: up, right, down, left",
                List.of(new Cell(0, 1), new Cell(1, 2), new Cell(2, 1), new Cell(1, 0)), grid.neighbors(new Cell(1, 1)));
        Check.equal("a corner has two neighbours", List.of(new Cell(0, 1), new Cell(1, 0)), grid.neighbors(new Cell(0, 0)));

        grid.setTerrain(new Cell(0, 1), Terrain.WALL);
        Check.equal("walls are not neighbours", List.of(new Cell(1, 0)), grid.neighbors(new Cell(0, 0)));
        grid.setTerrain(new Cell(1, 0), Terrain.MUD);
        Check.equal("mud is a neighbour that costs 5", 5, grid.cost(new Cell(1, 0)));

        Check.throwsException("the start cannot become a wall", IllegalArgumentException.class,
                () -> grid.setTerrain(grid.start(), Terrain.WALL));
        Check.throwsException("the goal cannot become a wall", IllegalArgumentException.class,
                () -> grid.setTerrain(grid.goal(), Terrain.WALL));
        grid.setStart(new Cell(0, 1));
        Check.isTrue("moving the start onto a wall clears the wall",
                grid.start().equals(new Cell(0, 1)) && grid.terrain(new Cell(0, 1)) == Terrain.OPEN);
        Check.throwsException("start and goal cannot be the same cell", IllegalArgumentException.class,
                () -> grid.setGoal(grid.start()));
        Check.throwsException("cells outside the grid are rejected", IndexOutOfBoundsException.class,
                () -> grid.setStart(new Cell(5, 0)));

        Grid copy = grid.copy();
        copy.setTerrain(new Cell(2, 0), Terrain.WALL);
        copy.setGoal(new Cell(2, 2));
        Check.isTrue("a copy is independent",
                grid.terrain(new Cell(2, 0)) == Terrain.OPEN && grid.goal().equals(new Cell(2, 3)));

        boolean roundTrip = true;
        for (int i = 0; i < grid.size(); i++) {
            roundTrip &= grid.index(grid.cellAt(i)) == i;
        }
        Check.isTrue("index and cellAt are opposites", roundTrip);
        Check.throwsException("a grid needs two cells", IllegalArgumentException.class, () -> new Grid(1, 1));
        Check.throwsException("a wall has no cost", IllegalStateException.class, () -> Terrain.WALL.cost());
    }

    private static void algorithmList() {
        Check.equal("there are four algorithms", 4, PathfindingAlgorithms.ALL.size());
        Set<String> names = new HashSet<>();
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            names.add(algorithm.name());
            Check.isTrue("byName finds " + algorithm.name(), PathfindingAlgorithms.byName(algorithm.name()) == algorithm);
        }
        Check.equal("names are unique", 4, names.size());
        Check.throwsException("an unknown name is rejected", IllegalArgumentException.class,
                () -> PathfindingAlgorithms.byName("Teleport"));
    }

    // ---- Paths ----

    private static void openGrid() {
        Grid grid = new Grid(10, 15);
        int straight = grid.start().manhattanDistance(grid.goal());
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            SearchResult result = SearchRun.record(algorithm, grid).result();
            Check.isTrue(algorithm.name() + " finds a valid path on an open grid", result.found() && validPath(grid, result.path()));
            if (algorithm.findsBestPath()) {
                Check.isTrue(algorithm.name() + " takes the " + straight + " steps of the Manhattan distance",
                        result.steps() == straight && result.cost() == straight);
            }
        }
    }

    private static void edgeCases() {
        Grid tiny = new Grid(1, 2);
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            SearchResult result = SearchRun.record(algorithm, tiny).result();
            Check.isTrue(algorithm.name() + ": goal right next to the start is 1 step, cost 1",
                    result.path().equals(List.of(new Cell(0, 0), new Cell(0, 1))) && result.cost() == 1);
        }

        // The goal at the bottom right, closed in by two walls
        Grid closed = new Grid(5, 5);
        closed.setTerrain(new Cell(3, 4), Terrain.WALL);
        closed.setTerrain(new Cell(4, 3), Terrain.WALL);
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            SearchResult result = SearchRun.record(algorithm, closed).result();
            Check.isTrue(algorithm.name() + ": an unreachable goal gives no path, cost 0 and visits every reachable cell",
                    !result.found() && result.cost() == 0 && result.steps() == 0 && result.visited() == 22);
        }
    }

    private static void againstReference() {
        int maps = 0;
        int unreachable = 0;
        boolean bfsShortest = true;
        boolean dijkstraCheapest = true;
        boolean aStarCheapest = true;
        boolean dfsValid = true;
        boolean allAgreeWhenImpossible = true;
        boolean aStarVisitsLess = true;
        boolean pathsValid = true;

        for (int seed = 1; seed <= 300; seed++) {
            int rows = 4 + seed % 9;
            int cols = 4 + (seed * 7) % 13;
            Grid grid = GridGenerator.scatter(rows, cols, 0.28, 0.22, seed);
            maps++;
            int[] steps = reference(grid, false);
            int[] costs = reference(grid, true);
            int goal = grid.index(grid.goal());

            SearchResult bfs = SearchRun.record(BFS, grid).result();
            SearchResult dfs = SearchRun.record(DFS, grid).result();
            SearchResult dijkstra = SearchRun.record(DIJKSTRA, grid).result();
            SearchResult aStar = SearchRun.record(A_STAR, grid).result();

            if (costs[goal] == Integer.MAX_VALUE) {
                unreachable++;
                allAgreeWhenImpossible &= !bfs.found() && !dfs.found() && !dijkstra.found() && !aStar.found();
                continue;
            }
            for (SearchResult r : List.of(bfs, dfs, dijkstra, aStar)) {
                pathsValid &= r.found() && validPath(grid, r.path()) && r.cost() == pathCost(grid, r.path());
            }
            bfsShortest &= bfs.steps() == steps[goal];
            dijkstraCheapest &= dijkstra.cost() == costs[goal];
            aStarCheapest &= aStar.cost() == costs[goal];
            dfsValid &= dfs.found();
            aStarVisitsLess &= aStar.visited() <= dijkstra.visited();
        }
        Check.isTrue(maps + " random maps tested, " + unreachable + " of them without a route (both cases covered)",
                unreachable > 10 && maps - unreachable > 100);
        Check.isTrue("every path found is valid and its cost is right", pathsValid);
        Check.isTrue("BFS always finds the fewest steps", bfsShortest);
        Check.isTrue("Dijkstra always finds the cheapest cost", dijkstraCheapest);
        Check.isTrue("A* always finds the cheapest cost", aStarCheapest);
        Check.isTrue("DFS always finds a route when there is one", dfsValid);
        Check.isTrue("no algorithm invents a route when there is none", allAgreeWhenImpossible);
        Check.isTrue("A* never explores more cells than Dijkstra", aStarVisitsLess);
    }

    private static void differences() {
        // Start and goal on the middle row; the straight line is mud, the rows above and below are open
        Grid mud = new Grid(3, 5);
        mud.setStart(new Cell(1, 0));
        mud.setGoal(new Cell(1, 4));
        for (int c = 1; c <= 3; c++) {
            mud.setTerrain(new Cell(1, c), Terrain.MUD);
        }
        SearchResult bfs = SearchRun.record(BFS, mud).result();
        SearchResult dijkstra = SearchRun.record(DIJKSTRA, mud).result();
        SearchResult aStar = SearchRun.record(A_STAR, mud).result();
        Check.isTrue("BFS walks straight through the mud: 4 steps, cost 16", bfs.steps() == 4 && bfs.cost() == 16);
        Check.isTrue("Dijkstra goes around: 6 steps, cost 6", dijkstra.steps() == 6 && dijkstra.cost() == 6);
        Check.isTrue("A* goes around too: cost 6", aStar.cost() == 6);

        // DFS on an open grid: it follows "up" first and wanders before reaching the goal
        Grid open = new Grid(5, 5);
        open.setStart(new Cell(2, 0));
        open.setGoal(new Cell(2, 4));
        Check.isTrue("DFS can find a longer path than BFS",
                SearchRun.record(DFS, open).result().steps() > SearchRun.record(BFS, open).result().steps());
        Check.equal("DFS tries up first: its second visit is the cell above the start",
                new Cell(1, 0), secondVisit(SearchRun.record(DFS, open)));
        Check.equal("BFS keeps the same order: its second visit is also the cell above",
                new Cell(1, 0), secondVisit(SearchRun.record(BFS, open)));

        Grid wide = new Grid(30, 30);
        int dijkstraVisits = SearchRun.record(DIJKSTRA, wide).result().visited();
        int aStarVisits = SearchRun.record(A_STAR, wide).result().visited();
        Check.isTrue("on an open 30 x 30 grid A* explores far fewer cells than Dijkstra ("
                + aStarVisits + " vs " + dijkstraVisits + ")", aStarVisits * 4 < dijkstraVisits);
    }

    // ---- Recording ----

    private static void replay() {
        Grid grid = GridGenerator.scatter(12, 18, 0.25, 0.2, 5);
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            SearchRun run = SearchRun.record(algorithm, grid);
            Timeline<SearchState, SearchStep> timeline = run.timeline();
            SearchState state = timeline.state();

            boolean stepsValid = true;
            boolean pathStepsLast = true;
            boolean seenPath = false;
            while (timeline.stepForward()) {
                SearchStep step = timeline.current();
                stepsValid &= grid.passable(step.cell()) && state.status(step.cell()) == step.after();
                if (step instanceof SearchStep.PathCell) {
                    seenPath = true;
                } else {
                    pathStepsLast &= !seenPath;
                }
            }
            Check.isTrue(algorithm.name() + ": every step touches an open cell and sets its status", stepsValid);
            Check.isTrue(algorithm.name() + ": the path is shown after the search", pathStepsLast);
            Check.isTrue(algorithm.name() + ": at the end the counters match the result",
                    state.visited() == run.result().visited() && state.pathCells() == run.result().path().size());
            boolean pathMarked = true;
            for (Cell cell : run.result().path()) {
                pathMarked &= state.status(cell) == CellStatus.PATH;
            }
            Check.isTrue(algorithm.name() + ": every path cell is marked as path", pathMarked);

            timeline.seek(0);
            boolean clean = state.visited() == 0 && state.pathCells() == 0;
            for (int i = 0; i < grid.size(); i++) {
                clean &= state.status(grid.cellAt(i)) == CellStatus.UNSEEN;
            }
            Check.isTrue(algorithm.name() + ": rewinding clears every cell and counter", clean);
        }

        Grid original = GridGenerator.scatter(6, 6, 0.2, 0.2, 2);
        Grid before = original.copy();
        SearchRun run = SearchRun.record(DIJKSTRA, original);
        original.setTerrain(new Cell(0, 1), Terrain.WALL);
        Check.isTrue("the recording keeps its own copy of the grid",
                run.timeline().state().grid().terrain(new Cell(0, 1)) == before.terrain(new Cell(0, 1)));
    }

    private static void trackerChecks() {
        Grid grid = new Grid(3, 3);
        SearchTracker tracker = new SearchTracker(grid);
        tracker.visit(grid.start());
        Check.throwsException("a cell cannot be visited twice", IllegalStateException.class,
                () -> tracker.visit(grid.start()));
        Check.throwsException("a path must start at the start", IllegalArgumentException.class,
                () -> tracker.foundPath(List.of(new Cell(1, 1), grid.goal())));
        Check.throwsException("a path cannot jump", IllegalArgumentException.class,
                () -> tracker.foundPath(List.of(grid.start(), grid.goal())));
        grid.setTerrain(new Cell(0, 1), Terrain.WALL);
        Check.throwsException("a path cannot go through a wall", IllegalArgumentException.class,
                () -> tracker.foundPath(List.of(grid.start(), new Cell(0, 1), new Cell(0, 2),
                        new Cell(1, 2), grid.goal())));
    }

    // ---- Generators ----

    private static void mazes() {
        Grid maze = GridGenerator.maze(21, 31, 0, 1);
        Check.isTrue("start and goal of a maze are the corner rooms",
                maze.start().equals(new Cell(0, 0)) && maze.goal().equals(new Cell(20, 30))
                        && maze.passable(maze.start()) && maze.passable(maze.goal()));
        boolean pillars = true;
        boolean roomsOpen = true;
        for (int r = 0; r < maze.rows(); r++) {
            for (int c = 0; c < maze.cols(); c++) {
                if (r % 2 == 1 && c % 2 == 1) {
                    pillars &= !maze.passable(new Cell(r, c));
                }
                if (r % 2 == 0 && c % 2 == 0) {
                    roomsOpen &= maze.passable(new Cell(r, c));
                }
            }
        }
        Check.isTrue("cells with both coordinates odd are always walls", pillars);
        Check.isTrue("every room is open", roomsOpen);

        int[] reach = reference(maze, false);
        boolean allReachable = true;
        for (int i = 0; i < maze.size(); i++) {
            allReachable &= !maze.passable(maze.cellAt(i)) || reach[i] != Integer.MAX_VALUE;
        }
        Check.isTrue("every open cell can be reached from the start", allReachable);
        Check.equal("a perfect maze is a tree: open cells - 1 connections", openCells(maze) - 1, connections(maze));

        int stepsBfs = SearchRun.record(BFS, maze).result().steps();
        boolean sameRoute = true;
        for (PathfindingAlgorithm algorithm : PathfindingAlgorithms.ALL) {
            sameRoute &= SearchRun.record(algorithm, maze).result().steps() == stepsBfs;
        }
        Check.isTrue("in a perfect maze there is one route, so all four agree (" + stepsBfs + " steps)", sameRoute);

        Grid loopy = GridGenerator.maze(21, 31, 0.3, 1);
        Check.isTrue("loops add extra connections", connections(loopy) > openCells(loopy) - 1);

        Check.isTrue("the same seed gives the same maze", sameMap(maze, GridGenerator.maze(21, 31, 0, 1)));
        Check.isTrue("another seed gives another maze", !sameMap(maze, GridGenerator.maze(21, 31, 0, 2)));

        Grid even = GridGenerator.maze(20, 30, 0, 3);
        Check.isTrue("with even sizes the goal is the last room", even.goal().equals(new Cell(18, 28)) && even.passable(even.goal()));
        Check.throwsException("a maze needs at least 3 x 3", IllegalArgumentException.class,
                () -> GridGenerator.maze(2, 9, 0, 1));
        Check.throwsException("loops must be between 0 and 1", IllegalArgumentException.class,
                () -> GridGenerator.maze(9, 9, 1.5, 1));
    }

    private static void scatter() {
        Grid grid = GridGenerator.scatter(40, 40, 0.3, 0.2, 9);
        Check.isTrue("scatter keeps start and goal open", grid.passable(grid.start()) && grid.passable(grid.goal()));
        int walls = 0;
        int mud = 0;
        for (int i = 0; i < grid.size(); i++) {
            Terrain t = grid.terrain(grid.cellAt(i));
            walls += t == Terrain.WALL ? 1 : 0;
            mud += t == Terrain.MUD ? 1 : 0;
        }
        Check.isTrue("about 30% walls and 20% mud (" + walls + " and " + mud + " of 1,600)",
                walls > 400 && walls < 560 && mud > 240 && mud < 400);
        Check.isTrue("the same seed gives the same map", sameMap(grid, GridGenerator.scatter(40, 40, 0.3, 0.2, 9)));
        Check.throwsException("chances over 100% are rejected", IllegalArgumentException.class,
                () -> GridGenerator.scatter(5, 5, 0.7, 0.5, 1));
    }

    // ---- Helpers ----

    /**
     * The reference solver: cost from the start to every cell, by repeating
     * "can a neighbour be reached more cheaply through this cell?" until nothing changes
     * (Bellman-Ford). Slow, but too simple to get wrong.
     *
     * @param weighted false counts steps (what BFS minimises), true adds terrain costs
     */
    private static int[] reference(Grid grid, boolean weighted) {
        int[] best = new int[grid.size()];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[grid.index(grid.start())] = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < grid.size(); i++) {
                Cell cell = grid.cellAt(i);
                if (best[i] == Integer.MAX_VALUE || !grid.passable(cell)) {
                    continue;
                }
                for (Cell next : grid.neighbors(cell)) {
                    int cost = best[i] + (weighted ? grid.cost(next) : 1);
                    if (cost < best[grid.index(next)]) {
                        best[grid.index(next)] = cost;
                        changed = true;
                    }
                }
            }
        }
        return best;
    }

    private static boolean validPath(Grid grid, List<Cell> path) {
        if (path.isEmpty() || !path.get(0).equals(grid.start()) || !path.get(path.size() - 1).equals(grid.goal())) {
            return false;
        }
        Set<Cell> seen = new HashSet<>();
        for (int k = 0; k < path.size(); k++) {
            Cell cell = path.get(k);
            // No walls, no cell twice, and each move to a neighbour
            if (!grid.passable(cell) || !seen.add(cell)) {
                return false;
            }
            if (k > 0 && path.get(k - 1).manhattanDistance(cell) != 1) {
                return false;
            }
        }
        return true;
    }

    /** The cell of the second Visit step (the first one is always the start). */
    private static Cell secondVisit(SearchRun run) {
        Timeline<SearchState, SearchStep> timeline = run.timeline();
        int visits = 0;
        while (timeline.stepForward()) {
            if (timeline.current() instanceof SearchStep.Visit visit && ++visits == 2) {
                return visit.cell();
            }
        }
        return null;
    }

    private static int pathCost(Grid grid, List<Cell> path) {
        int cost = 0;
        for (int k = 1; k < path.size(); k++) {
            cost += grid.cost(path.get(k));
        }
        return cost;
    }

    private static int openCells(Grid grid) {
        int count = 0;
        for (int i = 0; i < grid.size(); i++) {
            count += grid.passable(grid.cellAt(i)) ? 1 : 0;
        }
        return count;
    }

    /** Pairs of open cells next to each other (each pair counted once). */
    private static int connections(Grid grid) {
        int count = 0;
        for (int i = 0; i < grid.size(); i++) {
            Cell cell = grid.cellAt(i);
            if (!grid.passable(cell)) {
                continue;
            }
            for (Cell next : grid.neighbors(cell)) {
                count += grid.index(next) > i ? 1 : 0;
            }
        }
        return count;
    }

    private static boolean sameMap(Grid a, Grid b) {
        if (a.rows() != b.rows() || a.cols() != b.cols() || !a.start().equals(b.start()) || !a.goal().equals(b.goal())) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (a.terrain(a.cellAt(i)) != b.terrain(b.cellAt(i))) {
                return false;
            }
        }
        return true;
    }
}
