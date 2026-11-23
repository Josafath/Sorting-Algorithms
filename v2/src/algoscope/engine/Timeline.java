package algoscope.engine;

import java.util.List;

/**
 * A recorded run of an algorithm that can be played step by step, in both directions.
 * <p>
 * It holds the state as it looks <em>now</em> and the list of steps. The position
 * says how many steps have been applied: 0 is the initial state, {@link #size()} is
 * the final one. The animation in the window only has to call {@link #stepForward()}
 * on a timer; the buttons call {@link #stepBack()} or {@link #seek(int)}.
 *
 * @param <S> the state that changes (for example the array being sorted)
 * @param <T> the kind of step, so callers get the exact type back from {@link #current()}
 */
public final class Timeline<S, T extends Step<S>> {

    private final S state;
    private final List<T> steps;
    // How many steps have been applied to the state
    private int position = 0;

    /**
     * @param initialState the state before any step; the timeline changes it in place
     * @param steps        the steps in the order the algorithm made them
     */
    public Timeline(S initialState, List<T> steps) {
        this.state = initialState;
        // A copy, so nobody can change the recording from outside
        this.steps = List.copyOf(steps);
    }

    /** The state after the steps applied so far. */
    public S state() {
        return state;
    }

    /** Number of steps applied so far. */
    public int position() {
        return position;
    }

    /** Total number of steps in the recording. */
    public int size() {
        return steps.size();
    }

    public boolean atStart() {
        return position == 0;
    }

    public boolean atEnd() {
        return position == steps.size();
    }

    /** The last step applied (the one to highlight on screen), or null at the start. */
    public T current() {
        return position == 0 ? null : steps.get(position - 1);
    }

    /**
     * Applies the next step.
     *
     * @return false if the recording was already at the end
     */
    public boolean stepForward() {
        if (atEnd()) {
            return false;
        }
        steps.get(position).apply(state);
        position++;
        return true;
    }

    /**
     * Takes back the last applied step.
     *
     * @return false if the recording was already at the start
     */
    public boolean stepBack() {
        if (atStart()) {
            return false;
        }
        position--;
        steps.get(position).undo(state);
        return true;
    }

    /** Moves to any position, for example when the user drags the progress bar. */
    public void seek(int target) {
        if (target < 0 || target > steps.size()) {
            throw new IndexOutOfBoundsException("Position " + target + " is outside 0.." + steps.size());
        }
        while (position < target) {
            stepForward();
        }
        while (position > target) {
            stepBack();
        }
    }
}
