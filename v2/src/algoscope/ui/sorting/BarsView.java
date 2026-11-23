package algoscope.ui.sorting;

import algoscope.engine.Timeline;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;
import algoscope.ui.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * Draws the array as vertical bars, one per value, with the bars touched by the
 * current step in color (see {@link #colorFor}).
 */
public final class BarsView extends JComponent {

    private static final int PADDING = 12;

    private Timeline<SortState, SortStep> timeline;

    public BarsView() {
        setPreferredSize(new Dimension(900, 420));
        setOpaque(true);
    }

    public void setTimeline(Timeline<SortState, SortStep> timeline) {
        this.timeline = timeline;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Theme.CANVAS);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (timeline == null || timeline.state().length() == 0) {
            return;
        }
        SortState state = timeline.state();
        int n = state.length();
        int max = 1;
        for (int i = 0; i < n; i++) {
            max = Math.max(max, state.value(i));
        }

        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        int bottom = PADDING + height;
        // Finished: everything green (position > 0 so a fresh recording is not green)
        boolean done = timeline.atEnd() && timeline.position() > 0;
        SortStep step = timeline.current();
        // A 1 px gap between bars, only when they are wide enough to keep it readable
        boolean gap = width / n >= 4;

        for (int i = 0; i < n; i++) {
            // Integer edges computed from i and i + 1, so the bars fill the width exactly
            int left = PADDING + (int) ((long) i * width / n);
            int right = PADDING + (int) ((long) (i + 1) * width / n);
            int barWidth = Math.max(1, right - left - (gap ? 1 : 0));
            int barHeight = Math.max(1, (int) ((long) state.value(i) * height / max));
            g.setColor(colorFor(i, step, done));
            g.fillRect(left, bottom - barHeight, barWidth, barHeight);
        }
    }

    /**
     * The color of bar {@code index}: green when finished, otherwise the color of the
     * current step if the step touches that bar, otherwise the normal bar color.
     */
    static Color colorFor(int index, SortStep step, boolean done) {
        if (done) {
            return Theme.DONE;
        }
        return switch (step) {
            case null -> Theme.BAR;
            case SortStep.Compare c -> index == c.i() || index == c.j() ? Theme.COMPARE : Theme.BAR;
            case SortStep.Swap s -> index == s.i() || index == s.j() ? Theme.SWAP : Theme.BAR;
            case SortStep.Write w -> index == w.index() ? Theme.WRITE : Theme.BAR;
        };
    }
}
