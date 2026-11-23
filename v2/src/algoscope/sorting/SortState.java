package algoscope.sorting;

import java.util.Arrays;

/**
 * What the screen shows while a sort is replayed: the values and the counters
 * of comparisons, swaps and writes done up to the current step.
 * <p>
 * Only the steps of this package can change it; everybody else can only read.
 */
public final class SortState {

    private final int[] values;
    private int comparisons;
    private int swaps;
    private int writes;

    SortState(int[] values) {
        this.values = values;
    }

    public int length() {
        return values.length;
    }

    public int value(int index) {
        return values[index];
    }

    /** A copy of the values, safe to keep or change. */
    public int[] toArray() {
        return values.clone();
    }

    public int comparisons() {
        return comparisons;
    }

    public int swaps() {
        return swaps;
    }

    public int writes() {
        return writes;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    // ---- Used by SortStep to apply and undo ----

    void countComparison(int delta) {
        comparisons += delta;
    }

    void swapValues(int i, int j, int delta) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;
        swaps += delta;
    }

    void writeValue(int index, int value, int delta) {
        values[index] = value;
        writes += delta;
    }
}
