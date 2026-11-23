package algoscope.ui.pathfinding;

import algoscope.Check;
import algoscope.pathfinding.BreadthFirstSearch;
import algoscope.pathfinding.Cell;
import algoscope.pathfinding.CellStatus;
import algoscope.pathfinding.Grid;
import algoscope.pathfinding.SearchRun;
import algoscope.pathfinding.Terrain;
import algoscope.ui.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** Tests the Pathfinding tab without a screen: the session, the cell colors, the view and the mouse. */
public final class PathfindingUiTests {

    // Map cells are 10 px with an 8 px margin in the views built below
    private static final int CELL = 10;
    private static final int MARGIN = 8;

    private PathfindingUiTests() {
    }

    public static void run() {
        Check.group("Pathfinding session", PathfindingUiTests::session);
        Check.group("Cell colors", PathfindingUiTests::cellColors);
        Check.group("Grid view geometry", PathfindingUiTests::geometry);
        Check.group("Grid drawn on screen", PathfindingUiTests::painting);
        Check.group("Search descriptions", PathfindingUiTests::descriptions);
        Check.group("Pathfinding panel and mouse", PathfindingUiTests::panel);
    }

    private static void session() {
        PathfindingSession session = new PathfindingSession();
        Grid map = session.grid();
        Check.isTrue("starts with a medium maze",
                map.rows() == 25 && map.cols() == 41 && session.run().result().found());
        Check.isTrue("starts at the beginning of the recording", session.timeline().atStart());

        session.setAlgorithm(new algoscope.pathfinding.AStar());
        Check.isTrue("changing the algorithm keeps the map", sameMap(map, session.grid()));
        Check.equal("and records the new algorithm", "A*", session.algorithm().name());

        session.clear();
        Grid open = session.grid();
        boolean allOpen = true;
        for (int i = 0; i < open.size(); i++) {
            allOpen &= open.terrain(open.cellAt(i)) == Terrain.OPEN;
        }
        Check.isTrue("clear leaves an open map of the same size", allOpen && open.rows() == 25);

        Cell cell = new Cell(3, 3);
        SearchRun before = session.run();
        Check.isTrue("painting a cell changes the map", session.paint(cell, Terrain.WALL)
                && session.grid().terrain(cell) == Terrain.WALL);
        Check.isTrue("and records the search again", session.run() != before);
        Check.isTrue("painting the same terrain again changes nothing", !session.paint(cell, Terrain.WALL));
        Check.isTrue("the start is never painted", !session.paint(session.grid().start(), Terrain.WALL));
        Check.isTrue("cells outside the map are ignored", !session.paint(new Cell(-1, 0), Terrain.MUD));

        Check.isTrue("the start moves onto a wall and clears it",
                session.moveStart(cell) && session.grid().start().equals(cell) && session.grid().passable(cell));
        Check.isTrue("the start cannot move onto the goal", !session.moveStart(session.grid().goal()));
        Check.isTrue("the goal moves", session.moveGoal(new Cell(10, 10)) && session.grid().goal().equals(new Cell(10, 10)));
        Check.isTrue("the goal cannot move onto the start", !session.moveGoal(cell));

        Grid copy = session.grid();
        copy.setTerrain(new Cell(0, 5), Terrain.WALL);
        Check.isTrue("grid() hands out a copy", session.grid().terrain(new Cell(0, 5)) == Terrain.OPEN);

        session.newMaze();
        Grid maze1 = session.grid();
        session.newMaze();
        Check.isTrue("each new maze is different", !sameMap(maze1, session.grid()));
        session.newRandomMap();
        Check.isTrue("a random map has mud", hasTerrain(session.grid(), Terrain.MUD));
        session.setMapSize(PathfindingSession.MapSize.LARGE);
        Check.isTrue("changing the size creates a maze of that size",
                session.grid().rows() == 41 && session.grid().cols() == 71);
    }

    private static void cellColors() {
        Check.equal("walls are dark whatever their status", Theme.CELL_WALL, GridView.colorFor(Terrain.WALL, CellStatus.UNSEEN));
        Check.equal("unseen open ground is white", Theme.CELL_OPEN, GridView.colorFor(Terrain.OPEN, CellStatus.UNSEEN));
        Check.equal("unseen mud is brown", Theme.CELL_MUD, GridView.colorFor(Terrain.MUD, CellStatus.UNSEEN));
        Check.equal("the frontier is green", Theme.FRONTIER, GridView.colorFor(Terrain.OPEN, CellStatus.FRONTIER));
        Check.equal("explored cells are blue", Theme.VISITED, GridView.colorFor(Terrain.OPEN, CellStatus.VISITED));
        Check.equal("the path is yellow", Theme.PATH, GridView.colorFor(Terrain.OPEN, CellStatus.PATH));
        Color exploredMud = GridView.colorFor(Terrain.MUD, CellStatus.VISITED);
        Check.isTrue("explored mud is a mix: still different from explored ground and from mud",
                !exploredMud.equals(Theme.VISITED) && !exploredMud.equals(Theme.CELL_MUD));
        Check.equal("mixing nothing keeps the color", Color.RED, GridView.mix(Color.RED, Color.BLUE, 0));
        Check.equal("mixing everything gives the other color", Color.BLUE, GridView.mix(Color.RED, Color.BLUE, 1));
    }

