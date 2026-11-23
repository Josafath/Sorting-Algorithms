package algoscope.ui;

import algoscope.Check;
import algoscope.engine.Timeline;
import algoscope.sorting.BubbleSort;
import algoscope.sorting.SortRecorder;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;
import java.util.List;

/** Tests the speed scale, the step clock and the player, with time controlled by hand. */
public final class PlaybackTests {

    private static final long MS = 1_000_000L;

    private PlaybackTests() {
    }

    public static void run() {
        Check.group("Speed", PlaybackTests::speedScale);
        Check.group("Step clock", PlaybackTests::stepClock);
        Check.group("Player", PlaybackTests::player);
    }

    private static void speedScale() {
        Check.equal("the slowest speed is 1 step per second", 1.0, Speed.stepsPerSecond(Speed.MIN_POSITION));
        double fastest = Speed.stepsPerSecond(Speed.MAX_POSITION);
        Check.isTrue("the fastest speed is about 31,623 steps per second", Math.abs(fastest - 31_622.8) < 1);
        boolean increasing = true;
        for (int p = Speed.MIN_POSITION; p < Speed.MAX_POSITION; p++) {
            increasing &= Speed.stepsPerSecond(p + 1) > Speed.stepsPerSecond(p);
        }
        Check.isTrue("every slider position is faster than the previous one", increasing);
        Check.equal("positions below the slider are clamped", 1.0, Speed.stepsPerSecond(-20));
        Check.equal("positions above the slider are clamped", fastest, Speed.stepsPerSecond(500));
        Check.equal("the label rounds to whole steps", "178 steps/s", Speed.label(177.8));
    }

    private static void stepClock() {
        StepClock clock = new StepClock();
        Check.equal("100 ms at 10 steps/s is 1 step", 1, clock.stepsFor(100 * MS, 10));
        Check.equal("50 ms at 10 steps/s is half a step: 0 for now", 0, clock.stepsFor(50 * MS, 10));
        Check.equal("the half is carried: another 50 ms gives 1", 1, clock.stepsFor(50 * MS, 10));
        Check.equal("60 frames of 1/60 s at 60 steps/s give 60 steps", 60, frames(new StepClock(), 60, 60));
        Check.equal("a 10 s pause counts as 250 ms at most", 2, new StepClock().stepsFor(10_000 * MS, 10));
        Check.equal("negative time gives no steps", 0, new StepClock().stepsFor(-5 * MS, 1000));
        clock.stepsFor(50 * MS, 10);
        clock.reset();
        Check.equal("reset forgets the carried half", 0, clock.stepsFor(50 * MS, 10));
    }

    private static int frames(StepClock clock, int count, double stepsPerSecond) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            // 1/60 s rounded up to whole nanoseconds, so 60 frames add up to at least 1 s
            total += clock.stepsFor(16_666_667L, stepsPerSecond);
        }
        return total;
    }

    private static void player() {
        Timeline<SortState, SortStep> timeline = SortRecorder.record(new BubbleSort(), new int[]{5, 4, 3, 2, 1});
        int total = timeline.size();
        Player player = new Player(false);
        int[] updates = {0};
        player.addListener(() -> updates[0]++);
        player.setTimeline(timeline);
        Check.isTrue("setting a timeline notifies the screen", updates[0] == 1);

        player.tick(1000 * MS);
        Check.equal("nothing moves while paused", 0, timeline.position());

        player.setStepsPerSecond(10);
        player.play();
        Check.isTrue("play starts playing", player.isPlaying());
        player.tick(100 * MS);
        Check.equal("one frame of 100 ms at 10 steps/s applies 1 step", 1, timeline.position());
        player.tick(250 * MS);
        Check.equal("a frame of 250 ms applies 2 more (and carries half a step)", 3, timeline.position());

        player.stepForward();
        Check.isTrue("a manual step pauses and moves one step", !player.isPlaying() && timeline.position() == 4);
        player.stepBack();
        Check.equal("step back undoes one", 3, timeline.position());

        player.setStepsPerSecond(100_000);
        player.play();
        player.tick(250 * MS);
        Check.isTrue("playing to the end stops by itself", timeline.atEnd() && !player.isPlaying());

        player.play();
        Check.isTrue("play at the end starts over", player.isPlaying() && timeline.position() == 0);
        player.togglePlay();
        Check.isTrue("toggle pauses", !player.isPlaying());

        player.seek(total / 2);
        Check.equal("seek jumps to a position", total / 2, timeline.position());
        player.reset();
        Check.equal("reset goes back to the start", 0, timeline.position());
        Check.isTrue("every action notified the screen", updates[0] >= 10);

        Check.throwsException("speed must be positive", IllegalArgumentException.class,
                () -> player.setStepsPerSecond(0));

        Player empty = new Player(false);
        empty.setTimeline(new Timeline<SortState, SortStep>(null, List.of()));
        empty.play();
        Check.isTrue("play does nothing with an empty recording", !empty.isPlaying());
        Player none = new Player(false);
        none.play();
        none.stepForward();
        none.seek(0);
        Check.isTrue("buttons do nothing when there is no recording", !none.isPlaying());
    }
}
