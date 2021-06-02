package org.jason.flightgear.planes.c172p;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;

public class C172P_Run {
	
	private static void minAltitudeCheck(C172P plane, int minAlt, int targetAlt) {
		//read altitude
		//if altitude is lower than minAlt: pause, set to target, unpause
		
	}
	
	private static void pitchCheck(C172P Plane, int maxDifference, int targetPitch) {
		//read pitch
		//if pitch is too far from target in +/- directions, set to target
	}
	
	private static void rollCheck(C172P Plane, int maxDifference, int targetPitch) {
		
	}
	
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) throws InvalidTelnetOptionException, IOException {
		C172P plane = new C172P();
		
		plane.startupPlane();

		//wait for startup to complete and telemetry reads to arrive
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		basicUpdatesApp(plane);
		
		plane.shutdown();
	}
}
