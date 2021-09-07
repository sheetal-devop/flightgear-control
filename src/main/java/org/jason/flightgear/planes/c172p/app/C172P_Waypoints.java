package org.jason.flightgear.planes.c172p.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P_Waypoints {
		
	private static Logger logger = LoggerFactory.getLogger(C172P_Waypoints.class);

	
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
	
	private static void flyToLocation(C172P plane, double lat, double lon) {
		
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

		
		//control any roll
		
		boolean running = true;
		int cycles = 0;
		int maxCycles = 50* 1000;
			
		int cycleSleep = 8000;
		
		while(running && cycles < maxCycles) {
			
			logger.info("Cycle {} start ======================", cycles);
			//check altitude first, if we're in a nose dive that needs to be corrected first
			plane.altitudeCheck(500, targetAltitude);
			
			//plane.yawCheck(2, 0);
			
			plane.pitchCheck(4, 3.0);
			
			plane.rollCheck(4, 0.0);
			
			//check heading last, correct pitch/roll/yaw first otherwise the plane will probably drift off heading quickly
			plane.headingCheck(15, targetHeading);
			

			
//			plane.setPause(true);
//			plane.forceStabilize(TARGET_HEADING, TARGET_ALTITUDE, 0, 3, 0);
//			plane.setPause(false);
			
			try {
				Thread.sleep(cycleSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cycles++;
			
			logger.info("Cycle end ======================");
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
