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
public class Objeto {
    
    String nombre,valor;
    int tipo;
    LinkedList<Objeto> atributos;

    public Objeto(String nombre,String valor,int tipo) {
        this.nombre = nombre;
        this.valor = valor;
        this.tipo = tipo;
        this.atributos = new LinkedList<Objeto>();
    }

    public Objeto(String nombre,int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.atributos = new LinkedList<Objeto>();
    }
    
    public void agregarAtributo(Objeto atr){
        atributos.add(atr);
    }
    
    public void setNombre(String n){
        this.nombre = n;
    }
    
    public String getNombre() {
        return nombre;
    }

    public LinkedList<Objeto> getAtributos() {
        return atributos;
    }

    public String getValor() {
        return valor;
    }
    
    public int getTipo() {
        return tipo;
    }
    
}
