package algoscope.sorting;

import algoscope.engine.Step;

/**
 * The three things a sorting algorithm does to the array.
 * <p>
 * It is a {@code sealed} interface: these three records are the only possible steps,
 * so a {@code switch} over a SortStep (for example to choose the highlight color)
 * is checked by the compiler and cannot forget a case.
 */
public sealed interface SortStep extends Step<SortState> {

    /**
     * Two values were compared. It does not change the array, but it is the most
     * common step and the one that makes O(n²) algorithms slow, so it is recorded.
     *
     * @param i the first position compared
     * @param j the second position compared, or -1 when the other value is not in the
     *          array (like the pivot of Quick Sort, which is kept in a variable)
     */
    record Compare(int i, int j) implements SortStep {
        @Override
        public void apply(SortState state) {
            state.countComparison(+1);
        }

        @Override
        public void undo(SortState state) {
            state.countComparison(-1);
        }
    }

    /** The values at positions i and j changed places. */
    record Swap(int i, int j) implements SortStep {
        @Override
        public void apply(SortState state) {
            state.swapValues(i, j, +1);
        }

        @Override
        public void undo(SortState state) {
            // Swapping the same two positions again puts them back
            state.swapValues(i, j, -1);
        }
    }

    /**
     * A value was written over another one (Insertion Sort shifting values,
     * Merge Sort copying back from its helper array).
     * The old value is kept so the step can be undone.
     */
    record Write(int index, int oldValue, int newValue) implements SortStep {
        @Override
        public void apply(SortState state) {
            state.writeValue(index, newValue, +1);
        }

        @Override
        public void undo(SortState state) {
            state.writeValue(index, oldValue, -1);
        }
    }
}
