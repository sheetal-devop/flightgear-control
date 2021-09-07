package org.jason.flightgear.planes.c172p.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P_OutAndBack {
		
	private static Logger logger = LoggerFactory.getLogger(C172P_OutAndBack.class);

	
	private final static int TARGET_ALTITUDE = 5000;
	private final static int TARGET_HEADING = 0;
	
	private static void initialDrop(C172P plane) {
		//assume start unpaused;
		
		plane.setPause(true);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
		//retract landing gear if not fixed

		//head north
		plane.setHeading(TARGET_HEADING);
				
		plane.setPause(false);
		
		//set while not paused. this functions more like a boost- 
		//the plane can be acceled or decelled to the specified speed, 
		//but then the fdm takes over and stabilizes the air speed
		plane.setAirSpeed(100);
		
		//initial drop. allow to level off
		try {
			Thread.sleep(40*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//////
		//initial check that we've leveled off
		plane.altitudeCheck(500, TARGET_ALTITUDE);
		plane.pitchCheck(4, 3.0);
		plane.rollCheck(4, 0.0);
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(0.80);
		plane.setPause(false);
	}
	
	private static void flyHeading(C172P plane, double targetHeading, double targetAltitude) {
		int cycles = 0;
		int maxCycles = 10;
			
		int cycleSleep = 8000;
		
		int headingDifference = 10; 
		
		while(cycles < maxCycles) {
			
			logger.info("======================\nCycle {} start. Target heading: {} ", cycles, targetHeading);
			
			//check altitude first, if we're in a nose dive that needs to be corrected first
			plane.altitudeCheck(500, targetAltitude);
			
			
			plane.yawCheck(2, 0);
			plane.pitchCheck(4, 3.0);
			plane.rollCheck(4, 0.0);
			
			//check heading last, correct pitch/roll/yaw first otherwise the plane will probably drift off heading quickly
			plane.headingCheck(headingDifference, targetHeading);
			
			try {
				Thread.sleep(cycleSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cycles++;
			
			logger.info("Cycle end\n======================");
		}
	}
	
	public static void main(String [] args) throws InvalidTelnetOptionException, IOException {
		C172P plane = new C172P();
		
		plane.setDamageEnabled(false);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//true north
		double targetHeading = 0;
		double targetAltitude = 7000;
		
		//TODO: check if engine running, plane is in the air, speed is not zero
		//
		
		plane.startupPlane();

		//wait for startup to complete and telemetry reads to arrive
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//////////////////
		initialDrop(plane);
		
		boolean running = true;
		int cycles = 0;
		int maxCycles = 5;
		
		while(running && cycles < maxCycles) {
			
			//out -> heading of 0, held for a while
			
			targetHeading = 0;
			flyHeading(plane, targetHeading, targetAltitude);
			
			targetHeading = 180;
			flyHeading(plane, targetHeading, targetAltitude);
			
			//back -> heading of 180, held for a while
			
			cycles++;
		}
		
		
		plane.shutdown();
	}
}
