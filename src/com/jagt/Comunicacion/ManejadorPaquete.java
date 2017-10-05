/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Comunicacion;

import com.jagt.AST.ErrorUSQL;
import com.jagt.AST.Historia;
import com.jagt.GUI.Servidor;
import com.jagt.Logica.Registro;
import com.jagt.Logica.SistemaBaseDatos;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

/**
 *
 * @author Jose Antonio
 */
public class ManejadorPaquete {
    
    // Tipo paquete
    public static final int USQL = 0;
    public static final int ARBOL = 1;
    public static final int LOGIN = 2;
    public static final int LOGOUT = 3;
    public static final int REPORTE = 4;
    
    // Lista de errores, consultas
    public LinkedList<ErrorUSQL> errores;
    public LinkedList<String> mensajes;
    public int tipoPaquete;
    public LinkedList<LinkedList<Registro>> consultas; // Varios!
    public LinkedList<Historia> historial;
    public boolean login;
    public String usuario;
    public String reporteHTML;
    
    // LOGIN
    public ManejadorPaquete(String u,boolean sesion){
        this.login = sesion;
        this.usuario = u;
        this.tipoPaquete = LOGIN;
        Servidor.usuario = this.usuario;
    }
    
    // LOGOUT
    public ManejadorPaquete(int tipo){
        this.tipoPaquete = tipo;
        Servidor.usuario = "";
        String fecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        Servidor.getInstance().concatenarMensaje(">>> ["+fecha+"]: El usuario ha cerrado sesion "+Servidor.usuario+"\n");
    }

    public ManejadorPaquete(LinkedList<String> mensajes, LinkedList<LinkedList<Registro>> consultas, 
            LinkedList<Historia> historias) {
        this.mensajes = mensajes;
        this.consultas = consultas;
        this.historial = historias;
        this.tipoPaquete = USQL;
    }
    
    // Defecto!
    public ManejadorPaquete() {
        this.tipoPaquete = -1;
    }
    
    // Reporte
    public ManejadorPaquete(String cad){
        this.reporteHTML = cad;
        this.tipoPaquete = REPORTE;
    }
    
}
