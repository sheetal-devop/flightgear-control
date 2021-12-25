package org.jason.flightgear.aircraft.f15c.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.aircraft.f15c.F15C;
import org.jason.flightgear.aircraft.f15c.F15CFields;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SustainedFlight {

	private static Logger LOGGER = LoggerFactory.getLogger(SustainedFlight.class);
	
	private final static int TARGET_ALTITUDE = 9000;
	
	//0 => N, 90 => E
	private final static int TARGET_HEADING = 90;
	
	private static String telemetryReadOut(F15C plane) {
				
		return 
				String.format("\nCurrent Heading: %f", plane.getHeading()) +
				String.format("\nAir Speed: %f", plane.getAirSpeed()) +
				String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
				String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
				String.format("\nFuel tank 2 level: %f", plane.getFuelTank2Level()) +
				String.format("\nFuel tank 3 level: %f", plane.getFuelTank3Level()) +
				String.format("\nFuel tank 4 level: %f", plane.getFuelTank4Level()) +
//				String.format("\nFuel tank 5 level: %f", plane.getFuelTank5Level()) +
//				String.format("\nFuel tank 6 level: %f", plane.getFuelTank6Level()) +
				String.format("\nEngine running: %d", plane.getEngineRunning()) + 
				String.format("\nEngine 1 thrust: %f", plane.getEngine0Thrust()) + 
				String.format("\nEngine 2 thrust: %f", plane.getEngine1Thrust()) + 
				String.format("\nEnv Temp: %f", plane.getTemperature()) + 
				String.format("\nEngine 1 Throttle: %f", plane.getEngine0Throttle()) +
				String.format("\nEngine 2 Throttle: %f", plane.getEngine1Throttle()) +
				String.format("\nAltitude: %f", plane.getAltitude()) +
				String.format("\nLatitude: %f", plane.getLatitude()) + 
				String.format("\nLongitude: %f", plane.getLongitude()) +
				String.format("\nAileron: %f", plane.getAileron()) +
				String.format("\nAileron Trim: %f", plane.getAileronTrim()) +
				String.format("\nElevator: %f", plane.getElevator()) +
				String.format("\nElevator Trim: %f", plane.getElevatorTrim()) +
				String.format("\nFlaps: %f", plane.getFlaps()) +
				String.format("\nRudder: %f", plane.getRudder()) +
				String.format("\nRudder Trim: %f", plane.getRudderTrim()) +
				String.format("\nGear Down: %d", plane.getGearDown()) +
				String.format("\nParking Brake: %d", plane.getParkingBrake()) +
				"\nGMT: " + plane.getGMT();
	}
	
	
	
	public static void main(String[] args) {
		F15C plane = null;
		
		try {
			plane = new F15C();
			
			double currentHeading = (TARGET_HEADING);
		
			plane.setDamageEnabled(false);
			plane.setGMT("2021-07-01T20:00:00");
			
//			//in case we get a previously lightly-used environment
//			plane.refillFuel();
//			
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			
//			//shut down the engines if they're already running
//			if(!plane.isEngine0Cutoff()) {
//				plane.setEngine0Cutoff(true);
//			}
//			
//			if(!plane.isEngine1Cutoff()) {
//				plane.setEngine1Cutoff(true);
//			}
//			
//			
//			plane.setHeading(currentHeading);
//			
//			//begin startup sequence
//			plane.startupPlane();
//	
//			//wait for startup to complete and telemetry reads to arrive
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			//////////////////
//			launch(plane);
			
			//////////////////////////////////////////////
			
			
			
			
			//launched from shell script. starts paused
			//plane.refillFuel();
			plane.startupPlane();
			
			
			//!!! don't set the throttle until after the sim is unpaused with the startup sequence complete
			//will get flaps failures
			
			//check surfaces and orientations
			
			//gear down check
			//should auto-retract above an altitude threshold but double check
			if(plane.isGearDown()) {
				LOGGER.warn("Found gear down before throttle step up. Retracting");
				plane.setGearDown(false);
			}

			//parking brake check
			if(plane.isParkingBrakeEnabled()) {
				LOGGER.warn("Found parking brake on before throttle step up. Retracting");
				plane.setParkingBrake(false);
			}
			
			plane.resetControlSurfaces();
			
			
			//step up throttle from whatever value established in startup to the max
			double throttleEngine0 = plane.getEngine0Throttle();
			double throttleEngine1 = plane.getEngine1Throttle();
			
			plane.setPause(false);
			
			//sleep to let the plane get up to speed
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			//////////////////////////////////////////////
			//enter stable flight
			
			double minFuelTank0 = 125.0,
					minFuelTank1 = 175.0,
					minFuelTank2 = 425.0,
					minFuelTank3 = 375.0,
					minFuelTank4 = 375.0;
			
			LOGGER.info("Post startup starting");
			
			int cycles = 0;
			while( cycles < 20) {
				
				plane.resetControlSurfaces();
				
				FlightUtilities.pitchCheck(plane, 4, 2.0);

				FlightUtilities.rollCheck(plane, 4, 0.0);

				// check heading last-ish, correct pitch/roll first otherwise the plane will
				// probably drift off heading quickly
				
				FlightUtilities.headingCheck(plane, 4, currentHeading);
				
				//refill all tanks for balance
				if (
					plane.getFuelTank0Level() < minFuelTank0 || 
					plane.getFuelTank1Level() < minFuelTank1 ||
					plane.getFuelTank2Level() < minFuelTank2 ||
					plane.getFuelTank3Level() < minFuelTank3 ||
					plane.getFuelTank4Level() < minFuelTank4 

				) {
					plane.refillFuel();
				}
				
				cycles++;
			}
			
			//////////////////////////////////////////////
			
			LOGGER.info("Stepping up engine throttles");
			
			//precheck
			

			
			//step up to full throttle or the engines may have divergent thrust outputs
			//need to manually set throttle to both engines even though the sim slaves engine 1 throttle to engine 0
//			double startupThrottleMax = F15CFields.THROTTLE_MAX;
//			double throttleStep = 0.01;
//			int throttleAdjustments = 0;
//			int maxThrottleAdjustments = 100;
//			while( throttleEngine0 <= startupThrottleMax && throttleEngine1 <= startupThrottleMax )
//			{				
//				throttleEngine0 += throttleStep;
//				
//				//the sim will correct for values <0 and >1
//				plane.setEngineThrottles(throttleEngine0);
//				
//				try {
//					//have to sleep so that throttle adjustments will show up in subsequent telemetry reads
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				
////				throttleEngine1 += throttleStep;
////				plane.setEngine1Throttle(throttleEngine1);
//				
//				if(throttleAdjustments >= maxThrottleAdjustments) {
//					throw new FlightGearSetupException("Failure stepping up engine throttles to maximum");
//				}
//				
//				throttleAdjustments++;
//				
//				//have to keep the plane on track
//				plane.resetControlSurfaces();
//				
//				FlightUtilities.pitchCheck(plane, 4, 2.0);
//
//				FlightUtilities.rollCheck(plane, 4, 0.0);
//
//				// check heading last-ish, correct pitch/roll first otherwise the plane will
//				// probably drift off heading quickly
//				
//				FlightUtilities.headingCheck(plane, 4, currentHeading);
//				
//				try {
//					//have to sleep so that throttle adjustments will show up in subsequent telemetry reads
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
			
			//ensure we're at the max explicitly
			plane.setEngineThrottles(F15CFields.THROTTLE_MAX);
			
			LOGGER.info("Throttle step up completed");
			
			LOGGER.info("Post startup ending");

			
			//////////////////////////////////////////////
			
			boolean running = true;
			cycles = 5;
			int maxCycles = 50* 1000;
			
			//tailor the update rate to the speedup
			int cycleSleep = 10;
			

			
			double maxThrustDifferential = 1000.0;
			
			while(running && cycles < maxCycles) {
				
				LOGGER.info("======================\nCycle {} start. Target heading: {} ", cycles, currentHeading);
			
				//check altitude first, if we're in a nose dive that needs to be corrected first
				FlightUtilities.altitudeCheck(plane, 1000, TARGET_ALTITUDE);
				
				if(cycles % 50 == 0 ) {					
					plane.forceStabilize(currentHeading, 0, 2.0);
				} else {
					plane.resetControlSurfaces();
					
					FlightUtilities.pitchCheck(plane, 4, 2.0);

					FlightUtilities.rollCheck(plane, 4, 0.0);

					// check heading last-ish, correct pitch/roll first otherwise the plane will
					// probably drift off heading quickly
					
					FlightUtilities.headingCheck(plane, 4, currentHeading);
				}
				
				//refill all tanks for balance
				if (
					plane.getFuelTank0Level() < minFuelTank0 || 
					plane.getFuelTank1Level() < minFuelTank1 ||
					plane.getFuelTank2Level() < minFuelTank2 ||
					plane.getFuelTank3Level() < minFuelTank3 ||
					plane.getFuelTank4Level() < minFuelTank4 

				) {
					plane.refillFuel();
				}
				
//				if(cycles % 100 == 0 && cycles > 100) {
//					//only do this if we've been flying for a bit
//					if( Math.abs( plane.getEngine0Thrust() - plane.getEngine1Thrust() ) > maxThrustDifferential ) {
//						
//						LOGGER.warn("Engine thrust differential over threshold. Attempting throttle step to fix.");
//			
//						plane.setEngine0Throttle(0.7);
//						plane.setEngine1Throttle(0.7);
//						
//						plane.setEngine0Throttle(F15CFields.THROTTLE_MAX);
//						plane.setEngine1Throttle(F15CFields.THROTTLE_MAX);
//					}
//				}
				
				try {
					Thread.sleep(cycleSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
		
				LOGGER.info("Cycle end\n======================");
				
				cycles++;
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
