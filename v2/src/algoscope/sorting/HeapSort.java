package algoscope.sorting;

/**
 * Arranges the array as a max-heap (a binary tree stored in the array where every
 * parent is &gt;= its children), then repeatedly moves the root, the biggest value,
 * to the end and repairs the heap. O(n log n) always and no extra memory.
 * <p>
 * In the array, the children of position i are at 2i+1 and 2i+2.
 */
public final class HeapSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Heap Sort";
    }

    @Override
    public String description() {
        return "Arranges the values as a max-heap, then moves the biggest one to the end again and again.";
    }

    @Override
    public Complexity complexity() {
        return new Complexity("O(n log n)", "O(n log n)", "O(n log n)");
    }

    @Override
    public boolean stable() {
        return false;
    }

    @Override
    public void sort(TrackedArray a) {
        int n = a.length();
        // 1. Build the heap: repair every parent, from the last one up to the root
        for (int parent = n / 2 - 1; parent >= 0; parent--) {
            siftDown(a, parent, n);
        }
        // 2. The root is the biggest value: move it after the heap and shrink the heap by one
        for (int end = n - 1; end > 0; end--) {
            a.swap(0, end);
            siftDown(a, 0, end);
        }
    }

    /**
     * Moves the value at {@code root} down until it is &gt;= its children,
     * looking only at the first {@code size} positions.
     */
    private void siftDown(TrackedArray a, int root, int size) {
        while (true) {
            int child = 2 * root + 1;
            if (child >= size) {
                // No children: it is a leaf
                return;
            }
            // Pick the bigger of the two children
            if (child + 1 < size && a.compare(child + 1, child) > 0) {
                child++;
            }
            // The parent is already >= its children: the heap is repaired
            if (a.compare(child, root) <= 0) {
                return;
            }
            a.swap(root, child);
            root = child;
        }
    }
}
