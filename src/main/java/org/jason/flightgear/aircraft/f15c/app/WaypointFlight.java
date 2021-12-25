package org.jason.flightgear.aircraft.f15c.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.aircraft.f15c.F15C;
import org.jason.flightgear.aircraft.f15c.F15CFields;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.util.FlightLog;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.jason.flightgear.flight.waypoints.KnownPositions;
import org.jason.flightgear.flight.waypoints.WaypointManager;
import org.jason.flightgear.flight.waypoints.WaypointPosition;
import org.jason.flightgear.flight.waypoints.WaypointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointFlight {
		
	private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlight.class);
	
	private final static int TARGET_ALTITUDE = 8000;
	
	//if the sim is steering the plane by forcing positional constraints,
	//then the plane is essentially a missle so we need a wide margin of error
	private final static double WAYPOINT_ARRIVAL_THRESHOLD = 10 * 5280;
	
//	private static void launch(F15C plane) throws IOException {
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
//		FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
//		FlightUtilities.pitchCheck(plane, 4, 3.0);
//		FlightUtilities.rollCheck(plane, 4, 0.0);
//		
//		//increase throttle
//		plane.setPause(true);
//		plane.setThrottle(0.95);
//		plane.setPause(false);
//	}
	
	private static String telemetryReadOut(F15C plane, WaypointPosition position, double targetBearing) {
				
		return 
			String.format("\nWaypoint: %s", position.getName()) +
			String.format("\nWaypoint Latitude: %s", position.getLatitude()) +
			String.format("\nWaypoint Longitude: %s", position.getLongitude()) +
			String.format("\nDistance remaining to waypoint: %s", 
				WaypointUtilities.distanceBetweenPositions(plane.getPosition(), position)) +
			String.format("\nTarget bearing: %f", targetBearing) +
			
			String.format("\nCurrent Heading: %f", plane.getHeading()) +
			String.format("\nAir Speed: %f", plane.getAirSpeed()) +
			String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
			String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
			String.format("\nFuel tank 2 level: %f", plane.getFuelTank2Level()) +
			String.format("\nFuel tank 3 level: %f", plane.getFuelTank3Level()) +
			String.format("\nFuel tank 4 level: %f", plane.getFuelTank4Level()) +
//			String.format("\nFuel tank 5 level: %f", plane.getFuelTank5Level()) +
//			String.format("\nFuel tank 6 level: %f", plane.getFuelTank6Level()) +
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
		
	public static void main(String [] args) {
		F15C plane = null;
		
		long startTime = System.currentTimeMillis();
		
		FlightLog flightLog = new FlightLog();
		 
		WaypointManager waypointManager = new WaypointManager();
		
		//local tour
		//C172P script launches from YVR
//		waypointManager.addWaypoint(KnownPositions.STANLEY_PARK);
//		waypointManager.addWaypoint(KnownPositions.LONSDALE_QUAY);
//		waypointManager.addWaypoint(KnownPositions.WEST_LION);
//		waypointManager.addWaypoint(KnownPositions.MT_SEYMOUR);
//		waypointManager.addWaypoint(KnownPositions.BURNABY_8RINKS);
//		// loop again
//		waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
//		waypointManager.addWaypoint(KnownPositions.STANLEY_PARK);
//		waypointManager.addWaypoint(KnownPositions.LONSDALE_QUAY);
//		waypointManager.addWaypoint(KnownPositions.WEST_LION);
//		waypointManager.addWaypoint(KnownPositions.MT_SEYMOUR);
//		waypointManager.addWaypoint(KnownPositions.BURNABY_8RINKS);
//		waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
		
		//bc tour
		//C172P script launches from YVR
		//waypointManager.addWaypoint(KnownPositions.ABBOTSFORD);
		//waypointManager.addWaypoint(KnownPositions.PRINCETON);
		waypointManager.addWaypoint(KnownPositions.PENTICTON);
		//waypointManager.addWaypoint(KnownPositions.KELOWNA);
		//waypointManager.addWaypoint(KnownPositions.KAMLOOPS);
		waypointManager.addWaypoint(KnownPositions.REVELSTOKE);
		//waypointManager.addWaypoint(KnownPositions.HUNDRED_MI_HOUSE);
		//waypointManager.addWaypoint(KnownPositions.PRINCE_GEORGE);
		waypointManager.addWaypoint(KnownPositions.DAWSON_CREEK);
		//waypointManager.addWaypoint(KnownPositions.FORT_NELSON);
		waypointManager.addWaypoint(KnownPositions.JADE_CITY);
		//waypointManager.addWaypoint(KnownPositions.DEASE_LAKE);
		//waypointManager.addWaypoint(KnownPositions.HAZELTON);
		waypointManager.addWaypoint(KnownPositions.PRINCE_RUPERT);
		//waypointManager.addWaypoint(KnownPositions.BELLA_BELLA);
		waypointManager.addWaypoint(KnownPositions.PORT_HARDY);
		//waypointManager.addWaypoint(KnownPositions.TOFINO);
		waypointManager.addWaypoint(KnownPositions.VICTORIA);
		waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
		
		//for fun, mix it up
//		List<WaypointPosition> reverseOrder = waypointManager.getWaypoints();
//		Collections.reverse( reverseOrder );
//		waypointManager.setWaypoints( reverseOrder );

		
		WaypointPosition startingWaypoint = waypointManager.getNextWaypoint();

		try {
			plane = new F15C();
		
			plane.setDamageEnabled(false);
			plane.setGMT("2021-07-01T20:00:00");
			
			plane.refillFuel();
			
			//figure out the heading of our first waypoint based upon our current position
			WaypointPosition startPosition = plane.getPosition();
			double initialBearing = WaypointUtilities.getHeadingToTarget(startPosition, startingWaypoint);			
			
			//point the plane at our first waypoint
			LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
			
			//make sure the shell script is launched with the initial heading instead
			//plane.setHeading(initialBearing);
			
			//startup procedure to get the engines running
			plane.startupPlane();
			
			//full throttle or the engines will have divergent thrust outputs
			plane.setEngine0Throttle(F15CFields.THROTTLE_MAX);
			
			//
			plane.setPause(false);
			
			//////////////////
			//launch(plane);

			
			//i'm in a hurry and a c172p only goes so fast
			//plane.setSpeedUp(8);
		
			//not much of a min, but both tanks largely filled means even weight and more stable flight
			double minFuelTank0 = 125.0,
					minFuelTank1 = 175.0,
					minFuelTank2 = 425.0,
					minFuelTank3 = 375.0,
					minFuelTank4 = 375.0;
			
			//needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
			int bearingRecalcCycleInterval = 5;	
			
			WaypointPosition nextWaypoint;
			double nextWaypointBearing = 0.0; //default north
			int waypointFlightCycles;
			while(waypointManager.getWaypointCount() > 0) {
				
				nextWaypoint = waypointManager.getAndRemoveNextWaypoint();
				
				//possibly slow the simulator down if the next waypoint is close.
				//it's possible that hard and frequent course adjustments are needed
				
				LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
				
				nextWaypointBearing = WaypointUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
				
				LOGGER.info("Bearing to next waypoint: {}", nextWaypointBearing);
				
				waypointFlightCycles = 0;
				while( !WaypointUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint, WAYPOINT_ARRIVAL_THRESHOLD) ) {
				
					LOGGER.info("======================\nCycle {} start.", waypointFlightCycles);

					//TODO: consider slowing the sim down if reach some secondary threshold. turning a fast plane may be difficult
					
					flightLog.add(plane.getPosition());
					
					if(waypointFlightCycles % bearingRecalcCycleInterval == 0) {
						//reset bearing incase we've drifted
						nextWaypointBearing = WaypointUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
						
						LOGGER.info("Recalculating bearing to waypoint: {}", nextWaypointBearing);
					}
					
					// check altitude first, if we're in a nose dive that needs to be corrected first
					FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);

					// TODO: ground elevation check. it's a problem if your target alt is 5000ft and
					// you're facing a 5000ft mountain

					if(waypointFlightCycles % 50 == 0 ) {
						plane.forceStabilize(nextWaypointBearing, 0, 2.0);
					} else {
						FlightUtilities.pitchCheck(plane, 4, 2.0);

						FlightUtilities.rollCheck(plane, 4, 0.0);

						// check heading last-ish, correct pitch/roll first otherwise the plane will
						// probably drift off heading quickly
						
						FlightUtilities.headingCheck(plane, 4, nextWaypointBearing);
					}
					
					if(!plane.isEngineRunning()) {
						LOGGER.error("Engine found not running. Attempting to restart.");
						plane.startupPlane();
						
						//increase throttle
						plane.setPause(true);
						plane.setEngine0Throttle(1.0);
						plane.setPause(false);
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

					LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane, nextWaypoint, nextWaypointBearing));
					LOGGER.info("\nCycle {} end\n======================", waypointFlightCycles);
					
					waypointFlightCycles++;
				}
				
				LOGGER.info("Arrived at waypoint {}!", nextWaypoint.toString());
			}
			
			LOGGER.info("No more waypoints. Trip is finished!");
		} catch (FlightGearSetupException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(plane != null) {
				
				plane.shutdown();
				
				try {
					plane.terminateSimulator();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidTelnetOptionException e) {
					e.printStackTrace();
				}
			}
			
			flightLog.writeGPXFile(System.getProperty("user.dir") + "/f15c_"+System.currentTimeMillis() + ".gpx");
		}
		
		LOGGER.info("Completed course in: {}ms", (System.currentTimeMillis() - startTime));
	}
}
