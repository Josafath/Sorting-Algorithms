package algoscope.ui;

/**
 * Converts elapsed time into a whole number of steps.
 * <p>
 * At 10 steps per second and 60 frames per second each frame is worth 1/6 of a
 * step: the fraction is carried to the next frame instead of being lost, so the
 * real speed matches the chosen one.
 */
final class StepClock {

    /**
     * A frame never counts for more than this. If the computer was busy or asleep,
     * the animation continues smoothly instead of jumping far ahead.
     */
    static final long MAX_FRAME_NANOS = 250_000_000L;

    // The part of a step left over from previous frames
    private double carry = 0;

    /** Forgets the leftover fraction, for example when playback starts again. */
    void reset() {
        carry = 0;
    }

    /** How many steps to apply for a frame that lasted {@code elapsedNanos}. */
    int stepsFor(long elapsedNanos, double stepsPerSecond) {
        long nanos = Math.max(0, Math.min(elapsedNanos, MAX_FRAME_NANOS));
        double exact = carry + nanos / 1_000_000_000.0 * stepsPerSecond;
        int whole = (int) exact;
        carry = exact - whole;
        return whole;
    }
}
