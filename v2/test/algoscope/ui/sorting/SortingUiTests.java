package algoscope.ui.sorting;

import algoscope.Check;
import algoscope.engine.Timeline;
import algoscope.sorting.BubbleSort;
import algoscope.sorting.InputKind;
import algoscope.sorting.QuickSort;
import algoscope.sorting.SortAlgorithms;
import algoscope.sorting.SortRecorder;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;
import algoscope.ui.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/** Tests the Sorting tab without a screen: the session, the bar colors and the panel. */
public final class SortingUiTests {

    private SortingUiTests() {
    }

    public static void run() {
        Check.group("Sorting session", SortingUiTests::session);
        Check.group("Bar colors", SortingUiTests::barColors);
        Check.group("Bars drawn on screen", SortingUiTests::barsPainting);
        Check.group("Step descriptions", SortingUiTests::descriptions);
        Check.group("Sorting panel", SortingUiTests::panel);
    }

    private static void session() {
        SortingSession session = new SortingSession();
        Check.equal("starts with " + SortingSession.DEFAULT_SIZE + " values", SortingSession.DEFAULT_SIZE, session.input().length);
        Check.isTrue("starts at the beginning of the recording", session.timeline().atStart());

        int[] input = session.input();
        session.setAlgorithm(new QuickSort());
        Check.sameArray("changing the algorithm keeps the same input", input, session.input());
        Check.equal("and records the new algorithm", "Quick Sort", session.algorithm().name());

        session.newInput();
        Check.isTrue("New input gives another random array", !Arrays.equals(input, session.input()));

        session.setInputKind(InputKind.REVERSED);
        Check.sameArray("changing the kind creates that kind of input",
                InputKind.REVERSED.generate(SortingSession.DEFAULT_SIZE, 1), session.input());

        session.setSize(20);
        Check.equal("changing the size creates an input of that size", 20, session.input().length);
        session.setSize(1);
        Check.equal("too small sizes are clamped", SortingSession.MIN_SIZE, session.size());
        session.setSize(1_000_000);
        Check.equal("too big sizes are clamped", SortingSession.MAX_SIZE, session.size());

        int[] before = session.input();
        session.timeline().seek(session.timeline().size());
        Check.sameArray("playing the recording does not change the input", before, session.input());
    }

    private static void barColors() {
        SortStep compare = new SortStep.Compare(2, 5);
        Check.equal("compared bars are amber", Theme.COMPARE, BarsView.colorFor(5, compare, false));
        Check.equal("other bars stay grey", Theme.BAR, BarsView.colorFor(3, compare, false));
        Check.equal("a pivot comparison colors only its bar", Theme.BAR,
                BarsView.colorFor(0, new SortStep.Compare(4, -1), false));
        Check.equal("swapped bars are red", Theme.SWAP, BarsView.colorFor(1, new SortStep.Swap(1, 4), false));
        Check.equal("written bars are blue", Theme.WRITE, BarsView.colorFor(7, new SortStep.Write(7, 3, 9), false));
        Check.equal("before the first step all bars are grey", Theme.BAR, BarsView.colorFor(0, null, false));
        Check.equal("when finished all bars are green", Theme.DONE, BarsView.colorFor(0, compare, true));
    }

    private static void barsPainting() {
        // 5 bars in a 200 x 100 view: no screen needed, it paints into an image
        Timeline<SortState, SortStep> timeline = SortRecorder.record(new BubbleSort(), new int[]{5, 4, 3, 2, 1});
        BarsView view = new BarsView();
        view.setSize(200, 100);
        view.setTimeline(timeline);

        Check.isTrue("at the start every bar is grey", barsHaveColors(view, Theme.BAR, Theme.BAR, Theme.BAR));
        timeline.stepForward(); // Bubble Sort first compares #0 and #1
        Check.isTrue("the first comparison paints bars 0 and 1 amber", barsHaveColors(view, Theme.COMPARE, Theme.COMPARE, Theme.BAR));
        timeline.stepForward(); // ...then swaps them
        Check.isTrue("the swap paints them red", barsHaveColors(view, Theme.SWAP, Theme.SWAP, Theme.BAR));
        timeline.seek(timeline.size());
        Check.isTrue("at the end every bar is green", barsHaveColors(view, Theme.DONE, Theme.DONE, Theme.DONE));
        Check.isTrue("the tallest bar is drawn taller than the shortest",
                barTop(view, 4) < barTop(view, 0));
    }

