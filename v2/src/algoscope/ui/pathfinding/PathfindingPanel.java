package algoscope.ui.pathfinding;

import algoscope.engine.Timeline;
import algoscope.pathfinding.Cell;
import algoscope.pathfinding.PathfindingAlgorithm;
import algoscope.pathfinding.PathfindingAlgorithms;
import algoscope.pathfinding.SearchResult;
import algoscope.pathfinding.SearchRun;
import algoscope.pathfinding.SearchState;
import algoscope.pathfinding.SearchStep;
import algoscope.pathfinding.Terrain;
import algoscope.ui.PlaybackBar;
import algoscope.ui.Player;
import algoscope.ui.Theme;
import algoscope.ui.Widgets;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * The Pathfinding tab: choose an algorithm and a map on top, draw on the map with the
 * mouse, watch the search spread and the path appear, and read the result at the bottom.
 * <p>
 * Mouse: drag with the left button to draw with the chosen tool, with the right button
 * to erase, and drag the S or the G to move the start or the goal.
 */
public final class PathfindingPanel extends JPanel {

    /** What a mouse drag is doing. */
    private enum Drag { NONE, PAINT, START, GOAL }

    private final PathfindingSession session = new PathfindingSession();
    private final Player player = new Player();
    private final GridView gridView = new GridView();
    private final PlaybackBar playback = new PlaybackBar(player);

    // Top: what to search and on which map
    private final JComboBox<PathfindingAlgorithm> algorithmBox =
            new JComboBox<>(PathfindingAlgorithms.ALL.toArray(new PathfindingAlgorithm[0]));
    private final JComboBox<PathfindingSession.MapSize> sizeBox = new JComboBox<>(PathfindingSession.MapSize.values());
    private final JButton mazeButton = new JButton("Maze");
    private final JButton randomButton = new JButton("Random map");
    private final JButton clearButton = new JButton("Clear");
    private final JToggleButton wallTool = new JToggleButton("Wall", true);
    private final JToggleButton mudTool = new JToggleButton("Mud");
    private final JToggleButton eraseTool = new JToggleButton("Erase");
    private final JLabel descriptionLabel = new JLabel();
    private final JLabel propertiesLabel = new JLabel();

    // Bottom: what is happening
    private final JLabel actionLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();
    private final JLabel exploredLabel = new JLabel();
    private final JLabel pathLabel = new JLabel();

    // The drag in progress and the last cell it touched
    private Drag drag = Drag.NONE;
    private Terrain dragTerrain = Terrain.WALL;
    private Cell lastCell;

