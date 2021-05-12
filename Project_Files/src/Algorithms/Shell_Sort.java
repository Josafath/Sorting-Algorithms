/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author Tec
 */
public class Shell_Sort {
    public int[]  ordenar(int arr[])
    {
        int n = arr.length;
  
        // Start with a big gap, then reduce the gap
        for (int gap = n/2; gap > 0; gap /= 2)
        {
            // Do a gapped insertion sort for this gap size.
            // The first gap elements a[0..gap-1] are already
            // in gapped order keep adding one more element
            // until the entire array is gap sorted
            for (int i = gap; i < n; i += 1)
            {
                // add a[i] to the elements that have been gap
                // sorted save a[i] in temp and make a hole at
                // position i
                int temp = arr[i];
  
                // shift earlier gap-sorted elements up until
                // the correct location for a[i] is found
                int j;
                for (j = i; j >= gap && arr[j - gap] > temp; j -= gap)
                    arr[j] = arr[j - gap];
  
                // put temp (the original a[i]) in its correct
                // location
                arr[j] = temp;
            }
        }
        return arr;
    }
    public String printArray(int[] array,int size){
        return Arrays.toString(array);
    }
    public void writeArray(int[] arr, int size) throws IOException {
        String user= System.getProperty("user.name");
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\"+user+"\\Desktop\\Shell_Sort.txt", true));
        writer.write("Ordenado con Shell Sort");
        writer.append("\n");
        for (int i = 0; i < size; i++) {
            writer.write(String.valueOf(arr[i]));
            writer.append("\n");
        }
        writer.close();
        JOptionPane.showMessageDialog(null, "El archivo ordenado está en el Escritorio");
    }
}
