package org.jason.fgcontrol.aircraft.c172p.flight;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PFields;
import org.jason.fgcontrol.flight.position.PositionUtilities;
import org.jason.fgcontrol.flight.position.TrackPosition;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fly a set of waypoints with a C172P.
 * 
 * @author jason
 *
 */
public abstract class WaypointFlightExecutor {

	//TODO: put some of these fields into a FlightParameters and allow override
	
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlightExecutor.class);

    private final static double WAYPOINT_ADJUST_MIN = 0.75 * 5280.0; 
    
    private final static double TARGET_ALTITUDE = 5100.0;
    
    private final static double FLIGHT_MIXTURE = 0.95;
    private final static double FLIGHT_THROTTLE = 0.95;
    
    private final static double MAX_HEADING_CHANGE = 20.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    private final static double COURSE_ADJUSTMENT_INCREMENT = 12.5;
    
    private final static double TARGET_ROLL = 0.0;
    private final static double FLIGHT_ROLL_MAX = 2.0;
    
    private final static double TARGET_PITCH = 2.0;
    private final static double FLIGHT_PITCH_MAX = 4.0;  
    
    //adjust this according to the route. packed together waypoints can result in the plane orbiting where it wants to go
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 0.5 * 5280.0;
	
	/**
	 * Fly the plane. Assume simulator is launched with the plane in the air and the engine started. Plane object should 
	 * be configured by the invoker, and have a set of waypoints loaded. Blocks until flightplan is completed. 
	 * 
	 * @param plane		Planey McC172PFace
	 * @throws IOException 
	 */
	public static void runFlight(C172P plane) throws IOException {
		runFlight(plane, new FlightParameters());
	}
	
	public static void runFlight(C172P plane, FlightParameters parameters) throws IOException {
		//TODO: override any FlightParameters
		
		WaypointPosition startingWaypoint = plane.getNextWaypoint();
		plane.setCurrentWaypointTarget(startingWaypoint);
		
		//figure out the heading of our first waypoint based upon our current position
        double initialBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), startingWaypoint);    
        
        //point the plane at our first waypoint
        //***we really have to trust the sim is launched with the correct initial heading
        
        LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
        
        /////////////////////////////////
        //stabilize plane from the simulator start state
        
        //engine will spin up after unpause, but isn't considered running until it's fully spin up
        plane.setPause(false);
        
        //sometimes the flaps are down, which causes the plane to drift off course
        plane.resetControlSurfaces();
        
        stabilizeLaunch(plane, initialBearing);
        
        /////////////////////////////////
        //fly our waypoints
        plane.setBatterySwitch(false);
        plane.setAntiIce(true);
        
        //TODO: final and overrideable fields for the hardcoded values
        
        //i'm in a hurry and a c172p only goes so fast
        plane.setSimSpeedUp(8.0);
        
        //set the throttle here otherwise the mixture may cap it
        plane.setThrottle(FLIGHT_THROTTLE);
        
        //not much of a min, but both tanks largely filled means even weight and more stable flight
        double minFuelGal = 16.0;
        double minBatteryCharge = 0.25;
        
        //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
        int bearingRecalcCycleInterval = 5;    
        
        WaypointPosition nextWaypoint;
        TrackPosition currentPosition;
        double distanceToNextWaypoint;
        double nextWaypointBearing = initialBearing;
        long cycleSleep = 5;
        int waypointFlightCycles;
        
        while(plane.getWaypointCount() > 0) {
            
            nextWaypoint = plane.getAndRemoveNextWaypoint();
            plane.setCurrentWaypointTarget(nextWaypoint);
            
            LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
            
            nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
            
            //normalize to 0-360
            if(nextWaypointBearing < 0) {
                nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
            }
            
            LOGGER.info("Bearing to next waypoint: {}", nextWaypointBearing);
            
            ///////////////////////////////
            //transition to a stable path to next waypoint.
            
            double currentHeading;
            int headingComparisonResult;
            while(!FlightUtilities.withinHeadingThreshold(plane, MAX_HEADING_CHANGE, nextWaypointBearing)) {
                
                currentHeading = plane.getHeading();
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
                while(stablizeCount < 5) {
                    
                    FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);
                    stabilizeCheck(plane, intermediateHeading);
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        LOGGER.warn("Stabilization sleep interrupted", e);
                    }
                    
                    stablizeCount++;
                }
                
                //recalculate bearing since we've moved
                nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
                
                //normalize to 0-360
                if(nextWaypointBearing < FlightUtilities.DEGREES_ZERO) {
                    nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
                }
            }
            
            LOGGER.info("Heading change within tolerance");
            
            ///////////////////////////////
            //main flight path to way point
            waypointFlightCycles = 0;
            
            //add our next waypoint to the log
            plane.addWaypointToFlightLog(nextWaypoint);
            
            while( !PositionUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint, WAYPOINT_ARRIVAL_THRESHOLD) ) {
            
            	if(LOGGER.isTraceEnabled()) {
            		LOGGER.trace("======================\nCycle {} start.", waypointFlightCycles);
            	}

                //allow external interruption of flightplan
                if(plane.shouldAbandonCurrentWaypoint()) {
                	
                	LOGGER.info("Abandoning current waypoint");
                	
                	//reset abandon flag
                	plane.resetAbandonCurrentWaypoint();
                	
                	//break out of hasArrivedWaypoint loop and continue on onto the next waypoint in the flightplan
                	break;
                }
                
                currentPosition = plane.getPosition();
                
                distanceToNextWaypoint = PositionUtilities.distanceBetweenPositions(plane.getPosition(), nextWaypoint);
                
                plane.addTrackPositionToFlightLog(currentPosition);                    
                
                if(    
                    distanceToNextWaypoint >= WAYPOINT_ADJUST_MIN &&
                    waypointFlightCycles % bearingRecalcCycleInterval == 0
                ) 
                {
                    //reset bearing incase we've drifted, not not if we're too close
                    nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
                    
                    //normalize to 0-360
                    if(nextWaypointBearing < 0) {
                        nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
                    }
                    
                    LOGGER.info("Recalculating bearing to waypoint: {}", nextWaypointBearing);
                }
                
                // check altitude first, if we're in a nose dive that needs to be corrected first
                //TODO: altitude threshold into field 
                FlightUtilities.altitudeCheck(plane, 500, TARGET_ALTITUDE);

                // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                // you're facing a 5000ft mountain

                stabilizeCheck(plane, nextWaypointBearing);                    
                
                if(!plane.isEngineRunning()) {
                    LOGGER.error("Engine found not running.");

                    //TODO: try to recover
//                    LOGGER.error("Engine found not running. Attempting to restart.");
//                    plane.startupPlane();
//                    
//                    plane.setMixture(FLIGHT_MIXTURE);
//                    
//                    //increase throttle
//                    plane.setPause(true);
//                    plane.setThrottle(FLIGHT_THROTTLE);
//                    plane.setPause(false);
//                    
//                    plane.resetControlSurfaces();
                }
                
                //refill both tanks for balance
                if (plane.getFuelTank0Level() < minFuelGal || plane.getFuelTank1Level() < minFuelGal) {
                    plane.refillFuel();
                    
                    //check battery level
                    if (plane.getBatteryCharge() < minBatteryCharge) {
                        plane.setBatteryCharge(C172PFields.BATTERY_CHARGE_MAX);
                    }
                }
                
                if(LOGGER.isTraceEnabled()) {
                	LOGGER.trace("Telemetry Read: {}", telemetryReadOut(plane, nextWaypoint, nextWaypointBearing, distanceToNextWaypoint));
                	LOGGER.trace("\nCycle {} end\n======================", waypointFlightCycles);
                }
                
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
	
	private static void transitionToWaypoint(C172P plane, WaypointPosition targetWaypointPosition) {
		//TODO: move functionality from runFlight to here
	}
	
    private static String telemetryReadOut(C172P plane, WaypointPosition nextWaypoint, double waypointBearing, double waypointDistanceRemaining) {
        
        return 
            String.format("\nWaypoint: %s", nextWaypoint.getName()) +
            String.format("\nWaypoint Latitude: %s", nextWaypoint.getLatitude()) +
            String.format("\nWaypoint Longitude: %s", nextWaypoint.getLongitude()) +
            String.format("\nDistance remaining to waypoint: %s", waypointDistanceRemaining    ) +
            String.format("\nWaypoint bearing: %f", waypointBearing) +
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
    
    private static void stabilizeLaunch(C172P plane, double bearing) throws IOException {
    	LOGGER.info("============== Stabilizing plane on launch");
    	
        int i = 0;
        while(i < 100) {
            
            stabilizeCheck(plane, bearing);
            
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
            	LOGGER.warn("Launch stabilization phase 1 sleep interrupted", e);
            }
            
            i++;
        }
        
        plane.setMixture(FLIGHT_MIXTURE);
        
        //wait for mixture to take effect
        i = 0;
        while(i < 20) {
            
            stabilizeCheck(plane, bearing);
            
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
            	LOGGER.warn("Launch stabilization phase 2 sleep interrupted", e);
            }
            
            i++;
        }
        
    	LOGGER.info("============== Completed launch stabilization");
    }
    
    private static void stabilizeCheck(C172P plane, double bearing) throws IOException {
        if( 
            !FlightUtilities.withinRollThreshold(plane, FLIGHT_ROLL_MAX, TARGET_ROLL) ||
            !FlightUtilities.withinPitchThreshold(plane, FLIGHT_PITCH_MAX, TARGET_PITCH) ||
            !FlightUtilities.withinHeadingThreshold(plane, COURSE_ADJUSTMENT_INCREMENT, bearing)
        ) 
        {
            plane.forceStabilize(bearing, TARGET_ROLL, TARGET_PITCH, false);
                
            //keep seeing flaps extending on their own, probably as part of the plane autostart.
            //everything else on the c172p model seems to react to the launch altitude, but not this.
            //retracting flaps doesn't work immediately after starting the plane.
            //dumb.
            if(plane.getFlaps() != C172PFields.FLAPS_DEFAULT) {
                plane.resetControlSurfaces();
            }
        }
    }
}