    public PathfindingPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTop(), BorderLayout.NORTH);
        gridView.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(gridView, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        wireEvents();
        installMouse();
        player.addListener(this::refresh);
        loadRun();
    }

    /** Stops the animation, for example when the user switches to another tab. */
    public void pause() {
        player.pause();
    }

    // ---- Layout ----

    private JComponent buildTop() {
        algorithmBox.setRenderer(Widgets.textRenderer(PathfindingAlgorithm::name));
        sizeBox.setRenderer(Widgets.textRenderer(PathfindingSession.MapSize::label));
        sizeBox.setSelectedItem(session.mapSize());
        ButtonGroup tools = new ButtonGroup();
        for (JToggleButton tool : new JToggleButton[]{wallTool, mudTool, eraseTool}) {
            tools.add(tool);
        }
        // Nothing here takes the focus, so the playback shortcuts always work
        for (JComponent control : new JComponent[]{algorithmBox, sizeBox, mazeButton, randomButton, clearButton,
            wallTool, mudTool, eraseTool}) {
            control.setFocusable(false);
        }

        JPanel choices = Widgets.row();
        choices.add(Widgets.caption("Algorithm"));
        choices.add(algorithmBox);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(Widgets.caption("Map"));
        choices.add(sizeBox);
        choices.add(mazeButton);
        choices.add(randomButton);
        choices.add(clearButton);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(Widgets.caption("Draw"));
        choices.add(wallTool);
        choices.add(mudTool);
        choices.add(eraseTool);

        descriptionLabel.setForeground(Theme.TEXT);
        propertiesLabel.setForeground(Theme.MUTED_TEXT);
        JLabel hint = Widgets.caption("Drag to draw  ·  right-drag to erase  ·  drag S or G to move them  ·  mud costs 5, open ground 1");
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));
        info.add(descriptionLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(propertiesLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(hint);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(choices, BorderLayout.NORTH);
        top.add(info, BorderLayout.SOUTH);
        return top;
    }

    private JComponent buildBottom() {
        actionLabel.setFont(Theme.font(Font.BOLD, 14f));
        actionLabel.setForeground(Theme.TEXT);
        JPanel counters = Widgets.row();
        for (JLabel label : new JLabel[]{stepLabel, exploredLabel, pathLabel}) {
            label.setForeground(Theme.MUTED_TEXT);
            counters.add(label);
            counters.add(Box.createHorizontalStrut(14));
        }
        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);
        status.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 0));
        status.add(actionLabel, BorderLayout.WEST);
        status.add(counters, BorderLayout.EAST);

        JPanel legend = Widgets.row();
        legend.add(Widgets.legendItem(Theme.START, "Start"));
        legend.add(Widgets.legendItem(Theme.GOAL, "Goal"));
        legend.add(Widgets.legendItem(Theme.CELL_WALL, "Wall"));
        legend.add(Widgets.legendItem(Theme.CELL_MUD, "Mud"));
        legend.add(Widgets.legendItem(Theme.FRONTIER, "Frontier"));
        legend.add(Widgets.legendItem(Theme.VISITED, "Explored"));
        legend.add(Widgets.legendItem(Theme.PATH, "Path"));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        for (JComponent part : new JComponent[]{playback, status, legend}) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
            bottom.add(part);
        }
        return bottom;
    }

    // ---- Events ----

    private void wireEvents() {
        algorithmBox.addActionListener(e -> {
            session.setAlgorithm((PathfindingAlgorithm) algorithmBox.getSelectedItem());
            loadRun();
        });
        sizeBox.addActionListener(e -> {
            PathfindingSession.MapSize size = (PathfindingSession.MapSize) sizeBox.getSelectedItem();
            if (size != session.mapSize()) {
                session.setMapSize(size);
                loadRun();
            }
        });
        mazeButton.addActionListener(e -> {
            session.newMaze();
            loadRun();
        });
        randomButton.addActionListener(e -> {
            session.newRandomMap();
            loadRun();
        });
        clearButton.addActionListener(e -> {
            session.clear();
            loadRun();
        });
    }

    private void installMouse() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Cell cell = gridView.cellAt(e.getX(), e.getY());
                if (cell == null) {
                    return;
                }
                // Grabbing the start or the goal moves it; anywhere else draws
                if (cell.equals(session.run().timeline().state().grid().start())) {
                    drag = Drag.START;
                } else if (cell.equals(session.run().timeline().state().grid().goal())) {
                    drag = Drag.GOAL;
                } else {
                    drag = Drag.PAINT;
                    dragTerrain = SwingUtilities.isRightMouseButton(e) ? Terrain.OPEN : selectedTool();
                    if (session.paint(cell, dragTerrain)) {
                        loadRun();
                    }
                }
                lastCell = cell;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Cell cell = gridView.cellAt(e.getX(), e.getY());
                if (drag == Drag.NONE || cell == null || cell.equals(lastCell)) {
                    return;
                }
                boolean changed = switch (drag) {
                    case START -> session.moveStart(cell);
                    case GOAL -> session.moveGoal(cell);
                    case PAINT -> paintLine(lastCell, cell);
                    case NONE -> false;
                };
                if (changed) {
                    loadRun();
                }
                // An endpoint that could not move (onto the other one) stays where it was
                if (drag == Drag.PAINT || changed) {
                    lastCell = cell;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drag = Drag.NONE;
                lastCell = null;
            }
        };
        gridView.addMouseListener(mouse);
        gridView.addMouseMotionListener(mouse);
    }

    private Terrain selectedTool() {
        if (mudTool.isSelected()) {
            return Terrain.MUD;
        }
        return eraseTool.isSelected() ? Terrain.OPEN : Terrain.WALL;
    }

    /**
     * Paints every cell on the straight line between two cells. A fast mouse jumps
     * several cells between two events; without this the stroke would have holes.
     */
    private boolean paintLine(Cell from, Cell to) {
        int dr = to.row() - from.row();
        int dc = to.col() - from.col();
        int steps = Math.max(Math.abs(dr), Math.abs(dc));
        boolean changed = false;
        for (int k = 1; k <= steps; k++) {
            Cell cell = new Cell(from.row() + Math.round((float) dr * k / steps),
                    from.col() + Math.round((float) dc * k / steps));
            changed |= session.paint(cell, dragTerrain);
        }
        return changed;
    }

    // ---- Updating the screen ----

    /** Shows the recording of the current algorithm and map, paused at its start. */
    private void loadRun() {
        PathfindingAlgorithm algorithm = session.algorithm();
        descriptionLabel.setText(algorithm.description());
        String costs = algorithm.usesCosts() ? "Pays attention to mud" : "Ignores mud";
        String best = !algorithm.findsBestPath() ? "The path is not always the best"
                : algorithm.usesCosts() ? "Always finds the cheapest path" : "Always finds the fewest steps";
        propertiesLabel.setText(costs + "  ·  " + best);
        gridView.setTimeline(session.timeline());
        player.setTimeline(session.timeline());
    }

    /** Called by the player after every change. */
    private void refresh() {
        Timeline<SearchState, SearchStep> timeline = session.timeline();
        SearchState state = timeline.state();
        actionLabel.setText(describe(session.run()));
        stepLabel.setText(String.format("Step %,d / %,d", timeline.position(), timeline.size()));
        exploredLabel.setText(String.format("Explored %,d", state.visited()));
        pathLabel.setText(String.format("Path cells %,d", state.pathCells()));
        playback.refresh();
        gridView.repaint();
    }

    /** One line that says what the current step did, or the result at the end. */
    static String describe(SearchRun run) {
        Timeline<SearchState, SearchStep> timeline = run.timeline();
        if (timeline.atEnd() && timeline.position() > 0) {
            SearchResult result = run.result();
            return result.found()
                    ? "Path found: " + count(result.steps(), "step") + ", cost " + String.format("%,d", result.cost())
                            + ", " + count(result.visited(), "cell") + " explored."
                    : "No path: the goal cannot be reached (" + count(result.visited(), "cell") + " explored).";
        }
        return switch (timeline.current()) {
            case null -> "Ready. Press Play or Space, or draw on the map.";
            case SearchStep.Discover d -> "Added " + d.cell() + " to the frontier";
            case SearchStep.Visit v -> "Exploring " + v.cell();
            case SearchStep.PathCell p -> "Tracing the path: " + p.cell();
        };
    }

    /** "1 cell", "2 cells", "1,024 cells". */
    private static String count(int n, String word) {
        return String.format("%,d ", n) + (n == 1 ? word : word + "s");
    }

    // ---- For tests ----

    PathfindingSession session() {
        return session;
    }

    GridView gridView() {
        return gridView;
    }

    Player player() {
        return player;
    }

    JComboBox<PathfindingAlgorithm> algorithmBox() {
        return algorithmBox;
    }

    JToggleButton mudTool() {
        return mudTool;
    }

    JButton clearButton() {
        return clearButton;
    }

    PlaybackBar playback() {
        return playback;
    }

    String actionText() {
        return actionLabel.getText();
    }

    String stepText() {
        return stepLabel.getText();
    }
}