    /** A view of a run on a rows x cols map, sized so cells are exactly 10 px. */
    private static GridView view(SearchRun run, int rows, int cols) {
        GridView view = new GridView();
        view.setSize(cols * CELL + 2 * MARGIN, rows * CELL + 2 * MARGIN);
        view.setTimeline(run.timeline());
        return view;
    }

    private static void geometry() {
        GridView view = view(SearchRun.record(new BreadthFirstSearch(), new Grid(5, 10)), 5, 10);
        Check.equal("the top left pixel of the map is cell (0, 0)", new Cell(0, 0), view.cellAt(MARGIN, MARGIN));
        Check.equal("the bottom right pixel is cell (4, 9)", new Cell(4, 9), view.cellAt(MARGIN + 99, MARGIN + 49));
        Check.equal("a point in the middle of a cell", new Cell(2, 3), view.cellAt(MARGIN + 35, MARGIN + 25));
        Check.equal("left of the map is no cell", null, view.cellAt(MARGIN - 1, MARGIN));
        Check.equal("right of the map is no cell", null, view.cellAt(MARGIN + 100, MARGIN));
        Check.equal("below the map is no cell", null, view.cellAt(MARGIN, MARGIN + 50));
        Check.equal("a view without a map has no cells", null, new GridView().cellAt(10, 10));
    }

    private static void painting() {
        Grid grid = new Grid(5, 10);
        grid.setTerrain(new Cell(2, 2), Terrain.WALL);
        grid.setTerrain(new Cell(4, 2), Terrain.MUD);
        SearchRun run = SearchRun.record(new BreadthFirstSearch(), grid);
        GridView view = view(run, 5, 10);

        BufferedImage start = paint(view);
        Check.equal("the start is green", Theme.START, colorOf(start, new Cell(0, 0)));
        Check.equal("the goal is red", Theme.GOAL, colorOf(start, new Cell(4, 9)));
        Check.equal("a wall is dark", Theme.CELL_WALL, colorOf(start, new Cell(2, 2)));
        Check.equal("mud is brown", Theme.CELL_MUD, colorOf(start, new Cell(4, 2)));
        Check.equal("an unseen cell is white", Theme.CELL_OPEN, colorOf(start, new Cell(1, 5)));

        run.timeline().seek(run.timeline().size());
        BufferedImage end = paint(view);
        Cell onPath = run.result().path().get(3);
        Check.equal("at the end the path is yellow", Theme.PATH, colorOf(end, onPath));
        Check.equal("explored cells off the path are blue", Theme.VISITED, colorOf(end, firstExploredOffPath(run)));
    }

    private static Cell firstExploredOffPath(SearchRun run) {
        Grid grid = run.timeline().state().grid();
        for (int i = 0; i < grid.size(); i++) {
            Cell cell = grid.cellAt(i);
            if (run.timeline().state().status(cell) == CellStatus.VISITED && grid.terrain(cell) == Terrain.OPEN
                    && !cell.equals(grid.start()) && !cell.equals(grid.goal())) {
                return cell;
            }
        }
        throw new IllegalStateException("No explored cell off the path");
    }

