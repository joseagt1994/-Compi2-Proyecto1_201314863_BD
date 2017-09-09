/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Logica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Jose Antonio
 */
public class Archivo {
    File archivo;
    FileReader lector;
    FileWriter escritor;
    
    public String leer(String ruta){
        
        try{
            archivo = new File(ruta);
            lector = new FileReader(archivo);
            BufferedReader br = new BufferedReader(lector);
            
                String linea;
                String codigo="";
                while(true){
                    linea = br.readLine();
                    if(linea!=null){
                        codigo+=linea+"\n";
                    }else{
                        break;
                    }
                }
                br.close();
                lector.close();
                return codigo;
        }catch(Exception ex){
            System.out.println("Error: "+ex.getMessage());
            return null;
        }
    }
    
    public void escribir(String nombre,String texto){
        
        try{
            archivo = new File(nombre);
            escritor = new FileWriter(archivo);
            BufferedWriter bw = new BufferedWriter(escritor);
            PrintWriter imprimir = new PrintWriter(bw);
            
            imprimir.write(texto+"\n");
            
            imprimir.close();
            bw.close();
            escritor.close();
            
        }catch(IOException e){
            System.out.println("Error: "+e.getMessage());
        }
        
    }
    
    public void modificar(String ruta,String texto){
        
        try {
            Files.write(Paths.get(ruta), texto.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        
    }
}
