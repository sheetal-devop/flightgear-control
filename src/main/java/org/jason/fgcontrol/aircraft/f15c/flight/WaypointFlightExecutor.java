package org.jason.fgcontrol.aircraft.f15c.flight;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CFields;
import org.jason.fgcontrol.flight.position.PositionUtilities;
import org.jason.fgcontrol.flight.position.TrackPosition;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fly a set of waypoints with a F15C.
 * 
 * @author jason
 *
 */
public abstract class WaypointFlightExecutor {

	//TODO: put some of these fields into a FlightParameters and allow override
	
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlightExecutor.class);

    private final static double MAX_HEADING_CHANGE = 12.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    private final static double COURSE_ADJUSTMENT_INCREMENT = 3.5;
    
    //if the sim is steering the plane by forcing positional constraints,
    //then the plane is essentially a missile at typical f15c speeds so we need a wide margin of error
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 6.0 * 5280.0;
    
    //beyond this distance, increase throttle to crusing level (MAX)
    private final static double WAYPOINT_ADJUST_MIN_DIST = 16.0 * 5280.0; 
        
    private final static double THROTTLE_WAYPOINT_APPROACH = 0.75;
    private final static double THROTTLE_COURSE_CHANGE = 0.6;
    
    private final static double FUEL_LEVEL_REFILL_THRESHOLD_PERCENT = 0.9;
    
    private final static double TARGET_ROLL = 0.0;
    private final static double FLIGHT_ROLL_MAX = 2.0;
    
    private final static double TARGET_PITCH = 0.0;
    private final static double FLIGHT_PITCH_MAX = 2.0;    
    

    private final static double TARGET_ALTITUDE = 9000.0;
	
	/**
	 * Fly the plane. Assume simulator is launched with the plane in the air and the engine started. Plane object should 
	 * be configured by the invoker, and have a set of waypoints loaded. Blocks until flightplan is completed. 
	 * 
	 * @param plane		Planey McF15CFace
	 * @throws IOException 
	 */
	public static void runFlight(F15C plane) throws IOException {
		runFlight(plane, new FlightParameters());
	}
	
	public static void runFlight(F15C plane, FlightParameters parameters) throws IOException {
		//TODO: override any FlightParameters
		
		WaypointPosition startingWaypoint = plane.getNextWaypoint();
		plane.setCurrentWaypointTarget(startingWaypoint);
		
		//figure out the heading of our first waypoint based upon our current position
        double initialBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), startingWaypoint);    
        
        //point the plane at our first waypoint
        //***we really have to trust the sim is launched with the correct initial heading
        
        LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);

        /////////////////////////////////
        //f15c is pretty stable at launch on its own
        
        /////////////////////////////////
        //fly our waypoints
        
        plane.resetControlSurfaces();
        
        plane.setPause(false);
        
        //chase view
        plane.setCurrentView(2);

        //full throttle or the engines will have divergent thrust outputs
        plane.setEngineThrottles(F15CFields.THROTTLE_MAX);            
        
        //trouble doing waypoint flight at faster speeds with high speedup under the current threading model
        //TODO: separate threads for telemetry readouts and flight control
        //plane.setSimSpeedUp(2.0);
    
        //not much of a min, but all tanks largely filled means even weight distribution and more stable flight
        double minFuelTank0 = plane.getFuelTank0Capacity() * FUEL_LEVEL_REFILL_THRESHOLD_PERCENT,
                minFuelTank1 = plane.getFuelTank1Capacity() * FUEL_LEVEL_REFILL_THRESHOLD_PERCENT,
                minFuelTank2 = plane.getFuelTank2Capacity() * FUEL_LEVEL_REFILL_THRESHOLD_PERCENT,
                minFuelTank3 = plane.getFuelTank3Capacity() * FUEL_LEVEL_REFILL_THRESHOLD_PERCENT,
                minFuelTank4 = plane.getFuelTank4Capacity() * FUEL_LEVEL_REFILL_THRESHOLD_PERCENT;
        
        //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
        //int bearingRecalcCycleInterval = 5;     
        
        WaypointPosition nextWaypoint;
        TrackPosition currentPosition;
        double nextWaypointBearing = initialBearing;
        double distanceToNextWaypoint;
        int waypointFlightCycles;
        long cycleSleep = 5;
        while(plane.getWaypointCount() > 0) {
            
            nextWaypoint = plane.getAndRemoveNextWaypoint();
            
            //possibly slow the simulator down if the next waypoint is close.
            //it's possible that hard and frequent course adjustments are needed
            
            LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
            
            nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
            
            //normalize to 0-360
            if(nextWaypointBearing < FlightUtilities.DEGREES_ZERO) {
                nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
            }
            
            LOGGER.info("Bearing to next waypoint: {}", nextWaypointBearing);
            
            ///////////////////////////////
            //transition to a stable path to next waypoint.
            
            //turning to face next waypoint. throttle down
            plane.setEngineThrottles(THROTTLE_COURSE_CHANGE);
            
            double currentHeading;
            int headingComparisonResult;
            while(!FlightUtilities.withinHeadingThreshold(plane, MAX_HEADING_CHANGE, nextWaypointBearing)) {
                
                currentHeading = plane.getHeading();
                
                plane.addTrackPositionToFlightLog(plane.getPosition());
                
                headingComparisonResult = FlightUtilities.headingCompareTo(plane, nextWaypointBearing);
                
                LOGGER.info("Easing hard turn from current heading {} to target {}", currentHeading, nextWaypointBearing);
                
                //adjust clockwise or counter? 
                //this may actually change in the middle of the transition itself
                double intermediateHeading = currentHeading;
                if(headingComparisonResult == FlightUtilities.HEADING_NO_ADJUST) {
                    LOGGER.warn("Found no adjustment needed");
                    //shouldn't happen since we'd be with the heading threshold
                    break;
                } else if(headingComparisonResult == FlightUtilities.HEADING_CW_ADJUST) {
                    //1: adjust clockwise
                    intermediateHeading = (intermediateHeading + COURSE_ADJUSTMENT_INCREMENT ) % FlightUtilities.DEGREES_CIRCLE;
                } else {
                    //-1: adjust counterclockwise
                    intermediateHeading -= COURSE_ADJUSTMENT_INCREMENT;
                    
                    //normalize 0-360
                    if(intermediateHeading < 0) intermediateHeading += FlightUtilities.DEGREES_CIRCLE;
                }
                
                LOGGER.info("++++Stabilizing to intermediate heading {} from current {} with target {}", intermediateHeading, currentHeading, nextWaypointBearing);
                
                //low count here. if we're not on track by the end, the heading check should fail and get us back here
                //seeing close waypoints get overshot
                int stablizeCount = 0;
                while(stablizeCount < 10) {
                    
                    FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
                    stabilizeCheck(plane, intermediateHeading);
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    stablizeCount++;
                }
                
                //recalculate bearing since we've moved
                nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
                
                //normalize to 0-360
                if(nextWaypointBearing < 0) {
                    nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
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
            }
            
            LOGGER.info("Heading change within tolerance");
            
            //on our way. throttle up
            plane.setEngineThrottles(F15CFields.THROTTLE_MAX);
            
            ///////////////////////////////
            //main flight path to way point
            waypointFlightCycles = 0;
            
            //add our next waypoint to the log
            plane.addWaypointToFlightLog(nextWaypoint);
            
            while( !PositionUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint, WAYPOINT_ARRIVAL_THRESHOLD) ) {
            
                LOGGER.info("======================\nCycle {} start.", waypointFlightCycles);

                currentPosition = plane.getPosition();
                
                distanceToNextWaypoint = PositionUtilities.distanceBetweenPositions(plane.getPosition(), nextWaypoint);
                
                plane.addTrackPositionToFlightLog(currentPosition);
                
                //adjust the throttle once we've gotten close enough to ease waypoint transitions
                if(    
                    distanceToNextWaypoint > WAYPOINT_ADJUST_MIN_DIST //&&
                    //waypointFlightCycles % bearingRecalcCycleInterval == 0
                ) 
                {
                    //reset bearing incase we've drifted, not not if we're too close
                    nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
                    
                    //normalize to 0-360
                    if(nextWaypointBearing < 0) {
                        nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
                    }
                    
                    LOGGER.info("Recalculating bearing to waypoint: {}", nextWaypointBearing);
                } else if ( distanceToNextWaypoint < WAYPOINT_ARRIVAL_THRESHOLD * 3 ) {
                    //throttle down for waypoint approach to accommodate any late corrections
                    
                    plane.setEngineThrottles(THROTTLE_WAYPOINT_APPROACH);
                } else if (plane.getEngine0Throttle() != F15CFields.THROTTLE_MAX) {
                    
                    //far enough away from the previous waypoint and not close enough to the next
                    //throttle up to max if we haven't already
                    plane.setEngineThrottles(F15CFields.THROTTLE_MAX);
                }
                
                // check altitude first, if we're in a nose dive that needs to be corrected first
                FlightUtilities.altitudeCheck(plane, 1000, TARGET_ALTITUDE);

                // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                // you're facing a 5000ft mountain

                stabilizeCheck(plane, nextWaypointBearing);                    
                
                if(!plane.isEngineRunning()) {
                    LOGGER.error("Engine found not running. Attempting to restart.");
//                    plane.startupPlane();
//                    
//                    //increase throttle
//                    plane.setPause(true);
//                    plane.resetControlSurfaces();
//                    plane.setPause(false);
//                    
//                    plane.setEngineThrottles(F15CFields.THROTTLE_MAX);
                    
                    plane.setPause(true);
                    
                    throw new IOException("Engine not running");
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
                
                try {
                    Thread.sleep(cycleSleep);
                } catch (InterruptedException e) {
                    LOGGER.warn("Runtime sleep interrupted", e);
                }
                
                waypointFlightCycles++;
            }
            
            LOGGER.info("Arrived at waypoint {}!", nextWaypoint.toString());
        }
        
        LOGGER.info("No more waypoints. Trip is finished!");                  
	}
	
	private static void transitionToWaypoint(F15C plane, WaypointPosition targetWaypointPosition) {
		//TODO: move functionality from runFlight to here
	}
	
    private static String telemetryReadOut(F15C plane, WaypointPosition position, double targetBearing) {
        
        double distanceRemaining = PositionUtilities.distanceBetweenPositions(plane.getPosition(), position);
        
        return 
            String.format("\nWaypoint: %s", position.getName()) +
            String.format("\nWaypoint Latitude: %s", position.getLatitude()) +
            String.format("\nWaypoint Longitude: %s", position.getLongitude()) +
            String.format("\nDistance remaining to waypoint: %s ft (%s miles)", 
                    distanceRemaining, distanceRemaining/5280.0) +
            String.format("\nTarget bearing: %f", targetBearing) +
            String.format("\nCurrent Heading: %f", plane.getHeading()) +
            String.format("\nAir Speed: %f", plane.getAirSpeed()) +
            String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
            String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
            String.format("\nFuel tank 2 level: %f", plane.getFuelTank2Level()) +
            String.format("\nFuel tank 3 level: %f", plane.getFuelTank3Level()) +
            String.format("\nFuel tank 4 level: %f", plane.getFuelTank4Level()) +
//            String.format("\nFuel tank 5 level: %f", plane.getFuelTank5Level()) +
//            String.format("\nFuel tank 6 level: %f", plane.getFuelTank6Level()) +
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
    
    private static void stabilizeCheck(F15C plane, double bearing) throws IOException {
        if( 
            !FlightUtilities.withinRollThreshold(plane, FLIGHT_PITCH_MAX, TARGET_PITCH) ||
            !FlightUtilities.withinPitchThreshold(plane, FLIGHT_ROLL_MAX, TARGET_ROLL) ||
            !FlightUtilities.withinHeadingThreshold(plane, COURSE_ADJUSTMENT_INCREMENT, bearing)
        ) 
        {
            plane.forceStabilize(bearing, TARGET_PITCH, TARGET_ROLL, false);
        }
    }
}
