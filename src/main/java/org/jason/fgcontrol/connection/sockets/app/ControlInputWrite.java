package org.jason.fgcontrol.connection.sockets.app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * A test of updating fields in a custom-defined input protocol.
 *
 */
public class ControlInputWrite {

	public static void performWrite(String host, int port, String payload) {
        
        System.out.println("FG Control input: " + payload);
        
        DatagramSocket fgInputSocket = null;
        
        try {
	        byte[] fgInputPayload = payload.getBytes(Charset.forName("UTF-8"));
	        
	        DatagramPacket fgInputPacket = new DatagramPacket(
	                fgInputPayload, 
	                fgInputPayload.length, 
	                InetAddress.getByName(host), 
	                port
	        );
	        
	        fgInputPacket.setData(payload.getBytes(Charset.forName("UTF-8")));
	        
	        fgInputSocket = new DatagramSocket();
	        
	        fgInputSocket.setSoTimeout(5000);
	        
	        fgInputSocket.send(fgInputPacket);
        }
        catch(Exception e) {
        	e.printStackTrace();
        } 
        finally {
        	if(fgInputSocket != null) {
        		fgInputSocket.close();
        	}
        }
	}
	
    public static void main(String[] args) {
        
        if(args.length != 2) {
        	System.err.println("Usage: ControlInputWrite [host] [inputPort]");
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

		for (int i = 0; i < 5; i++) {

			// test input//////////////
			// read on next loop iteration

			// fgInputSocket = new DatagramSocket(FG_SOCKETS_INPUT_PORT,
			// InetAddress.getByName(FG_SOCKETS_HOST) );

			// rotate the plane in 15 degree chunks clockwise
			String fgInput = "\n" + ((double) i * 15.0) % 360.0 + "," + 0.0 + "," + 0.0 + "\n";

			System.out.println("FG Control input: " + fgInput);

			ControlInputWrite.performWrite(host, inputPort, fgInput);
		}
    }
    
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
}
