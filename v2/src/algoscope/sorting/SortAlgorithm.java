package algoscope.sorting;

/**
 * A sorting algorithm. Every implementation sorts a {@link TrackedArray} in place,
 * in ascending order, using only its compare, swap and set methods.
 */
public interface SortAlgorithm {

    /** The name shown in the window, like "Merge Sort". */
    String name();

    /** Big-O time in the best, average and worst case, shown next to the name. */
    Complexity complexity();

    /** True if equal values keep their original order. */
    boolean stable();

    /** Sorts the array in place. */
    void sort(TrackedArray array);

    /** Shortcut to sort a plain int[] in place, counting but not recording. */
    default void sort(int[] values) {
        sort(TrackedArray.of(values));
    }

    /** The three Big-O times of an algorithm, as text ("O(n log n)"). */
    record Complexity(String best, String average, String worst) {
    }
}
