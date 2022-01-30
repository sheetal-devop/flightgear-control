package org.jason.fgcontrol.aircraft.c172p.app;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PFields;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
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

public class WaypointFlight {
        
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlight.class);
    
    private final static double WAYPOINT_ADJUST_MIN = 0.75 * 5280.0; 
    
    private final static double FLIGHT_MIXTURE = 0.90;
    private final static double FLIGHT_THROTTLE = 0.90;
    
    private final static double MAX_HEADING_CHANGE = 20.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    private final static double COURSE_ADJUSTMENT_INCREMENT = 12.5;
    
    private final static String LAUNCH_TIME_GMT = "2021-07-01T20:00:00";
    
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 0.75 * 5280.0;
    
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
    
    private static void stabilizeCheck(C172P plane, double bearing) throws IOException {
        if( 
            !FlightUtilities.withinRollThreshold(plane, 2.0, 0.0) ||
            !FlightUtilities.withinPitchThreshold(plane, 4.0, 2.0) ||
            !FlightUtilities.withinHeadingThreshold(plane, COURSE_ADJUSTMENT_INCREMENT, bearing)
        ) 
        {
            plane.forceStabilize(bearing, 0.0, 2.0);
                
            //keep seeing flaps extending on their own, probably as part of the plane autostart.
            //everything else on the c172p model seems to react to the launch altitude, but not this.
            //retracting flaps doesn't work immediately after starting the plane.
            //dumb.
            if(plane.getFlaps() != C172PFields.FLAPS_DEFAULT) {
                plane.resetControlSurfaces();
            }
        }
    }
    
    public static void main(String [] args) {
        C172P plane = null;
        
        double targetAltitude;
        
        long startTime = System.currentTimeMillis();
        
        FlightLog flightLog = new FlightLog();
         
        WaypointManager waypointManager = new WaypointManager();
        
        //local tour
        //C172P script launches from YVR
//        waypointManager.setWaypoints( KnownRoutes.VANCOUVER_TOUR );
//        targetAltitude = 6000;
//        flightMixture = 0.93;
//        flightThrottle = 0.93;
        
        //bc tour
        //C172P script launches from YVR
        ArrayList<WaypointPosition> route = KnownRoutes.BC_SOUTH_TOUR;
        
        //for fun, mix it up
        //Collections.reverse(route);
        
        waypointManager.setWaypoints(route);
        
        targetAltitude = 9000;
//        flightMixture = 0.90;
//        flightThrottle = 0.90;
        
        //for fun, mix it up
//        List<WaypointPosition> reverseOrder = waypointManager.getWaypoints();
//        Collections.reverse( reverseOrder );
//        waypointManager.setWaypoints( reverseOrder );
        
        WaypointPosition startingWaypoint = waypointManager.getNextWaypoint();

        try {
            plane = new C172P();
        
            //chase view
            plane.setCurrentView(2);
            
            plane.setDamageEnabled(false);
            plane.setComplexEngineProcedures(false);
            plane.setWinterKitInstalled(true);
            plane.setGMT(LAUNCH_TIME_GMT);
            
            //in case we get a previously lightly-used environment
            plane.refillFuel();
            plane.setBatteryCharge(C172PFields.BATTERY_CHARGE_MAX);
            
            //figure out the heading of our first waypoint based upon our current position
            double initialBearing = PositionUtilities.calcBearingToGPSCoordinates(plane.getPosition(), startingWaypoint);            
            
            //point the plane at our first waypoint
            LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
            plane.setHeading(initialBearing);
            
            //startup procedure to get the engines running
            if(!plane.isEngineRunning()) {
                plane.startupPlane();
            } else {
                LOGGER.info("Engine was running on launch. Skipping startup");
            }
            
            //wait for startup to complete and telemetry reads to arrive
            
            int i = 0;
            while(i < 80) {
                
                stabilizeCheck(plane, initialBearing);
                
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                i++;
            }
            
            //////////////////
            //launch(plane);
            
            plane.setMixture(FLIGHT_MIXTURE);
            
            //wait for mixture to take effect
            i = 0;
            while(i < 20) {
                
                stabilizeCheck(plane, initialBearing);
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                i++;
            }
            
            //increase throttle
            plane.setPause(true);
            plane.setThrottle(FLIGHT_THROTTLE);
            plane.setPause(false);
            
            plane.resetControlSurfaces();
            
            plane.setBatterySwitch(false);
            plane.setAntiIce(true);
            
            //i'm in a hurry and a c172p only goes so fast
            plane.setSimSpeedUp(8.0);
        
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
            
            while(waypointManager.getWaypointCount() > 0) {
                
                nextWaypoint = waypointManager.getAndRemoveNextWaypoint();
                
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
                    if(nextWaypointBearing < 0.0) {
                        nextWaypointBearing += FlightUtilities.DEGREES_CIRCLE;
                    }
                }
                
                LOGGER.info("Heading change within tolerance");
                
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
                    FlightUtilities.altitudeCheck(plane, 500, targetAltitude);

                    // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                    // you're facing a 5000ft mountain

                    stabilizeCheck(plane, nextWaypointBearing);                    
                    
                    if(!plane.isEngineRunning()) {
                        LOGGER.error("Engine found not running. Attempting to restart.");
                        plane.startupPlane();
                        
                        plane.setMixture(FLIGHT_MIXTURE);
                        
                        //increase throttle
                        plane.setPause(true);
                        plane.setThrottle(FLIGHT_THROTTLE);
                        plane.setPause(false);
                        
                        plane.resetControlSurfaces();
                    }
                    
                    //refill both tanks for balance
                    if (plane.getFuelTank0Level() < minFuelGal || plane.getFuelTank1Level() < minFuelGal) {
                        plane.refillFuel();
                        
                        //check battery level
                        if (plane.getBatteryCharge() < minBatteryCharge) {
                            plane.setBatteryCharge(0.9);
                        }
                    }
                    
                    LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane, nextWaypoint, nextWaypointBearing, distanceToNextWaypoint));
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
            
            //pause so the plane doesn't list from its heading and crash
            plane.setPause(true);
        } catch (FlightGearSetupException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AircraftStartupException e) {
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
        
        long tripTime = (System.currentTimeMillis() - startTime);
        
        LOGGER.info("Completed course in: {}ms => {} minutes", tripTime, ( ((double)tripTime / 1000.0) * 60.0 ) );
    }
}