    /** Checks the color at the bottom of bars 0, 1 and 2. */
    private static boolean barsHaveColors(BarsView view, Color bar0, Color bar1, Color bar2) {
        BufferedImage image = paint(view);
        return colorAt(image, 0).equals(bar0) && colorAt(image, 1).equals(bar1) && colorAt(image, 2).equals(bar2);
    }

    private static BufferedImage paint(BarsView view) {
        BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    /** The middle of bar i, just above the bottom padding (12 px) of a 5-bar, 200 px wide view. */
    private static Color colorAt(BufferedImage image, int bar) {
        int x = 12 + (2 * bar + 1) * (image.getWidth() - 24) / 10;
        return new Color(image.getRGB(x, image.getHeight() - 13));
    }

    /** The highest painted row of bar i (smaller = taller bar). */
    private static int barTop(BarsView view, int bar) {
        BufferedImage image = paint(view);
        int x = 12 + (2 * bar + 1) * (image.getWidth() - 24) / 10;
        for (int y = 0; y < image.getHeight(); y++) {
            if (!new Color(image.getRGB(x, y)).equals(Theme.CANVAS)) {
                return y;
            }
        }
        return image.getHeight();
    }

    private static void descriptions() {
        Timeline<SortState, SortStep> timeline = SortRecorder.record(new BubbleSort(), new int[]{3, 1, 2});
        Check.equal("before the first step", "Ready. Press Play or Space.", SortingPanel.describe(timeline));
        timeline.stepForward();
        Check.equal("a comparison shows positions and values", "Comparing #0 (3) and #1 (1)", SortingPanel.describe(timeline));
        timeline.stepForward();
        Check.equal("a swap", "Swapped #0 and #1", SortingPanel.describe(timeline));
        timeline.seek(timeline.size());
        Check.equal("the end", "Sorted.", SortingPanel.describe(timeline));

        Timeline<SortState, SortStep> quick = SortRecorder.record(new QuickSort(), new int[]{3, 1, 2});
        quick.stepForward();
        Check.isTrue("Quick Sort comparisons mention the pivot", SortingPanel.describe(quick).endsWith("with the pivot"));

        Timeline<SortState, SortStep> insertion = SortRecorder.record(SortAlgorithms.byName("Insertion Sort"), new int[]{2, 1});
        insertion.seek(2);
        Check.equal("a write shows the value and the position", "Wrote 2 at #1", SortingPanel.describe(insertion));
    }

    private static void panel() {
        SortingPanel panel = new SortingPanel();
        Check.isTrue("a new panel is ready at step 0", panel.stepText().startsWith("Step 0 /")
                && panel.actionText().startsWith("Ready"));
        Check.isTrue("back is disabled at the start", !panel.backButton().isEnabled());

        panel.forwardButton().doClick(0);
        Check.isTrue("the step button moves one step", panel.stepText().startsWith("Step 1 /")
                && panel.actionText().startsWith("Comparing"));
        Check.isTrue("back is enabled after a step", panel.backButton().isEnabled());
        panel.backButton().doClick(0);
        Check.isTrue("the back button returns to step 0", panel.stepText().startsWith("Step 0 /"));

        panel.playButton().doClick(0);
        Check.isTrue("the play button starts playing", panel.player().isPlaying());
        panel.playButton().doClick(0);
        Check.isTrue("pressing it again pauses", !panel.player().isPlaying());

        panel.forwardButton().doClick(0);
        panel.algorithmBox().setSelectedIndex(4);
        Check.isTrue("choosing another algorithm starts its recording from step 0",
                panel.stepText().startsWith("Step 0 /") && panel.player().timeline().atStart());
    }
}
