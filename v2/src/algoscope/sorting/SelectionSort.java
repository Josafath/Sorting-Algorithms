package algoscope.sorting;

/**
 * Finds the smallest value of the unsorted part and swaps it to the front.
 * Always does n(n-1)/2 comparisons, but at most n-1 swaps: the fewest of all
 * the algorithms here, useful when writing is expensive.
 */
public final class SelectionSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Selection Sort";
    }

    @Override
    public Complexity complexity() {
        return new Complexity("O(n²)", "O(n²)", "O(n²)");
    }

    @Override
    public boolean stable() {
        // The long swap can jump a value over an equal one: [2a, 2b, 1] ends as [1, 2b, 2a]
        return false;
    }

    @Override
    public void sort(TrackedArray a) {
        for (int i = 0; i < a.length() - 1; i++) {
            // Position of the smallest value seen so far in a[i..end]
            int min = i;
            for (int j = i + 1; j < a.length(); j++) {
                if (a.compare(j, min) < 0) {
                    min = j;
                }
            }
            // swap() ignores min == i, when the smallest value was already in place
            a.swap(i, min);
        }
    }
}
