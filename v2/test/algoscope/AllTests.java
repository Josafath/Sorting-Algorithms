package algoscope;

import algoscope.engine.TimelineTests;
import algoscope.sorting.SortingTests;

/** Runs every test of AlgoScope. Exit code 1 if anything failed, for CI. */
public final class AllTests {

    public static void main(String[] args) {
        long start = System.nanoTime();

        TimelineTests.run();
        SortingTests.run();

        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println();
        System.out.println(Check.passed() + " passed, " + Check.failed() + " failed (" + millis + " ms)");
        if (Check.failed() > 0) {
            System.exit(1);
        }
    }
}
