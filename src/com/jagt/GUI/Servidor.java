/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jagt.GUI;

import com.jagt.Analizadores.USQL.Sintactico;
import com.jagt.Comunicacion.*;
import com.jagt.Logica.SistemaBaseDatos;
import com.jagt.Logica.Usuario;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *
 * @author Jose Antonio
 */
public class Servidor extends javax.swing.JFrame {

    // Variables a usar en el servidor!!!
    public static String bd_actual = "";
    public static String usuario = "";
    public static int codUsuario = 1;
    public static String rutaMaestra = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\SistemaArchivos\\master.usac";
    public static String rutaUsuarios = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\SistemaArchivos\\usuarios.usac";
    public static String rutaBDS = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\SistemaArchivos\\BD\\";
    public static String rutaLOGS = "C:\\Users\\Jose Antonio\\Documents\\GitHub\\[Compi2]Proyecto1_201314863_BD\\src\\com\\jagt\\SistemaArchivos\\LOG\\";
    public static Usuario logueado = new Usuario(0,"","");
    
    private static Servidor server = null;
    
    public static Sintactico parser = null;
    
    public static Servidor getInstance(){
        if(server == null){
            server = new Servidor();
        }
        return server;
    }
    /**
     * Creates new form Servidor
     */
    private Servidor() {
        initComponents();
        // Iniciar procesos
//        SistemaBaseDatos bd = SistemaBaseDatos.getInstance();
//        //bd.crearBD("Prueba", logueado.getCodigo());
//        bd.crearUsuario("Jose", "123");
        Comunicacion puente = Comunicacion.getInstance();
        puente.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtConsola = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Consola del Servidor");

        txtConsola.setEditable(false);
        txtConsola.setBackground(new java.awt.Color(0, 0, 0));
        txtConsola.setColumns(20);
        txtConsola.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        txtConsola.setForeground(new java.awt.Color(255, 255, 255));
        txtConsola.setRows(5);
        txtConsola.setTabSize(12);
        txtConsola.setToolTipText("");
        txtConsola.setBorder(null);
        txtConsola.setCaretColor(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(txtConsola);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 551, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public void concatenarMensaje(String mensaje){
        txtConsola.append(mensaje);
    }
    
    public ManejadorPaquete ejecutarUSQL(String texto){
        ManejadorPaquete maneja = new ManejadorPaquete();
        SistemaBaseDatos.textoCompilado = texto;
        InputStream is = new ByteArrayInputStream(texto.getBytes());
        if(parser == null) parser = new Sintactico(is);   
        else parser.ReInit(is);
        try
        {
            maneja = parser.inicio();
            txtConsola.append("Se ejecuto correctamente!\n");
        }
        catch (Exception e)
        {
            txtConsola.append("Error:\n"+ e.getMessage()+"\n");
        }
        catch (Error e)
        {
            txtConsola.append("Error:\n"+ e.getMessage()+"\n");
        }
        finally
        {
            return maneja;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Servidor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Servidor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Servidor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Servidor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                getInstance().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtConsola;
    // End of variables declaration//GEN-END:variables

}
