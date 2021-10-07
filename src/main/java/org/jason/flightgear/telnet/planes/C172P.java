package org.jason.flightgear.telnet.planes;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;

public class C172P {
	
	private final static String FG_TELNET_HOST = "localhost";
	private final static int FG_TELNET_PORT = 5501;

	private FlightGearManagerTelnet mgr;
	
	public C172P() throws InvalidTelnetOptionException, IOException {
		mgr = new FlightGearManagerTelnet(FG_TELNET_HOST, FG_TELNET_PORT);
	}
	
	public void startupPlane() throws IOException {
		
		try {
			System.out.println("Running before: " + mgr.getPropertyValue("/engines/active-engine/running") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//nasal script to autostart from c172p menu
		mgr.runNasal("c172p.autostart();");
		
		//startup may be asynchronous so we have to wait for the next prompt 
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//verify
	}
	
	public void shutdown() {
		//plane shutdown
		
		//FGM shutdown
		if(mgr != null) {
			//end simulator - needs streams to write commands
			mgr.exit();
			
			//disconnect streams
			mgr.disconnect();
			
		}
	}
	
	public void flyPlane() throws IOException {
		
		System.out.println("After /engines: " + mgr.sendCommandReadRawOutput("ls /engines/") );
		

		System.out.println("-----\nAfter /engines/active-engine: " + mgr.sendCommandReadRawOutput("ls /engines/active-engine") );
		
		
		System.out.println("-----\nRunning after: " + mgr.getPropertyValue("/engines/active-engine/running") );
	}
	
	public static void main(String[] args) {
		
		//fork execution of shell script and wait for telnet port to open
		//use shell script because we may want to run it from elsewhere
		
		
		C172P plane = null;
		
		try {
			plane = new C172P();
			
			plane.startupPlane();
			
			plane.flyPlane();

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
