/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

/**
 *
 * @author Tec
 */
public class Quick_Sort {
    public void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

/* Hoare partition. The pivot is the middle element: the last element
   made already sorted input the worst case, with one recursive call per
   element (StackOverflowError with ~50,000 numbers).
   Two indexes walk towards each other and swap the pairs that are on the
   wrong side, so equal values are split evenly between both halves.
   Returns j: every element of arr[low..j] is <= every element of arr[j+1..high] */
    public int partition(int[] arr, int low, int high) {
        // pivot (written this way so low + high can never overflow)
        int pivot = arr[low + (high - low) / 2];
        int i = low - 1;
        int j = high + 1;

        while (true) {
            // Find an element on the left that should go to the right
            do {
                i++;
            } while (arr[i] < pivot);
            // Find an element on the right that should go to the left
            do {
                j--;
            } while (arr[j] > pivot);
            // The indexes crossed: the range is split
            if (i >= j) {
                return j;
            }
            swap(arr, i, j);
        }
    }

/* The main function that implements QuickSort
          arr[] --> Array to be sorted,
          low --> Starting index,
          high --> Ending index
 */
    public void quickSort(int[] arr, int low, int high) {
        while (low < high) {
            int pi = partition(arr, low, high);
            // Recurse only into the smaller half and loop over the bigger one,
            // so the recursion depth stays O(log n) whatever the input looks like
            if (pi - low < high - pi) {
                quickSort(arr, low, pi);
                low = pi + 1;
            } else {
                quickSort(arr, pi + 1, high);
                high = pi;
            }
        }
    }
}
