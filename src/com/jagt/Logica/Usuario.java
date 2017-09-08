/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Logica;

/**
 *
 * @author Jose Antonio
 */
public class Usuario {
    
    private int codigo;
    private String nombre,contrasenia;

    public Usuario(int codigo, String nombre, String contrasenia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.contrasenia = contrasenia;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    
}
