package algoscope.sorting;

/**
 * Divide and conquer: splits the array around a pivot so that everything on the
 * left is &lt;= everything on the right, then sorts both sides.
 * <p>
 * Same approach as the fixed v1: Hoare partition with the middle value as pivot,
 * and recursion only into the smaller side so the depth stays O(log n).
 */
public final class QuickSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Quick Sort";
    }

    @Override
    public Complexity complexity() {
        return new Complexity("O(n log n)", "O(n log n)", "O(n²)");
    }

    @Override
    public boolean stable() {
        return false;
    }

    @Override
    public void sort(TrackedArray a) {
        sort(a, 0, a.length() - 1);
    }

    /** Sorts a[low..high], both ends included. */
    private void sort(TrackedArray a, int low, int high) {
        while (low < high) {
            int split = partition(a, low, high);
            // Recurse into the smaller side and loop over the bigger one:
            // even with bad pivots the recursion is at most log2(n) deep
            if (split - low < high - split) {
                sort(a, low, split);
                low = split + 1;
            } else {
                sort(a, split + 1, high);
                high = split;
            }
        }
    }

    /**
     * Hoare partition. Two indexes walk towards each other and swap the pairs that
     * are on the wrong side of the pivot.
     *
     * @return the split: every value of a[low..split] is &lt;= every value of a[split+1..high]
     */
    private int partition(TrackedArray a, int low, int high) {
        // The middle value: the first or last one would make sorted input the worst case
        int pivot = a.get(low + (high - low) / 2);
        int i = low - 1;
        int j = high + 1;
        while (true) {
            // From the left, find a value that is not smaller than the pivot
            do {
                i++;
            } while (a.compareValues(a.get(i), pivot, i, -1) < 0);
            // From the right, find a value that is not bigger than the pivot
            do {
                j--;
            } while (a.compareValues(a.get(j), pivot, j, -1) > 0);
            // The indexes crossed: the range is split
            if (i >= j) {
                return j;
            }
            a.swap(i, j);
        }
    }
}
