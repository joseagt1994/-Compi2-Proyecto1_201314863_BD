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
public class DataBase {
    
    private String nombre,ruta;
    private LinkedList<String> permisos;

    public DataBase() {
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public void setPermisos(LinkedList<String> permisos) {
        this.permisos = permisos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRuta() {
        return ruta;
    }

    public LinkedList<String> getPermisos() {
        return permisos;
    }
    
}