    private static BufferedImage paint(GridView view) {
        BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    /** The color in the middle of a cell. */
    private static Color colorOf(BufferedImage image, Cell cell) {
        return new Color(image.getRGB(MARGIN + cell.col() * CELL + CELL / 2, MARGIN + cell.row() * CELL + CELL / 2));
    }

    private static void descriptions() {
        Grid grid = new Grid(1, 3);
        SearchRun run = SearchRun.record(new BreadthFirstSearch(), grid);
        Check.equal("before the first step", "Ready. Press Play or Space, or draw on the map.", PathfindingPanel.describe(run));
        run.timeline().stepForward();
        Check.equal("discovering a cell", "Added (0, 0) to the frontier", PathfindingPanel.describe(run));
        run.timeline().stepForward();
        Check.equal("exploring a cell", "Exploring (0, 0)", PathfindingPanel.describe(run));
        run.timeline().seek(run.timeline().size() - 1);
        Check.equal("tracing the path", "Tracing the path: (0, 1)", PathfindingPanel.describe(run));
        run.timeline().seek(run.timeline().size());
        Check.equal("the result at the end", "Path found: 2 steps, cost 2, 3 cells explored.", PathfindingPanel.describe(run));

        Grid blocked = new Grid(1, 3);
        blocked.setTerrain(new Cell(0, 1), Terrain.WALL);
        SearchRun none = SearchRun.record(new BreadthFirstSearch(), blocked);
        none.timeline().seek(none.timeline().size());
        Check.equal("no path at the end (and 1 cell, not 1 cells)",
                "No path: the goal cannot be reached (1 cell explored).", PathfindingPanel.describe(none));
        SearchRun oneStep = SearchRun.record(new BreadthFirstSearch(), new Grid(1, 2));
        oneStep.timeline().seek(oneStep.timeline().size());
        Check.equal("1 step, not 1 steps", "Path found: 1 step, cost 1, 2 cells explored.", PathfindingPanel.describe(oneStep));
    }

    private static void panel() {
        PathfindingPanel panel = new PathfindingPanel();
        Check.isTrue("a new panel is ready at step 0", panel.stepText().startsWith("Step 0 /")
                && panel.actionText().startsWith("Ready"));
        panel.playback().forwardButton().doClick(0);
        Check.isTrue("the step button moves one step", panel.stepText().startsWith("Step 1 /"));

        // An open medium map, in a view where every cell is 10 px
        panel.clearButton().doClick(0);
        GridView view = panel.gridView();
        view.setSize(41 * CELL + 2 * MARGIN, 25 * CELL + 2 * MARGIN);
        PathfindingSession session = panel.session();

        // Drawing a wall: press on (5, 5) and jump straight to (5, 10) as a fast mouse would
        press(view, new Cell(5, 5), false);
        drag(view, new Cell(5, 10), false);
        release(view, new Cell(5, 10));
        boolean line = true;
        for (int c = 5; c <= 10; c++) {
            line &= session.grid().terrain(new Cell(5, c)) == Terrain.WALL;
        }
        Check.isTrue("dragging draws a continuous wall, even when the mouse jumps", line);
        Check.isTrue("editing goes back to step 0 of the new recording", panel.player().timeline().atStart());

        press(view, new Cell(5, 7), true);
        release(view, new Cell(5, 7));
        Check.equal("the right button erases", Terrain.OPEN, session.grid().terrain(new Cell(5, 7)));

        panel.mudTool().doClick(0);
        press(view, new Cell(8, 8), false);
        release(view, new Cell(8, 8));
        Check.equal("the mud tool draws mud", Terrain.MUD, session.grid().terrain(new Cell(8, 8)));

        press(view, new Cell(0, 0), false);
        drag(view, new Cell(2, 3), false);
        release(view, new Cell(2, 3));
        Check.isTrue("dragging the S moves the start, without painting on the way",
                session.grid().start().equals(new Cell(2, 3)) && session.grid().terrain(new Cell(0, 0)) == Terrain.OPEN);

        Cell goal = session.grid().goal();
        press(view, goal, false);
        drag(view, new Cell(2, 3), false);
        release(view, new Cell(2, 3));
        Check.equal("the goal cannot be dropped on the start", goal, session.grid().goal());

        panel.algorithmBox().setSelectedIndex(3);
        Check.isTrue("choosing another algorithm keeps the drawing and starts at step 0",
                session.grid().terrain(new Cell(5, 5)) == Terrain.WALL && panel.player().timeline().atStart()
                        && session.algorithm().name().equals("A*"));
    }

    // ---- Mouse events sent straight to the view ----

    private static void press(GridView view, Cell cell, boolean right) {
        send(view, MouseEvent.MOUSE_PRESSED, cell, right);
    }

    private static void drag(GridView view, Cell cell, boolean right) {
        send(view, MouseEvent.MOUSE_DRAGGED, cell, right);
    }

    private static void release(GridView view, Cell cell) {
        send(view, MouseEvent.MOUSE_RELEASED, cell, false);
    }

    private static void send(GridView view, int id, Cell cell, boolean right) {
        int x = MARGIN + cell.col() * CELL + CELL / 2;
        int y = MARGIN + cell.row() * CELL + CELL / 2;
        int mask = id == MouseEvent.MOUSE_RELEASED ? 0
                : right ? InputEvent.BUTTON3_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK;
        int button = id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.NOBUTTON
                : right ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1;
        view.dispatchEvent(new MouseEvent(view, id, System.currentTimeMillis(), mask, x, y, 1, false, button));
    }

    // ---- Helpers ----

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

    private static boolean hasTerrain(Grid grid, Terrain terrain) {
        for (int i = 0; i < grid.size(); i++) {
            if (grid.terrain(grid.cellAt(i)) == terrain) {
                return true;
            }
        }
        return false;
    }
}
