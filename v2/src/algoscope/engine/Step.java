package algoscope.engine;

/**
 * One small change an algorithm made to a state, like "swap positions 3 and 7"
 * or "visit this cell".
 * <p>
 * A step knows how to do the change and how to take it back, so a recorded
 * algorithm can be played forwards and backwards like a video.
 *
 * @param <S> the state the step changes (the array being sorted, the grid being searched...)
 */
public interface Step<S> {

    /** Makes the change on the state. */
    void apply(S state);

    /** Takes the change back: {@code apply} followed by {@code undo} leaves the state as it was. */
    void undo(S state);
}
