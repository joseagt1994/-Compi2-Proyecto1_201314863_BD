/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class NodoParser {
    
    private final String valor,nombre;
    public final int linea,columna;
    private final LinkedList<NodoParser> nodosHijos;
    public int id;
    
    public NodoParser(String nombre,String valor,int linea,int columna){
        this.nombre = nombre;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
        nodosHijos = new LinkedList<NodoParser>();
    }
    
    public LinkedList<NodoParser> hijos(){
        return nodosHijos;
    }
    
    public String nombre(){
        return nombre;
    }
    
    public String valor(){
        return valor;
    }
    
}
