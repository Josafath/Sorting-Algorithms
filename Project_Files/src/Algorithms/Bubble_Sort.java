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
public class Bubble_Sort {
    private static int [] array;
    public Bubble_Sort(int[] n) {
	array = n;
    }
    public static void swap(int indexOne,int indexTwo){
	/* 
        * Devolver el cambio de elementos tomando como paramatros
	*  los índices de estos
        *  */
	int temp = array[indexOne];
        array[indexOne] = array[indexTwo];
	array[indexTwo] = temp;
	}
    public static int[] ordenar() {
        for(int i = 0; i< array.length; i++)  // n
        {
            for(int j = i+1;j<array.length; j++) // n 
            {
                if(array[i] > array[j]){
                    swap(i,j);
                }
            }
            // O(n^2)
        }
        return array;
    }
    public String toString(){
        return Arrays.toString(array); 
    }
    public void writeArray(int[] arr, int size) throws IOException {
        String user= System.getProperty("user.name");
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\"+user+"\\Desktop\\Bubble_Sort.txt", true));
        writer.write("Ordenado con Bubble Sort");
        writer.append("\n");
        for (int i = 0; i < size; i++) {
            writer.write(String.valueOf(arr[i]));
            writer.append("\n");
        }
        writer.close();
        JOptionPane.showMessageDialog(null, "El archivo ordenado está en el Escritorio");
    }
}
