package tests;

import Algorithms.Bubble_Sort;
import Algorithms.Insertion_Sort;
import Algorithms.Merge_Sort;
import Algorithms.Quick_Sort;
import Algorithms.Shell_Sort;
import Menu.NumberIO;
import Menu.Principal;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Tests for v1 with no external library: only the JDK is needed.
 * Every check prints PASS or FAIL and the program exits with code 1 if any failed,
 * so it works the same in a terminal, in NetBeans (Run File) and in GitHub Actions.
 */
public class SortingTests {

    // The names exactly as they appear in the combo box of the window
    private static final String[] ALGORITHMS = {
        "Bubble Sort", "Insertion Sort", "QuickSort", "Merge Sort", "ShellSort"
    };

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        sortsSmallAndSpecialArrays();
        sortsRandomArrays();
        eachAlgorithmClassSortsDirectly();
        bubbleAndInsertionDoNotShareTheirArray();
        quickSortHandlesBigSortedInput();
        unknownAlgorithmIsRejected();
        parseAcceptsCommonFormats();
        parseRejectsBadInput();
        readUsesEveryLineOfTheFile();
        writeReplacesTheFile();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---- Sorting ----

    private static void sortsSmallAndSpecialArrays() {
        int[][] cases = {
            {},
            {7},
            {2, 1},
            {1, 2, 3, 4, 5},
            {5, 4, 3, 2, 1},
            {3, 1, 3, 1, 3, 1},
            {9, 9, 9, 9},
            {-5, 0, -1, 8, -100, 3},
            {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1, 1},
        };
        for (String algorithm : ALGORITHMS) {
            for (int[] input : cases) {
                checkSorts(algorithm, input, Arrays.toString(input));
            }
        }
    }

    private static void sortsRandomArrays() {
        // A fixed seed makes every run test the same arrays
        Random random = new Random(42);
        int[] sizes = {10, 101, 1000, 2500};
        for (String algorithm : ALGORITHMS) {
            for (int size : sizes) {
                int[] input = new int[size];
                for (int i = 0; i < size; i++) {
                    // A small range forces many repeated values
                    input[i] = random.nextInt(200) - 100;
                }
                checkSorts(algorithm, input, size + " random numbers");
            }
        }
    }

