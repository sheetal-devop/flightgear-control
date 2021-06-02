package org.jason.flightgear.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class SocketsTelemetryTestUDP {
	
	private final static String FG_SOCKETS_HOST = "localhost";
	private final static int FG_SOCKETS_TELEM_PORT = 6501;
	private final static int FG_SOCKETS_INPUT_PORT = 6601;

	
	String[] properties = {
			"/controls/engines/current-engine/throttle",
			"/controls/engines/current-engine/mixture",
			"/controls/flight/rudder",
			"/controls/flight/aileron",
			"/controls/flight/elevator",
			"/controls/flight/flaps",
			"/controls/gear/gear-down",
			"/controls/gear/brake-parking",
			"/environment/relative-humidity",
			"/environment/effective-visibility-m",
			"/environment/temperature-degf",
			"/environment/dewpoint-degc",
			"/environment/pressure-inhg",
			"/environment/visibility-m",
			"/environment/wind-speed-kt",
			"/environment/wind-from-north-fps",
			"/environment/wind-from-east-fps",
			"/environment/wind-from-down-fps",
			"/sim/speed-up",
			"/sim/time/local-day-seconds",
			"/sim/time/elapsed-sec",
			"/sim/freeze/master",
			"/sim/freeze/time",
			"/orientation/pitch-deg",
			"/orientation/heading-deg", 
			"/orientation/heading-magnetic-deg",
			"/orientation/track-magnetic-deg",
			"/orientation/beta-deg",
			"/orientation/alpha-deg",
			"/position/longitude-deg",
			"/position/latitude-deg",
			"/position/longitude-string",
			"/position/latitude-string",
			"/position/ground-elev-ft",
			"/engines/engine/running",
			"/consumables/fuel/total-fuel-gals",
			"/consumables/fuel/total-fuel-lbs",
			"/controls/flight/elevator",
			"/controls/flight/rudder",
			"/controls/flight/aileron",
			"/controls/flight/flaps",
			"/controls/gear/gear-down",
			"/controls/gear/brake-parking",
			"/controls/engines/current-engine/throttle",
			"/controls/engines/current-engine/mixture", 
			"/velocities/airspeed-kt",
			"/velocities/groundspeed-kt",
			"/velocities/vertical-speed-fps",
		};
	
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
					// TODO Auto-generated catch block
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
				
				fgInputSocket.close();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			

		} 
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(fgConnection != null) {
				try {
					fgConnection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
