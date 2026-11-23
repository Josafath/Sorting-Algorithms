package algoscope.sorting;

/**
 * Compares neighbours and swaps them when they are in the wrong order.
 * After each pass the biggest value left has "bubbled up" to the end.
 */
public final class BubbleSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Bubble Sort";
    }

    @Override
    public Complexity complexity() {
        return new Complexity("O(n)", "O(n²)", "O(n²)");
    }

    @Override
    public boolean stable() {
        return true;
    }

    @Override
    public void sort(TrackedArray a) {
        // Everything after "end" is already in its final place
        for (int end = a.length() - 1; end > 0; end--) {
            boolean swapped = false;
            for (int j = 0; j < end; j++) {
                // Only a strictly bigger value moves, so equal values keep their order (stable)
                if (a.compare(j, j + 1) > 0) {
                    a.swap(j, j + 1);
                    swapped = true;
                }
            }
            // A pass with no swaps means the array is sorted: an already sorted input takes O(n)
            if (!swapped) {
                return;
            }
        }
    }
}
