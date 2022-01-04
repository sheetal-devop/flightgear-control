package org.jason.flightgear.aircraft.c172p.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.aircraft.c172p.C172P;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fly a C172P at a constant heading with a set altitude. 
 * 
 * @author jason
 *
 */
public class SustainedFlight {
		
	private static Logger LOGGER = LoggerFactory.getLogger(SustainedFlight.class);
	
	//at 9000 ft the mixture requirements become finicky and control is tougher
	//5000 ft - stable
	//5125 ft - trim failures
	//ground - no trim failures
	private final static int TARGET_ALTITUDE = 7125;
	
	//depending on where in the air or ground the plane launches, the mixture will 
	//be set accordingly by the startup nasal script if fgfs launches the plane
	//9000: 0.837500
	//8000: 0.88
	//7500: 0.89
	//7250: 0.89
	//7125: 0.9
	//7000: 0.9
	//6000: 0.93
	//5500: 0.94
	//5250: 0.94
	//5125: 0.95
	//5000: 0.95
	//ground: 0.95
	private final static double MIXTURE = 0.90;
	private final static double THROTTLE = MIXTURE;
	
	//heading in degrees
	//0 => N, 90 => E
	//From YVR:
	//93 crashes into mountains a ways past chilliwack. likely in bridal veil falls park 
	//99 hits mountains short of 0S7 airport
	private final static int TARGET_HEADING = 99;
	
//	private static void launch(C172P plane) throws IOException {
//		//assume start unpaused;
//		
//		//assume already set
//		double takeoffHeading = plane.getHeading();
//		
//		plane.setPause(true);
//		
//		//place in the air
//		plane.setAltitude(TARGET_ALTITUDE);
//		
//		//high initially to cut down on the plane falling out of the air
//		plane.setAirSpeed(200);
//		
//		plane.setPause(false);
//		
//		int i = 0;
//		while( i < 20) {
//			//FlightUtilities.airSpeedCheck(plane, 10, 100);
//			
//			FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
//			FlightUtilities.pitchCheck(plane, 4, 3.0);
//			FlightUtilities.rollCheck(plane, 4, 0.0);
//			
//			//narrow heading check on launch
//			FlightUtilities.headingCheck(plane, 4, takeoffHeading);
//			
//			i++;
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		//set while not paused. this functions more like a boost- 
//		//the plane can be acceled or deceled to the specified speed, 
//		//but then the fdm takes over and stabilizes the air speed
////		plane.setAirSpeed(100);
////		
////		//initial drop. allow to level off
////		try {
////			Thread.sleep(40*1000);
////		} catch (InterruptedException e) {
////			e.printStackTrace();
////		}
//		
//		//////
//		//initial check that we've leveled off
//		FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
//		FlightUtilities.pitchCheck(plane, 4, 3.0);
//		FlightUtilities.rollCheck(plane, 4, 0.0);
//		
//		//increase throttle
//		plane.setPause(true);
//		plane.setThrottle(0.95);
//		plane.setPause(false);
//	}
	
	private static String telemetryReadOut(C172P plane) {
				
		return 
				String.format("\nCurrent Heading: %f", plane.getHeading()) +
				String.format("\nAir Speed: %f", plane.getAirSpeed()) +
				String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
				String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
				String.format("\nBattery level: %f", plane.getBatteryCharge()) +
				String.format("\nEngine running: %d", plane.getEngineRunning()) + 
				String.format("\nEngine rpms: %f", plane.getEngineRpms()) + 
				String.format("\nEnv Temp: %f", plane.getTemperature()) + 
				String.format("\nThrottle: %f", plane.getThrottle()) +
				String.format("\nMixture: %f", plane.getMixture()) +
				String.format("\nAltitude: %f", plane.getAltitude()) +
				String.format("\nLatitude: %f", plane.getLatitude()) + 
				String.format("\nLongitude: %f", plane.getLongitude()) +
				"\nGMT: " + plane.getGMT();
	}
	
	public static void main(String [] args) {
		C172P plane = null;
		
		try {
			plane = new C172P();
		
			plane.setDamageEnabled(false);
			plane.setComplexEngineProcedures(false);
			plane.setWinterKitInstalled(true);
			plane.setGMT("2021-07-01T20:00:00");
			
			//in case we get a previously lightly-used environment
			plane.refillFuel();
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			double currentTargetHeading = (TARGET_HEADING);
			
			//should be set by the fgfs launch script, but sometimes it's off.
			//possibly due to airport runway orientation
			plane.setHeading(currentTargetHeading);
			
			if(!plane.isEngineRunning()) {
				plane.startupPlane();
			} else {
				LOGGER.info("Engine was running on launch. Skipping startup");
			}

			plane.setPause(false);
			
			//////////////////
			//launch(plane);
			
			//wait for startup to complete and telemetry reads to arrive
			//hold the plane steady and keep it pointed at target heading
			int i = 0;
			while( i < 20) {
				
				FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
				
				if( !FlightUtilities.withinRollThreshold(plane, 2.0, 0.0) ||
					!FlightUtilities.withinPitchThreshold(plane, 4.0, 3.0) ||
					!FlightUtilities.withinHeadingThreshold(plane, 4.0, currentTargetHeading)
				) {
					plane.forceStabilize(currentTargetHeading, 0, 2.0);
				}
				
				i++;
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			plane.setMixture(MIXTURE);
	
			boolean running = true;
			int cycles = 0;
			int maxCycles = 50* 1000;
				
			int minFuelGal = 16;
			
			//tailor the update rate to the speedup
			int cycleSleep = 25;
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			plane.setBatterySwitch(false);
			
			//prevent common icing problems that can cause mechanical failures
			plane.setAntiIce(true);
			
			//this will sometimes set the flaps to the maximum. not sure why.
			//seems be internal to the simulator
			//reset flaps right after
			//needs to wait on mixture
			//pausing seems to be necessary
			plane.setPause(true);
			plane.setThrottle(THROTTLE);
			plane.setPause(false);
			
			plane.resetControlSurfaces();
			
			//i'm in a hurry and a c172p only goes so fast
			plane.setSimSpeedUp(8);
			
			while(running && cycles < maxCycles) {
				
				LOGGER.info("======================\nCycle {} start. Target heading: {} ", cycles, currentTargetHeading);
				
				//check altitude first, if we're in a nose dive that needs to be corrected first
				FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
				
				//TODO: ground elevation check. it's a problem if your target alt is 5000ft and you're facing a 5000ft mountain
				
				if( 
					!FlightUtilities.withinRollThreshold(plane, 2.0, 0.0) ||
					!FlightUtilities.withinPitchThreshold(plane, 4.0, 3.0) ||
					!FlightUtilities.withinHeadingThreshold(plane, 4.0, currentTargetHeading)
					) 
				{
					plane.forceStabilize(currentTargetHeading, 0, 2.0);
				}
				
				//check fuel last last. easy to refuel
				//refill both tanks for balance
				if (plane.getFuelTank0Level() < minFuelGal || plane.getFuelTank1Level() < minFuelGal) {
					plane.refillFuel();
				}
				
				LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
				
				try {
					Thread.sleep(cycleSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//optionally change direction slightly
//				if(cycles % 100 == 0) {
//					currentHeading += 30;
//					currentHeading %= 360;
//				}
				
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
