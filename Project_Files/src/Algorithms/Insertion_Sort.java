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
public class Insertion_Sort {
    private static int [] array;
    public Insertion_Sort(int[] n) {
	array = n;
    }
    public static int[] ordenar_insertion()
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
    public String toString(){
        return Arrays.toString(array); 
    }
    public void writeArray(int[] arr, int size) throws IOException {
        String user= System.getProperty("user.name");
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\"+user+"\\Desktop\\Insertion_Sort.txt", true));
        writer.write("Ordenado con Insertion Sort");
        writer.append("\n");
        for (int i = 0; i < size; i++) {
            writer.write(String.valueOf(arr[i]));
            writer.append("\n");
        }
        writer.close();
        JOptionPane.showMessageDialog(null, "El archivo ordenado está en el Escritorio");
    }
}
