package algoscope.ui;

import algoscope.engine.Timeline;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;

/**
 * The playback controls every tab shares: back to the start, one step back,
 * play/pause, one step forward, a progress bar to jump anywhere and the speed.
 * <p>
 * Keyboard: Space plays or pauses, Left/Right move one step, Home goes back to the
 * start. The keys only reach the bar of the tab on screen (Swing ignores bindings of
 * hidden components).
 */
public final class PlaybackBar extends JPanel {

    private final Player player;

    private final JButton resetButton = iconButton(ShapeIcon.Shape.RESET, "Back to the start (Home)");
    private final JButton backButton = iconButton(ShapeIcon.Shape.STEP_BACK, "One step back (Left)");
    private final JButton playButton = iconButton(ShapeIcon.Shape.PLAY, "Play / pause (Space)");
    private final JButton forwardButton = iconButton(ShapeIcon.Shape.STEP_FORWARD, "One step forward (Right)");
    private final JSlider progressSlider = new JSlider(0, 1, 0);
    private final JSlider speedSlider = new JSlider(Speed.MIN_POSITION, Speed.MAX_POSITION, Speed.DEFAULT_POSITION);
    private final JLabel speedLabel = new JLabel();

    // True while refresh() moves the progress slider, so that move is not taken as a seek
    private boolean updatingProgress = false;

    public PlaybackBar(Player player) {
        super(new BorderLayout(8, 0));
        this.player = player;
        setOpaque(false);

        JPanel buttons = Widgets.row();
        buttons.add(resetButton);
        buttons.add(backButton);
        buttons.add(playButton);
        buttons.add(forwardButton);

        JPanel speed = Widgets.row();
        speed.add(Widgets.caption("Speed"));
        speedSlider.setPreferredSize(new Dimension(160, speedSlider.getPreferredSize().height));
        speed.add(speedSlider);
        // Measured with the widest text, so the label neither starts empty nor jumps
        speedLabel.setText(Speed.label(Speed.stepsPerSecond(Speed.MAX_POSITION)));
        speedLabel.setPreferredSize(speedLabel.getPreferredSize());
        speed.add(speedLabel);

        progressSlider.setOpaque(false);
        add(buttons, BorderLayout.WEST);
        add(progressSlider, BorderLayout.CENTER);
        add(speed, BorderLayout.EAST);

        wireEvents();
        installShortcuts();
    }

    private void wireEvents() {
        resetButton.addActionListener(e -> player.reset());
        backButton.addActionListener(e -> player.stepBack());
        playButton.addActionListener(e -> player.togglePlay());
        forwardButton.addActionListener(e -> player.stepForward());
        progressSlider.addChangeListener(e -> {
            if (!updatingProgress) {
                player.seek(progressSlider.getValue());
            }
        });
        speedSlider.addChangeListener(e -> applySpeed());
        applySpeed();
    }

    private void applySpeed() {
        player.setStepsPerSecond(Speed.stepsPerSecond(speedSlider.getValue()));
        speedLabel.setText(Speed.label(player.stepsPerSecond()));
    }

    private void installShortcuts() {
        // The controls do not take the keyboard focus, so these keys always reach the bar
        for (JComponent control : new JComponent[]{resetButton, backButton, playButton, forwardButton,
            progressSlider, speedSlider}) {
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

    /** Updates the buttons and the progress bar to the player's timeline. */
    public void refresh() {
        Timeline<?, ?> timeline = player.timeline();
        boolean any = timeline != null;

        updatingProgress = true;
        progressSlider.setMaximum(any ? Math.max(1, timeline.size()) : 1);
        progressSlider.setValue(any ? timeline.position() : 0);
        updatingProgress = false;

        playButton.setIcon(new ShapeIcon(player.isPlaying() ? ShapeIcon.Shape.PAUSE : ShapeIcon.Shape.PLAY));
        resetButton.setEnabled(any && !timeline.atStart());
        backButton.setEnabled(any && !timeline.atStart());
        forwardButton.setEnabled(any && !timeline.atEnd());
    }

    // ---- For tests ----

    public JButton playButton() {
        return playButton;
    }

    public JButton forwardButton() {
        return forwardButton;
    }

    public JButton backButton() {
        return backButton;
    }

    private static JButton iconButton(ShapeIcon.Shape shape, String tooltip) {
        JButton button = new JButton(new ShapeIcon(shape));
        button.setToolTipText(tooltip);
        return button;
    }
}
