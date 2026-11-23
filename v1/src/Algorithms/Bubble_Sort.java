/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import java.util.Arrays;

/**
 *
 * @author Tec
 */
public class Bubble_Sort {
    // One array per object (it used to be static, so every Bubble_Sort shared the last array given)
    private final int[] array;

    public Bubble_Sort(int[] n) {
        array = n;
    }

    public void swap(int indexOne, int indexTwo) {
        /*
         * Devolver el cambio de elementos tomando como paramatros
         * los índices de estos
         */
        int temp = array[indexOne];
        array[indexOne] = array[indexTwo];
        array[indexTwo] = temp;
    }

    public int[] ordenar() {
        // Each pass compares neighbours and pushes the biggest value left to the end,
        // so after pass p the last p elements are already in their final place
        for (int pass = 0; pass < array.length - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < array.length - 1 - pass; j++) {
                if (array[j] > array[j + 1]) {
                    swap(j, j + 1);
                    swapped = true;
                }
            }
            // A pass without swaps means the array is sorted: stop early (best case O(n))
            if (!swapped) {
                break;
            }
        }
        // Worst and average case O(n^2)
        return array;
    }

    public String toString() {
        return Arrays.toString(array);
    }
}
