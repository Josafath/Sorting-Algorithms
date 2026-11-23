package algoscope.pathfinding;

import algoscope.engine.Step;

/**
 * The three things a search does to a cell: discover it, visit it, or mark it as part
 * of the path. Each step remembers the status the cell had before, so it can be undone.
 * <p>
 * {@code sealed}: these three records are the only possible steps.
 */
public sealed interface SearchStep extends Step<SearchState> {

    Cell cell();

    /** Status of the cell before the step. */
    CellStatus before();

    /** Status of the cell after the step. */
    CellStatus after();

    @Override
    default void apply(SearchState state) {
        state.setStatus(cell(), after());
    }

    @Override
    default void undo(SearchState state) {
        state.setStatus(cell(), before());
    }

    /** The cell was reached for the first time and waits to be explored. */
    record Discover(Cell cell, CellStatus before) implements SearchStep {
        @Override
        public CellStatus after() {
            return CellStatus.FRONTIER;
        }
    }

    /** The cell is explored: the algorithm looks at its neighbours. */
    record Visit(Cell cell, CellStatus before) implements SearchStep {
        @Override
        public CellStatus after() {
            return CellStatus.VISITED;
        }

        @Override
        public void apply(SearchState state) {
            SearchStep.super.apply(state);
            state.countVisit(+1);
        }

        @Override
        public void undo(SearchState state) {
            SearchStep.super.undo(state);
            state.countVisit(-1);
        }
    }

    /** The cell belongs to the path found. Path steps come last, from start to goal. */
    record PathCell(Cell cell, CellStatus before) implements SearchStep {
        @Override
        public CellStatus after() {
            return CellStatus.PATH;
        }

        @Override
        public void apply(SearchState state) {
            SearchStep.super.apply(state);
            state.countPathCell(+1);
        }

        @Override
        public void undo(SearchState state) {
            SearchStep.super.undo(state);
            state.countPathCell(-1);
        }
    }
}
