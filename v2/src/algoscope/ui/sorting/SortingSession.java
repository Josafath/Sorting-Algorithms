package algoscope.ui.sorting;

import algoscope.engine.Timeline;
import algoscope.sorting.InputKind;
import algoscope.sorting.SortAlgorithm;
import algoscope.sorting.SortAlgorithms;
import algoscope.sorting.SortRecorder;
import algoscope.sorting.SortState;
import algoscope.sorting.SortStep;

/**
 * What the user chose in the Sorting tab (algorithm, kind of input, size) and the
 * recording that results. It has no Swing code, so it can be tested on its own.
 * <p>
 * Changing the algorithm keeps the same input array: that way two algorithms can be
 * compared on exactly the same data. Changing the kind, the size or asking for a
 * new input creates a new array.
 */
public final class SortingSession {

    public static final int MIN_SIZE = 5;
    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = SortRecorder.MAX_LENGTH;

    private SortAlgorithm algorithm = SortAlgorithms.ALL.get(0);
    private InputKind inputKind = InputKind.RANDOM;
    private int size = DEFAULT_SIZE;
    // Changes with "New input", so RANDOM and FEW_UNIQUE give another array
    private long seed = 1;

    private int[] input;
    private Timeline<SortState, SortStep> timeline;

    public SortingSession() {
        newArray();
    }

    public SortAlgorithm algorithm() {
        return algorithm;
    }

    public InputKind inputKind() {
        return inputKind;
    }

    public int size() {
        return size;
    }

    /** A copy of the unsorted input. */
    public int[] input() {
        return input.clone();
    }

    /** The recording of the current algorithm on the current input, at its start. */
    public Timeline<SortState, SortStep> timeline() {
        return timeline;
    }

    /** Another algorithm on the same input. */
    public void setAlgorithm(SortAlgorithm algorithm) {
        this.algorithm = algorithm;
        record();
    }

    public void setInputKind(InputKind inputKind) {
        this.inputKind = inputKind;
        newArray();
    }

    /** Sizes outside {@link #MIN_SIZE}..{@link #MAX_SIZE} are clamped. */
    public void setSize(int size) {
        this.size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
        newArray();
    }

    /** Another array of the same kind and size. */
    public void newInput() {
        seed++;
        newArray();
    }

    private void newArray() {
        input = inputKind.generate(size, seed);
        record();
    }

    private void record() {
        timeline = SortRecorder.record(algorithm, input);
    }
}
