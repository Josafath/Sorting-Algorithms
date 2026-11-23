package algoscope.ui;

import algoscope.engine.Timeline;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

/**
 * Plays a {@link Timeline} like a video: play, pause, one step at a time, jump.
 * <p>
 * It works with any timeline (sorting now, pathfinding later). A Swing timer ticks
 * about 60 times per second on the Swing thread and applies as many steps as the
 * chosen speed asks for; after each change the listeners redraw the screen.
 */
public final class Player {

    /** About 60 frames per second. */
    static final int FRAME_MILLIS = 16;

    // null in tests, which call tick() by hand to control time
    private final Timer timer;
    private final StepClock clock = new StepClock();
    private final List<Runnable> listeners = new ArrayList<>();

    private Timeline<?, ?> timeline;
    private double stepsPerSecond = Speed.stepsPerSecond(Speed.DEFAULT_POSITION);
    private boolean playing = false;
    private long lastTick;

    public Player() {
        this(true);
    }

    /** @param useTimer false for tests: nothing moves unless {@link #tick(long)} is called */
    Player(boolean useTimer) {
        if (useTimer) {
            timer = new Timer(FRAME_MILLIS, event -> {
                long now = System.nanoTime();
                tick(now - lastTick);
                lastTick = now;
            });
        } else {
            timer = null;
        }
    }

    /** Called after every change, to redraw the screen. */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /** Shows another recording, paused at its current position. */
    public void setTimeline(Timeline<?, ?> timeline) {
        pauseQuietly();
        this.timeline = timeline;
        notifyListeners();
    }

    public Timeline<?, ?> timeline() {
        return timeline;
    }

    public boolean isPlaying() {
        return playing;
    }

    public double stepsPerSecond() {
        return stepsPerSecond;
    }

    public void setStepsPerSecond(double stepsPerSecond) {
        if (stepsPerSecond <= 0) {
            throw new IllegalArgumentException("Speed must be positive: " + stepsPerSecond);
        }
        this.stepsPerSecond = stepsPerSecond;
    }

    /** Starts playing. At the end it starts over, like a video player. */
    public void play() {
        if (timeline == null || timeline.size() == 0) {
            return;
        }
        if (timeline.atEnd()) {
            timeline.seek(0);
        }
        playing = true;
        clock.reset();
        lastTick = System.nanoTime();
        if (timer != null) {
            timer.start();
        }
        notifyListeners();
    }

    public void pause() {
        pauseQuietly();
        notifyListeners();
    }

    public void togglePlay() {
        if (playing) {
            pause();
        } else {
            play();
        }
    }

    /** Pauses and applies exactly one step. */
    public void stepForward() {
        if (timeline == null) {
            return;
        }
        pauseQuietly();
        timeline.stepForward();
        notifyListeners();
    }

    /** Pauses and takes back exactly one step. */
    public void stepBack() {
        if (timeline == null) {
            return;
        }
        pauseQuietly();
        timeline.stepBack();
        notifyListeners();
    }

    /** Pauses and jumps to a position, for example from the progress bar. */
    public void seek(int position) {
        if (timeline == null) {
            return;
        }
        pauseQuietly();
        timeline.seek(position);
        notifyListeners();
    }

    /** Pauses and goes back to the first step. */
    public void reset() {
        seek(0);
    }

    /**
     * One frame of the animation: applies the steps that fit in the elapsed time.
     * The Swing timer calls it; tests call it directly.
     */
    void tick(long elapsedNanos) {
        if (!playing || timeline == null) {
            return;
        }
        int steps = clock.stepsFor(elapsedNanos, stepsPerSecond);
        for (int i = 0; i < steps && timeline.stepForward(); i++) {
            // stepForward() does the work; the loop stops early at the end
        }
        if (timeline.atEnd()) {
            // Finished: stop the timer so it does not keep waking up for nothing
            pauseQuietly();
        }
        notifyListeners();
    }

    private void pauseQuietly() {
        playing = false;
        if (timer != null) {
            timer.stop();
        }
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
