package org.jason.flightgear.planes.c172p.app;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;
import org.jason.flightgear.planes.c172p.C172PFlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P_SustainedFlight {
		
	private static Logger logger = LoggerFactory.getLogger(C172P_SustainedFlight.class);

	
	private final static int TARGET_ALTITUDE = 5000;
	private final static int TARGET_HEADING = 0;
	
	private static void launch(C172P plane) {
		//assume start unpaused;
		
		plane.setPause(true);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
		//retract landing gear if not fixed

		//head north
		plane.setHeading(TARGET_HEADING);
				
		plane.setPause(false);
		
		//set while not paused. this functions more like a boost- 
		//the plane can be acceled or deceled to the specified speed, 
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
		C172PFlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
		C172PFlightUtilities.pitchCheck(plane, 4, 3.0);
		C172PFlightUtilities.rollCheck(plane, 4, 0.0);
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(0.80);
		plane.setPause(false);
	}
	
	public static void main(String [] args) throws InvalidTelnetOptionException, Exception {
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
		launch(plane);

		
		//control any roll
		
		boolean running = true;
		int cycles = 0;
		int maxCycles = 50* 1000;
			
		int cycleSleep = 8000;
		
		while(running && cycles < maxCycles) {
			
			logger.info("======================\nCycle {} start. Target heading: {} ", cycles, targetHeading);
			//check altitude first, if we're in a nose dive that needs to be corrected first
			C172PFlightUtilities.altitudeCheck(plane, 500, targetAltitude);
			
			//plane.yawCheck(2, 0);
			
			C172PFlightUtilities.pitchCheck(plane, 4, 3.0);
			
			C172PFlightUtilities.rollCheck(plane, 4, 0.0);
			
			//check heading last, correct pitch/roll/yaw first otherwise the plane will probably drift off heading quickly
			C172PFlightUtilities.headingCheck(plane, 15, targetHeading);
			

			
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
			
			logger.info("Cycle end\n======================");
		}
		
		plane.shutdown();
	}
}
