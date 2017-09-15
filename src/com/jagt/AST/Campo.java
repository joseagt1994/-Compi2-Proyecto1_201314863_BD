/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

/**
 *
 * @author Jose Antonio
 */
public class Campo {
    
    String nombre,tforanea,objeto;
    int tipo;
    boolean nulo,autoincrementable,primaria,foranea,unica;

    public Campo(String nombre, int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.nulo = this.autoincrementable = this.primaria = this.foranea = this.unica = false;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public void setNulo(boolean nulo) {
        this.nulo = nulo;
    }

    public void setAutoincrementable(boolean autoincrementable) {
        this.autoincrementable = autoincrementable;
    }

    public void setPrimaria(boolean primaria) {
        this.primaria = primaria;
        this.nulo = false;
        this.unica = true;
    }

    public void setForanea(String tabla) {
        this.foranea = true;
        this.tforanea = tabla;
    }

    public void setUnica(boolean unica) {
        this.unica = unica;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTforanea() {
        return tforanea;
    }

    public int getTipo() {
        return tipo;
    }

    public boolean isNulo() {
        return nulo;
    }

    public boolean isAutoincrementable() {
        return autoincrementable;
    }

    public boolean isPrimaria() {
        return primaria;
    }

    public boolean isForanea() {
        return foranea;
    }

    public boolean isUnica() {
        return unica;
    }
    
    public String getComplementos(){
        String complementos = "";
        if(nulo){
            complementos += "nulo ";
        }
        if(autoincrementable){
            complementos += "auto ";
        }
        if(primaria){
            complementos += "primaria ";
        }
        if(unica){
            complementos += "unica ";
        }
        return complementos;
    }
}
