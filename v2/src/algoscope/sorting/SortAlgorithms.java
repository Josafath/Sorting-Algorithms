package algoscope.sorting;

import java.util.List;

/** The list of sorting algorithms the app offers, in the order of the menu. */
public final class SortAlgorithms {

    /** From the simplest O(n²) ones to the O(n log n) ones. */
    public static final List<SortAlgorithm> ALL = List.of(
            new BubbleSort(),
            new InsertionSort(),
            new SelectionSort(),
            new MergeSort(),
            new QuickSort(),
            new HeapSort());

    private SortAlgorithms() {
    }

    /** Finds an algorithm by its name, like "Heap Sort". */
    public static SortAlgorithm byName(String name) {
        for (SortAlgorithm algorithm : ALL) {
            if (algorithm.name().equals(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Unknown sorting algorithm: " + name);
    }
}
