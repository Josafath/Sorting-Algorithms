/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import java.io.*;
import java.util.Arrays;
import javax.swing.JOptionPane;

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
 
/* This function takes last element as pivot, places
   the pivot element at its correct position in sorted
   array, and places all smaller (smaller than pivot)
   to left of pivot and all greater elements to right
   of pivot */
    public int partition(int[] arr, int low, int high) {
        // pivot
        int pivot = arr[high];
     
        // Index of smaller element and
        // indicates the right position
        // of pivot found so far
        int i = (low - 1);
 
        for(int j = low; j <= high - 1; j++)
        {     
            // If current element is smaller
            // than the pivot
            if (arr[j] < pivot){
                // Increment index of
                // smaller element
                i++;
                swap(arr, i, j);
            }
        }   
        swap(arr, i + 1, high);
        return (i + 1);
    }
 
/* The main function that implements QuickSort
          arr[] --> Array to be sorted,
          low --> Starting index,
          high --> Ending index
 */
    public void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // pi is partitioning index, arr[p]
            // is now at right place
            int pi = partition(arr, low, high); 
            // Separately sort elements before
            // partition and after partition
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }   
    }
    public String printArray(int[] array,int size){
        return Arrays.toString(array);
    }
    // Function to print an array
    public void writeArray(int[] arr, int size) throws IOException {
        String user= System.getProperty("user.name");
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\"+user+"\\Desktop\\Quick_Sort.txt", true));
        writer.write("Ordenado con Quick Sort");
        writer.append("\n");
        for (int i = 0; i < size; i++) {
            System.out.println(arr[i]);
            writer.write(String.valueOf(arr[i]));
            writer.append("\n");
        }
        writer.close();
        JOptionPane.showMessageDialog(null, "El archivo ordenado está en el Escritorio");
    }   
}
