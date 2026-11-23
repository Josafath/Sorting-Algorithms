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
public class Insertion_Sort {
    // One array per object (it used to be static, so every Insertion_Sort shared the last array given)
    private final int[] array;

    public Insertion_Sort(int[] n) {
        array = n;
    }

    public int[] ordenar_insertion()
    {
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            int key = array[i];
            int j = i - 1;
            /* Move elements of arr[0..i-1], that are
               greater than key, to one position ahead
               of their current position */
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j = j - 1;
            }
            array[j + 1] = key;
        }
        return array;
    }

    public String toString() {
        return Arrays.toString(array);
    }
}
