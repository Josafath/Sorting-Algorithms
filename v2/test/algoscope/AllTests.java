package algoscope;

import algoscope.engine.TimelineTests;
import algoscope.pathfinding.PathfindingTests;
import algoscope.sorting.SortingTests;
import algoscope.ui.PlaybackTests;
import algoscope.ui.sorting.SortingUiTests;

/** Runs every test of AlgoScope. Exit code 1 if anything failed, for CI. */
public final class AllTests {

    public static void main(String[] args) {
        long start = System.nanoTime();

        TimelineTests.run();
        SortingTests.run();
        PathfindingTests.run();
        PlaybackTests.run();
        SortingUiTests.run();

        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println();
        System.out.println(Check.passed() + " passed, " + Check.failed() + " failed (" + millis + " ms)");
        // exit() also stops any Swing thread a test may have started
        System.exit(Check.failed() > 0 ? 1 : 0);
    }
}
