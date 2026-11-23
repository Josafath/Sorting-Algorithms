package algoscope.ui.pathfinding;

import algoscope.engine.Timeline;
import algoscope.pathfinding.Cell;
import algoscope.pathfinding.CellStatus;
import algoscope.pathfinding.Grid;
import algoscope.pathfinding.SearchState;
import algoscope.pathfinding.SearchStep;
import algoscope.pathfinding.Terrain;
import algoscope.ui.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * Draws the map: every cell colored by its terrain and by what the search knows
 * about it (see {@link #colorFor}), the start and the goal, and an outline around the
 * cell of the current step. It also turns mouse positions into cells for editing.
 */
public final class GridView extends JComponent {

    private static final int PADDING = 8;

    private Timeline<SearchState, SearchStep> timeline;

    public GridView() {
        setPreferredSize(new Dimension(900, 520));
        setOpaque(true);
    }

    public void setTimeline(Timeline<SearchState, SearchStep> timeline) {
        this.timeline = timeline;
        repaint();
    }

    // ---- Geometry: cells are squares, as big as fit, centered ----

    private int cellSize(Grid grid) {
        int byWidth = (getWidth() - 2 * PADDING) / grid.cols();
        int byHeight = (getHeight() - 2 * PADDING) / grid.rows();
        return Math.max(1, Math.min(byWidth, byHeight));
    }

    private int originX(Grid grid) {
        return (getWidth() - cellSize(grid) * grid.cols()) / 2;
    }

    private int originY(Grid grid) {
        return (getHeight() - cellSize(grid) * grid.rows()) / 2;
    }

    /** The cell under a point of this component, or null if the point is outside the map. */
    public Cell cellAt(int x, int y) {
        if (timeline == null) {
            return null;
        }
        Grid grid = timeline.state().grid();
        int size = cellSize(grid);
        int dx = x - originX(grid);
        int dy = y - originY(grid);
        if (dx < 0 || dy < 0) {
            return null;
        }
        Cell cell = new Cell(dy / size, dx / size);
        return grid.inside(cell) ? cell : null;
    }

    // ---- Painting ----

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Theme.CANVAS);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (timeline == null) {
            return;
        }
        SearchState state = timeline.state();
        Grid grid = state.grid();
        int size = cellSize(grid);
        int x0 = originX(grid);
        int y0 = originY(grid);
        // A thin line between cells when they are big enough to keep it
        boolean lines = size >= 8;
        if (lines) {
            g.setColor(Theme.GRID_LINE);
            g.fillRect(x0, y0, size * grid.cols(), size * grid.rows());
        }

        for (int r = 0; r < grid.rows(); r++) {
            for (int c = 0; c < grid.cols(); c++) {
                Cell cell = new Cell(r, c);
                g.setColor(colorFor(grid.terrain(cell), state.status(cell)));
                int inner = lines ? size - 1 : size;
                g.fillRect(x0 + c * size, y0 + r * size, inner, inner);
            }
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawEndpoint(g, grid.start(), "S", Theme.START, size, x0, y0);
        drawEndpoint(g, grid.goal(), "G", Theme.GOAL, size, x0, y0);

        // An outline around the cell the current step touched, so it can be followed
        SearchStep step = timeline.current();
        if (step != null && !timeline.atEnd()) {
            Cell cell = step.cell();
            g.setColor(Theme.TEXT);
            g.setStroke(new BasicStroke(Math.max(1.5f, size / 8f)));
            g.drawRect(x0 + cell.col() * size, y0 + cell.row() * size, size - 1, size - 1);
        }
    }

    private void drawEndpoint(Graphics2D g, Cell cell, String letter, Color color, int size, int x0, int y0) {
        int x = x0 + cell.col() * size;
        int y = y0 + cell.row() * size;
        g.setColor(color);
        g.fillRect(x, y, size - 1, size - 1);
        // The letter only when the cell is big enough to read it
        if (size >= 12) {
            g.setColor(Color.WHITE);
            g.setFont(Theme.font(Font.BOLD, size * 0.6f));
            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (size - metrics.stringWidth(letter)) / 2;
            int textY = y + (size - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(letter, textX, textY);
        }
    }

    /**
     * The color of a cell. Walls are always dark; unseen cells show their terrain;
     * otherwise the color of the status, mixed with brown on mud so the mud stays visible.
     */
    static Color colorFor(Terrain terrain, CellStatus status) {
        if (terrain == Terrain.WALL) {
            return Theme.CELL_WALL;
        }
        Color statusColor = switch (status) {
            case UNSEEN -> null;
            case FRONTIER -> Theme.FRONTIER;
            case VISITED -> Theme.VISITED;
            case PATH -> Theme.PATH;
        };
        if (statusColor == null) {
            return terrain == Terrain.MUD ? Theme.CELL_MUD : Theme.CELL_OPEN;
        }
        return terrain == Terrain.MUD ? mix(statusColor, Theme.CELL_MUD, 0.45) : statusColor;
    }

    /** {@code a} with a fraction {@code amountOfB} of {@code b} mixed in. */
    static Color mix(Color a, Color b, double amountOfB) {
        double k = 1 - amountOfB;
        return new Color(
                (int) Math.round(a.getRed() * k + b.getRed() * amountOfB),
                (int) Math.round(a.getGreen() * k + b.getGreen() * amountOfB),
                (int) Math.round(a.getBlue() * k + b.getBlue() * amountOfB));
    }
}