    /** Sorts a copy with the app and compares it with Java's own Arrays.sort. */
    private static void checkSorts(String algorithm, int[] input, String description) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        int[] actual = input.clone();
        Principal.sort(algorithm, actual);
        check(algorithm + " sorts " + shorten(description), Arrays.equals(expected, actual));
    }

    private static void eachAlgorithmClassSortsDirectly() {
        int[] input = {4, -2, 9, 0, 4, 1};
        int[] expected = {-2, 0, 1, 4, 4, 9};

        check("Bubble_Sort.ordenar returns the sorted array",
                Arrays.equals(expected, new Bubble_Sort(input.clone()).ordenar()));
        check("Insertion_Sort.ordenar_insertion returns the sorted array",
                Arrays.equals(expected, new Insertion_Sort(input.clone()).ordenar_insertion()));
        check("Shell_Sort.ordenar returns the sorted array",
                Arrays.equals(expected, new Shell_Sort().ordenar(input.clone())));

        int[] quick = input.clone();
        new Quick_Sort().quickSort(quick, 0, quick.length - 1);
        check("Quick_Sort.quickSort sorts in place", Arrays.equals(expected, quick));

        int[] merge = input.clone();
        new Merge_Sort().ordenar(merge, 0, merge.length - 1);
        check("Merge_Sort.ordenar sorts in place", Arrays.equals(expected, merge));
    }

    private static void bubbleAndInsertionDoNotShareTheirArray() {
        // Before v1 the array was static: creating the second object replaced the first one's array
        int[] first = {3, 1, 2};
        int[] second = {9, 8, 7};
        Bubble_Sort a = new Bubble_Sort(first);
        new Bubble_Sort(second);
        a.ordenar();
        check("two Bubble_Sort objects keep their own array",
                Arrays.equals(new int[]{1, 2, 3}, first) && Arrays.equals(new int[]{9, 8, 7}, second));

        int[] third = {3, 1, 2};
        int[] fourth = {9, 8, 7};
        Insertion_Sort b = new Insertion_Sort(third);
        new Insertion_Sort(fourth);
        b.ordenar_insertion();
        check("two Insertion_Sort objects keep their own array",
                Arrays.equals(new int[]{1, 2, 3}, third) && Arrays.equals(new int[]{9, 8, 7}, fourth));
    }

    private static void quickSortHandlesBigSortedInput() {
        // With the last element as pivot these inputs needed one recursive call per number
        int size = 200_000;
        int[] ascending = new int[size];
        int[] descending = new int[size];
        int[] equal = new int[size];
        for (int i = 0; i < size; i++) {
            ascending[i] = i;
            descending[i] = size - i;
            equal[i] = 5;
        }
        checkQuickSortFinishes("200,000 numbers already sorted", ascending);
        checkQuickSortFinishes("200,000 numbers in reverse order", descending);
        checkQuickSortFinishes("200,000 equal numbers", equal);
    }

    private static void checkQuickSortFinishes(String description, int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        try {
            Principal.sort("QuickSort", input);
            check("QuickSort sorts " + description, Arrays.equals(expected, input));
        } catch (StackOverflowError e) {
            check("QuickSort sorts " + description + " (StackOverflowError)", false);
        }
    }

    private static void unknownAlgorithmIsRejected() {
        try {
            Principal.sort("Bogo Sort", new int[]{2, 1});
            check("an unknown algorithm name is rejected", false);
        } catch (IllegalArgumentException e) {
            check("an unknown algorithm name is rejected", true);
        }
    }

    // ---- Reading and writing numbers ----

    private static void parseAcceptsCommonFormats() {
        checkParse("5,3,8", new int[]{5, 3, 8});
        checkParse("3, 4", new int[]{3, 4});
        checkParse("  10 ,  -2 ,0  ", new int[]{10, -2, 0});
        checkParse("1,2,", new int[]{1, 2});
        checkParse(",1,,2", new int[]{1, 2});
        checkParse("7\n8\r\n9", new int[]{7, 8, 9});
        checkParse("4\t5 6", new int[]{4, 5, 6});
        checkParse("\uFEFF1,2", new int[]{1, 2});
        checkParse("2147483647,-2147483648", new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE});
    }

    private static void checkParse(String text, int[] expected) {
        try {
            check("parse " + visible(text), Arrays.equals(expected, NumberIO.parse(text)));
        } catch (RuntimeException e) {
            check("parse " + visible(text) + " (" + e.getMessage() + ")", false);
        }
    }

    private static void parseRejectsBadInput() {
        checkRejected("", IllegalArgumentException.class, "No hay números");
        checkRejected("   ", IllegalArgumentException.class, "No hay números");
        checkRejected(" , ,", IllegalArgumentException.class, "No hay números");
        checkRejected("1,a,3", NumberFormatException.class, "\"a\"");
        checkRejected("3.5", NumberFormatException.class, "\"3.5\"");
        checkRejected("99999999999", NumberFormatException.class, "\"99999999999\"");
    }

    private static void checkRejected(String text, Class<? extends RuntimeException> type, String inMessage) {
        String name = "parse " + visible(text) + " is rejected";
        try {
            NumberIO.parse(text);
            check(name, false);
        } catch (RuntimeException e) {
            check(name, type.isInstance(e) && e.getMessage().contains(inMessage));
        }
    }

    private static void readUsesEveryLineOfTheFile() throws IOException {
        // The old code kept only the last line of the file
        File file = File.createTempFile("numbers", ".txt");
        try {
            Files.write(file.toPath(), "\uFEFF5,3\n8, 1\n\n2\n".getBytes(StandardCharsets.UTF_8));
            check("read uses every line of the file",
                    Arrays.equals(new int[]{5, 3, 8, 1, 2}, NumberIO.read(file)));
        } finally {
            file.delete();
        }
    }

    private static void writeReplacesTheFile() throws IOException {
        File file = File.createTempFile("sorted", ".txt");
        try {
            int[] sorted = {1, 2, 3};
            // The old code appended: writing twice doubled the content
            NumberIO.write(file, "Merge Sort", sorted);
            NumberIO.write(file, "Merge Sort", sorted);
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            check("write replaces the file instead of appending",
                    lines.equals(Arrays.asList("Ordenado con Merge Sort", "1", "2", "3")));
            // The last number used to be lost by Quick Sort and Merge Sort
            check("written numbers read back the same",
                    Arrays.equals(sorted, NumberIO.parse(String.join(",", lines.subList(1, lines.size())))));
        } finally {
            file.delete();
        }
    }

    // ---- Helpers ----

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static String shorten(String text) {
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }

    /** Shows new lines, tabs and the BOM so the test names stay on one line. */
    private static String visible(String text) {
        return "\"" + text.replace("\uFEFF", "<BOM>").replace("\r", "\\r")
                .replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }
}
