/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArbolAST;
    import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Jherson Sazo
 */
public class Servidor {

public static void main(String args[]) throws IOException{
    
    ServerSocket s = new ServerSocket(5000);
            try {
                Socket ss = s.accept();
                PrintWriter pw = new PrintWriter(ss.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader br1 = new BufferedReader(new InputStreamReader(ss.getInputStream()));

                System.out.println("Client connected..");
                while(true)
                {
                    System.out.println(br1.readLine());
                    System.out.println("Enter command:");
                    pw.println(br.readLine());
                    
                }
                //
                //ss.close();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            s.close();
       }
}
