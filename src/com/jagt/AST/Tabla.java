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
public class Tabla {
    
    String nombre,ruta;
    LinkedList<Campo> campos;
    
    public Tabla(String nombre){
        this.nombre = nombre;
        this.campos = new LinkedList<Campo>();
    }
    
    public Tabla(String nombre,String ruta){
        this.nombre = nombre;
        this.ruta = ruta;
        this.campos = new LinkedList<Campo>();
    }

    public String getNombre() {
        return nombre;
    }
    
    public LinkedList<Campo> getCampos() {
        return campos;
    }

    public void agregarCampo(Campo campo) {
        this.campos.add(campo);
    }

    public String getRuta() {
        return ruta;
    }
    
}
