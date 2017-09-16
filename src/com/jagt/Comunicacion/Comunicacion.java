/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.Comunicacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jose Antonio
 */
public class Comunicacion extends Thread implements Runnable {
    
    private static Comunicacion comunicacion = null;
    int contador;
    
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
                byte[] bytes = new byte[100000];
                ss.getInputStream().read(bytes);
                String str = new String(bytes, StandardCharsets.UTF_8);
                // Envia servidor al cliente
                for(int i = 0; i < 10; i++){
                    Thread.sleep(100);
                    pw.println(i);
                }
                Thread.sleep(100);
                pw.println("201314863");
            } catch (IOException ex) {
                Logger.getLogger(ServidorSocket.class.getName()).log(Level.SEVERE, null, ex);
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
    
}
