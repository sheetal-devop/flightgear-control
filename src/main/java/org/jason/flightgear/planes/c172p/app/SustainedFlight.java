package org.jason.flightgear.planes.c172p.app;

import java.io.IOException;

import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SustainedFlight {
		
	private static Logger logger = LoggerFactory.getLogger(SustainedFlight.class);
	
	//private final static int EXPECTED_TRACK = 30;
	private final static int TARGET_ALTITUDE = 9000;
	
	//0 => N, 90 => E
	private final static int TARGET_HEADING = 180;
	
	private static void launch(C172P plane) throws IOException {
		//assume start unpaused;
		
		plane.setPause(true);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
		//high initially to cut down on the plane falling out of the air
		plane.setAirSpeed(200);
		
		plane.setPause(false);
		
		int i = 0;
		while( i < 50) {
			FlightUtilities.airSpeedCheck(plane, 10, 90);
			
			FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
			FlightUtilities.pitchCheck(plane, 4, 3.0);
			FlightUtilities.rollCheck(plane, 4, 0.0);
			
			//narrow heading check on launch
			FlightUtilities.headingCheck(plane, 4, TARGET_HEADING);
			
			i++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
			String.format("\nAir Speed: %f", plane.getAirSpeed()) +
			String.format("\nThrottle: %f", plane.getThrottle()) +
			String.format("\nMixture: %f", plane.getMixture()) +
			String.format("\nFuel level: %f", plane.getFuelLevel()) +
			String.format("\nEngine running: %d", plane.getEngineRunning()) + 
			String.format("\nAltitude: %f", plane.getAltitude()) +
			String.format("\nLatitude: %f", plane.getLatitude()) + 
			String.format("\nLongitude: %f", plane.getLongitude());
	}
	
	public static void main(String [] args) throws IOException {
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
			
			double currentHeading = (TARGET_HEADING);
			
			//head north
			plane.setHeading(currentHeading);
			
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
			
			plane.setMixture(0.95);
			plane.setThrottle(0.95);
			
			//i'm in a hurry and a c172p only goes so fast
			plane.setSpeedUp(8);
			
			//tailor the update rate to the speedup
			int cycleSleep = 50;
			
			
			while(running && cycles < maxCycles) {
				
				logger.info("======================\nCycle {} start. Target heading: {} ", cycles, currentHeading);
				
				//check altitude first, if we're in a nose dive that needs to be corrected first
				FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
				
				//TODO: ground elevation check. it's a problem if your target alt is 5000ft and you're facing a 5000ft mountain
				
				FlightUtilities.pitchCheck(plane, 4, 2.0);
				
				FlightUtilities.rollCheck(plane, 4, 0.0);
				
				//check heading last-ish, correct pitch/roll first otherwise the plane will probably drift off heading quickly
				FlightUtilities.headingCheck(plane, 8, currentHeading);
				
				//FlightUtilities.airSpeedCheck(plane, 10, 90);
				
				//check fuel last last. easy to refuel
				if(plane.getFuelLevel() < minFuelGal) {
					plane.refillFuelTank();
				}
				
				logger.info("Telemetry Read: {}", telemetryReadOut(plane));
				
				try {
					Thread.sleep(cycleSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(cycles % 100 == 0) {
					currentHeading += 30;
					currentHeading %= 360;
				}
				
				cycles++;
				
				logger.info("Cycle end\n======================");
			}
		} catch (FlightGearSetupException e) {
			e.printStackTrace();
		}
		finally {
			if(plane != null) {
				plane.shutdown();
			}
		}
	}
}
