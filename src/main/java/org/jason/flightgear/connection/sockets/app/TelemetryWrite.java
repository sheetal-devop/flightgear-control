package org.jason.flightgear.connection.sockets.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class TelemetryWrite {
	
	private final static String FG_SOCKETS_HOST = "localhost";
	private final static int FG_SOCKETS_TELEM_PORT = 6501;
	private final static int FG_SOCKETS_INPUT_PORT = 6601;

	public static void main(String[] args) {
		
		Socket fgConnection = null; 
		PrintWriter output = null;
		BufferedReader input = null;
				
	    byte[] receivingDataBuffer = null;
		
		try {
			DatagramSocket fgTelemetrySocket = null;
		    DatagramPacket fgTelemetryPacket = null;
		    
			DatagramSocket fgInputSocket = null;
		    DatagramPacket fgInputPacket = null;

			receivingDataBuffer = new byte[1024];
			
			fgTelemetryPacket = new DatagramPacket(
					receivingDataBuffer, 
					receivingDataBuffer.length
				);

			for(int i = 0; i< 5; i++) {
				//test output////////////

//				//need to restablish datagram socket connection on every read, or else updates don't arrive
				fgTelemetrySocket = new DatagramSocket(FG_SOCKETS_TELEM_PORT, InetAddress.getByName(FG_SOCKETS_HOST) );

				fgTelemetrySocket.receive(fgTelemetryPacket);
				
				String receivedData = new String(fgTelemetryPacket.getData());
				System.out.println("Received telemetry from flightgear: " + receivedData);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
								
				fgTelemetrySocket.close();
				
				//test input//////////////
				//read on next loop iteration
				
				//fgInputSocket = new DatagramSocket(FG_SOCKETS_INPUT_PORT, InetAddress.getByName(FG_SOCKETS_HOST) );
				
				/*
				/controls/flight/elevator,/controls/flight/aileron
				 */
				String fgInput = "\n" + ((double)i/10.0) + "," + ((2*(double)i)/10) +"\n";
				
				System.out.println("FG Control input: " + fgInput);
				
				byte[] fgInputPayload = fgInput.getBytes(Charset.forName("UTF-8"));
				
				fgInputPacket = new DatagramPacket(
						fgInputPayload, 
						fgInputPayload.length, 
						InetAddress.getByName(FG_SOCKETS_HOST), 
						FG_SOCKETS_INPUT_PORT
				);
				
				fgInputPacket.setData(fgInput.getBytes(Charset.forName("UTF-8")));
				
				fgInputSocket = new DatagramSocket();
				
				fgInputSocket.send(fgInputPacket);

	            fgInputSocket.setSoTimeout(5000);
				
				fgInputSocket.close();
				
				try {
					Thread.sleep(1000);
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
