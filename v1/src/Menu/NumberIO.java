package Menu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Reads the numbers typed by the user or stored in a .txt file, and writes
 * the sorted result. The text field and the file share the same rules.
 */
public final class NumberIO {

    private NumberIO() {
    }

    /**
     * Turns "5, 3,8\n 1" into {5, 3, 8, 1}.
     * Numbers can be separated by commas, spaces, tabs or new lines,
     * so "3, 4" (with a space) works and so does a file with one number per line.
     *
     * @throws IllegalArgumentException if there are no numbers
     * @throws NumberFormatException if a value is not a whole number that fits in an int
     */
    public static int[] parse(String text) {
        // Notepad on Windows can save UTF-8 with an invisible mark (BOM) at the start
        String clean = text.replace("﻿", "").trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("No hay números para ordenar");
        }
        // One or more commas or blanks together count as a single separator
        String[] parts = clean.split("[,\\s]+");
        int[] numbers = new int[parts.length];
        int count = 0;
        for (String part : parts) {
            // A leading comma (",5,3") leaves an empty part: skip it
            if (part.isEmpty()) {
                continue;
            }
            try {
                numbers[count++] = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                // Say which value is wrong instead of Java's generic message
                throw new NumberFormatException("\"" + part + "\" no es un número entero válido"
                        + " (entre " + Integer.MIN_VALUE + " y " + Integer.MAX_VALUE + ")");
            }
        }
        // Only commas, like " , ,", leave nothing to sort
        if (count == 0) {
            throw new IllegalArgumentException("No hay números para ordenar");
        }
        return Arrays.copyOf(numbers, count);
    }

    /**
     * Reads every number in the file, on any number of lines
     * (the old code kept only the last line).
     */
    public static int[] read(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return parse(new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * Writes a header line and then one number per line.
     * The file is replaced, not appended to, so running twice gives the same file.
     */
    public static void write(File file, String algorithm, int[] sorted) throws IOException {
        // try-with-resources closes the file even if writing fails
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("Ordenado con " + algorithm);
            writer.newLine();
            for (int number : sorted) {
                writer.write(String.valueOf(number));
                writer.newLine();
            }
        }
    }
}
