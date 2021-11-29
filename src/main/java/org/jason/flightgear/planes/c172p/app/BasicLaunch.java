package org.jason.flightgear.planes.c172p.app;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;

/**
 * Start the plane and throw it into the air.
 * 
 * @author jason
 *
 */
public class BasicLaunch {
	
	private static void basicUpdatesApp(C172P plane) {
		//assume start unpaused;
		
		plane.setPause(true);

		//place in the air
		plane.setAltitude(3000);
		plane.setHeading(0);
		
		
		plane.setPause(false);
		
		//initial drop. allow to level off
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(.90);
		plane.setPause(false);
		
		//let fly for a bit
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//reposition
		plane.setPause(true);
		
		plane.setAltitude(9000);
		plane.setHeading(270);

		plane.setPause(false);

		//let fly for a bit
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) throws InvalidTelnetOptionException, Exception {
		C172P plane = new C172P();
		
		plane.startupPlane();

		//wait for startup to complete and telemetry reads to arrive
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		basicUpdatesApp(plane);
		
		plane.shutdown();
	}
}
