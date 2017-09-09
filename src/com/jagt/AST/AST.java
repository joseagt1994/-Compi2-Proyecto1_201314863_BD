/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jose Antonio
 */
public class AST {
    
    public NodoParser inicio;
    ArrayList<String> arbol = new ArrayList();
    public int c = 0;
    
    public AST(){
    }
    
    public void graficar(NodoParser n){
        
        generarNodos(n);
        enlazarNodos(n);
        try {
            generarArchivo(n.valor()+".txt");
            graficar(n.nombre());
        } catch (IOException ex) {
            Logger.getLogger(AST.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void generarNodos(NodoParser raiz){
        if(raiz!=null)
       {
           String tempo = "";
           if(raiz.valor().equals("")){
               tempo = raiz.nombre();
           }else{
                String[] caracteres = raiz.valor().split("");
                String tempo2 = "";
                for(int i = 0; i<caracteres.length; i++){
                    if(!caracteres[i].equals("\"")){
                        tempo2+=caracteres[i];
                    }
                }
               tempo = raiz.nombre()+" "+tempo2;
           }
           
           System.out.println("nodo"+c+"[label=\""+tempo+"\"];");
           arbol.add("nodo"+c+"[label=\""+tempo+"\"];");
           raiz.id=c;
           c++;
           if(raiz.hijos()!=null)
           {
               for(NodoParser temp: raiz.hijos())
               {
                   if(temp!=null)
                   {
                       generarNodos(temp);
                   }
               }
           }
       }    
    }
    
    public void enlazarNodos(NodoParser raiz)
   {
       if(raiz!=null)
       {
           if(raiz.hijos()!=null)
           {
               for(NodoParser temp: raiz.hijos())
               {
                   if(temp!=null)
                   {
                       arbol.add("nodo"+raiz.id+"->"+"nodo"+temp.id+";"+"\n");
                       //grafo
                       enlazarNodos(temp);
                   }
               }
           }
       }    
   }
   
    public void generarArchivo(String nombre) throws IOException
   {
       FileWriter fichero=null;
       PrintWriter escritura=null;
       try
       {
           fichero= new FileWriter("C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\Imagenes\\"+nombre);
           escritura = new PrintWriter(fichero);
           escritura.printf("digraph grafica {\n");
           for(int i=0;i<arbol.size();i++)
           {
               escritura.print(arbol.get(i)+"\n");
           }
           escritura.printf("}");
       }catch(Exception e)
       {
           System.out.println(""+e.getMessage());
       }
       finally
       {
           try
           {
               if(fichero!=null)
                fichero.close();
           }catch(Exception e2)
           {
               System.out.println(""+e2.getMessage());
           }
       }
   }
    
    public void graficar(String nombre){
            try {
                String dotPath = "C:\\Graphviz\\bin\\dot.exe";
                String fileInputPath = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\Imagenes\\"+nombre+".txt";
                String fileOutputPath = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\Imagenes\\"+nombre+".jpg";
                String tParam="-Tjpg";
                String tOParam = "-o";
                String [] cmd= new String [5];
                cmd[0]= dotPath;
                cmd[1]= tParam;
                cmd[2]= fileInputPath;
                cmd[3]=tOParam;
                cmd[4]=fileOutputPath;
                Runtime rt = Runtime.getRuntime();
                rt.exec(cmd);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
    }
    
}
