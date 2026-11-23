package algoscope.ui.sorting;

import algoscope.engine.Timeline;
import algoscope.sorting.InputKind;
import algoscope.sorting.SortAlgorithm;
import algoscope.sorting.SortAlgorithms;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;
import algoscope.ui.Player;
import algoscope.ui.ShapeIcon;
import algoscope.ui.Speed;
import algoscope.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

/**
 * The Sorting tab: choose an algorithm and an input on top, watch the bars in the
 * middle, control the playback and read the counters at the bottom.
 * <p>
 * Keyboard: Space plays or pauses, Left/Right move one step, Home goes back to the start.
 */
public final class SortingPanel extends JPanel {

    private final SortingSession session = new SortingSession();
    // Its timer only runs while playing, so building the panel starts nothing
    private final Player player = new Player();
    private final BarsView bars = new BarsView();

    // Top: what to sort
    private final JComboBox<SortAlgorithm> algorithmBox = new JComboBox<>(SortAlgorithms.ALL.toArray(new SortAlgorithm[0]));
    private final JComboBox<InputKind> inputBox = new JComboBox<>(InputKind.values());
    private final JSlider sizeSlider = new JSlider(SortingSession.MIN_SIZE, SortingSession.MAX_SIZE, SortingSession.DEFAULT_SIZE);
    private final JLabel sizeLabel = new JLabel();
    private final JButton newInputButton = new JButton("New input");
    private final JLabel descriptionLabel = new JLabel();
    private final JLabel complexityLabel = new JLabel();

    // Bottom: playback
    private final JButton resetButton = iconButton(ShapeIcon.Shape.RESET, "Back to the start (Home)");
    private final JButton backButton = iconButton(ShapeIcon.Shape.STEP_BACK, "One step back (Left)");
    private final JButton playButton = iconButton(ShapeIcon.Shape.PLAY, "Play / pause (Space)");
    private final JButton forwardButton = iconButton(ShapeIcon.Shape.STEP_FORWARD, "One step forward (Right)");
    private final JSlider progressSlider = new JSlider(0, 1, 0);
    private final JSlider speedSlider = new JSlider(Speed.MIN_POSITION, Speed.MAX_POSITION, Speed.DEFAULT_POSITION);
    private final JLabel speedLabel = new JLabel();

    // Bottom: what is happening
    private final JLabel actionLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();
    private final JLabel comparisonsLabel = new JLabel();
    private final JLabel swapsLabel = new JLabel();
    private final JLabel writesLabel = new JLabel();

    // True while refresh() moves the progress slider, so that move is not taken as a seek
    private boolean updatingProgress = false;

