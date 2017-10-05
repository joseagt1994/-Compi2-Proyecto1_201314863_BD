/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.AST;

import com.jagt.GUI.Servidor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Jose Antonio
 */
public class Historia {
    
    public String operacion,duracion,fecha;
    public int lineas;
    public boolean exito;

    public Historia(String operacion, String duracion, int lineas, boolean exito) {
        this.operacion = operacion;
        this.duracion = duracion;
        this.lineas = lineas;
        this.exito = exito;
        this.fecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        Servidor.getInstance().concatenarMensaje(">>> ["+fecha+"] Instruccion realizada en "+duracion+" ms.. "+this.operacion+"\n");
    }
    
    
}
