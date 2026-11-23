package algoscope;

import java.util.Arrays;
import java.util.Objects;

/**
 * A tiny test helper so the project needs nothing but the JDK.
 * Each check prints PASS or FAIL; {@link AllTests} exits with code 1 if any failed.
 */
public final class Check {

    private static int passed = 0;
    private static int failed = 0;

    private Check() {
    }

    /**
     * Runs a group of checks under a title. If the code under test throws, the group
     * counts as one FAIL and the next groups still run, instead of stopping everything.
     */
    public static void group(String title, Runnable checks) {
        System.out.println();
        System.out.println("== " + title);
        try {
            checks.run();
        } catch (Throwable e) {
            isTrue(title + " crashed: " + e, false);
        }
    }

    public static void isTrue(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    public static void equal(String name, Object expected, Object actual) {
        boolean same = Objects.equals(expected, actual);
        isTrue(same ? name : name + "  (expected " + expected + ", got " + actual + ")", same);
    }

    public static void sameArray(String name, int[] expected, int[] actual) {
        boolean same = Arrays.equals(expected, actual);
        isTrue(same ? name : name + "  (expected " + shorten(expected) + ", got " + shorten(actual) + ")", same);
    }

    /** Passes if running the code throws an exception of the given type. */
    public static void throwsException(String name, Class<? extends Throwable> type, Runnable code) {
        try {
            code.run();
            isTrue(name + "  (nothing was thrown)", false);
        } catch (Throwable e) {
            isTrue(type.isInstance(e) ? name : name + "  (threw " + e + ")", type.isInstance(e));
        }
    }

    public static int failed() {
        return failed;
    }

    public static int passed() {
        return passed;
    }

    private static String shorten(int[] values) {
        String text = Arrays.toString(values);
        return text.length() > 80 ? text.substring(0, 77) + "..." : text;
    }
}
