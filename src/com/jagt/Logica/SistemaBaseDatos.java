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
        llenarBases();
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
                           "\t<password>\""+password+"\"</password>\n" +
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
                           "\t<path>\""+rutaBD+"\"</path>\n" +
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
                              "\t<path>\""+rutaMetodo+"\"</path>\n" + 
                              "</Procedure>\n" + 
                              "<Object>\n" +
                              "\t<path>\""+rutaObj+"\"</path>\n" +
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
                           "\t<path>\""+rutaTabla+"\"</path>\n" +
                           "\t<rows>\n";
            for(Campo c : tabla.getCampos()){
                nueva += "\t\t<campo ";
                // Agregar complementos
                // Nulo!
                if(c.isNulo()){
                    nueva += "nulo = \"true\" ";
                }else{
                    nueva += "nulo = \"false\" ";
                }
                // Autoincrementable!
                if(c.isAutoincrementable()){
                    nueva += "auto = \"true\" ";
                }else{
                    nueva += "auto = \"false\" ";
                }
                // Primaria!
                if(c.isPrimaria()){
                    nueva += "primaria = \"true\" ";
                }else{
                    nueva += "primaria = \"false\" ";
                }
                // Unica!
                if(c.isUnica()){
                    nueva += "unico = \"true\" ";
                }else{
                    nueva += "unico = \"false\" ";
                }
                nueva += ">\n";
                // Agregar nombre del campo y tipo
                String tipo = obtenerTipo(c.getTipo());
                nueva += "\t\t\t<"+tipo+">"+c.getNombre()+"</"+tipo+">\n";
                // Foranea!
                if(c.isForanea()){
                    nueva += "\t\t\t<foranea>"+c.getTforanea()+"</foranea>\n";
                }
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
        String rutaMetodo = Servidor.rutaBDS+"proc_"+Servidor.bd_actual+".usac";
        if(!existeMetodo(metodo.getNombre())){
            // No existe entonces se guarda en el XML
            String nueva = "<Proc>\n" +
                           "\t<nombre>"+metodo.getNombre()+"</nombre>\n";
            // Si tiene retorno
            if(!(metodo.getTipo() == -1)){
                nueva += "\t<ret>"+obtenerTipo(metodo.getTipo())+"</ret>\n";
            }
            // Parametros
            nueva += "\t<params>\n";
            // Recorrer la lista de parametros
            for(Parametro p : metodo.getParametros()){
                String tipo = obtenerTipo(p.getTipo());
                nueva += "\t\t<"+tipo+">"+p.getNombre()+"</"+tipo+">\n";
            }
            nueva += "\t</params>\n";
            // Listado de expresiones
            nueva += "\t<src>\n\n";
            nueva += metodo.getTextoInstrucciones();
            nueva += "\n\t</src>\n";
            nueva += "</Proc>\n";
            master.modificar(rutaMetodo, nueva);
            llenarMetodos();
        }else{
            // Ya existe!
        }
    }
    
    public boolean existeMetodo(String nombre){
        for(Metodo metodo : metodos){
            if(metodo.getNombre().equals(nombre)){
                return true;
            }
        }
        return false;
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
    // Insertar de forma normal
    public String insertarEnTabla(String nombre,Registro nuevo){
        Tabla tabla = buscarTabla(nombre);
        if(tabla != null){
            // Insertar el registro en esa tabla
            if(tabla.getNumeroCampos() == nuevo.getColumnas().size()){
                // Verificar tipos y verificar que se pueda insertar
                if(tabla.comprobarTipos(nuevo.getColumnas())){
                    String nueva = "<Row>\n"; // cadena para escribir en el archivo de registro
                    LinkedList<Registro> registros = getRegistro(tabla.getNombre());
                    int idato = 0;
                    for(int i = 0; i < tabla.getCampos().size(); i++){
                        Campo campo = tabla.getCampos().get(i);
                        String nc = campo.getNombre();
                        Objeto objeto = nuevo.getColumnas().get(idato);
                        if(objeto.getTipo() == OBJETO){
                            // Es objeto!
                            nueva += "\t<"+nc+">\n";
                            // Recorrer atributos!
                            for(Objeto a : objeto.getAtributos()){
                                String na = a.getNombre();
                                nueva += "\t\t<"+na+">"+a.texto+"</"+na+">\n";
                            }
                            nueva += "</"+nc+">\n";
                        }else{
                            // No es objeto!
                            if(campo.isAutoincrementable()){
                                if(campo.getTipo() == ENTERO){
                                    nueva += "\t<"+nc+">"+(getSiguiente(registros,i).numero+1)+"</"+nc+">\n";
                                }
                            }else if(campo.isPrimaria() || campo.isUnica()){
                                if(!datoExistente(registros,i,objeto)){
                                    nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                }else{
                                    // Error! El dato a ingresar no es unico.. ya existe en el registro
                                }
                                idato++;
                            }else if(campo.isForanea()){
                                if(getForanea(campo.getTforanea(),nuevo.getColumnas().get(idato))){
                                    nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                }
                                idato++;
                            }else{
                                if(campo.getTipo() == TEXTO){
                                    nueva += "\t<"+nc+">\""+objeto.texto+"\"</"+nc+">\n";
                                }else{
                                    nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                }
                                idato++;
                            }
                        }
                    }
                    nueva += "</Row>\n";
                    String ruta = Servidor.rutaBDS+tabla.getNombre()+"_"+Servidor.bd_actual+".usac";
                    master.modificar(ruta, nueva);
                    return "";
                }else{
                    // Error! Los tipos no coinciden! -> 2
                }
            }else{
                // Error! Faltan campos! -> 1
            }
        }else{
            // Error! La tabla a la que se quiere insertar no existe! -> 0
        }
        return "";
    }
    
    // Insertar de forma especial
    public String insertarEnTablaEspecial(String nombre,LinkedList<String> campos,Registro nuevo){
        Tabla tabla = buscarTabla(nombre);
        if(tabla != null){
            // Insertar el registro en esa tabla
            if(campos.size() == nuevo.getColumnas().size()){
                // Verificar tipos y verificar que se pueda insertar
                if(tabla.verificarCampos(campos, nuevo)){
                    String nueva = "<Row>\n"; // cadena para escribir en el archivo de registro
                    LinkedList<Registro> registros = getRegistro(tabla.getNombre());
                    for(int i = 0; i < tabla.getCampos().size(); i++){
                        Campo campo = tabla.getCampos().get(i);
                        String nc = campo.getNombre();
                        if(tabla.esFaltante(nc, campos)){
                            if(campo.getTipo() == OBJETO){
                                nueva += "\t<"+nc+">\n";
                                nueva += "\t\t<"+nc+">\" \"</"+nc+">\n";
                                nueva += "\t</"+nc+">\n";
                            }else{
                                if(campo.isAutoincrementable()){
                                    if(campo.getTipo() == ENTERO){
                                        nueva += "\t<"+nc+">"+(getSiguiente(registros,i).numero+1)+"</"+nc+">\n";
                                    }
                                }else{
                                    nueva += "\t<"+nc+">\" \"</"+nc+">\n";
                                }
                            }
                        }else{
                            // No es faltante, esta en columnas
                            int idato = tabla.buscarIndice(nc, campos);
                            if(idato >= 0){
                                Objeto objeto = nuevo.getColumnas().get(idato);
                                if(objeto.getTipo() == OBJETO){
                                    // Es objeto!
                                    nueva += "\t<"+nc+">\n";
                                    // Recorrer atributos!
                                    for(Objeto a : objeto.getAtributos()){
                                        String na = a.getNombre();
                                        nueva += "\t\t<"+na+">"+a.texto+"</"+na+">\n";
                                    }
                                    nueva += "</"+nc+">\n";
                                }else{
                                    // No es objeto!
                                    if(campo.isPrimaria() || campo.isUnica()){
                                        if(!datoExistente(registros,i,objeto)){
                                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                        }else{
                                            // Error! El dato a ingresar no es unico.. ya existe en el registro
                                        }
                                    }else if(campo.isForanea()){
                                        if(getForanea(campo.getTforanea(),nuevo.getColumnas().get(idato))){
                                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                        }
                                    }else{
                                        if(campo.getTipo() == TEXTO){
                                            nueva += "\t<"+nc+">\""+objeto.texto+"\"</"+nc+">\n";
                                        }else{
                                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                                        }
                                    }
                                }
                            }
                        }
                    }
                    nueva += "</Row>\n";
                    String ruta = Servidor.rutaBDS+tabla.getNombre()+"_"+Servidor.bd_actual+".usac";
                    master.modificar(ruta, nueva);
                    return "";
                }else{
                    // Error! Los tipos no coinciden! -> 2
                }
            }else{
                // Error! Faltan campos! -> 1
            }
        }else{
            // Error! La tabla a la que se quiere insertar no existe! -> 0
        }
        return "";
    }
    
    // Buscar tabla existente
    public Tabla buscarTabla(String nombre){
        for(Tabla t : tablas){
            if(t.getNombre().equals(nombre)){
                return t;
            }
        }
        return null;
    }
    
    // Obtener siguiente numero (autoincremental)
    public Objeto getSiguiente(LinkedList<Registro> registros,int indice){
        if(registros.size() > 0){
            Registro ultimo = registros.getLast();
            return ultimo.getColumnas().get(indice);
        }else{
            return new Objeto(0);
        }
    }
    
    // Ver si el dato ya existe
    public boolean datoExistente(LinkedList<Registro> registros,int indice,Objeto dato){
        if(registros.size() > 0){
            for(Registro r : registros){
                if(Compilador.evaluarIGUAL(r.getColumnas().get(indice), dato).bool){
                        return true;
                }
            }
        }
        return false;
    }
    
    // Ver si la foranea existe
    public boolean getForanea(String nombre,Objeto dato){
        Tabla tabla = buscarTabla(nombre);
        if(tabla != null){
            int indice = tabla.getIndicePrimaria();
            if(indice >= 0){
                LinkedList<Registro> registros = getRegistro(tabla.getNombre());
                return datoExistente(registros,indice,dato);
            }
        }
        return false;
    }
    
    /**************************** ACTUALIZAR *************************/
    public String verificarCampos(String nombre,LinkedList<String> campos,Registro nuevo){
        Tabla tabla = buscarTabla(nombre);
        if(tabla != null){
            // Insertar el registro en esa tabla
            if(campos.size() == nuevo.getColumnas().size()){
                // Verificar tipos y verificar que se pueda insertar
                if(tabla.verificarCampos(campos, nuevo)){
                    return "correcto";
                }else{
                    return "error";
                }
            }else{
                return "error";
            }
        }else{
            return "error";
        }
    }
    
    // Reescribir los registros!
    public void modificarRegistro(Tabla tabla,LinkedList<Registro> nuevos){
        String text = "";
        String ruta = Servidor.rutaBDS+tabla.getNombre()+"_"+Servidor.bd_actual+".usac";
        master.escribir(ruta, text);
        for(Registro nuevo : nuevos){
            String nueva = "<Row>\n"; // cadena para escribir en el archivo de registro
            LinkedList<Registro> registros = getRegistro(tabla.getNombre());
            for(int i = 0; i < tabla.getCampos().size(); i++){
                Campo campo = tabla.getCampos().get(i);
                String nc = campo.getNombre();
                Objeto objeto = nuevo.getColumnas().get(i);
                if(objeto.getTipo() == OBJETO){
                    // Es objeto!
                    nueva += "\t<"+nc+">\n";
                    // Recorrer atributos!
                    for(Objeto a : objeto.getAtributos()){
                        String na = a.getNombre();
                        nueva += "\t\t<"+na+">"+a.texto+"</"+na+">\n";
                    }
                    nueva += "</"+nc+">\n";
                }else{
                    // No es objeto!
                    if(campo.isAutoincrementable()){
                        if(campo.getTipo() == ENTERO){
                            nueva += "\t<"+nc+">"+(getSiguiente(registros,i).numero+1)+"</"+nc+">\n";
                        }
                    }else if(campo.isPrimaria() || campo.isUnica()){
                        if(!datoExistente(registros,i,objeto)){
                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                        }else{
                            // Error! El dato a ingresar no es unico.. ya existe en el registro
                        }
                    }else if(campo.isForanea()){
                        if(getForanea(campo.getTforanea(),nuevo.getColumnas().get(i))){
                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                        }
                    }else{
                        if(campo.getTipo() == TEXTO){
                            nueva += "\t<"+nc+">\""+objeto.texto+"\"</"+nc+">\n";
                        }else{
                            nueva += "\t<"+nc+">"+objeto.texto+"</"+nc+">\n";
                        }
                    }
                }
            }
            nueva += "</Row>\n";
            master.modificar(ruta, nueva);
        }
    }
    
    /**************************** BORRAR *************************/
    
    /**************************** SELECCIONAR *************************/
    
    // Realizar el producto cartesiano
    public LinkedList<Registro> realizarProductoCartesiano(LinkedList<String> tablas){
        LinkedList<Registro> registros = getRegistro(tablas.get(0));
        for(int i = 1; i < tablas.size(); i++){
            // Obtener el registro auxiliar que tendra el producto cartesiano
            LinkedList<Registro> auxiliar = new LinkedList<Registro>();
            LinkedList<Registro> otras = getRegistro(tablas.get(i));
            for(Registro r1 : registros){
                for(Registro r2 : otras){
                    Registro nuevo = new Registro();
                    // Llenar los objetos del registro de la tabla registros
                    for(Objeto ob : r1.getColumnas()){
                        nuevo.agregarColumna(ob);
                    }
                    // Llenar los objetos del registro de la tabla otras
                    for(Objeto obj : r2.getColumnas()){
                        nuevo.agregarColumna(obj);
                    }
                    auxiliar.add(nuevo);
                }
            }
            registros = auxiliar;
        }
        return registros;
    }
    
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
        String tregistros = master.leer(Servidor.rutaBDS+tabla+"_"+Servidor.bd_actual+".usac");
        LinkedList<Registro> registros = XML.getRegistros(tregistros);
        for(Registro r : registros){
            for(Objeto obj : r.getColumnas()){
                obj.tabla = tabla;
            }
        }
        return registros;
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
