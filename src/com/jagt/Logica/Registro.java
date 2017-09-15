/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Logica;

import com.jagt.AST.Objeto;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class Registro {
    
    private int numero;
    private LinkedList<Objeto> columnas;
    
    public void agregarColumna(Objeto v){
        columnas.add(v);
    }

    public Registro() {
        columnas = new LinkedList<Objeto>();
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LinkedList<Objeto> getColumnas() {
        return columnas;
    }

    public void setColumnas(LinkedList<Objeto> columnas) {
        this.columnas = columnas;
    }
    
    public void modificarColumna(String nombre,Objeto objeto){
        objeto.setNombre(nombre);
        for(int i = 0; i < columnas.size(); i++){
            if(columnas.get(i).getNombre().equals(nombre)){
                columnas.remove(i);
                columnas.add(i, objeto);
                break;
            }
        }
    }
    
}
