package org.jason.flightgear.planes.f15c.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.jason.flightgear.planes.f15c.F15C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SustainedFlight {

	private static Logger LOGGER = LoggerFactory.getLogger(SustainedFlight.class);
	
	private final static int TARGET_ALTITUDE = 10000;
	
	//0 => N, 90 => E
	private final static int TARGET_HEADING = 90;
	
	private static void launch(F15C plane) throws IOException {
		//assume start unpaused;
		
		//assume already set
		double takeoffHeading = plane.getHeading();
		
		plane.setPause(true);
		
		plane.setParkingBrake(false);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
		//high initially to cut down on the plane falling out of the air
		plane.setAirSpeed(600);
		
		if(plane.isGearDown()) {
			
		}
		
		plane.setThrottle(0.3);
		
		plane.setPause(false);
		
		int i = 0;
		while( i < 20) {
			//FlightUtilities.airSpeedCheck(plane, 400, 600);
			
			FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
			FlightUtilities.pitchCheck(plane, 4, 3.0);
			FlightUtilities.rollCheck(plane, 4, 0.0);
			
			//narrow heading check on launch
			//FlightUtilities.headingCheck(plane, 4, takeoffHeading);
			
			i++;
		}
		
		//set while not paused. this functions more like a boost- 
		//the plane can be acceled or deceled to the specified speed, 
		//but then the fdm takes over and stabilizes the air speed
//		plane.setAirSpeed(100);
//		
//		//initial drop. allow to level off
//		try {
//			Thread.sleep(40*1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		//////
		//initial check that we've leveled off
		FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
		FlightUtilities.pitchCheck(plane, 4, 3.0);
		FlightUtilities.rollCheck(plane, 4, 0.0);
		
		//increase throttle
		plane.setPause(true);
		plane.setThrottle(1.0);
		plane.setPause(false);
	}
	
	private static String telemetryReadOut(F15C plane) {
		
		return 
				String.format("\nCurrent Heading: %f", plane.getHeading()) +
				String.format("\nAir Speed: %f", plane.getAirSpeed()) +
				String.format("\nFuel tank 0 level: %f", plane.getFuelLevel()) +
				String.format("\nEngine running: %d", plane.getEngineRunning()) + 
				String.format("\nEnv Temp: %f", plane.getTemperature()) + 
				String.format("\nThrottle: %f", plane.getThrottle()) +
				String.format("\nAltitude: %f", plane.getAltitude()) +
				String.format("\nLatitude: %f", plane.getLatitude()) + 
				String.format("\nLongitude: %f", plane.getLongitude());
	}
	
	public static void main(String[] args) {
		F15C plane = null;
		
		try {
			plane = new F15C();
		
			plane.setDamageEnabled(false);
			
			//in case we get a previously lightly-used environment
			plane.refillFuel();
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			double currentHeading = (TARGET_HEADING);
			
			plane.setHeading(currentHeading);
			
			//shut down the engines if they're already running
			if(!plane.isEngine0Cutoff()) {
				plane.setEngine0Cutoff(true);
			}
			
			if(!plane.isEngine1Cutoff()) {
				plane.setEngine1Cutoff(true);
			}
			
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
			
			//tailor the update rate to the speedup
			int cycleSleep = 50;
			
			while(running && cycles < maxCycles) {
				
				LOGGER.info("======================\nCycle {} start. Target heading: {} ", cycles, currentHeading);
			
				//check altitude first, if we're in a nose dive that needs to be corrected first
				FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
				
				if(cycles % 50 == 0 ) {
					plane.forceStabilize(currentHeading, 0, 2.0);
				} else {
					FlightUtilities.pitchCheck(plane, 4, 2.0);

					FlightUtilities.rollCheck(plane, 4, 0.0);

					// check heading last-ish, correct pitch/roll first otherwise the plane will
					// probably drift off heading quickly
					
					FlightUtilities.headingCheck(plane, 4, currentHeading);
				}
				
				try {
					Thread.sleep(cycleSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
				
				cycles++;
				
				LOGGER.info("Cycle end\n======================");
			}
			
			LOGGER.info("Trip is finished!");
		} catch (FlightGearSetupException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(plane != null) {
				plane.shutdown();
			}
			
			try {
				plane.terminateSimulator();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidTelnetOptionException e) {
				e.printStackTrace();
			}
		}
	}

}
