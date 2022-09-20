package org.jason.fgcontrol.aircraft.f15c.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CConfig;
import org.jason.fgcontrol.aircraft.f15c.F15CFields;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.flight.position.KnownRoutes;
import org.jason.fgcontrol.flight.position.PositionUtilities;
import org.jason.fgcontrol.flight.position.TrackPosition;
import org.jason.fgcontrol.flight.position.WaypointManager;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.jason.fgcontrol.flight.util.FlightLog;
import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointFlightFleet {
        
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlightFleet.class);
    
    private final static double MAX_HEADING_CHANGE = 12.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    private final static double COURSE_ADJUSTMENT_INCREMENT = 3.5;
    
    //if the sim is steering the plane by forcing positional constraints,
    //then the plane is essentially a missile at typical f15c speeds so we need a wide margin of error
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 10.0 * 5280.0;
    
    //beyond this distance, increase throttle to crusing level (MAX)
    private final static double WAYPOINT_ADJUST_MIN_DIST = 30.0 * 5280.0; 
    
    private final static String LAUNCH_TIME_GMT = "2021-07-01T20:00:00";
    
    private final static double THROTTLE_WAYPOINT_APPROACH = 0.75;
    private final static double THROTTLE_COURSE_CHANGE = 0.6;
    
    private final static double FUEL_LEVEL_REFILL_THRESHOLD_PERCENT = 0.9;
    
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
            !FlightUtilities.withinRollThreshold(plane, 1.5, 0.0) ||
            !FlightUtilities.withinPitchThreshold(plane, 3.0, 1.5) ||
            !FlightUtilities.withinHeadingThreshold(plane, COURSE_ADJUSTMENT_INCREMENT, bearing)
        ) 
        {
            plane.forceStabilize(bearing, 0.0, 2.0);
        }
    }
        
    public static void main(String [] args) {
        F15C plane = null;
        
        long startTime = System.currentTimeMillis();
        
        FlightLog flightLog = new FlightLog();
         
        WaypointManager waypointManager = new WaypointManager();
        
        //too fast for the local tour
        
        double targetAltitude = 11500.0;
        
        //bc tour
        //f15c script launches from YVR
        
        ArrayList<WaypointPosition> route = KnownRoutes.BC_SOUTH_TOUR;
        
        //for fun, mix it up
        //Collections.reverse(route);
        
        waypointManager.setWaypoints(route);

        WaypointPosition startingWaypoint = waypointManager.getNextWaypoint();

        try {
        	
        	String confFile = "./f15c.properties";
        	if(args.length >= 1) {
        		confFile = args[0];	
        	}
        	
        	Properties simProperties = new Properties();
        	simProperties.load(new FileInputStream(confFile) );
        	
        	F15CConfig f15cConfig = new F15CConfig(simProperties); 
        	
        	LOGGER.info("Using config:\n{}", f15cConfig.toString() );
        	
            plane = new F15C(f15cConfig);
        
            plane.setDamageEnabled(false);
            plane.setGMT(LAUNCH_TIME_GMT);
            
            plane.refillFuel();
            
            //figure out the heading of our first waypoint based upon our current position
            TrackPosition startPosition = plane.getPosition();
            double initialBearing = PositionUtilities.calcBearingToGPSCoordinates(startPosition, startingWaypoint);            
            
            //point the plane at our first waypoint
            LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
            
            //make sure the shell script is launched with the initial heading instead
            //plane.setHeading(initialBearing);
            
            //startup procedure to get the engines running
            plane.startupPlane();
            
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
            while(waypointManager.getWaypointCount() > 0) {
                
                nextWaypoint = waypointManager.getAndRemoveNextWaypoint();
                
                //possibly slow the simulator down if the next waypoint is close.
                //it's possible that hard and frequent course adjustments are needed
                
                LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
                
                nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), nextWaypoint);
                
                //normalize to 0-360
                if(nextWaypointBearing < 0.0) {
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
                    
                    flightLog.addTrackPosition(plane.getPosition());
                    
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
                        
                        FlightUtilities.altitudeCheck(plane, 500, targetAltitude);
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
                flightLog.addWaypoint(nextWaypoint);
                
                while( !PositionUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint, WAYPOINT_ARRIVAL_THRESHOLD) ) {
                
                    LOGGER.info("======================\nCycle {} start.", waypointFlightCycles);

                    currentPosition = plane.getPosition();
                    
                    distanceToNextWaypoint = PositionUtilities.distanceBetweenPositions(plane.getPosition(), nextWaypoint);
                    
                    flightLog.addTrackPosition(currentPosition);
                    
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
                    FlightUtilities.altitudeCheck(plane, 500, targetAltitude);

                    // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                    // you're facing a 5000ft mountain

                    stabilizeCheck(plane, nextWaypointBearing);                    
                    
                    if(!plane.isEngineRunning()) {
                        LOGGER.error("Engine found not running. Attempting to restart.");
//                        plane.startupPlane();
//                        
//                        //increase throttle
//                        plane.setPause(true);
//                        plane.resetControlSurfaces();
//                        plane.setPause(false);
//                        
//                        plane.setEngineThrottles(F15CFields.THROTTLE_MAX);
                        
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
            
            //since we're done and no longer stabilizing the plane, pause the sim so the plane doesn't fall
            plane.setPause(true);
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
