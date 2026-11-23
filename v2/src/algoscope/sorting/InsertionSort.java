package algoscope.sorting;

/**
 * Grows a sorted part on the left: takes the next value, shifts the bigger
 * values of the sorted part one place to the right and drops it into the gap.
 * Like sorting playing cards in your hand.
 */
public final class InsertionSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Insertion Sort";
    }

    @Override
    public String description() {
        return "Takes each value and shifts the bigger ones to the right until it fits, like sorting cards in your hand.";
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
        for (int i = 1; i < a.length(); i++) {
            // The value to insert; its position i becomes the gap
            int key = a.get(i);
            int j = i - 1;
            // Shift every bigger value one place right (j + 1 is where the gap is now)
            while (j >= 0 && a.compareValues(a.get(j), key, j, j + 1) > 0) {
                a.set(j + 1, a.get(j));
                j--;
            }
            // Drop the key into the gap, unless it never moved
            if (j + 1 != i) {
                a.set(j + 1, key);
            }
        }
    }
}
