package algoscope.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Small player icons (play, pause, step...) drawn with shapes.
 * Symbols like ▶ or ⏸ are missing from some fonts, so drawing them is safer.
 */
public final class ShapeIcon implements Icon {

    public enum Shape { PLAY, PAUSE, STEP_FORWARD, STEP_BACK, RESET }

    private static final int SIZE = 12;

    private final Shape shape;

    public ShapeIcon(Shape shape) {
        this.shape = shape;
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Grey when the button is disabled, like its text
        g.setColor(c != null && !c.isEnabled() ? Theme.BAR : Theme.TEXT);
        int s = SIZE;
        switch (shape) {
            case PLAY -> g.fill(triangleRight(x + 1, y, s - 2, s));
            case PAUSE -> {
                g.fillRect(x + 1, y, 4, s);
                g.fillRect(x + s - 5, y, 4, s);
            }
            // |> with the bar on the right: one step forward
            case STEP_FORWARD -> {
                g.fill(triangleRight(x, y, s - 4, s));
                g.fillRect(x + s - 3, y, 3, s);
            }
            // <| with the bar on the left: one step back
            case STEP_BACK -> {
                g.fillRect(x, y, 3, s);
                g.fill(triangleLeft(x + 4, y, s - 4, s));
            }
            // |<< back to the start
            case RESET -> {
                g.fillRect(x, y, 2, s);
                g.fill(triangleLeft(x + 2, y, s / 2 - 1, s));
                g.fill(triangleLeft(x + s / 2 + 1, y, s / 2 - 1, s));
            }
        }
        g.dispose();
    }

    private static Polygon triangleRight(int x, int y, int width, int height) {
        return new Polygon(new int[]{x, x, x + width}, new int[]{y, y + height, y + height / 2}, 3);
    }

    private static Polygon triangleLeft(int x, int y, int width, int height) {
        return new Polygon(new int[]{x + width, x + width, x}, new int[]{y, y + height, y + height / 2}, 3);
    }
}
