package algoscope.ui;

/**
 * Turns the position of the speed slider into steps per second.
 * <p>
 * The scale is logarithmic: each fifth of the slider multiplies the speed by about 8,
 * so the same slider can show a single comparison per second (to follow the idea)
 * and tens of thousands (to watch 1,000 bars finish).
 */
public final class Speed {

    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 100;
    public static final int DEFAULT_POSITION = 50;

    // From 10^0 = 1 to 10^4.5 ≈ 31,623 steps per second
    private static final double DECADES = 4.5;

    private Speed() {
    }

    /** Steps per second for a slider position (positions outside 0..100 are clamped). */
    public static double stepsPerSecond(int position) {
        int clamped = Math.max(MIN_POSITION, Math.min(MAX_POSITION, position));
        return Math.pow(10, clamped * DECADES / MAX_POSITION);
    }

    /** Text for the label next to the slider, like "178 steps/s". */
    public static String label(double stepsPerSecond) {
        return String.format("%,d steps/s", Math.round(stepsPerSecond));
    }
}
