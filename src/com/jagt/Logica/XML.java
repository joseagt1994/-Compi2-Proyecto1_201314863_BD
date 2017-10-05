/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Logica;

import com.jagt.AST.*;
import com.jagt.Analizadores.XML.Master.*;
import com.jagt.Analizadores.XML.Usuarios.*;
import com.jagt.Analizadores.XML.Tablas.*;
import com.jagt.Analizadores.XML.Registros.*;
import com.jagt.Analizadores.XML.Objetos.*;
import com.jagt.Analizadores.XML.Metodos.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class XML {
    
    // Sirve para manejar los compiladores del XML
    private static SintacticoMaster parser = null;
    private static SintacticoUsuarios parserU = null;
    private static SintacticoTablas parserT = null;
    private static SintacticoRegistros parserR = null;
    private static SintacticoMetodos parserM = null;
    private static SintacticoObjetos parserO = null;
    
    // Devuelve el listado de las bases de datos encontradas en el master.usac
    public static LinkedList<DataBase> getBasesDatos(String texto){
        LinkedList<DataBase> bases = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parser == null) parser = new SintacticoMaster(is);   
        else parser.ReInit(is);
        try
        {
            bases = parser.inicio();
        }
        catch (Exception e)
        {
            bases = new LinkedList<DataBase>();
        }
        catch (Error e)
        {
            bases = new LinkedList<DataBase>();
        }
        finally
        {
            return bases;
        }
    }
    
    // Devuelve el listado de Usuarios encontrados en el archivo de usuarios.usac
    public static LinkedList<Usuario> getUsuarios(String texto){
        LinkedList<Usuario> usuarios = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parserU == null) parserU = new SintacticoUsuarios(is);   
        else parserU.ReInit(is);
        try
        {
            usuarios = parserU.inicio();
        }
        catch (Exception e)
        {
            usuarios = new LinkedList<Usuario>();
        }
        catch (Error e)
        {
            usuarios = new LinkedList<Usuario>();
        }
        finally
        {
            return usuarios;
        }
    }
    
    // Devuelve el listado de las tablas contenidas en una base de datos
    public static LinkedList<Tabla> getTablas(String texto){
        LinkedList<Tabla> tablas = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parserT == null) parserT = new SintacticoTablas(is);   
        else parserT.ReInit(is);
        try
        {
            tablas = parserT.inicio();
        }
        catch (Exception e)
        {
            tablas = new LinkedList<Tabla>();
        }
        catch (Error e)
        {
            tablas = new LinkedList<Tabla>();
        }
        finally
        {
            return tablas;
        }
    }
    
    // Devuelve los registros de una tabla 
    public static LinkedList<Registro> getRegistros(String texto){
        LinkedList<Registro> registros = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parserR == null) parserR = new SintacticoRegistros(is);   
        else parserR.ReInit(is);
        try
        {
            registros = parserR.inicio();
        }
        catch (Exception e)
        {
            registros = new LinkedList<Registro>();
        }
        catch (Error e)
        {
            registros = new LinkedList<Registro>();
        }
        finally
        {
            return registros;
        }
    }
    
    // Devuelve el listado de metodos de una base de datos
    public static LinkedList<Metodo> getMetodos(String texto){
        LinkedList<Metodo> metodos = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parserM == null) parserM = new SintacticoMetodos(is);   
        else parserM.ReInit(is);
        try
        {
            metodos = parserM.inicio();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            metodos = new LinkedList<Metodo>();
        }
        catch (Error e)
        {
            System.out.println(e.getMessage());
            metodos = new LinkedList<Metodo>();
        }
        finally
        {
            return metodos;
        }
    }
    
    // Devuelve el listado de plantillas de objetos de una base de datos
    public static LinkedList<Objeto> getObjetos(String texto){
        LinkedList<Objeto> objetos = null;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parserO == null) parserO = new SintacticoObjetos(is);   
        else parserO.ReInit(is);
        try
        {
            objetos = parserO.inicio();
        }
        catch (Exception e)
        {
            objetos = new LinkedList<Objeto>();
        }
        catch (Error e)
        {
            objetos = new LinkedList<Objeto>();
        }
        finally
        {
            return objetos;
        }
    }
    
}
