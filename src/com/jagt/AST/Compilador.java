/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.GUI.Servidor;
import com.jagt.Logica.SistemaBaseDatos;

/**
 *
 * @author Jose Antonio
 */
public class Compilador {
    
    // Instancia unica de la base de datos
    SistemaBaseDatos bd = SistemaBaseDatos.getInstance();
    String[] lineas;
    
    public Compilador(NodoParser nodo){
        ejecutarSentencias(nodo);
        lineas = SistemaBaseDatos.textoCompilado.split("\n");
    }
    
    private void ejecutarSentencias(NodoParser nodo){
        switch(nodo.nombre()){
            case "CREAR":
            case "USAR":
            case "ALTERAR":
            case "OTORGAR":
            case "DENEGAR":
            case "BACKUP":
            case "RESTAURAR":
            case "INSERTAR":
            case "ELIMINAR":
            case "SELECCIONAR":
            case "ACTUALIZAR":
            case "BORRAR":
            case "DECLARAR":
            case "ASIGNAR":
            case "IMPRIMIR":
            case "LLAMADA":
            case "RETORNO":
            case "DETENER":
            case "SI":
            case "SELECCIONA":
            case "PARA":
            case "MIENTRAS":
            case "CONTAR":
        }
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
                bd.crearMetodo();
                break;
        }
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
            NodoParser valor = com.hijos().get(0);
            switch(valor.hijos().get(0).nombre()){
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
                    nuevo.setForanea(valor.hijos().get(1).valor());
                    break;
                default:
                    // UNICO
                    nuevo.setUnica(true);
                    break;
            }
        }
        return nuevo;
    }
    
    /*********************************************************
     * EVALUAR EXPRESION
     *********************************************************/
    private Objeto evaluarExpresion(NodoParser nodo){
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
                        return new Objeto(nodo.hijos().get(0).valor());
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
                        break;
                    case "ID":
                        break;
                    default:
                        return evaluarExpresion(nodo.hijos().get(0));
                }
        }
        return null;
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
                    if(izq.getFecha().compareTo(der.getFecha()) > 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.getFecha().compareTo(der.getFecha()) > 0){
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
                    if(izq.getFecha().compareTo(der.getFecha()) >= 0){
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
                    if(izq.getFecha().compareTo(der.getFecha()) < 0){
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
                    if(izq.getFecha().compareTo(der.getFecha()) <= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.getFecha().compareTo(der.getFecha()) <= 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
            }
        }
        return null;
    }
    
    private Objeto evaluarIGUAL(Objeto izq,Objeto der){
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
                    if(izq.getFecha().compareTo(der.getFecha()) == 0){
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
                    if(izq.getFecha().compareTo(der.getFecha()) != 0){
                        return new Objeto(true);
                    }else{
                        return new Objeto(false);
                    }
                case SistemaBaseDatos.DATETIME:
                    if(izq.getFecha().compareTo(der.getFecha()) != 0){
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
