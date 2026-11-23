package algoscope.sorting;

import algoscope.Check;
import algoscope.engine.Timeline;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Tests the six sorting algorithms, the recorder and the input generator. */
public final class SortingTests {

    private SortingTests() {
    }

    public static void run() {
        Check.group("Algorithm list", SortingTests::listHasTheSixAlgorithms);
        Check.group("Special arrays", SortingTests::sortsSpecialArrays);
        Check.group("Every input kind", SortingTests::sortsEveryInputKind);
        Check.group("Quick Sort on huge easy inputs", SortingTests::quickSortSurvivesHugeEasyInputs);
        Check.group("Counters", SortingTests::countersMatchTheTheory);
        Check.group("Replay", SortingTests::replayEndsSortedAndRewindsToTheInput);
        Check.group("Step indexes", SortingTests::stepsPointInsideTheArray);
        Check.group("Recording limits", SortingTests::recordingLimits);
        Check.group("Input kinds", SortingTests::inputKindsLookRight);
    }

    // ---- Algorithm list ----

    private static void listHasTheSixAlgorithms() {
        Check.equal("there are six algorithms", 6, SortAlgorithms.ALL.size());
        Set<String> names = new HashSet<>();
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            names.add(algorithm.name());
            Check.isTrue(algorithm.name() + " has a complexity", algorithm.complexity() != null);
            Check.isTrue("byName finds " + algorithm.name(), SortAlgorithms.byName(algorithm.name()) == algorithm);
        }
        Check.equal("names are unique", 6, names.size());
        Check.throwsException("an unknown name is rejected", IllegalArgumentException.class,
                () -> SortAlgorithms.byName("Bogo Sort"));
    }

    // ---- Sorting ----

    private static void sortsSpecialArrays() {
        int[][] cases = {
            {},
            {7},
            {2, 1},
            {1, 2},
            {3, 1, 3, 1, 3, 1},
            {9, 9, 9, 9, 9},
            {-5, 0, -1, 8, -100, 3},
            {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1, 1, Integer.MAX_VALUE},
        };
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            boolean all = true;
            for (int[] input : cases) {
                all &= sortsLikeArraysSort(algorithm, input);
            }
            Check.isTrue(algorithm.name() + " sorts empty, single, duplicates, negatives and int limits", all);
        }
    }

    private static void sortsEveryInputKind() {
        int[] sizes = {1, 2, 3, 10, 101, 1000, 5000};
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            for (InputKind kind : InputKind.values()) {
                boolean all = true;
                for (int size : sizes) {
                    // A few seeds, so random inputs are not always the same one
                    for (long seed = 1; seed <= 3; seed++) {
                        all &= sortsLikeArraysSort(algorithm, kind.generate(size, seed));
                    }
                }
                Check.isTrue(algorithm.name() + " sorts " + kind.label().toLowerCase() + " input (1 to 5,000 values)", all);
            }
        }
        // Values far from 1..n, with a lot of repetition
        Random random = new Random(7);
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            int[] input = new int[2000];
            for (int i = 0; i < input.length; i++) {
                input[i] = random.nextInt(21) - 10;
            }
            Check.isTrue(algorithm.name() + " sorts 2,000 values between -10 and 10", sortsLikeArraysSort(algorithm, input));
        }
    }

    /** Sorts a copy with the algorithm and compares it with Java's own Arrays.sort. */
    private static boolean sortsLikeArraysSort(SortAlgorithm algorithm, int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        int[] actual = input.clone();
        algorithm.sort(actual);
        return Arrays.equals(expected, actual);
    }

    private static void quickSortSurvivesHugeEasyInputs() {
        // A first or last pivot would need one recursive call per value here
        int size = 1_000_000;
        int[] equal = new int[size];
        Arrays.fill(equal, 5);
        for (int[] input : new int[][]{
            InputKind.SORTED.generate(size, 1),
            InputKind.REVERSED.generate(size, 1),
            equal}) {
            try {
                Check.isTrue("Quick Sort sorts 1,000,000 sorted / reversed / equal values",
                        sortsLikeArraysSort(new QuickSort(), input));
            } catch (StackOverflowError e) {
                Check.isTrue("Quick Sort sorts 1,000,000 values without StackOverflowError", false);
            }
        }
    }

    // ---- Counters ----

    private static TrackedArray counted(SortAlgorithm algorithm, int[] input) {
        TrackedArray tracked = TrackedArray.of(input.clone());
        algorithm.sort(tracked);
        return tracked;
    }

    private static void countersMatchTheTheory() {
        int n = 100;
        int[] sorted = InputKind.SORTED.generate(n, 1);
        int[] reversed = InputKind.REVERSED.generate(n, 1);
        int[] random = InputKind.RANDOM.generate(n, 1);
        long allPairs = (long) n * (n - 1) / 2;

        TrackedArray bubbleSorted = counted(new BubbleSort(), sorted);
        Check.isTrue("Bubble Sort on sorted input: n-1 comparisons and no swaps (stops early)",
                bubbleSorted.comparisons() == n - 1 && bubbleSorted.swaps() == 0);
        Check.equal("Bubble Sort on reversed input swaps every pair", allPairs, counted(new BubbleSort(), reversed).swaps());

        TrackedArray insertionSorted = counted(new InsertionSort(), sorted);
        Check.isTrue("Insertion Sort on sorted input: n-1 comparisons and no writes",
                insertionSorted.comparisons() == n - 1 && insertionSorted.writes() == 0);

        TrackedArray selection = counted(new SelectionSort(), random);
        Check.equal("Selection Sort always compares every pair", allPairs, selection.comparisons());
        Check.isTrue("Selection Sort swaps at most n-1 times", selection.swaps() <= n - 1);
        Check.equal("Selection Sort compares every pair even on sorted input",
                allPairs, counted(new SelectionSort(), sorted).comparisons());

        TrackedArray mergeSorted = counted(new MergeSort(), sorted);
        Check.isTrue("Merge Sort on sorted input: n-1 comparisons and no writes (merges skipped)",
                mergeSorted.comparisons() == n - 1 && mergeSorted.writes() == 0);
        int[] equal = new int[n];
        Arrays.fill(equal, 7);
        TrackedArray mergeEqual = counted(new MergeSort(), equal);
        Check.isTrue("Merge Sort on equal values: n-1 comparisons and no writes (equal halves are in order)",
                mergeEqual.comparisons() == n - 1 && mergeEqual.writes() == 0);

        // n log2 n is the order of growth; these limits are generous but still far below n²
        int big = 4096;
        long nLogN = (long) big * 12; // log2(4096) = 12
        int[] bigRandom = InputKind.RANDOM.generate(big, 1);
        Check.isTrue("Merge Sort on 4,096 random values: at most n·log2(n) comparisons",
                counted(new MergeSort(), bigRandom).comparisons() <= nLogN);
        Check.isTrue("Heap Sort on 4,096 random values: at most 2·n·log2(n) comparisons",
                counted(new HeapSort(), bigRandom).comparisons() <= 2 * nLogN);
        Check.isTrue("Quick Sort on 4,096 random values: at most 2·n·log2(n) comparisons",
                counted(new QuickSort(), bigRandom).comparisons() <= 2 * nLogN);
    }

    // ---- Recording and replay ----

    private static void replayEndsSortedAndRewindsToTheInput() {
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            boolean sortedAtEnd = true;
            boolean inputAtStart = true;
            boolean countersMatch = true;
            boolean inputUntouched = true;
            for (InputKind kind : InputKind.values()) {
                for (int size : new int[]{0, 1, 2, 60}) {
                    int[] input = kind.generate(size, 3);
                    int[] original = input.clone();
                    Timeline<SortState, SortStep> timeline = SortRecorder.record(algorithm, input);
                    inputUntouched &= Arrays.equals(original, input);

                    // Play to the end: the array must be sorted
                    timeline.seek(timeline.size());
                    int[] expected = input.clone();
                    Arrays.sort(expected);
                    sortedAtEnd &= Arrays.equals(expected, timeline.state().toArray());

                    // The counters of the replay are the ones of a normal run
                    SortState state = timeline.state();
                    TrackedArray normal = counted(algorithm, input);
                    countersMatch &= state.comparisons() == normal.comparisons()
                            && state.swaps() == normal.swaps()
                            && state.writes() == normal.writes()
                            && timeline.size() == state.comparisons() + state.swaps() + state.writes();

                    // Rewind: the array and the counters go back to the start
                    timeline.seek(0);
                    inputAtStart &= Arrays.equals(input, timeline.state().toArray())
                            && timeline.state().comparisons() == 0
                            && timeline.state().swaps() == 0
                            && timeline.state().writes() == 0;
                }
            }
            Check.isTrue(algorithm.name() + ": replay ends with the sorted array", sortedAtEnd);
            Check.isTrue(algorithm.name() + ": rewinding gives back the input and zero counters", inputAtStart);
            Check.isTrue(algorithm.name() + ": replay counters equal a normal run", countersMatch);
            Check.isTrue(algorithm.name() + ": recording does not change the input array", inputUntouched);
        }
    }

    private static void stepsPointInsideTheArray() {
        // The window will use these indexes to color bars: none may be out of range
        int size = 80;
        for (SortAlgorithm algorithm : SortAlgorithms.ALL) {
            Timeline<SortState, SortStep> timeline =
                    SortRecorder.record(algorithm, InputKind.RANDOM.generate(size, 9));
            boolean inside = true;
            while (timeline.stepForward()) {
                inside &= switch (timeline.current()) {
                    case SortStep.Compare c -> inRange(c.i(), size) && (c.j() == -1 || inRange(c.j(), size));
                    case SortStep.Swap s -> inRange(s.i(), size) && inRange(s.j(), size) && s.i() != s.j();
                    case SortStep.Write w -> inRange(w.index(), size);
                };
            }
            Check.isTrue(algorithm.name() + ": every step points inside the array", inside);
        }
    }

    private static boolean inRange(int index, int size) {
        return index >= 0 && index < size;
    }

    private static void recordingLimits() {
        SortAlgorithm bubble = new BubbleSort();
        int[] biggest = InputKind.REVERSED.generate(SortRecorder.MAX_LENGTH, 1);
        Timeline<SortState, SortStep> timeline = SortRecorder.record(bubble, biggest);
        Check.isTrue("the biggest allowed array can be recorded (worst case for Bubble Sort)", timeline.size() > 0);
        Check.throwsException("a bigger array is rejected", IllegalArgumentException.class,
                () -> SortRecorder.record(bubble, new int[SortRecorder.MAX_LENGTH + 1]));
        Check.throwsException("a normal array cannot hand out steps it did not record", IllegalStateException.class,
                () -> TrackedArray.of(new int[3]).steps());
    }

    // ---- Input kinds ----

    private static void inputKindsLookRight() {
        int n = 500;
        int[] ascending = new int[n];
        for (int i = 0; i < n; i++) {
            ascending[i] = i + 1;
        }

        Check.sameArray("SORTED is 1..n", ascending, InputKind.SORTED.generate(n, 1));

        int[] reversed = InputKind.REVERSED.generate(n, 1);
        Check.isTrue("REVERSED is n..1", reversed[0] == n && reversed[n - 1] == 1 && isDescending(reversed));

        int[] random = InputKind.RANDOM.generate(n, 1);
        int[] randomSorted = random.clone();
        Arrays.sort(randomSorted);
        Check.sameArray("RANDOM is a shuffle of 1..n", ascending, randomSorted);
        Check.sameArray("the same seed gives the same array", random, InputKind.RANDOM.generate(n, 1));
        Check.isTrue("another seed gives another array", !Arrays.equals(random, InputKind.RANDOM.generate(n, 2)));

        int[] few = InputKind.FEW_UNIQUE.generate(n, 1);
        Check.isTrue("FEW_UNIQUE uses at most " + InputKind.LEVELS + " different values",
                Arrays.stream(few).distinct().count() <= InputKind.LEVELS);
        Check.isTrue("FEW_UNIQUE values stay between 1 and n", Arrays.stream(few).allMatch(v -> v >= 1 && v <= n));
        Check.isTrue("FEW_UNIQUE with 3 values still uses positive heights",
                Arrays.stream(InputKind.FEW_UNIQUE.generate(3, 1)).allMatch(v -> v >= 1));

        Check.equal("size 0 gives an empty array", 0, InputKind.RANDOM.generate(0, 1).length);
        Check.throwsException("a negative size is rejected", IllegalArgumentException.class,
                () -> InputKind.SORTED.generate(-1, 1));
    }

    private static boolean isDescending(int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[i - 1]) {
                return false;
            }
        }
        return true;
    }
}
