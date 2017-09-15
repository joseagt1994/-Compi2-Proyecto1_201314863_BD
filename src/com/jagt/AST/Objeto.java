/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.Logica.SistemaBaseDatos;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class Objeto {
    
    public int tipo;
    // Atributos para el manejo de expresiones!
    public int numero;
    public double decimal;
    public boolean bool;
    public String texto;
    protected Date fecha;
    
    // Atributos para el manejo de objetos!
    String nombre;
    LinkedList<Objeto> atributos;
    
    public String tabla = "";

    // Constructor para numeros enteros
    public Objeto(int numero){
        this.numero = numero;
        this.texto = Integer.toString(numero);
        this.tipo = SistemaBaseDatos.ENTERO;
    }

    // Constructor para numeros decimales
    public Objeto(double decimal){
        this.decimal = decimal;
        this.texto = Double.toString(decimal);
        this.tipo = SistemaBaseDatos.DOBLE;
    }
    
    // Constructor para booleanos
    public Objeto(boolean bool){
        this.bool = bool;
        if(bool){
            this.texto = "true";
        }else{
            this.texto = "false";
        }
        this.tipo = SistemaBaseDatos.BOOL;
    }
    
    // Constructor para cadenas
    public Objeto(String texto){
        this.texto = texto;
        this.tipo = SistemaBaseDatos.TEXTO;
    }
    
    // Constructor para fechas (date,datetime)
    public Objeto(int tipo,String fecha){
        this.texto = fecha;
        SimpleDateFormat ft;
        if(tipo == SistemaBaseDatos.DATE){
            ft = new SimpleDateFormat ("dd-MM-yyyy");
        }else{
            ft = new SimpleDateFormat ("dd-MM-yyyy hh:mm:ss");
        }
        try {
           this.fecha = ft.parse(fecha);
        }catch (ParseException e) {
            this.fecha = new Date();
            System.out.println("Unparseable using " + ft); 
        }
        this.tipo = tipo;
    }
    
    // Obtener fecha
    public String getFecha(){
        SimpleDateFormat formato;
        if(tipo == SistemaBaseDatos.DATE){
            formato = new SimpleDateFormat ("dd-MM-yyyy");
        }else{
            formato = new SimpleDateFormat ("dd-MM-yyyy hh:mm:ss");
        }
        return formato.format(fecha);
    }
    
    // Constructor para objetos
    public Objeto(String nombre,int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.atributos = new LinkedList<Objeto>();
    }
    
    // Agregar un atributo al objeto
    public void agregarAtributo(Objeto atr){
        atributos.add(atr);
    }
    
    // Colocar un nombre al objeto
    public void setNombre(String n){
        this.nombre = n;
    }
    
    // Obtener el nombre del objeto
    public String getNombre() {
        return nombre;
    }

    // Obtener los atributos del objeto
    public LinkedList<Objeto> getAtributos() {
        return atributos;
    }
    
    // Obtener el tipo de la clase Objeto.. INT,DOUBLE,BOOL,DATE,DATETIME,TEXT
    public int getTipo() {
        return tipo;
    }
    
    public void modificarAtributo(String nombre,Objeto objeto){
        objeto.setNombre(nombre);
        for(int i = 0; i < atributos.size(); i++){
            if(atributos.get(i).getNombre().equals(nombre)){
                atributos.remove(i);
                atributos.add(i, objeto);
                break;
            }
        }
    }
    
}