    public SortingPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTop(), BorderLayout.NORTH);
        bars.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        add(bars, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        wireEvents();
        installShortcuts();
        player.addListener(this::refresh);
        player.setStepsPerSecond(Speed.stepsPerSecond(speedSlider.getValue()));
        loadTimeline();
    }

    // ---- For tests: press the buttons and read what the user would see ----

    JButton playButton() {
        return playButton;
    }

    JButton forwardButton() {
        return forwardButton;
    }

    JButton backButton() {
        return backButton;
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

    // ---- Layout ----

    private JComponent buildTop() {
        algorithmBox.setRenderer(textRenderer(SortAlgorithm::name));
        inputBox.setRenderer(textRenderer(InputKind::label));
        sizeSlider.setPreferredSize(new java.awt.Dimension(220, sizeSlider.getPreferredSize().height));
        sizeLabel.setText(sizeText(sizeSlider.getValue()));

        JPanel choices = row();
        choices.add(caption("Algorithm"));
        choices.add(algorithmBox);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(caption("Input"));
        choices.add(inputBox);
        choices.add(Box.createHorizontalStrut(12));
        choices.add(caption("Size"));
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
        JPanel buttons = row();
        buttons.add(resetButton);
        buttons.add(backButton);
        buttons.add(playButton);
        buttons.add(forwardButton);

        JPanel speed = row();
        speed.add(caption("Speed"));
        speedSlider.setPreferredSize(new java.awt.Dimension(160, speedSlider.getPreferredSize().height));
        speed.add(speedSlider);
        // Measured with the widest text, so the label neither starts empty nor jumps while the speed changes
        speedLabel.setText(Speed.label(Speed.stepsPerSecond(Speed.MAX_POSITION)));
        speedLabel.setPreferredSize(speedLabel.getPreferredSize());
        speed.add(speedLabel);

        progressSlider.setOpaque(false);
        JPanel playback = new JPanel(new BorderLayout(8, 0));
        playback.setOpaque(false);
        playback.add(buttons, BorderLayout.WEST);
        playback.add(progressSlider, BorderLayout.CENTER);
        playback.add(speed, BorderLayout.EAST);

        actionLabel.setFont(Theme.font(Font.BOLD, 14f));
        actionLabel.setForeground(Theme.TEXT);
        JPanel counters = row();
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

        JPanel legend = row();
        legend.add(legendItem(Theme.COMPARE, "Compare"));
        legend.add(legendItem(Theme.SWAP, "Swap"));
        legend.add(legendItem(Theme.WRITE, "Write"));
        legend.add(legendItem(Theme.DONE, "Sorted"));

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
            sizeLabel.setText(sizeText(sizeSlider.getValue()));
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

        resetButton.addActionListener(e -> player.reset());
        backButton.addActionListener(e -> player.stepBack());
        playButton.addActionListener(e -> player.togglePlay());
        forwardButton.addActionListener(e -> player.stepForward());
        progressSlider.addChangeListener(e -> {
            if (!updatingProgress) {
                player.seek(progressSlider.getValue());
            }
        });
        speedSlider.addChangeListener(e -> {
            player.setStepsPerSecond(Speed.stepsPerSecond(speedSlider.getValue()));
            speedLabel.setText(Speed.label(player.stepsPerSecond()));
        });
        speedLabel.setText(Speed.label(Speed.stepsPerSecond(speedSlider.getValue())));
    }

    private void installShortcuts() {
        // The controls do not take the keyboard focus, so these keys always reach the panel
        for (JComponent control : new JComponent[]{algorithmBox, inputBox, sizeSlider, newInputButton,
            resetButton, backButton, playButton, forwardButton, progressSlider, speedSlider}) {
            control.setFocusable(false);
        }
        InputMap keys = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        bindKey(keys, "SPACE", "togglePlay", player::togglePlay);
        bindKey(keys, "RIGHT", "stepForward", player::stepForward);
        bindKey(keys, "LEFT", "stepBack", player::stepBack);
        bindKey(keys, "HOME", "reset", player::reset);
    }

    private void bindKey(InputMap keys, String key, String name, Runnable action) {
        keys.put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
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

        updatingProgress = true;
        progressSlider.setMaximum(Math.max(1, timeline.size()));
        progressSlider.setValue(timeline.position());
        updatingProgress = false;

        playButton.setIcon(new ShapeIcon(player.isPlaying() ? ShapeIcon.Shape.PAUSE : ShapeIcon.Shape.PLAY));
        resetButton.setEnabled(!timeline.atStart());
        backButton.setEnabled(!timeline.atStart());
        forwardButton.setEnabled(!timeline.atEnd());
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

    // ---- Small helpers ----

    private static JButton iconButton(ShapeIcon.Shape shape, String tooltip) {
        JButton button = new JButton(new ShapeIcon(shape));
        button.setToolTipText(tooltip);
        return button;
    }

    private static JPanel row() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private static String sizeText(int size) {
        return String.format("%,d", size);
    }

    /** A colored square and its meaning. */
    private static JLabel legendItem(Color color, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED_TEXT);
        label.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, 10, 10);
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        });
        return label;
    }

    /** Shows an item of a combo box as text, without needing a toString(). */
    private static <T> ListCellRenderer<Object> textRenderer(Function<T, String> text) {
        return new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value != null) {
                    setText(text.apply((T) value));
                }
                return this;
            }
        };
    }
}
