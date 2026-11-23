package algoscope.engine;

import algoscope.Check;
import java.util.ArrayList;
import java.util.List;

/** Tests the replay engine with a toy state: a list of letters. */
public final class TimelineTests {

    /** Adds a letter at the end; undo removes it. */
    private record Append(String letter) implements Step<List<String>> {
        @Override
        public void apply(List<String> state) {
            state.add(letter);
        }

        @Override
        public void undo(List<String> state) {
            state.remove(state.size() - 1);
        }
    }

    private TimelineTests() {
    }

    public static void run() {
        Check.group("Timeline", () -> {
            startsAtTheBeginning();
            movesForwardAndBack();
            stopsAtBothEnds();
            seeksAnywhere();
            handlesAnEmptyRecording();
            recordingCannotBeChangedFromOutside();
        });
    }

    private static Timeline<List<String>, Append> abc() {
        return new Timeline<>(new ArrayList<>(), List.of(new Append("a"), new Append("b"), new Append("c")));
    }

    private static void startsAtTheBeginning() {
        Timeline<List<String>, Append> timeline = abc();
        Check.equal("a new timeline is at position 0", 0, timeline.position());
        Check.equal("it knows its size", 3, timeline.size());
        Check.isTrue("it is at the start and not at the end", timeline.atStart() && !timeline.atEnd());
        Check.equal("there is no current step yet", null, timeline.current());
        Check.equal("the state is untouched", List.of(), timeline.state());
    }

    private static void movesForwardAndBack() {
        Timeline<List<String>, Append> timeline = abc();
        timeline.stepForward();
        timeline.stepForward();
        Check.equal("two steps forward apply two steps", List.of("a", "b"), timeline.state());
        Check.equal("current is the last applied step", new Append("b"), timeline.current());
        timeline.stepBack();
        Check.equal("one step back undoes the last one", List.of("a"), timeline.state());
        Check.equal("position follows", 1, timeline.position());
    }

    private static void stopsAtBothEnds() {
        Timeline<List<String>, Append> timeline = abc();
        Check.isTrue("stepBack at the start returns false", !timeline.stepBack());
        while (timeline.stepForward()) {
            // play to the end
        }
        Check.equal("playing to the end applies everything", List.of("a", "b", "c"), timeline.state());
        Check.isTrue("it is at the end", timeline.atEnd());
        Check.isTrue("stepForward at the end returns false", !timeline.stepForward());
        Check.equal("and changes nothing", List.of("a", "b", "c"), timeline.state());
    }

    private static void seeksAnywhere() {
        Timeline<List<String>, Append> timeline = abc();
        timeline.seek(3);
        Check.equal("seek to the end", List.of("a", "b", "c"), timeline.state());
        timeline.seek(1);
        Check.equal("seek back to the middle", List.of("a"), timeline.state());
        timeline.seek(0);
        Check.isTrue("seek to the start", timeline.atStart() && timeline.state().isEmpty());
        Check.throwsException("seek past the end is rejected", IndexOutOfBoundsException.class, () -> timeline.seek(4));
        Check.throwsException("seek before the start is rejected", IndexOutOfBoundsException.class, () -> timeline.seek(-1));
    }

    private static void handlesAnEmptyRecording() {
        Timeline<List<String>, Append> timeline = new Timeline<>(new ArrayList<>(), List.of());
        Check.isTrue("an empty recording is at the start and at the end", timeline.atStart() && timeline.atEnd());
        Check.isTrue("and cannot move", !timeline.stepForward() && !timeline.stepBack());
    }

    private static void recordingCannotBeChangedFromOutside() {
        List<Append> steps = new ArrayList<>(List.of(new Append("a")));
        Timeline<List<String>, Append> timeline = new Timeline<>(new ArrayList<>(), steps);
        steps.add(new Append("b"));
        Check.equal("changing the original list does not change the timeline", 1, timeline.size());
    }
}
