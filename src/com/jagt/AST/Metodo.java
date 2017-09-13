/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.Logica.SistemaBaseDatos;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class Metodo {
    
    private String nombre;
    private LinkedList<Parametro> parametros;
    private int tipo = -1;
    private NodoParser instrucciones;
    private String textoInstrucciones;

    public String getTextoInstrucciones() {
        return textoInstrucciones;
    }

    public void setTextoInstrucciones(String textoInstrucciones) {
        this.textoInstrucciones = textoInstrucciones;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LinkedList<Parametro> getParametros() {
        return parametros;
    }

    public void setParametros(LinkedList<Parametro> parametros) {
        this.parametros = parametros;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public NodoParser getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(NodoParser instrucciones) {
        this.instrucciones = instrucciones;
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
