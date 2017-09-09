/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Logica;

import com.jagt.AST.*;
import com.jagt.GUI.Servidor;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class BaseDatos {
    
    public BaseDatos(){}
    
    // Metodos de la base de datos!
    /************************************************************/
    /***************************** DDL **************************/
    /************************************************************/
    
    /**************************** CREAR *************************/
    public void crearUsuario(String nombre,String password){
        Archivo master = new Archivo();
        String listado = master.leer(Servidor.rutaUsuarios);
        // Analizar archivo maestro
        LinkedList<Usuario> usuarios = new LinkedList<Usuario>();
        usuarios.add(Servidor.logueado);
        Servidor.codUsuario = usuarios.getLast().getCodigo()+1;
        if(!existeUsuario(nombre,usuarios)){
            // Se escribe en el archivo usuarios.usac
            String nuevo = "<usuario id=\""+Servidor.codUsuario+"\">\n" +
                           "\t<nombre>"+nombre+"</nombre>\n" +
                           "\t<password>"+password+"</password>\n" +
                           "</usuario>\n";
            master.modificar(Servidor.rutaUsuarios, nuevo);
        }else{
            //Error! El usuario ya existe!
        }
    }
    
    public boolean existeUsuario(String nombre,LinkedList<Usuario> usuarios){
        for(Usuario u : usuarios){
            if(u.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearBD(String nombreBD,int usuario){
        Archivo master = new Archivo();
        String bases = master.leer(Servidor.rutaMaestra);
        // Analizar archivo maestro
        LinkedList<String> basesDatos = new LinkedList<String>();
        if(!existeBD(nombreBD,basesDatos)){
            // Se escribe en el archivo master.usac
            String rutaBD = Servidor.rutaBDS+nombreBD+".usac";
            String nueva = "<DB>\n"+
                           "\t<nombre>"+nombreBD+"</nombre>\n" +
                           "\t<path>"+rutaBD+"</path>\n" +
                           "\t<permisos>\n" +
                           "\t\t<usuario>"+Servidor.logueado.getCodigo()+"</usuario>\n" + 
                           "\t</permisos>\n" +
                           "</DB>\n";
            master.modificar(Servidor.rutaMaestra, nueva);
            // Se crean los archivos necesarios --> bd.usac, objeto_bd.usac, metodo_bd.usac
            String rutaObj = Servidor.rutaBDS+"obj_"+nombreBD+".usac";
            String rutaMetodo = Servidor.rutaBDS+"proc_"+nombreBD+".usac";
            master.escribir(rutaObj, "");
            master.escribir(rutaMetodo, "");
            String cuerpoBD = "<Procedure>\n"+
                              "\t<path>"+rutaMetodo+"</path>\n" + 
                              "</Procedure>\n" + 
                              "<Object>\n" +
                              "\t<path>"+rutaObj+"</path>\n" +
                              "</Object>\n";
            master.escribir(rutaBD, cuerpoBD);
        }else{
            // Error! La base de datos ya existe!
        }
    }
    
    public boolean existeBD(String nombre,LinkedList<String> bds){
        for(String bd : bds){
            if(bd.equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearTabla(Tabla tabla){
        // Se escribe en la base de datos seleccionada, bd.usac
        
    }
    
    public void crearObjeto(){
        // Se escribe en objeto_bd.usac
    }
    
    public void crearMetodo(){
        // Se escribe en metodo_bd.usac
    }
    
}
