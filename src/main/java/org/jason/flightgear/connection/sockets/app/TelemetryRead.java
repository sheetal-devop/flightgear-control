package org.jason.flightgear.connection.sockets.app;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jason.flightgear.connection.sockets.FlightGearTelemetryConnection;

/**
 * Simple telemetry read from the flightgear sockets telemetry port. Doesn't start or modify the state of the plane.
 * 
 * @author jason
 *
 */
public class TelemetryRead {
	private final static String FG_SOCKETS_HOST = "localhost";
	private final static int FG_SOCKETS_TELEM_PORT = 6501;

	private FlightGearTelemetryConnection fgSocketsClient;
	
	public TelemetryRead() throws SocketException, UnknownHostException  {
		fgSocketsClient = new FlightGearTelemetryConnection(FG_SOCKETS_HOST, FG_SOCKETS_TELEM_PORT);
	}
	
	public String readTelemetry() throws IOException {
		return fgSocketsClient.readTelemetry();
	}
		
	public static void main(String[] args) {
		
		TelemetryRead telemetryReader = null;
		
		int readSleep = 3000;
		int maxCycles = 50;
		
		try {
			telemetryReader = new TelemetryRead();
			
			for(int i = 0; i< maxCycles; i++) {
				System.out.println("========\n" + telemetryReader.readTelemetry() + "\n========");
				
				try {
					Thread.sleep(readSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
