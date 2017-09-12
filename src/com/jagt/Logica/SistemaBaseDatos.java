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
public class SistemaBaseDatos {
    
    /****************** ESTATICAS *******************************/
    public static final int ENTERO = 0;
    public static final int DOBLE = 1;
    public static final int TEXTO = 2;
    public static final int BOOL = 3;
    public static final int DATE = 4;
    public static final int DATETIME = 5;
    public static final int OBJETO = 6;
    
    /****************** LISTA DE ATRIBUTOS **********************/
    public LinkedList<Usuario> usuarios;
    public LinkedList<DataBase> basesDatos;
    public LinkedList<Tabla> tablas;
    public LinkedList<Objeto> objetos;
    public LinkedList<Metodo> metodos;
    public LinkedList<Variable> variables;
    
    // Unica instancia de SistemaBaseDatos
    private static SistemaBaseDatos BD = null;
    
    private SistemaBaseDatos(){
        usuarios = new LinkedList<Usuario>();
        basesDatos = new LinkedList<DataBase>();
        tablas = new LinkedList<Tabla>();
        objetos = new LinkedList<Objeto>();
        metodos = new LinkedList<Metodo>();
        variables = new LinkedList<Variable>();
    }
    
    public static SistemaBaseDatos getInstance(){
        if(BD == null){
            BD = new SistemaBaseDatos();
        }
        return BD;
    }
    
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
        Archivo master = new Archivo();
        String bd = master.leer(Servidor.rutaBDS+Servidor.bd_actual+".usac");
        String rutaTabla = Servidor.rutaBDS+tabla.getNombre()+"_"+Servidor.bd_actual+".usac";
        master.escribir(rutaTabla, "");
        LinkedList<String> tablas = new LinkedList<String>();
        if(!existeTabla(tabla.getNombre(),tablas)){
            String nueva = "<Tabla>\n" +
                           "\t<nombre>"+tabla.getNombre()+"</nombre>\n" +
                           "\t<path>"+rutaTabla+"</path>\n" +
                           "\t<rows>";
            for(Campo c : tabla.getCampos()){
                nueva += "\t\t<campo ";
                // Agregar complementos
                nueva += "complementos=\""+c.getComplementos()+"\" >\n";
                // Agregar nombre del campo y tipo
                nueva += "\t\t\t<"+c.getTipo()+">"+c.getNombre()+"</"+c.getTipo()+">\n";
                nueva += "\t\t</campo>\n";
            }
            nueva += "\t</rows>\n" +
                     "</Tabla>\n";
            master.modificar(Servidor.rutaBDS+Servidor.bd_actual+".usac", nueva);
        }else{
            // Error! La tabla ya existe en la base de datos!
        }
    }
    
    public boolean existeTabla(String nombre,LinkedList<String> tablas){
        for(String tabla : tablas){
            if(tabla.equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearObjeto(Objeto objeto){
        // Se escribe en obj_bd.usac
        Archivo master = new Archivo();
        String rutaTabla = Servidor.rutaBDS+"objeto_"+Servidor.bd_actual+".usac";
        LinkedList<String> objetos = new LinkedList<String>();
        if(!existeTabla(objeto.getNombre(),objetos)){
            String nueva = "<Obj>\n" +
                           "\t<nombre>"+objeto.getNombre()+"</nombre>\n" +
                           "\t<attr>\n";
            for(Objeto a : objeto.getAtributos()){
                // Agregar nombre del atributo y tipo
                //nueva += "\t\t<"+a.getTipo()+">"+a.getNombre()+"</"+a.getTipo()+">\n";
            }
            nueva += "\t</attr>\n" +
                     "</Obj>\n";
            master.modificar(Servidor.rutaBDS+Servidor.bd_actual+".usac", nueva);
        }else{
            // Error! La tabla ya existe en la base de datos!
        }
    }
    
    public boolean existeObjeto(String nombre,LinkedList<String> objs){
        for(String obj : objs){
            if(obj.equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearMetodo(){
        // Se escribe en proc_bd.usac
        
    }
    
    public void crearVariable(){
        // Se escribe en vars_bd.usac
        
    }
    
    /**************************** USAR *************************/
    public boolean usarBD(String nombre){
        Archivo master = new Archivo();
        String bases = master.leer(Servidor.rutaMaestra);
        // Analizar archivo maestro
        LinkedList<String> basesDatos = new LinkedList<String>();
        if(existeBD(nombre,basesDatos)){
            return true;
        }else{
            return false;
        }
    }
    
    /**************************** ALTERAR *************************/
    
    /**************************** ELIMINAR *************************/
    
    /************************************************************/
    /***************************** DML **************************/
    /************************************************************/
    
    /**************************** INSERTAR *************************/
    
    /**************************** ACTUALIZAR *************************/
    
    /**************************** BORRAR *************************/
    
    /**************************** SELECCIONAR *************************/
    
    /************************************************************/
    /***************************** DCL **************************/
    /************************************************************/
    
    /**************************** OTORGAR *************************/
    
    /**************************** DENEGAR *************************/
    
}
