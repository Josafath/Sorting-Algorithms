package algoscope.sorting;

import algoscope.engine.Timeline;

/** Runs an algorithm once, recording every step, so the window can replay it. */
public final class SortRecorder {

    /**
     * The biggest array that can be recorded. Bubble Sort on 1,000 values makes
     * about 750,000 steps; bigger arrays would use too much memory and nobody could
     * follow the animation anyway. The benchmark sorts without recording, with no limit.
     */
    public static final int MAX_LENGTH = 1_000;

    private SortRecorder() {
    }

    /**
     * Sorts a copy of {@code input} with the algorithm and returns the recording,
     * positioned at the start (the unsorted array). {@code input} is not changed.
     */
    public static Timeline<SortState, SortStep> record(SortAlgorithm algorithm, int[] input) {
        if (input.length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Only arrays of up to " + MAX_LENGTH + " values can be animated, got " + input.length);
        }
        // The algorithm sorts one copy while recording...
        TrackedArray tracked = TrackedArray.recording(input.clone());
        algorithm.sort(tracked);
        // ...and the timeline replays the steps on another copy, starting unsorted
        return new Timeline<>(new SortState(input.clone()), tracked.steps());
    }
}
