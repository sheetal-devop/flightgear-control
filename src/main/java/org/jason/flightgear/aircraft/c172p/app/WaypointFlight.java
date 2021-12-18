package org.jason.flightgear.aircraft.c172p.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.aircraft.c172p.C172P;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.flight.WaypointPosition;
import org.jason.flightgear.flight.util.FlightLog;
import org.jason.flightgear.flight.util.FlightUtilities;
import org.jason.flightgear.flight.waypoints.KnownPositions;
import org.jason.flightgear.flight.waypoints.WaypointManager;
import org.jason.flightgear.flight.waypoints.WaypointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointFlight {
		
	private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlight.class);
	
	private final static int TARGET_ALTITUDE = 9000;
	
	private static void launch(C172P plane) throws IOException {
		//assume start unpaused;
		
		//assume already set
		double takeoffHeading = plane.getHeading();
		
		plane.setPause(true);
		
		//place in the air
		plane.setAltitude(TARGET_ALTITUDE);
		
		//high initially to cut down on the plane falling out of the air
		plane.setAirSpeed(200);
		
		plane.setPause(false);
		
		int i = 0;
		while( i < 20) {
			//FlightUtilities.airSpeedCheck(plane, 10, 100);
			
			FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
			FlightUtilities.pitchCheck(plane, 4, 3.0);
			FlightUtilities.rollCheck(plane, 4, 0.0);
			
			//narrow heading check on launch
			FlightUtilities.headingCheck(plane, 4, takeoffHeading);
			
			i++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		plane.setThrottle(0.95);
		plane.setPause(false);
	}
	
	private static String telemetryReadOut(C172P plane, WaypointPosition position, double targetBearing) {
				
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
			String.format("\nBattery level: %f", plane.getBatteryCharge()) +
			String.format("\nEngine running: %d", plane.getEngineRunning()) + 
			String.format("\nEngine rpms: %f", plane.getEngineRpms()) + 
			String.format("\nEnv Temp: %f", plane.getTemperature()) + 
			String.format("\nThrottle: %f", plane.getThrottle()) +
			String.format("\nMixture: %f", plane.getMixture()) +
			String.format("\nAltitude: %f", plane.getAltitude()) +
			String.format("\nLatitude: %f", plane.getLatitude()) + 
			String.format("\nLongitude: %f", plane.getLongitude());
	}
	
	public static void main(String [] args) {
		C172P plane = null;
		
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
		waypointManager.addWaypoint(KnownPositions.ABBOTSFORD);
		waypointManager.addWaypoint(KnownPositions.PRINCETON);
		waypointManager.addWaypoint(KnownPositions.PENTICTON);
		waypointManager.addWaypoint(KnownPositions.KELOWNA);
		waypointManager.addWaypoint(KnownPositions.KAMLOOPS);
		waypointManager.addWaypoint(KnownPositions.REVELSTOKE);
		waypointManager.addWaypoint(KnownPositions.HUNDRED_MI_HOUSE);
		waypointManager.addWaypoint(KnownPositions.PRINCE_GEORGE);
		waypointManager.addWaypoint(KnownPositions.DAWSON_CREEK);
		waypointManager.addWaypoint(KnownPositions.FORT_NELSON);
		waypointManager.addWaypoint(KnownPositions.JADE_CITY);
		waypointManager.addWaypoint(KnownPositions.DEASE_LAKE);
		waypointManager.addWaypoint(KnownPositions.HAZELTON);
		waypointManager.addWaypoint(KnownPositions.PRINCE_RUPERT);
		waypointManager.addWaypoint(KnownPositions.BELLA_BELLA);
		waypointManager.addWaypoint(KnownPositions.PORT_HARDY);
		waypointManager.addWaypoint(KnownPositions.TOFINO);
		waypointManager.addWaypoint(KnownPositions.VICTORIA);
		waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
		
		//for fun, mix it up
//		List<WaypointPosition> reverseOrder = waypointManager.getWaypoints();
//		Collections.reverse( reverseOrder );
//		waypointManager.setWaypoints( reverseOrder );

		
		WaypointPosition startingWaypoint = waypointManager.getNextWaypoint();

		try {
			plane = new C172P();
		
			plane.setDamageEnabled(false);
			
			
			//in case we get a previously lightly-used environment
			plane.refillFuel();
			plane.setBatteryCharge(1.0);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//figure out the heading of our first waypoint based upon our current position
			WaypointPosition startPosition = plane.getPosition();
			double initialBearing = WaypointUtilities.getHeadingToTarget(startPosition, startingWaypoint);			
			
			//point the plane at our first waypoint
			LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
			plane.setHeading(initialBearing);
			
			//startup procedure to get the engines running
			plane.startupPlane();
	
			//wait for startup to complete and telemetry reads to arrive
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//////////////////
			launch(plane);
	
			plane.setBatterySwitch(false);
			
			//i'm in a hurry and a c172p only goes so fast
			plane.setSpeedUp(8);
		
			//not much of a min, but both tanks largely filled means even weight and more stable flight
			double minFuelGal = 16.0;
			double minBatteryCharge = 0.25;
			
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
				while( !WaypointUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint) ) {
				
					LOGGER.info("======================\nCycle {} start.", waypointFlightCycles);

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
						plane.setThrottle(0.95);
						plane.setPause(false);
					}
					
					//refill both tanks for balance
					if (plane.getFuelTank0Level() < minFuelGal || plane.getFuelTank1Level() < minFuelGal) {
						plane.refillFuelTank0();
						plane.refillFuelTank1();
					}
					
					//check battery level
					if (plane.getBatteryCharge() < minBatteryCharge) {
						plane.setBatteryCharge(0.9);
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
			
			flightLog.writeGPXFile(System.getProperty("user.dir") + "/c172p_"+System.currentTimeMillis() + ".gpx");
		}
		
		LOGGER.info("Completed course in: {}ms", (System.currentTimeMillis() - startTime));
	}
}
