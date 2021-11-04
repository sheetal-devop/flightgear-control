package org.jason.flightgear.planes.c172p.app;

import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SustainedFlight {
		
	private static Logger logger = LoggerFactory.getLogger(SustainedFlight.class);
	
	private final static int EXPECTED_TRACK = 30;
	private final static int TARGET_ALTITUDE = 9000;
	
	//0 => N, 90 => E
	private final static int TARGET_HEADING = 180;
	
	private static void launch(C172P plane) {
		//assume start unpaused;
		
		plane.setPause(true);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
				
		plane.setPause(false);
		
		//set while not paused. this functions more like a boost- 
		//the plane can be acceled or deceled to the specified speed, 
		//but then the fdm takes over and stabilizes the air speed
		plane.setAirSpeed(100);
		
		//initial drop. allow to level off
		try {
			Thread.sleep(40*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//////
		//initial check that we've leveled off
		FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
		FlightUtilities.pitchCheck(plane, 4, 3.0);
		FlightUtilities.rollCheck(plane, 4, 0.0);
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(0.80);
		plane.setPause(false);
	}
	
	private static String telemetryReadOut(C172P plane) {
		
		return 
			String.format("\nHeading: %f", plane.getHeading()) +
			String.format("\nFuel level: %f", plane.getFuelLevel()) +
			String.format("\nEngine running: %d", plane.getEngineRunning()) + 
			String.format("\nAltitude: %f", plane.getAltitude()) +
			String.format("\nLatitude: %f", plane.getLatitude()) + 
			String.format("\nLongitude: %f", plane.getLongitude());
	}
	
	public static void main(String [] args) {
		C172P plane = null;
		
		try {
			plane = new C172P();
		
			plane.setDamageEnabled(false);
			
			//in case we get a previously lightly-used environment
			plane.refillFuelTank();
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			//c172p tracks hard and i haven't figured out how to solve this with the autopilot
			//double trueHeading = (TARGET_HEADING + EXPECTED_TRACK);
			
			double trueHeading = (TARGET_HEADING);
			
			
//			if(trueHeading < 0) {
//				trueHeading += 360;
//			} else {
//				trueHeading %= 360;
//			}
			
			//head north
			plane.setHeading(trueHeading);
			
			//TODO: check if engine running, plane is in the air, speed is not zero
			//
			
			plane.startupPlane();
	
			//wait for startup to complete and telemetry reads to arrive
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//////////////////
			launch(plane);
	
			boolean running = true;
			int cycles = 0;
			int maxCycles = 50* 1000;
				
			int minFuelGal = 4;
			
			
						
			plane.setBatterySwitch(false);
			
			//i'm in a hurry and a c172p only goes so fast
			plane.setSpeedUp(8);
			
			//tailor the update rate to the speedup
			int cycleSleep = 50;
			
			
			while(running && cycles < maxCycles) {
				
				logger.info("======================\nCycle {} start. Target heading: {} ", cycles, trueHeading);
				
				
				
				//check altitude first, if we're in a nose dive that needs to be corrected first
				FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
				
				//TODO: ground elevation check. it's a problem if your target alt is 5000ft and you're facing a 5000ft mountain
				
				FlightUtilities.pitchCheck(plane, 4, 3.0);
				
				FlightUtilities.rollCheck(plane, 4, 0.0);
				
				//check heading last, correct pitch/roll first otherwise the plane will probably drift off heading quickly
				FlightUtilities.headingCheck(plane, 8, trueHeading);
				
				//check fuel last last. easy to refuel
				if(plane.getFuelLevel() < minFuelGal) {
					plane.refillFuelTank();
				}
				
				logger.info("Telemetry Read: {}", telemetryReadOut(plane));
				
	//			plane.setPause(true);
	//			plane.forceStabilize(TARGET_HEADING, TARGET_ALTITUDE, 0, 3, 0);
	//			plane.setPause(false);
				
				try {
					Thread.sleep(cycleSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(cycles % 100 == 0) {
					trueHeading += 30;
					trueHeading %= 360;
				}
				
				cycles++;
				
				logger.info("Cycle end\n======================");
			}
		} catch (FlightGearSetupException e1) {
			e1.printStackTrace();
		}
		finally {
			if(plane != null) {
				plane.shutdown();
			}
		}
	}
}
