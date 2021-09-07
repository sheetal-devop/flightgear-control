package org.jason.flightgear.planes.c172p.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.sockets.FlightGearManagerSockets;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;

public class C172P_Telemetry_Test {
	
	private final static String FG_SOCKETS_HOST = "localhost";
	
	private final static int FG_SOCKETS_TELEM_PORT = 6501;
	private final static int FG_SOCKETS_INPUT_PORT = 6601;

	private FlightGearManagerSockets fgSocketsClient;
	
	public C172P_Telemetry_Test() throws InvalidTelnetOptionException, IOException {
		fgSocketsClient = new FlightGearManagerSockets(FG_SOCKETS_HOST, FG_SOCKETS_TELEM_PORT);
		
		//maybe try to get control schema from input file
	}
	
	public String readTelemetry() throws IOException {
		return fgSocketsClient.readTelemetry();
	}
	
	
	
	public void shutdown() {
		//plane shutdown
		
		//FGM shutdown
		if(fgSocketsClient != null) {
			//end simulator - needs streams to write commands
			fgSocketsClient.shutdown();
			
		}
	}
	

	
	public static void main(String[] args) {
		
		//fork execution of shell script and wait for telnet port to open
		//use shell script because we may want to run it from elsewhere
		
		
		C172P_Telemetry_Test plane = null;
		
		try {
			plane = new C172P_Telemetry_Test();
			
			for(int i = 0; i< 5; i++) {
				System.out.println("========\n" + plane.readTelemetry() + "\n========");
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (InvalidTelnetOptionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (plane != null) {
				plane.shutdown();
			}
		}
	}
}
