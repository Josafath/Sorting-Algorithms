package algoscope.sorting;

/**
 * Divide and conquer: sorts each half and then merges the two sorted halves
 * by repeatedly taking the smaller front value. Always O(n log n), but needs
 * a helper array as big as the input.
 */
public final class MergeSort implements SortAlgorithm {

    @Override
    public String name() {
        return "Merge Sort";
    }

    @Override
    public String description() {
        return "Sorts each half, then merges the two sorted halves. Always O(n log n), but uses a helper array.";
    }

    @Override
    public Complexity complexity() {
        return new Complexity("O(n log n)", "O(n log n)", "O(n log n)");
    }

    @Override
    public boolean stable() {
        return true;
    }

    @Override
    public void sort(TrackedArray a) {
        // One helper array for the whole sort instead of a new one in every merge
        int[] helper = new int[a.length()];
        sort(a, helper, 0, a.length() - 1);
    }

    /** Sorts a[low..high], both ends included. */
    private void sort(TrackedArray a, int[] helper, int low, int high) {
        if (low >= high) {
            // Zero or one value: already sorted
            return;
        }
        // Written this way so low + high can never overflow
        int mid = low + (high - low) / 2;
        sort(a, helper, low, mid);
        sort(a, helper, mid + 1, high);
        // If the last value on the left is not bigger than the first on the right,
        // the two halves are already in order: skip the merge (sorted input becomes O(n))
        if (a.compare(mid, mid + 1) <= 0) {
            return;
        }
        merge(a, helper, low, mid, high);
    }

    /** Merges the sorted runs a[low..mid] and a[mid+1..high]. */
    private void merge(TrackedArray a, int[] helper, int low, int mid, int high) {
        // Copy both halves aside: reading is free, only the writes back are steps
        for (int k = low; k <= high; k++) {
            helper[k] = a.get(k);
        }
        int left = low;       // next value of the left half
        int right = mid + 1;  // next value of the right half
        for (int k = low; k <= high; k++) {
            if (left > mid) {
                // Left half used up: the rest comes from the right
                a.set(k, helper[right++]);
            } else if (right > high) {
                // Right half used up: the rest comes from the left
                a.set(k, helper[left++]);
            } else if (a.compareValues(helper[right], helper[left], right, left) < 0) {
                // Take from the right only when strictly smaller: equal values keep their order
                a.set(k, helper[right++]);
            } else {
                a.set(k, helper[left++]);
            }
        }
    }
}
