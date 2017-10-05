/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.Analizadores.Paquetes.SintacticoPaquetes;
import com.jagt.Comunicacion.ManejadorPaquete;
import com.jagt.GUI.Servidor;
import com.jagt.Logica.Archivo;
import com.jagt.Logica.Registro;
import com.jagt.Logica.SistemaBaseDatos;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class Compilador {
    
    // Instancia unica de la base de datos
    SistemaBaseDatos bd = SistemaBaseDatos.getInstance();
    String[] lineas;
    
    // Variables
    LinkedList<Hashtable<String,Objeto>> variables;
    
    // Retorno e interrupciones
    Objeto retorno = null;
    boolean hayRetorno = false,interrumpir = false;
    
    // Temporales
    Hashtable<String,Objeto> temporales; // -> Tabla,Columna
    int numeroTablas = 0;
    boolean USAR = false;
    
    // Consultas,Mensajes,Errores
    public LinkedList<LinkedList<Registro>> registros = new LinkedList<LinkedList<Registro>>();
    public LinkedList<String> mensajes = new LinkedList<String>();
    public LinkedList<Historia> historial = new LinkedList<Historia>();
    
    //long duracion = System.currentTimeMillis();
    
    public Compilador(NodoParser nodo){
        this.variables = new LinkedList<Hashtable<String,Objeto>>();
        lineas = SistemaBaseDatos.textoCompilado.split("\n");
        aumentarAmbito();
        ejecutarSentencias(nodo);
        disminuirAmbito();
    }
    
    // REPORTE!
    public String getReporte(){
        String tabla = "";
        // Tabla html
        // Columnas <tr><th>
        // Datos    <tr><td>
        for(LinkedList<Registro> lista : registros){
            tabla += "\t<table>\n";
            for(Registro registro : lista){
                if(lista.getFirst().equals(registro)){
                    // Encabezados!
                    tabla += "\t\t<tr>\n";
                    for(Objeto e : registro.getColumnas()){
                        tabla += "\t\t\t<th>"+e.getNombre()+"</th>\n";
                    }
                    tabla += "\t\t</tr>\n";
                }
                tabla += "\t\t<tr>\n";
                for(Objeto e : registro.getColumnas()){
                    tabla += "\t\t\t<td>"+e.texto+"</td>\n";
                }
                tabla += "\t\t</tr>\n";
            }
            tabla += "\t</table>\n\n";
        }
        return tabla;
    }
    
    private void ejecutarSentencias(NodoParser nodo){
        boolean b;
        long dt,duracion;
        switch(nodo.nombre()){
            case "CUERPOS":
                for(NodoParser hijo : nodo.hijos()){
                    ejecutarSentencias(hijo);
                }
                if(!USAR){
                    bd.backup_usqldump();
                }
                break;
            case "SENTENCIAS":
                for(NodoParser hijo : nodo.hijos()){
                    ejecutarSentencias(hijo);
                    if(hayRetorno || interrumpir){
                        break;
                    }
                }
                break;
            case "CREAR":
                crear(nodo);
                break;
            case "USAR":
                int linea = nodo.linea;
                duracion = System.currentTimeMillis();
                if(!Servidor.bd_actual.equals(nodo.hijos().get(0).valor())){
                    b = bd.usarBD(nodo.hijos().get(0).valor());
                    dt = System.currentTimeMillis()-duracion;
                    historial.add(new Historia("USAR BASE_DATOS",Long.toString(dt),0,b));
                    if(b){
                        USAR = true;
                    }
                }
                break;
            case "ALTERAR":
                break;
            case "OTORGAR":
                break;
            case "DENEGAR":
                break;
            case "BACKUP":
                // MINIMO!
                // USQLDUMP o TOTAL
                break;
            case "RESTAURAR":
                // MINIMO!
                // USQLDUMP o TOTAL
                // RESTAURAR TIPO CADENA
                if(nodo.hijos().get(1).valor().equals("USQLDUMP")){
                    Archivo a = new Archivo();
                    String contenido = a.leer(nodo.hijos().get(2).valor().replaceAll("\"", ""));
                    String cadena = "'[\n'paquete': 'usql',\n'instruccion':\n ?\n"+contenido+"\n?\n]\n'";
                    ManejadorPaquete mp = new ManejadorPaquete();
                    InputStream is = new ByteArrayInputStream(cadena.getBytes());
                    SintacticoPaquetes parser = null;
                    if(parser == null) parser = new SintacticoPaquetes(is);   
                    else parser.ReInit(is);
                    try
                    {
                        mp = parser.inicio();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error:\n"+ e.getMessage()+"\n");
                    }
                    catch (Error e)
                    {
                        System.out.println("Error:\n"+ e.getMessage()+"\n");
                    }
                    finally
                    {
                    }
                }
                break;
            case "INSERTAR":
                // MINIMO!
                insertar(nodo);
                break;
            case "ELIMINAR":
                break;
            case "SELECCIONAR":
                // MINIMO!
                duracion = System.currentTimeMillis();
                LinkedList<Registro> reg = evaluarSeleccionar(nodo);
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("SELECCIONAR",Long.toString(dt),0,true));
                registros.add(reg);
                break;
            case "ACTUALIZAR":
                // MINIMO!
                duracion = System.currentTimeMillis();
                b = actualizar(nodo);
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("ACTUALIZAR",Long.toString(dt),0,b));
                break;
            case "BORRAR":
                // HACER!
                break;
            case "DECLARAR":
                // MINIMO!
                declarar(nodo);
                break;
            case "ASIGNAR":
                // MINIMO!
                Objeto dato = evaluarExpresion(nodo.hijos().get(1));
                modificarVariable(nodo.hijos().get(0),dato);
                break;
            case "IMPRIMIR":
                // MINIMO!
                Objeto valor = evaluarExpresion(nodo.hijos().get(0));
                if(valor != null){
                    mensajes.add(valor.texto);
                    System.out.println(valor.texto);
                }
                break;
            case "LLAMADA":
                llamadaProcedimiento(nodo);
                break;
            case "RETORNAR":
                retorno = evaluarExpresion(nodo.hijos().get(0));
                hayRetorno = true;
                break;
            case "DETENER":
                interrumpir = true;
                break;
            case "SI":
                // MINIMO!
                evaluarSI(nodo);
                break;
            case "SELECCIONA":
                // MINIMO!
                evaluarSELECCIONA(nodo);
                break;
            case "PARA":
                // HACER!
                //evaluarPARA(nodo);
                break;
            case "MIENTRAS":
                // MINIMO!
                evaluarMIENTRAS(nodo);
                break;
            case "CONTAR":
                // HACER
                break;
        }
    }
    
    /*********************************************************
     * MANEJO DE VARIABLES Y AMBITOS
     *********************************************************/
    private void aumentarAmbito(){
        this.variables.addFirst(new Hashtable<String,Objeto>());
    }
    
    private void disminuirAmbito(){
        this.variables.removeFirst();
    }
    
    // Declarar una lista de variables y asignarle un objeto
    private void declararVariables(LinkedList<String> nombres,Objeto dato){
        for(String nombre : nombres){
            if(buscarVariable(nombre) == null){
                this.variables.getFirst().put(nombre, dato);
            }else{
                // ERROR! La variable ya existe!
            }
        }
    }
    
    // Modificar el valor de una variable
    private void modificarVariable(NodoParser nodo,Objeto dato){
        // ACCESO -> VAR (.ID)?
        String nombre = nodo.hijos().get(0).valor();
        Objeto var = buscarVariable(nombre);
        Objeto atr = null;
        if(nodo.hijos().size() == 2){
            // Modificar atributo
            atr = buscarAtributo(var,nodo.hijos().get(1).valor());
            if(atr != null){
                var.modificarAtributo(nodo.hijos().get(1).valor(), dato);
            }else{
                // ERROR! El atributo no existe!
                return;
            }
        }
        if(var != null){
            for(Hashtable<String,Objeto> ambito : variables){
                if(ambito.containsKey(nombre)){
                    if(nodo.hijos().size() == 2){
                        if(atr.getTipo() == dato.getTipo()){
                            ambito.put(nombre, var);
                        }else{
                            
                        }
                    }else{
                        if(var.getTipo() == dato.getTipo()){
                            ambito.put(nombre, dato);
                        }else{
                            // ERROR! No coincide con el tipo!
                        }
                    }
                }
            }
        }else{
            // ERROR! La variable no existe!
        }
    }
    
    // Buscar variable
    private Objeto buscarVariable(String nombre){
        for(Hashtable<String,Objeto> ambito : variables){
            if(ambito.containsKey(nombre)){
                return ambito.get(nombre);
            }
        }
        return null;
    }
    
    /*********************************************************
     * OPERACIONES CON LA BASE DE DATOS
     *********************************************************/
    /*
        ************ CREAR ************
    */
    private void crear(NodoParser nodo){
        int linea = nodo.hijos().get(0).linea;
        long duracion = System.currentTimeMillis();
        long dt;
        boolean b;
        switch(nodo.hijos().get(0).nombre()){
            case "BD":
                // CREAR -> BD ID
                b = bd.crearBD(nodo.hijos().get(1).valor(),Servidor.logueado.getCodigo());
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("CREAR BASE_DATOS",Long.toString(dt),0,b));
                break;
            case "TABLA":
                // CREAR -> TABLA ID CAMPOS
                b = bd.crearTabla(crearTabla(nodo.hijos().get(1).valor(),nodo.hijos().get(2)));
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("CREAR TABLA",Long.toString(dt),0,b));
                break;
            case "OBJETO":
                // CREAR -> OBJETO ID ATRIBUTOS
                b = bd.crearObjeto(crearObjeto(nodo.hijos().get(1).valor(),nodo.hijos().get(2)));
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("CREAR OBJETO",Long.toString(dt),0,b));
                break;
            case "USUARIO":
                // CREAR -> USUARIO ID CADENA
                b = bd.crearUsuario(nodo.hijos().get(1).valor(),nodo.hijos().get(2).valor());
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("CREAR USUARIO",Long.toString(dt),0,b));
                break;
            default:
                // FUNCION O PROCEDIMIENTO
                b = bd.crearMetodo(crearProcedimiento(nodo));
                dt = System.currentTimeMillis()-duracion;
                historial.add(new Historia("CREAR "+nodo.hijos().get(0).nombre(),Long.toString(dt),0,b));
                break;
        }
    }
    
    /*
        ************ CREAR PROCEDIMIENTO ************
    */
    private Metodo crearProcedimiento(NodoParser nodo){
        // CREAR -> PROCEDIMIENTO ID PARAMS? SENTENCIAS
        Metodo nuevo = new Metodo();
        nuevo.setNombre(nodo.hijos().get(1).valor());
        // Guardar sentencias
        nuevo.setTextoInstrucciones(getSentencias(nodo.inicio,nodo.fin));
        int i_tipo = 3;
        if(nodo.hijos().get(2).nombre().equals("PARAMS")){
            // Guardar parametros si tiene
            nuevo.setParametros(getParametros(nodo.hijos().get(2)));
        }else{
            nuevo.setParametros(new LinkedList<Parametro>());
            i_tipo = 2;
        }
        if(nodo.hijos().get(0).nombre().equals("FUNCION")){
            // CREAR -> FUNCION ID PARAMS? TIPO SENTENCIAS
            nuevo.setTipo(bd.obtenerTipo(nodo.hijos().get(i_tipo).valor()));
            if(nodo.hijos().get(i_tipo).nombre().equals("ID")){
                nuevo.setObjeto(nodo.hijos().get(i_tipo).valor());
            }
        }else{
            nuevo.setTipo(-1);
        }
        return nuevo;
    }
    
    // Guardar sentencias
    private String getSentencias(int inicio,int fin){
        String sentencias = "";
        for(int i = inicio; i < fin-1; i++){
            sentencias += lineas[i]+"\n";
        }
        return sentencias;
    }
    
    // Guardar los parametros
    private LinkedList<Parametro> getParametros(NodoParser nodo){
        LinkedList<Parametro> params = new LinkedList<Parametro>();
        for(NodoParser p : nodo.hijos()){
            Parametro nuevo = new Parametro(p.hijos().get(1).valor(),bd.obtenerTipo(p.hijos().get(0).valor()));
            if(nuevo.tipo == SistemaBaseDatos.OBJETO){
                nuevo.objeto = p.hijos().get(0).valor();
            }
            params.add(nuevo);
        }
        return params;
    }
    
    /*
        ************ CREAR OBJETO ************
    */
    private Objeto crearObjeto(String nombre,NodoParser atributos){
        Objeto obj = new Objeto(nombre,SistemaBaseDatos.OBJETO);
        for(NodoParser atr : atributos.hijos()){
            obj.agregarAtributo(crearAtributo(atr));
        }
        return obj;
    }
    
    // Crear Atributo del Objeto
    private Objeto crearAtributo(NodoParser atributo){
        return new Objeto(atributo.hijos().get(1).valor(),bd.obtenerTipo(atributo.hijos().get(0).valor()));
    }
    
    /*
        ************ CREAR TABLA ************
    */
    private Tabla crearTabla(String nombre,NodoParser campos){
        Tabla tabla = new Tabla(nombre);
        for(NodoParser campo : campos.hijos()){
            tabla.agregarCampo(crearCampo(campo));
        }
        return tabla;
    }
    
    // Crear Campo de la Tabla
    private Campo crearCampo(NodoParser campo){
        // CAMPO -> TIPO ID COMPLEMENTOS
        // Crear el cmapo con su respectivo tipo y nombre del campo
        Campo nuevo = new Campo(campo.hijos().get(1).valor(),bd.obtenerTipo(campo.hijos().get(0).valor()));
        if(campo.hijos().get(0).nombre().equals("ID")){
            nuevo.setObjeto(campo.hijos().get(0).valor());
        }
        // Recorrer los complementos del campo
        NodoParser complementos = campo.hijos().get(2);
        for(NodoParser com : complementos.hijos()){
            switch(com.hijos().get(0).nombre()){
                case "PRIMARIA":
                    nuevo.setPrimaria(true);
                    break;
                case "AUTOINC":
                    nuevo.setAutoincrementable(true);
                    break;
                case "NO NULO":
                    nuevo.setNulo(false);
                    break;
                case "NULO":
                    nuevo.setNulo(true);
                    break;
                case "FORANEA":
                    nuevo.setForanea(com.hijos().get(1).valor());
                    break;
                default:
                    // UNICO
                    nuevo.setUnica(true);
                    break;
            }
        }
        return nuevo;
    }
    
    /*
        ************ ALTERAR ************
    */
    
    /*
        ************ OTORGAR ************
    */
    
    /*
        ************ DENEGAR ************
    */
    
    /*
        ************ BACKUP ************
    */
    
    /*
        ************ RESTAURAR ************
    */
    
    /*
        ************ INSERTAR ************
    */
    private void insertar(NodoParser nodo){
        if(nodo.hijos().size() == 2){
            // Normal!
            insertarNormal(nodo.hijos().get(0).valor(),nodo.hijos().get(1));
        }else{
            // Especial!
            insertarEspecial(nodo.hijos().get(0).valor(),nodo.hijos().get(1),nodo.hijos().get(2));
        }
    }
    
    // Insertar de forma normal
    private void insertarNormal(String nombre,NodoParser exps){
        // INSERTAR -> ID EXPRESIONES
        Registro nuevo = new Registro();
        for(NodoParser exp : exps.hijos()){
            nuevo.agregarColumna(evaluarExpresion(exp));
        }
        bd.insertarEnTabla(nombre, nuevo);
    }
    
    // Insertar de forma especial
    private void insertarEspecial(String nombre,NodoParser lista,NodoParser exps){
        // INSERTAR -> ID LISTA_IDS EXPRESIONES
        // Llenar lista de IDS
        LinkedList<String> ids = new LinkedList<String>();
        for(NodoParser id : lista.hijos()){
            ids.add(id.valor());
        }
        // Llenar registro
        Registro nuevo = new Registro();
        for(NodoParser exp : exps.hijos()){
            nuevo.agregarColumna(evaluarExpresion(exp));
        }
        bd.insertarEnTablaEspecial(nombre, ids, nuevo);
    }
    
    /*
        ************ SELECCIONAR ************
    */
    private LinkedList<Registro> evaluarSeleccionar(NodoParser nodo){
        // SELECCIONAR -> (TODO | LISTA_ACCESO) IDS (EXP)? (ORDENAR)?
        // Llenar lista de IDS
        LinkedList<String> ids = new LinkedList<String>();
        String valor = nodo.hijos().get(0).valor();
        for(NodoParser id : nodo.hijos().get(1).hijos()){
            ids.add(id.valor());
        }
        numeroTablas = ids.size();
        LinkedList<Registro> registros = bd.realizarProductoCartesiano(ids);
        imprimirColumnas(registros);
        if(nodo.hijos().size() > 2){
            // DONDE
            LinkedList<Registro> registrosDonde = new LinkedList<Registro>();
            for(Registro r : registros){
                // Guardar en temporales
                temporales = new Hashtable<String,Objeto>();
                guardarTemporales(r);
                // Evaluar expresion
                if(evaluarExpresion(nodo.hijos().get(2)).bool){
                    registrosDonde.add(r);
                }
            }
            System.out.println();
            System.out.println("************ SELECCIONAR CON DONDE ************");
            imprimirColumnas(registrosDonde);
            if(nodo.hijos().size() == 4){
                // ORDENAR
            }
            return registrosDonde;
        }else{
            return registros;
        }
    }
    
    // Guardar solo los campos requeridos!
    private LinkedList<Registro> buscarSolicitados(LinkedList<Registro> registros,NodoParser lista){
        LinkedList<Registro> nuevo = new LinkedList<Registro>();
        
        return nuevo;
    }
    
    // Guardar temporales
    private void guardarTemporales(Registro r){
        for(Objeto obj : r.getColumnas()){
            if(numeroTablas == 1){
                temporales.put(obj.getNombre(), obj);
            }else{
                temporales.put(obj.tabla+","+obj.getNombre(), obj);
            }
        }
    }
    
    // Imprimir el Seleccionar
    public void imprimirColumnas(LinkedList<Registro> registros){
        System.out.println("******** SELECCIONAR *******");
        for(Registro r : registros){
            for(Objeto obj : r.getColumnas()){
                System.out.print(obj.tabla+"."+obj.texto+" ");
            }
            System.out.println();
        }
    }
    
    /*
        ************ ACTUALIZAR ************
    */
    private boolean actualizar(NodoParser nodo){
        // ACTUALIZAR -> ID LISTA_IDS EXPRESIONES (EXP_DONDE)?
        // Llenar lista de IDS
        String tabla = nodo.hijos().get(0).valor();
        LinkedList<String> ids = new LinkedList<String>();
        for(NodoParser id : nodo.hijos().get(1).hijos()){
            ids.add(id.valor());
        }
        // Llenar registro
        Registro nuevo = new Registro();
        for(NodoParser exp : nodo.hijos().get(2).hijos()){
            nuevo.agregarColumna(evaluarExpresion(exp));
        }
        String mensaje = bd.verificarCampos(tabla, ids, nuevo);
        if(mensaje.equals("correcto")){
            // Todo bien!
            LinkedList<Registro> registros = bd.getRegistro(tabla);
            numeroTablas = 1;
            for(Registro r : registros){
                temporales = new Hashtable<String,Objeto>();
                guardarTemporales(r);
                for(int i = 0; i < ids.size(); i++){
                    if(nodo.hijos().size() == 4){
                        if(evaluarExpresion(nodo.hijos().get(3)).bool){
                            r.modificarColumna(ids.get(i), nuevo.getColumnas().get(i));
                        }
                    }else{
                        r.modificarColumna(ids.get(i), nuevo.getColumnas().get(i));
                    }
                }
            }
            Tabla t = bd.buscarTabla(tabla);
            bd.modificarRegistro(t, registros);
            return true;
        }else{
            // Error!
            return false;
        }
    }
    
    /*********************************************************
     * HERRAMIENTAS DEL COMPILADOR
     *********************************************************/
    // Declarar
    private void declarar(NodoParser nodo){
        // DECLARAR -> LISTA_IDS TIPO (EXP)?
        // Llenar listado de variables
        LinkedList<String> nombres = new LinkedList<String>();
        for(NodoParser var : nodo.hijos().get(0).hijos()){
            nombres.add(var.valor());
        }
        int tipo = bd.obtenerTipo(nodo.hijos().get(1).valor());
        Objeto obj = new Objeto(0);
        obj.tipo = tipo;
        if(nodo.hijos().size() == 2){
            if(tipo == SistemaBaseDatos.OBJETO){
                // Instancia!
                Objeto plantilla = bd.buscarObjeto(nodo.hijos().get(1).valor());
                if(plantilla != null){
                    declararVariables(nombres, plantilla);
                }else{
                    // ERROR! El objeto no existe!
                }
            }else{
                // Normal!
                declararVariables(nombres,obj);
            }
        }else{
            // Tiene expresion!
            if(tipo != SistemaBaseDatos.OBJETO){
                // No es una instancia
                Objeto exp = evaluarExpresion(nodo.hijos().get(2));
                if(exp.getTipo() == tipo){
                    declararVariables(nombres,exp);
                }else{
                    // No son del mismo tipo
                }
            }else{
                // ERROR! No se puede declarar y asignar una instancia
            }
        }
    }
    
    /*********************************************************
     * SENTENCIAS DE CONTROL Y CICLOS
     *********************************************************/
    // SI o SI SINO
    public void evaluarSI(NodoParser nodo){
        // SI -> EXP SENTENCIAS (SENTENCIAS)?
        Objeto cond = evaluarExpresion(nodo.hijos().get(0));
        if(cond.tipo == SistemaBaseDatos.BOOL){
            if(cond.bool){
                ejecutarSentencias(nodo.hijos().get(1));
            }else{
                if(nodo.hijos().size() == 3){
                    ejecutarSentencias(nodo.hijos().get(2));
                }
            }
        }else{
            // ERROR! La condicion no da valor Booleano
        }
    }
    
    // SELECCIONA
    public void evaluarSELECCIONA(NodoParser nodo){
        // SELECCIONA -> EXP CASOS (SENTENCIAS)?
        Objeto dato = evaluarExpresion(nodo.hijos().get(0));
        boolean encontrado = false;
        if(dato != null){
            for(NodoParser caso : nodo.hijos().get(1).hijos()){
                // Recorrer cada caso
                // CASO -> EXP SENTENCIAS
                if(encontrado && !interrumpir){
                    ejecutarSentencias(caso.hijos().get(1));
                    continue;
                }
                Objeto valor = evaluarExpresion(caso.hijos().get(0));
                Objeto cond = evaluarIGUAL(dato,valor);
                if(cond != null){
                    if(cond.bool){
                        encontrado = true;
                        ejecutarSentencias(caso.hijos().get(1));
                    }
                }
            }
            // DEFECTO!
            if(nodo.hijos().size() == 3 && !encontrado){
                ejecutarSentencias(nodo.hijos().get(2));
            }
            if(interrumpir){
                interrumpir = false;
            }
        }
    }
    
    // PARA
    
    // MIENTRAS
    public void evaluarMIENTRAS(NodoParser nodo){
        // MIENTRAS -> EXP SENTENCIAS
        Objeto cond = evaluarExpresion(nodo.hijos().get(0));
        if(cond.tipo == SistemaBaseDatos.BOOL){
            while(cond.bool){
                ejecutarSentencias(nodo.hijos().get(1));
                if(interrumpir){
                    interrumpir = false;
                    break;
                }
                cond = evaluarExpresion(nodo.hijos().get(0));
                if(cond.tipo != SistemaBaseDatos.BOOL){
                    break;
                }
            }
        }
    }
    
    /*********************************************************
     * EVALUAR EXPRESION
     * @param nodo
     * @return 
     *********************************************************/
    protected Objeto evaluarExpresion(NodoParser nodo){
        switch(nodo.hijos().size()){
            case 3:
                // 3 hijos
                Objeto izq = evaluarExpresion(nodo.hijos().get(0));
                Objeto der = evaluarExpresion(nodo.hijos().get(2));
                if(izq == null && der == null){
                    break;
                }else if(izq != null && der == null){
                    return izq;
                }else if(izq == null && der != null){
                    return der;
                }
                if(nodo.hijos().get(1).nombre().equals("OPELOGICA")){
                    return evaluarLogica(izq,der,nodo.hijos().get(1).valor());
                }else{
                    return evaluarAritmetica(izq,der,nodo.hijos().get(1).valor());
                }
            case 2:
                // 2 hijos
                Objeto nder = evaluarExpresion(nodo.hijos().get(1));
                return evaluarNOT(nder);
            default:
                // 1 hijo
                switch(nodo.hijos().get(0).nombre()){
                    case "entero":
                        return new Objeto(Integer.parseInt(nodo.hijos().get(0).valor()));
                    case "doble":
                        return new Objeto(Double.parseDouble(nodo.hijos().get(0).valor()));
                    case "cadena":
                        return new Objeto(nodo.hijos().get(0).valor().replaceAll("\"", ""));
                    case "fecha":
                        return new Objeto(SistemaBaseDatos.DATE,nodo.hijos().get(0).valor());
                    case "fechahora":
                        return new Objeto(SistemaBaseDatos.DATETIME,nodo.hijos().get(0).valor());
                    case "boolean":
                        if(nodo.hijos().get(0).valor().equals("1")){
                            return new Objeto(true);
                        }else{
                            return new Objeto(false);
                        }
                    case "LLAMADA":
                        return llamadaProcedimiento(nodo.hijos().get(0));
                    case "ACCESO":
                        // ACCESO -> (ID | VAR)(. ID)*
                        return getAcceso(nodo.hijos().get(0));
                    case "date":
                        String fecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                        return new Objeto(SistemaBaseDatos.DATE,fecha);
                    case "datetime":
                        String fechah = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                        return new Objeto(SistemaBaseDatos.DATETIME,fechah);
                    case "CONTAR":
                        return new Objeto(evaluarSeleccionar(nodo.hijos().get(0)).size());
                    case "VAR":
                        return getAcceso(nodo);
                    default:
                        return evaluarExpresion(nodo.hijos().get(0));
                }
        }
        return null;
    }
    
    /*
        ************ ACCESO ************
    */
    private Objeto getAcceso(NodoParser nodo){
        // Al menos 1, puede ser VAR o ID
        NodoParser hijo_1 = nodo.hijos().get(0);
        if(hijo_1.nombre().equals("VAR")){
            // buscar el nombre de la variable
            Objeto var = buscarVariable(hijo_1.valor());
            if(var != null){
                return var;
            }
        }else{
            String llave = hijo_1.valor();
            if(numeroTablas == 1){
                if(nodo.hijos().size() > 1){
                    llave += ","+nodo.hijos().get(1).valor();
                }
                return temporales.get(llave);
            }else{
                llave += ","+nodo.hijos().get(1).valor();
                if(nodo.hijos().size() == 3){
                    // Buscar tabla.columna.atributo
                    return buscarAtributo(temporales.get(llave),nodo.hijos().get(2).valor());
                }else{
                    return temporales.get(llave);
                }
            }
        }
        return new Objeto(0);
    }
    
    // Buscar atributo
    private Objeto buscarAtributo(Objeto objeto,String nombre){
        for(Objeto atributo : objeto.getAtributos()){
            if(atributo.getNombre().equals(nombre)){
                return atributo;
            }
        }
        return new Objeto(0);
    }
    
    /*
        ************ LLAMADA ************
    */
    private Objeto llamadaProcedimiento(NodoParser nodo){
        Metodo proc = bd.buscarMetodo(nodo.hijos().get(0).valor());
        if(proc != null){
            aumentarAmbito();
            if(nodo.hijos().size() == 2){
                // Con parametros!
                if(nodo.hijos().get(1).hijos().size() == proc.getParametros().size()){
                    // Guardamos los parametros en las variables
                    NodoParser expresiones = nodo.hijos().get(1);
                    LinkedList<String> par = new LinkedList<String>();
                    for(int i = 0; i < proc.getParametros().size(); i++){
                        Objeto dato = evaluarExpresion(expresiones.hijos().get(i));
                        if(dato != null){
                            Parametro p = proc.getParametros().get(i);
                            if(dato.getTipo() == p.getTipo()){
                                par.add(p.getNombre());
                                declararVariables(par,dato);
                            }else{
                                // ERROR! El tipo de parametro y expresion no coincide
                                return null;
                            }
                        }else{
                            return null;
                        }
                    }
                }else{
                    // ERROR! Faltan parametros!
                    return null;
                }
            }else{
                // Sin parametros!
                if(proc.getParametros().size() != 0){
                    // ERROR! Se necesitan parametros!
                    return null;
                }
            }
            // Ejecutar las sentencias!
            if(proc.getInstrucciones() != null){
                ejecutarSentencias(proc.getInstrucciones());
            }
            disminuirAmbito();
            hayRetorno = false;
            return retorno;
        }else{
            // ERROR! El metodo no existe!
            return null;
        }
    }
    
    /*
        ************ OPERACIONES LOGICAS ************
    */
    private Objeto evaluarLogica(Objeto izq,Objeto der,String operando){
        switch(operando){
            case "AND":
                return evaluarAND(izq,der);
            case "OR":
                return evaluarOR(izq,der);
            case "==":
                return evaluarIGUAL(izq,der);
            case "!=":
                return evaluarDIFERENTE(izq,der);
            case ">=":
                return evaluarMAYORIGUAL(izq,der);
            case ">":
                return evaluarMAYOR(izq,der);
            case "<=":
                return evaluarMENORIGUAL(izq,der);
            default:
                return evaluarMENOR(izq,der);
        }
    }
    
    private Objeto evaluarAND(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == SistemaBaseDatos.BOOL && tder == SistemaBaseDatos.BOOL){
            return new Objeto(izq.bool && der.bool);
        }
        return null;
    }
    
    private Objeto evaluarOR(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == SistemaBaseDatos.BOOL && tder == SistemaBaseDatos.BOOL){
            return new Objeto(izq.bool || der.bool);
        }
        return null;
    }
    
    private Objeto evaluarNOT(Objeto der){
        int tder = der.getTipo();
        if(tder == SistemaBaseDatos.BOOL){
            return new Objeto(!der.bool);
        }
        return null;
    }
    
    /*
        ************ OPERACIONES RELACIONALES ************
    */
    private Objeto evaluarMAYOR(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero > der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal > der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        if(der.bool){
                            return new Objeto(true);
                        }else{
                            return new Objeto(false);
                        }
                    }else{
                        if(der.bool){
                            return new Objeto(false);
                        }
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) > 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.fecha.compareTo(der.fecha) > 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.fecha.compareTo(der.fecha) > 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    private Objeto evaluarMAYORIGUAL(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero >= der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal >= der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        return new Objeto(true);
                    }else{
                        if(der.bool){
                            return new Objeto(false);
                        }else{
                            return new Objeto(true);
                        }
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) >= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.fecha.compareTo(der.fecha) >= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.getFecha().compareTo(der.getFecha()) >= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    private Objeto evaluarMENOR(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero < der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal < der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        return new Objeto(false);
                    }else{
                        if(der.bool){
                            return new Objeto(true);
                        }else{
                            return new Objeto(true);
                        }
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) < 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.fecha.compareTo(der.fecha) < 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.getFecha().compareTo(der.getFecha()) < 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    private Objeto evaluarMENORIGUAL(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero <= der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal <= der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        if(der.bool){
                            return new Objeto(true);
                        }else{
                            return new Objeto(false);
                        }
                    }else{
                        return new Objeto(true);
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) <= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.fecha.compareTo(der.fecha) <= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.fecha.compareTo(der.fecha) <= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    public static Objeto evaluarIGUAL(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero == der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal == der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        if(der.bool){
                            return new Objeto(true);
                        }else{
                            return new Objeto(false);
                        }
                    }else{
                        if(der.bool){
                            return new Objeto(false);
                        }else{
                            return new Objeto(true);
                        }
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) == 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.getFecha().compareTo(der.getFecha()) == 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.fecha.compareTo(der.fecha) == 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    private Objeto evaluarDIFERENTE(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        if(tizq == tder){
            switch(tizq){
                case SistemaBaseDatos.ENTERO:
                    if(izq.numero != der.numero){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DOBLE:
                    if(izq.decimal != der.decimal){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.BOOL:
                    if(izq.bool){
                        if(der.bool){
                            return new Objeto(false);
                        }else{
                            return new Objeto(true);
                        }
                    }else{
                        if(der.bool){
                            return new Objeto(true);
                        }else{
                            return new Objeto(false);
                        }
                    }
                case SistemaBaseDatos.TEXTO:
                    if(izq.texto.compareTo(der.texto) != 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATE:
                    if(izq.fecha.compareTo(der.fecha) != 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.fecha.compareTo(der.fecha) != 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    /*
        ************ OPERACIONES ARITMETICAS ************
    */
    private Objeto evaluarAritmetica(Objeto izq,Objeto der,String operando){
        switch(operando){
            case "+":
                return evaluarMAS(izq,der);
            case "-":
                return evaluarMENOS(izq,der);
            case "*":
                return evaluarPOR(izq,der);
            case "/":
                return evaluarDIV(izq,der);
            default:
                return evaluarPOT(izq,der);
        }
    }
    
    private Objeto evaluarMAS(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        switch(tizq){
            case SistemaBaseDatos.ENTERO:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.numero + der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.numero + der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(der.bool){
                        return new Objeto(izq.numero + 1);
                    }
                    return izq;
                }else if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.numero + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DOBLE:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.decimal + der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.decimal + der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(der.bool){
                        return new Objeto(izq.decimal + 1);
                    }
                    return izq;
                }else if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.decimal + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.BOOL:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(izq.bool){
                        return new Objeto(1 + der.numero);
                    }
                    return new Objeto(der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(izq.bool){
                        return new Objeto(1 + der.decimal);
                    }
                    return new Objeto(der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    return evaluarOR(izq,der);
                }else if(tder == SistemaBaseDatos.TEXTO){
                    if(izq.bool){
                        return new Objeto("1" + der.texto);
                    }
                    return new Objeto("0" + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.TEXTO:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.texto + der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.texto + der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(der.bool){
                        return new Objeto(izq.texto + "1");
                    }
                    return new Objeto(izq.texto + "0");
                }else if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.texto + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATE:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATETIME:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
        }
        return null;
    }
    
    private Objeto evaluarMENOS(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        switch(tizq){
            case SistemaBaseDatos.ENTERO:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.numero - der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.numero - der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(der.bool){
                        return new Objeto(izq.numero - 1);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DOBLE:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.decimal - der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.decimal - der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(der.bool){
                        return new Objeto(izq.decimal - 1);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.BOOL:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(izq.bool){
                        return new Objeto(1 - der.numero);
                    }
                    return new Objeto(0 - der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(izq.bool){
                        return new Objeto(1 - der.decimal);
                    }
                    return new Objeto(0 - der.decimal);
                }else{
                    // ERROR!
                    break;
                }
        }
        return null;
    }
    
    private Objeto evaluarPOR(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        switch(tizq){
            case SistemaBaseDatos.ENTERO:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.numero * der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.numero * der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        return new Objeto(0);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DOBLE:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(izq.decimal * der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(izq.decimal * der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        return new Objeto(0);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.BOOL:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(!izq.bool){
                        return new Objeto(0);
                    }
                    return der;
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(!izq.bool){
                        return new Objeto(0);
                    }
                    return der;
                }else if(tder == SistemaBaseDatos.BOOL){
                    return evaluarAND(izq,der);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATE:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATETIME:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
        }
        return null;
    }
    
    private Objeto evaluarDIV(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        switch(tizq){
            case SistemaBaseDatos.ENTERO:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(der.numero == 0){
                        // ERROR!
                        break;
                    }
                    double val = izq.numero / der.numero;
                    return new Objeto(val);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(der.decimal == 0.0){
                        // ERROR!
                        break;
                    }
                    return new Objeto(izq.numero / der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        // ERROR!
                        break;
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DOBLE:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(der.numero == 0){
                        // ERROR!
                        break;
                    }
                    return new Objeto(izq.decimal / der.numero);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(der.decimal == 0){
                        // ERROR!
                        break;
                    }
                    return new Objeto(izq.decimal / der.decimal);
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        // ERROR!
                        break;
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.BOOL:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(der.numero == 0){
                        // ERROR!
                        break;
                    }
                    if(izq.bool){
                        return new Objeto(1 / der.numero);
                    }
                    return new Objeto(0);
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(der.decimal == 0.0){
                        // ERROR!
                        break;
                    }
                    if(izq.bool){
                        return new Objeto(1 / der.decimal);
                    }
                    return new Objeto(0);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATE:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATETIME:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
        }
        return null;
    }
    
    private Objeto evaluarPOT(Objeto izq,Objeto der){
        int tizq = izq.getTipo();
        int tder = der.getTipo();
        switch(tizq){
            case SistemaBaseDatos.ENTERO:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(Math.pow(izq.numero,der.numero));
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(Math.pow(izq.numero,der.decimal));
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        return new Objeto(1);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DOBLE:
                if(tder == SistemaBaseDatos.ENTERO){
                    return new Objeto(Math.pow(izq.decimal,der.numero));
                }else if(tder == SistemaBaseDatos.DOBLE){
                    return new Objeto(Math.pow(izq.decimal,der.decimal));
                }else if(tder == SistemaBaseDatos.BOOL){
                    if(!der.bool){
                        return new Objeto(1);
                    }
                    return izq;
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.BOOL:
                if(tder == SistemaBaseDatos.ENTERO){
                    if(izq.bool){
                        return new Objeto(1);
                    }
                    return new Objeto(Math.pow(0,der.numero));
                }else if(tder == SistemaBaseDatos.DOBLE){
                    if(izq.bool){
                        return new Objeto(1);
                    }
                    return new Objeto(Math.pow(0,der.decimal));
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATE:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
            case SistemaBaseDatos.DATETIME:
                if(tder == SistemaBaseDatos.TEXTO){
                    return new Objeto(izq.getFecha() + der.texto);
                }else{
                    // ERROR!
                    break;
                }
        }
        return null;
    }
    
}
