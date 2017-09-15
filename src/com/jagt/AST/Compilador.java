/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.GUI.Servidor;
import com.jagt.Logica.Registro;
import com.jagt.Logica.SistemaBaseDatos;
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
    
    // Temporales
    Hashtable<String,Objeto> temporales; // -> Tabla,Columna
    int numeroTablas = 0;
    
    public Compilador(NodoParser nodo){
        this.variables = new LinkedList<Hashtable<String,Objeto>>();
        lineas = SistemaBaseDatos.textoCompilado.split("\n");
        aumentarAmbito();
        ejecutarSentencias(nodo);
        disminuirAmbito();
    }
    
    private void ejecutarSentencias(NodoParser nodo){
        switch(nodo.nombre()){
            case "CUERPOS":
                for(NodoParser hijo : nodo.hijos()){
                    ejecutarSentencias(hijo);
                }
                break;
            case "CREAR":
                crear(nodo);
                break;
            case "USAR":
                Servidor.bd_actual = nodo.hijos().get(0).valor();
                bd.usarBD(Servidor.bd_actual);
                break;
            case "ALTERAR":
            case "OTORGAR":
            case "DENEGAR":
            case "BACKUP":
                // MINIMO!
                break;
            case "RESTAURAR":
                // MINIMO!
                break;
            case "INSERTAR":
                // MINIMO!
                insertar(nodo);
                break;
            case "ELIMINAR":
            case "SELECCIONAR":
                // MINIMO!
                evaluarSeleccionar(nodo);
                break;
            case "ACTUALIZAR":
                // MINIMO!
                actualizar(nodo);
                break;
            case "BORRAR":
            case "DECLARAR":
                // MINIMO!
                declarar(nodo);
                break;
            case "ASIGNAR":
                // MINIMO!
                break;
            case "IMPRIMIR":
                // MINIMO!
                break;
            case "LLAMADA":
            case "RETORNO":
            case "DETENER":
            case "SI":
                // MINIMO!
                break;
            case "SELECCIONA":
                // MINIMO!
                break;
            case "PARA":
            case "MIENTRAS":
                // MINIMO!
                break;
            case "CONTAR":
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
        if(var != null){
            if(var.getTipo() == dato.getTipo()){
                for(Hashtable<String,Objeto> ambito : variables){
                    if(ambito.containsKey(nombre)){
                        ambito.put(nombre, dato);
                    }
                }
            }else{
                // ERROR! No coincide con el tipo!
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
        switch(nodo.hijos().get(0).nombre()){
            case "BD":
                // CREAR -> BD ID
                bd.crearBD(nodo.hijos().get(1).valor(),Servidor.logueado.getCodigo());
                break;
            case "TABLA":
                // CREAR -> TABLA ID CAMPOS
                bd.crearTabla(crearTabla(nodo.hijos().get(1).valor(),nodo.hijos().get(2)));
                break;
            case "OBJETO":
                // CREAR -> OBJETO ID ATRIBUTOS
                bd.crearObjeto(crearObjeto(nodo.hijos().get(1).valor(),nodo.hijos().get(2)));
                break;
            case "USUARIO":
                // CREAR -> USUARIO ID CADENA
                bd.crearUsuario(nodo.hijos().get(1).valor(),nodo.hijos().get(2).valor());
                break;
            default:
                // FUNCION O PROCEDIMIENTO
                bd.crearMetodo(crearProcedimiento(nodo));
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
            params.add(new Parametro(p.hijos().get(1).valor(),bd.obtenerTipo(p.hijos().get(0).valor())));
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
    private void evaluarSeleccionar(NodoParser nodo){
        // SELECCIONAR -> (TODO | LISTA_ACCESO) IDS (EXP)? (ORDENAR)?
        // Llenar lista de IDS
        LinkedList<String> ids = new LinkedList<String>();
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
        }
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
    private void actualizar(NodoParser nodo){
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
        }else{
            // Error!
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
    
    // Asignar
    
    /*********************************************************
     * EVALUAR EXPRESION
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
                        break;
                    case "ACCESO":
                        // ACCESO -> (ID | VAR)(. ID)*
                        return getAcceso(nodo.hijos().get(0));
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
                    return new Objeto(izq.numero / der.numero);
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
