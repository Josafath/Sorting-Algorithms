package algoscope.sorting;

import java.util.ArrayList;
import java.util.List;

/**
 * The array the algorithms sort. They never touch the int[] directly: every
 * comparison, swap and write goes through here, so it can be counted and,
 * when an animation is wanted, recorded as a {@link SortStep}.
 * <p>
 * Without recording (the benchmark) it only counts, so it stays fast and uses no memory.
 */
public final class TrackedArray {

    private final int[] data;
    // null when the steps are not being recorded
    private final List<SortStep> steps;

    // long: a benchmark of Bubble Sort with 100,000 numbers does ~5,000,000,000 comparisons
    private long comparisons;
    private long swaps;
    private long writes;

    private TrackedArray(int[] data, boolean record) {
        this.data = data;
        this.steps = record ? new ArrayList<>() : null;
    }

    /** Sorts {@code data} in place, counting but not recording. */
    public static TrackedArray of(int[] data) {
        return new TrackedArray(data, false);
    }

    /** Sorts {@code data} in place and records every step (used by {@link SortRecorder}). */
    static TrackedArray recording(int[] data) {
        return new TrackedArray(data, true);
    }

    public int length() {
        return data.length;
    }

    /** Reads a value. Reading is free: it is not counted or recorded. */
    public int get(int index) {
        return data[index];
    }

    /**
     * Compares the values at two positions.
     *
     * @return negative, zero or positive, like {@link Integer#compare}
     */
    public int compare(int i, int j) {
        return compareValues(data[i], data[j], i, j);
    }

    /**
     * Compares two values the algorithm already has in variables
     * (the key of Insertion Sort, the helper array of Merge Sort, the pivot of Quick Sort).
     *
     * @param i the position to highlight for {@code a}
     * @param j the position to highlight for {@code b}, or -1 for none
     */
    public int compareValues(int a, int b, int i, int j) {
        comparisons++;
        if (steps != null) {
            steps.add(new SortStep.Compare(i, j));
        }
        return Integer.compare(a, b);
    }

    /** Exchanges the values at two positions. Swapping a position with itself does nothing. */
    public void swap(int i, int j) {
        if (i == j) {
            return;
        }
        swaps++;
        if (steps != null) {
            steps.add(new SortStep.Swap(i, j));
        }
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    /** Writes a value at a position, replacing the one that was there. */
    public void set(int index, int value) {
        writes++;
        if (steps != null) {
            steps.add(new SortStep.Write(index, data[index], value));
        }
        data[index] = value;
    }

    public long comparisons() {
        return comparisons;
    }

    public long swaps() {
        return swaps;
    }

    public long writes() {
        return writes;
    }

    /** The recorded steps, in order. */
    List<SortStep> steps() {
        if (steps == null) {
            throw new IllegalStateException("This array was not recording");
        }
        return steps;
    }
}
