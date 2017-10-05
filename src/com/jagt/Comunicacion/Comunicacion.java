/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Comunicacion;

import com.jagt.AST.Historia;
import com.jagt.AST.Objeto;
import com.jagt.Analizadores.Paquetes.SintacticoPaquetes;
import com.jagt.Analizadores.USQL.Sintactico;
import com.jagt.GUI.Servidor;
import com.jagt.Logica.Registro;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jose Antonio
 */
public class Comunicacion extends Thread implements Runnable {
    
    private static Comunicacion comunicacion = null;
    int contador;
    SintacticoPaquetes parser;
    
    public static Comunicacion getInstance(){
        if(comunicacion == null){
            comunicacion = new Comunicacion();
        }
        return comunicacion;
    }
    
    private Comunicacion(){
    }
    
    public void resetear(){
        this.contador = 0;
    }
    
    @Override
    public void run() {
        //REVISAR SI HAY CLIENTES QUE NO HAN PAGADO DOS (Cada 6 de Mes)
        while(true){
            ServerSocket s = null;
            try {
                s = new ServerSocket(5000);
                Socket ss=s.accept();
                PrintWriter pw = new PrintWriter(ss.getOutputStream(),true);
                // Mensaje del cliente!
                byte[] bytes = new byte[50000];
                ss.getInputStream().read(bytes);
                String str = new String(bytes, StandardCharsets.UTF_8);
                String cadena = "";
                for(int i = 0; i < str.length(); i++){
                    char c = (char)0;
                    if(str.charAt(i) != c){
                        cadena += str.charAt(i);
                    }else{
                        break;
                    }
                }
                System.out.println(cadena);
                // Envia servidor al cliente
                long duracion = System.currentTimeMillis();
                ManejadorPaquete mp = ejecutarPaquete(cadena);
                if(mp.tipoPaquete == -1){
                    // Error!
                }else{
                    // Tipo Paquete!
                    if(mp.tipoPaquete == ManejadorPaquete.LOGIN){
                        Thread.sleep(100);
                        pw.println(getPaqueteSesion(mp.usuario,mp.login));
                    }else if(mp.tipoPaquete == ManejadorPaquete.USQL){
                        // Enviar paquete de Resultados
                        long tiempo = System.currentTimeMillis() - duracion;
                        mp.mensajes.add(">> Instrucciones realizadas en: "+Long.toString(tiempo)+" ms");
                        mp.mensajes.add(">> "+mp.consultas.size()+" consultas realizadas");
                        for(LinkedList<Registro> consulta : mp.consultas){
                            Thread.sleep(100);
                            mp.mensajes.add(">> "+consulta.size()+" filas recuperadas");
                            pw.println(getPaqueteConsulta(consulta));
                        }
                        // Enviar paquete de Mensajes
                        for(String mensaje : mp.mensajes){
                            Thread.sleep(100);
                            pw.println(getPaqueteMensaje(mensaje));
                        }
                        // Enviar paquete de Historias
                        for(Historia h : mp.historial){
                            Thread.sleep(100);
                            pw.println(getPaqueteHistoria(h));
                        }
                    }else if(mp.tipoPaquete == ManejadorPaquete.REPORTE){
                        Thread.sleep(100);
                        pw.println("REPORTE!!!");
                        Thread.sleep(100);
                        pw.println(mp.reporteHTML);
                    }
                }
//                for(int i = 0; i < 10; i++){
//                    Thread.sleep(100);
//                    pw.println(i);
//                }
                Thread.sleep(100);
                pw.println("201314863");
            } catch (IOException ex) {
                Logger.getLogger(Comunicacion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Comunicacion.class.getName()).log(Level.SEVERE, null, ex);
            }        
            finally{
                try {
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(Comunicacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    // Ejecucion de paquetes!
    public ManejadorPaquete ejecutarPaquete(String cadena){
        ManejadorPaquete mp = new ManejadorPaquete();
        InputStream is = new ByteArrayInputStream(cadena.getBytes());
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
            return mp;
        }
    }
    
    // CREACION DE LOS PAQUETES A ENVIAR
    public String getPaqueteSesion(String usuario,boolean sesion){
        String paquete = "[\n'login':\n [\n 'usuario': "+usuario+",\n'login': ";
        if(sesion){
            paquete += "true\n";
            Servidor.getInstance().concatenarMensaje("El usuario "+usuario+" ha ingresado!\n");
        }else{
            paquete += "false\n";
        }
        paquete += "]\n]";
        return paquete;
    }

    private String getPaqueteConsulta(LinkedList<Registro> consultas) {
        String paquete = "[\n'paquete': 'usql':,\n'datos': [\n";
        // Recorrer la consulta!
        for(Registro r : consultas){
            paquete += "[";
            for(Objeto col : r.getColumnas()){
                paquete += "\""+col.getNombre()+"\": \""+col.texto + "\",\n";
            }
            paquete += "]\n";
        }
        paquete += "]\n]\n";
        return paquete;
    }
    
    private String getPaqueteMensaje(String mensaje){
        String paquete = "[\n'msg': \""+mensaje+"\",\n]\n";
        return paquete;
    }
    
    private String getPaqueteHistoria(Historia h){
        // HISTORIA ':' '[' FECHA ':' expresion ',' INSTRUCCION ':' expresion ',' FILA ':' expresion ',' TMP ':' expresion ',' MSG ':' expresion ']'
        String paquete = "[\n'historia': [\n 'fecha': \""+h.fecha+"\"," +
                         " 'instruccion': \""+h.operacion+"\"," +
                         " 'fila': \""+h.lineas+"\"," +
                         " 'tiempo': \""+h.duracion+"\",";
        if(h.exito){
            paquete += " 'msg': \"exito\"";
        }else{
            paquete += " 'msg': \"fracaso\"";
        }
        paquete += "\n]\n]";
        return paquete;
    }

    private String getPaqueteReporte(LinkedList<Registro> consulta) {
        String paquete = "";
        return paquete;
    }
    
}
