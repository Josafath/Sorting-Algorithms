package algoscope.sorting;

import java.util.Random;

/**
 * The kinds of input the user can pick. Each algorithm has a best and a worst
 * kind, and seeing them side by side is the point of the app: Insertion Sort
 * flies on sorted input, Selection Sort does not care, and so on.
 * <p>
 * Values go from 1 to size, so they can be drawn as bars of that height.
 */
public enum InputKind {

    RANDOM("Random") {
        @Override
        int[] fill(int size, Random random) {
            int[] values = SORTED.fill(size, random);
            // Fisher–Yates shuffle: every order is equally likely
            for (int i = size - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = values[i];
                values[i] = values[j];
                values[j] = temp;
            }
            return values;
        }
    },

    SORTED("Already sorted") {
        @Override
        int[] fill(int size, Random random) {
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = i + 1;
            }
            return values;
        }
    },

    REVERSED("Reversed") {
        @Override
        int[] fill(int size, Random random) {
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = size - i;
            }
            return values;
        }
    },

    FEW_UNIQUE("Few unique values") {
        @Override
        int[] fill(int size, Random random) {
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                // Only 4 different heights: 1/4, 2/4, 3/4 and 4/4 of the size
                int level = random.nextInt(LEVELS) + 1;
                values[i] = Math.max(1, size * level / LEVELS);
            }
            return values;
        }
    };

    /** How many different values FEW_UNIQUE uses. */
    static final int LEVELS = 4;

    private final String label;

    InputKind(String label) {
        this.label = label;
    }

    /** The text shown in the menu. */
    public String label() {
        return label;
    }

    /**
     * Creates an input of this kind.
     *
     * @param seed the same seed always gives the same array, so a run can be repeated
     */
    public int[] generate(int size, long seed) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative: " + size);
        }
        return fill(size, new Random(seed));
    }

    abstract int[] fill(int size, Random random);
}
