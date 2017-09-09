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
    
    String nombre;
    LinkedList<Campo> campos = new LinkedList<Campo>();
    
    public Tabla(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
    
    public LinkedList<Campo> getCampos() {
        return campos;
    }

    public void setCampos(LinkedList<Campo> campos) {
        this.campos = campos;
    }
    
    
    
}
