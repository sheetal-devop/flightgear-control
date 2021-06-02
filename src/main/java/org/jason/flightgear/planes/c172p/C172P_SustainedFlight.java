package org.jason.flightgear.planes.c172p;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;

public class C172P_SustainedFlight {
		
	public static void main(String [] args) throws InvalidTelnetOptionException, IOException {
		C172P plane = new C172P();
		
		//true north
		double targetHeading = 0;
		
		plane.startupPlane();

		//wait for startup to complete and telemetry reads to arrive
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//////////////////
		
		//assume start unpaused;
		
		plane.setPause(true);

		//place in the air
		plane.setAltitude(5000);
		
		//retract landing gear if not fixed

		//head north
		plane.setHeading(targetHeading);
		
		
		plane.setPause(false);
		
		//initial drop. allow to level off
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//////
		//initial check that we've leveled off
		plane.altitudeCheck(500, 3000.0);
		plane.pitchCheck(20, 3.0);
		plane.rollCheck(10, 0.0);
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(0.80);
		plane.setPause(false);
		
		//control any roll
		
		boolean running = true;
		int cycles = 0;
		int maxCycles = 50* 1000;
			
		int cycleSleep = 5000;
		
		while(running && cycles < maxCycles) {
			
			plane.altitudeCheck(500, 3000.0);
			
			plane.pitchCheck(20, 3.0);
			
			plane.rollCheck(10, 0.0);
			
			plane.headingCheck(20, targetHeading);
			
			try {
				Thread.sleep(cycleSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cycles++;
		}
		


		//let fly for a bit
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		plane.shutdown();
	}
}
