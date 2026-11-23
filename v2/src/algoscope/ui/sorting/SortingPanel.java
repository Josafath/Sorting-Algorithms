package algoscope.ui.sorting;

import algoscope.engine.Timeline;
import algoscope.sorting.InputKind;
import algoscope.sorting.SortAlgorithm;
import algoscope.sorting.SortAlgorithms;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;
import algoscope.ui.PlaybackBar;
import algoscope.ui.Player;
import algoscope.ui.Theme;
import algoscope.ui.Widgets;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * The Sorting tab: choose an algorithm and an input on top, watch the bars in the
 * middle, control the playback and read the counters at the bottom.
 */
public final class SortingPanel extends JPanel {

    private final SortingSession session = new SortingSession();
    // Its timer only runs while playing, so building the panel starts nothing
    private final Player player = new Player();
    private final BarsView bars = new BarsView();
    private final PlaybackBar playback = new PlaybackBar(player);

    // Top: what to sort
    private final JComboBox<SortAlgorithm> algorithmBox = new JComboBox<>(SortAlgorithms.ALL.toArray(new SortAlgorithm[0]));
    private final JComboBox<InputKind> inputBox = new JComboBox<>(InputKind.values());
    private final JSlider sizeSlider = new JSlider(SortingSession.MIN_SIZE, SortingSession.MAX_SIZE, SortingSession.DEFAULT_SIZE);
    private final JLabel sizeLabel = new JLabel();
    private final JButton newInputButton = new JButton("New input");
    private final JLabel descriptionLabel = new JLabel();
    private final JLabel complexityLabel = new JLabel();

    // Bottom: what is happening
    private final JLabel actionLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();
    private final JLabel comparisonsLabel = new JLabel();
    private final JLabel swapsLabel = new JLabel();
    private final JLabel writesLabel = new JLabel();

    public SortingPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTop(), BorderLayout.NORTH);
        bars.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(bars, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        wireEvents();
        player.addListener(this::refresh);
        loadTimeline();
    }

    /** Stops the animation, for example when the user switches to another tab. */
    public void pause() {
        player.pause();
    }

    // ---- Layout ----

    private JComponent buildTop() {
        algorithmBox.setRenderer(Widgets.textRenderer(SortAlgorithm::name));
        inputBox.setRenderer(Widgets.textRenderer(InputKind::label));
        sizeSlider.setPreferredSize(new Dimension(220, sizeSlider.getPreferredSize().height));
        sizeLabel.setText(String.format("%,d", sizeSlider.getValue()));
        // Like the playback controls, these never take the focus, so the shortcuts always work
        for (JComponent control : new JComponent[]{algorithmBox, inputBox, sizeSlider, newInputButton}) {
            control.setFocusable(false);
        }

        JPanel choices = Widgets.row();
        choices.add(Widgets.caption("Algorithm"));
        choices.add(algorithmBox);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(Widgets.caption("Input"));
        choices.add(inputBox);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(Widgets.caption("Size"));
        choices.add(sizeSlider);
        choices.add(sizeLabel);
        choices.add(newInputButton);

        descriptionLabel.setForeground(Theme.TEXT);
        complexityLabel.setForeground(Theme.MUTED_TEXT);
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(4, 4, 10, 4));
        info.add(descriptionLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(complexityLabel);

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
        for (JLabel label : new JLabel[]{stepLabel, comparisonsLabel, swapsLabel, writesLabel}) {
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
        legend.add(Widgets.legendItem(Theme.COMPARE, "Compare"));
        legend.add(Widgets.legendItem(Theme.SWAP, "Swap"));
        legend.add(Widgets.legendItem(Theme.WRITE, "Write"));
        legend.add(Widgets.legendItem(Theme.DONE, "Sorted"));

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
            session.setAlgorithm((SortAlgorithm) algorithmBox.getSelectedItem());
            loadTimeline();
        });
        inputBox.addActionListener(e -> {
            session.setInputKind((InputKind) inputBox.getSelectedItem());
            loadTimeline();
        });
        sizeSlider.addChangeListener(e -> {
            sizeLabel.setText(String.format("%,d", sizeSlider.getValue()));
            // Record only when the user lets go: recording 1,000 bars on every pixel would lag
            if (!sizeSlider.getValueIsAdjusting() && sizeSlider.getValue() != session.size()) {
                session.setSize(sizeSlider.getValue());
                loadTimeline();
            }
        });
        newInputButton.addActionListener(e -> {
            session.newInput();
            loadTimeline();
        });
    }

    // ---- Updating the screen ----

    /** Shows the recording of the current choices, paused at its start. */
    private void loadTimeline() {
        SortAlgorithm algorithm = session.algorithm();
        descriptionLabel.setText(algorithm.description());
        SortAlgorithm.Complexity c = algorithm.complexity();
        complexityLabel.setText("Best " + c.best() + "  ·  Average " + c.average() + "  ·  Worst " + c.worst()
                + "  ·  " + (algorithm.stable() ? "Stable" : "Not stable"));
        bars.setTimeline(session.timeline());
        player.setTimeline(session.timeline());
    }

    /** Called by the player after every change. */
    private void refresh() {
        Timeline<SortState, SortStep> timeline = session.timeline();
        SortState state = timeline.state();

        actionLabel.setText(describe(timeline));
        stepLabel.setText(String.format("Step %,d / %,d", timeline.position(), timeline.size()));
        comparisonsLabel.setText(String.format("Comparisons %,d", state.comparisons()));
        swapsLabel.setText(String.format("Swaps %,d", state.swaps()));
        writesLabel.setText(String.format("Writes %,d", state.writes()));
        playback.refresh();
        bars.repaint();
    }

    /** One line that says what the current step did, in words. */
    static String describe(Timeline<SortState, SortStep> timeline) {
        if (timeline.atEnd() && timeline.position() > 0) {
            return "Sorted.";
        }
        SortState state = timeline.state();
        return switch (timeline.current()) {
            case null -> "Ready. Press Play or Space.";
            case SortStep.Compare c when c.j() == -1 ->
                    "Comparing #" + c.i() + " (" + state.value(c.i()) + ") with the pivot";
            case SortStep.Compare c ->
                    "Comparing #" + c.i() + " (" + state.value(c.i()) + ") and #" + c.j() + " (" + state.value(c.j()) + ")";
            case SortStep.Swap s -> "Swapped #" + s.i() + " and #" + s.j();
            case SortStep.Write w -> "Wrote " + w.newValue() + " at #" + w.index();
        };
    }

    // ---- For tests: press the buttons and read what the user would see ----

    JButton playButton() {
        return playback.playButton();
    }

    JButton forwardButton() {
        return playback.forwardButton();
    }

    JButton backButton() {
        return playback.backButton();
    }

    JComboBox<SortAlgorithm> algorithmBox() {
        return algorithmBox;
    }

    String actionText() {
        return actionLabel.getText();
    }

    String stepText() {
        return stepLabel.getText();
    }

    Player player() {
        return player;
    }
}
