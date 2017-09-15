/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.Logica.Registro;
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
    
    // verificaciones a la hora de insertar
    public int getNumeroCampos(){
        int num = 0;
        for(Campo c : campos){
            if(!c.isAutoincrementable()){
                num++;
            }
        }
        return num;
    }
    
    // verificacion de los tipos entre campos y datos ingresados
    public boolean comprobarTipos(LinkedList<Objeto> datos){
        int indice = 0;
        for(Campo c : campos){
            if(c.isAutoincrementable()){
                continue;
            }
            // Comparar el tipo del campo con el tipo de dato
            if(c.getTipo() != datos.get(indice).getTipo()){
                return false;
            }
            indice++;
        }
        return true;
    }
    
    // obtener el indice de la primaria si existe
    public int getIndicePrimaria(){
        for(int i = 0; i < campos.size(); i++){
            Campo c = campos.get(i);
            if(c.isPrimaria()){
                return i;
            }
        }
        return -1;
    }
    
    // verificar que los campos existen y son del tipo que se pide
    public boolean verificarCampos(LinkedList<String> columnas,Registro nuevo){
        for(int i = 0; i < columnas.size(); i++){
            int tipo = existeCampo(columnas.get(i));
            if(tipo == -1 || tipo != nuevo.getColumnas().get(i).getTipo()){
                return false;
            }
        }
        return true;
    }
    
    // verificar que el campo existe
    public int existeCampo(String nombre){
        for(Campo c : campos){
            if(c.getNombre().equals(nombre)){
                return c.getTipo();
            }
        }
        return -1;
    }
    
    // determina si el campo es faltante
    public boolean esFaltante(String nombre,LinkedList<String> columnas){
        for(String col : columnas){
            if(col.equals(nombre)){
                return false;
            }
        }
        return true;
    }
    
    // get indice del campo auxiliar 
    public int buscarIndice(String nombre,LinkedList<String> columnas){
        for(int i = 0; i < columnas.size(); i++){
            if(columnas.get(i).equals(nombre)){
                return i;
            }
        }
        return -1;
    }
}
