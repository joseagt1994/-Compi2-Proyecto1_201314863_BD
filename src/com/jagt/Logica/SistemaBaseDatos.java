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
    //public LinkedList<Variable> variables;
     Archivo master = new Archivo();
     public static String textoCompilado = "";
    
    // Unica instancia de SistemaBaseDatos
    private static SistemaBaseDatos BD = null;
    
    private SistemaBaseDatos(){
        llenarUsuarios();
        basesDatos = new LinkedList<DataBase>();
        tablas = new LinkedList<Tabla>();
        objetos = new LinkedList<Objeto>();
        metodos = new LinkedList<Metodo>();
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
        // Analizar archivo maestro
        Servidor.codUsuario = usuarios.getLast().getCodigo()+1;
        if(!existeUsuario(nombre)){
            // Se escribe en el archivo usuarios.usac
            String nuevo = "<usuario id=\""+Servidor.codUsuario+"\">\n" +
                           "\t<nombre>"+nombre+"</nombre>\n" +
                           "\t<password>"+password+"</password>\n" +
                           "</usuario>\n";
            master.modificar(Servidor.rutaUsuarios, nuevo);
            llenarUsuarios();
        }else{
            //Error! El usuario ya existe!
        }
    }
    
    public boolean existeUsuario(String nombre){
        for(Usuario u : usuarios){
            if(u.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearBD(String nombreBD,int usuario){
        // Analizar archivo maestro
        if(!existeBD(nombreBD)){
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
            llenarBases();
        }else{
            // Error! La base de datos ya existe!
        }
    }
    
    public boolean existeBD(String nombre){
        for(DataBase bd : basesDatos){
            if(bd.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearTabla(Tabla tabla){
        // Se escribe en la base de datos seleccionada, bd.usac
        String rutaTabla = Servidor.rutaBDS+tabla.getNombre()+"_"+Servidor.bd_actual+".usac";
        master.escribir(rutaTabla, "");
        LinkedList<String> tablas = new LinkedList<String>();
        if(!existeTabla(tabla.getNombre())){
            String nueva = "<Tabla>\n" +
                           "\t<nombre>"+tabla.getNombre()+"</nombre>\n" +
                           "\t<path>"+rutaTabla+"</path>\n" +
                           "\t<rows>";
            for(Campo c : tabla.getCampos()){
                nueva += "\t\t<campo ";
                // Agregar complementos
                nueva += "complementos=\""+c.getComplementos()+"\" >\n";
                // Agregar nombre del campo y tipo
                String tipo = obtenerTipo(c.getTipo());
                nueva += "\t\t\t<"+tipo+">"+c.getNombre()+"</"+tipo+">\n";
                nueva += "\t\t</campo>\n";
            }
            nueva += "\t</rows>\n" +
                     "</Tabla>\n";
            master.modificar(Servidor.rutaBDS+Servidor.bd_actual+".usac", nueva);
            llenarTablas();
        }else{
            // Error! La tabla ya existe en la base de datos!
        }
    }
    
    public boolean existeTabla(String nombre){
        for(Tabla tabla : tablas){
            if(tabla.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearObjeto(Objeto objeto){
        // Se escribe en obj_bd.usac
        String rutaObjeto = Servidor.rutaBDS+"obj_"+Servidor.bd_actual+".usac";
        if(!existeObjeto(objeto.getNombre())){
            String nueva = "<Obj>\n" +
                           "\t<nombre>"+objeto.getNombre()+"</nombre>\n" +
                           "\t<attr>\n";
            for(Objeto a : objeto.getAtributos()){
                // Agregar nombre del atributo y tipo
                String tipo = obtenerTipo(a.getTipo());
                nueva += "\t\t<"+tipo+">"+a.getNombre()+"</"+tipo+">\n";
            }
            nueva += "\t</attr>\n" +
                     "</Obj>\n";
            master.modificar(rutaObjeto, nueva);
            llenarObjetos();
        }else{
            // Error! La tabla ya existe en la base de datos!
        }
    }
    
    public boolean existeObjeto(String nombre){
        for(Objeto obj : objetos){
            if(obj.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
    }
    
    public void crearMetodo(Metodo metodo){
        // Se escribe en proc_bd.usac
        
    }
    
    /**************************** USAR *************************/
    public boolean usarBD(String nombre){
        // Llenar las bases!
        llenarBases();
        if(existeBD(nombre)){
            // Obtener los demas listados!
            llenarTablas();
            llenarMetodos();
            llenarObjetos();
            return true;
        }else{
            // Reiniciar los listados!
            tablas = new LinkedList<Tabla>();
            objetos = new LinkedList<Objeto>();
            metodos = new LinkedList<Metodo>();
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
    
    /************************************************************/
    /***************************** LEE **************************/
    /************************************************************/
    
    public final void llenarUsuarios(){
        String listado = master.leer(Servidor.rutaUsuarios);
        usuarios = XML.getUsuarios(listado);
    }
    
    public final void llenarBases(){
        String bases = master.leer(Servidor.rutaMaestra);
        basesDatos = XML.getBasesDatos(bases);
    }
    
    public final void llenarTablas(){
        String contenido = master.leer(Servidor.rutaBDS+Servidor.bd_actual+".usac");
        tablas = XML.getTablas(contenido);
    }
    
    public final void llenarObjetos(){
        String contenido = master.leer(Servidor.rutaBDS+"obj_"+Servidor.bd_actual+".usac");
        objetos = XML.getObjetos(contenido);
    }
    
    // Se usa cuando se hace un Seleccionar
    public final LinkedList<Registro> getRegistro(String tabla){
        String registros = master.leer(Servidor.rutaBDS+tabla+"_"+Servidor.bd_actual+".usac");
        return XML.getRegistros(registros);
    }
    
    public final void llenarMetodos(){
        String contenido = master.leer(Servidor.rutaBDS+"proc_"+Servidor.bd_actual+".usac");
        metodos = XML.getMetodos(contenido);
    }
    
    //************* Manejo de Tipos *************************//
    public String obtenerTipo(int tipo){
        switch(tipo){
            case ENTERO:
                return "int";
            case DOBLE:
                return "double";
            case TEXTO:
                return "text";
            case BOOL:
                return "bool";
            case DATETIME:
                return "datetime";
            case DATE:
                return "date";
            default:
                return "objeto";
        }
    }
    
    public int obtenerTipo(String tipo){
        switch(tipo){
            case "entero":
                return ENTERO;
            case "doble":
                return DOBLE;
            case "cadena":
                return TEXTO;
            case "boolean":
                return BOOL;
            case "fecha":
                return DATE;
            case "fechahora":
                return DATETIME;
            default:
                return OBJETO;
        }
    }
    
}
