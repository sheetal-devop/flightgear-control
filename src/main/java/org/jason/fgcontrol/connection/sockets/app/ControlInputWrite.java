package org.jason.fgcontrol.connection.sockets.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class ControlInputWrite {

    public static void main(String[] args) {
        
        if(args.length != 2) {
        	System.err.println("Usage: TelemetryReadControlInputWrite [host] [inputPort]");
        	System.exit(-1);
        }
        
        int inputPort = -1;
        String host = args[0];
    	
        try {
        	inputPort = Integer.parseInt(args[1]);
        } catch (Exception e) {
        	
        } finally {
        	if(inputPort == -1 ) {
        		System.err.println("Invalid port");
        		System.exit(-1);
        	}
        }
        
        Socket fgConnection = null; 
        PrintWriter output = null;
        BufferedReader input = null;
                       
        try {            
            DatagramSocket fgInputSocket = null;
            DatagramPacket fgInputPacket = null;
            
            for(int i = 0; i< 5; i++) {
                
                //test input//////////////
                //read on next loop iteration
                
                //fgInputSocket = new DatagramSocket(FG_SOCKETS_INPUT_PORT, InetAddress.getByName(FG_SOCKETS_HOST) );
                
                /*	f15c orientation typically on port 5227 for F15C Beta
                 
                     <!-- orientation -->
			         <chunk>
			            <node>/orientation/heading-deg</node>
			            <name>heading-deg</name>
			            <type>float</type>
			         </chunk>
			         <chunk>
			            <node>/orientation/pitch-deg</node>
			            <name>pitch-deg</name>
			            <type>float</type>
			         </chunk>
			         <chunk>
			            <node>/orientation/roll-deg</node>
			            <name>roll-deg</name>
			            <type>float</type>
			         </chunk>
                 */
            	//rotate the plane in 15 degree chunks clockwise
                String fgInput = "\n" + ((double)i * 15.0) % 360.0 + "," + 0.0 + "," + 0.0 +"\n";
                
                System.out.println("FG Control input: " + fgInput);
                
                byte[] fgInputPayload = fgInput.getBytes(Charset.forName("UTF-8"));
                
                fgInputPacket = new DatagramPacket(
                        fgInputPayload, 
                        fgInputPayload.length, 
                        InetAddress.getByName(host), 
                        inputPort
                );
                
                fgInputPacket.setData(fgInput.getBytes(Charset.forName("UTF-8")));
                
                fgInputSocket = new DatagramSocket();
                
                fgInputSocket.setSoTimeout(5000);
                
                fgInputSocket.send(fgInputPacket);

                
                
                fgInputSocket.close();
                
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } 
        catch (UnknownHostException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            
            if(output != null) {
                output.close();
            }
            
            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(fgConnection != null) {
                try {
                    fgConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
