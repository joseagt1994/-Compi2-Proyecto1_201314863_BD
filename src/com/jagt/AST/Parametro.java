/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.Logica.SistemaBaseDatos;

/**
 *
 * @author Jose Antonio
 */
public class Parametro {
    
    public String nombre,objeto;
    int tipo;

    public Parametro(String nombre) {
        this.nombre = nombre;
    }
    
    public Parametro(String nombre, int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getTipo() {
        return tipo;
    }
    
    public void guardarTipo(NodoParser nodo){
        switch(nodo.valor()){
            case "entero":
                tipo = SistemaBaseDatos.ENTERO;
                break;
            case "doble":
                tipo = SistemaBaseDatos.DOBLE;
                break;
            case "cadena":
                tipo = SistemaBaseDatos.TEXTO;
                break;
            case "boolean":
                tipo = SistemaBaseDatos.BOOL;
                break;
            case "fecha":
                tipo = SistemaBaseDatos.DATE;
                break;
            case "fechahora":
                tipo = SistemaBaseDatos.DATETIME;
                break;
            default:
                tipo = SistemaBaseDatos.OBJETO;
                break;
        }
    }
    
}
