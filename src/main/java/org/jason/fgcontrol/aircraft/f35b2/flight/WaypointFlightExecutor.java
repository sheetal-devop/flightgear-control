package org.jason.fgcontrol.aircraft.f35b2.flight;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.f35b2.F35B2;
import org.jason.fgcontrol.flight.position.PositionUtilities;
import org.jason.fgcontrol.flight.position.TrackPosition;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fly a set of waypoints with a F35B2.
 * 
 * @author jason
 *
 */
public abstract class WaypointFlightExecutor {
	
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlightExecutor.class);

	/**
	 * Fly the plane. Assume simulator is launched with the plane in the air and the engine started. Plane object should 
	 * be configured by the invoker, and have a set of waypoints loaded. Blocks until flightplan is completed. 
	 * 
	 * @param plane		Planey McF35B2Face
	 * 
	 * @throws IOException 
	 */
	public static void runFlight(F35B2 plane) throws IOException {
		runFlight(plane, new F35B2FlightParameters());
	}
	
	public static void runFlight(F35B2 plane, F35B2FlightParameters parameters) throws IOException {
		
		WaypointPosition startingWaypoint = plane.getNextWaypoint();
		plane.setCurrentWaypointTarget(startingWaypoint);
		
		//figure out the heading of our first waypoint based upon our current position
        double initialBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), startingWaypoint);    
        
        //point the plane at our first waypoint
        //***we really have to trust the sim is launched with the correct initial heading
        
        LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);

        
        ////////////////////////////        
        //flight parameters
        double fuelLevelRefillThresholdPercent = parameters.getFuelLevelRefillThresholdPercent();
        double throttleCourseChange = parameters.getThrottleCourseChange();
        double maxHeadingChange = parameters.getMaxHeadingChange();
        double courseAdjustmentIncrement = parameters.getCourseAdjustmentIncrement();
        double targetAltitude = parameters.getTargetAltitude();
        double maxAltitudeDeviation = parameters.getMaxAltitudeDeviation();
        double waypointArrivalThreshold = parameters.getWaypointArrivalThreshold();
        double waypointAdjustMinimumDistance = parameters.getWaypointAdjustMinimumDistance();
        double throttleWaypointApproach = parameters.getThrottleWaypointApproach();
        double throttleFlight = parameters.getThrottleFlight();
        double maxRoll = parameters.getFlightRollMax();
        double targetRoll = parameters.getTargetRoll();
        double maxPitch = parameters.getFlightPitchMax();
        double targetPitch = parameters.getTargetPitch();
        long cycleSleep = parameters.getBearingRecalculationCycleSleep();
        int stabilizationCycleCount = parameters.getStabilizationCycleCount();
        long stabilizationCycleSleep = parameters.getStabilizationCycleSleep();
        
        /////////////////////////////////
        //f15c is pretty stable at launch on its own
        
        /////////////////////////////////
        //fly our waypoints
        
        plane.resetControlSurfaces();
        
        plane.setPause(false);
        
        //chase view
        plane.setCurrentView(2);
        
        //f35b2 doesnt retract gear automatically
        plane.setGearDown(false);

        //full throttle or the engines will have divergent thrust outputs
        plane.setEngineThrottle(throttleFlight);            
        
        //trouble doing waypoint flight at faster speeds with high speedup under the current threading model
        //TODO: separate threads for telemetry readouts and flight control
        //plane.setSimSpeedUp(2.0);
    
        //not much of a min, but all tanks largely filled means even weight distribution and more stable flight
        double minFuelTank0 = plane.getFuelTank0Capacity() * fuelLevelRefillThresholdPercent,
                minFuelTank1 = plane.getFuelTank1Capacity() * fuelLevelRefillThresholdPercent,
                minFuelTank2 = plane.getFuelTank2Capacity() * fuelLevelRefillThresholdPercent,
                minFuelTank3 = plane.getFuelTank3Capacity() * fuelLevelRefillThresholdPercent,
                minFuelTank4 = plane.getFuelTank4Capacity() * fuelLevelRefillThresholdPercent;
        
        //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
        //int bearingRecalcCycleInterval = 5;     
        
        WaypointPosition nextWaypoint;
        TrackPosition currentPosition;
        double nextWaypointBearing = initialBearing;
        double distanceToNextWaypoint;
        int waypointFlightCycles;
        while(plane.getWaypointCount() > 0) {
            
            nextWaypoint = plane.getAndRemoveNextWaypoint();
            plane.setCurrentWaypointTarget(nextWaypoint);
            
            //possibly slow the simulator down if the next waypoint is close.
            //it's possible that hard and frequent course adjustments are needed
            
            LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
            
            nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
            
            LOGGER.info("Bearing to next waypoint: {}", nextWaypointBearing);
            
            ///////////////////////////////
            //transition to a stable path to next waypoint.
            
            //turning to face next waypoint. throttle down
            plane.setEngineThrottle(throttleCourseChange);
            
            double currentHeading;
            
            //waypoint approach flag since the f15c travels at high speeds
            boolean waypointApproach = false;
            
            while(!FlightUtilities.withinHeadingThreshold(plane, maxHeadingChange, nextWaypointBearing)) {
                
                currentHeading = plane.getHeading();
                
                plane.addTrackPositionToFlightLog(plane.getPosition());
                                
                LOGGER.debug("Easing hard turn from current heading {} to target heading {} for waypoint", 
                		currentHeading, nextWaypointBearing, nextWaypoint.getName());
                
                double intermediateHeading = FlightUtilities.determineCourseChangeAdjustment(currentHeading, courseAdjustmentIncrement, nextWaypointBearing);
                if( intermediateHeading == currentHeading) {
                  LOGGER.warn("Found no adjustment needed");

                  //shouldn't happen since we'd be with the heading threshold tested by the loop conditional. break
                  //TODO: ^^^ really? what if we just held the course until we were far enough away
                  
                  //continue;
                  break;
                }
                
                LOGGER.info("++++Stabilizing to intermediate heading {} from current {} with target {}", intermediateHeading, currentHeading, nextWaypointBearing);
                
                //low count here. if we're not on track by the end, the heading check should fail and get us back here
                //seeing close waypoints get overshot
                int stablizeCount = 0;
                while(stablizeCount < stabilizationCycleCount) {
                    
                    FlightUtilities.altitudeCheck(plane, maxAltitudeDeviation, targetAltitude);
                    stabilizeCheck(plane, intermediateHeading,
                    	maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement
                    );
                    
                    try {
                        Thread.sleep(stabilizationCycleSleep);
                    } catch (InterruptedException e) {
                    	LOGGER.warn("Stabilization sleep interrupted", e);
                    }
                    
                    stablizeCount++;
                }
                
                //recalculate bearing since we've moved
                nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
                
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
            plane.setEngineThrottle(throttleFlight);
            waypointApproach = false;
            
            ///////////////////////////////
            //main flight path to way point
            waypointFlightCycles = 0;
            
            //add our next waypoint to the log
            plane.addWaypointToFlightLog(nextWaypoint);

            while( !PositionUtilities.hasArrivedAtWaypoint(plane.getPosition(), nextWaypoint, waypointArrivalThreshold) ) {
            
            	if(LOGGER.isTraceEnabled()) {
            		LOGGER.trace("======================\nCycle {} start.", waypointFlightCycles);
            	}

                //allow external interruption of flightplan
                if(plane.shouldAbandonCurrentWaypoint()) {
                	
                	LOGGER.info("Abandoning current waypoint {}", nextWaypoint.toString());
                	
                	//reset abandon flag
                	plane.resetAbandonCurrentWaypoint();
                	
                	//break out of hasArrivedWaypoint loop and continue on onto the next waypoint in the flightplan
                	break;
                }
                
                currentPosition = plane.getPosition();
                
                distanceToNextWaypoint = PositionUtilities.distanceBetweenPositions(plane.getPosition(), nextWaypoint);
                
                plane.addTrackPositionToFlightLog(currentPosition);
                
                //adjust the throttle once we've gotten close enough to ease waypoint transitions
                
                if(    
                    distanceToNextWaypoint > waypointAdjustMinimumDistance //&&
                    //waypointFlightCycles % bearingRecalcCycleInterval == 0
                ) 
                {
                	//normal transit between waypoints. 
                	//far enough away from the last, but not close enough to the next
                	
                    //reset bearing incase we've drifted, not not if we're too close
                    nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
                    
                    LOGGER.info("Recalculating bearing to waypoint {}: {}. Distance remaining: {}", 
                    		nextWaypoint.getName(), 
                    		nextWaypointBearing,
                    		distanceToNextWaypoint
                    );   
                } else if ( !waypointApproach && distanceToNextWaypoint < waypointArrivalThreshold * 3 ) {
                	
                    //throttle down for waypoint approach to accommodate any late corrections
                    
                	LOGGER.info("Setting throttle for waypoint approach: {}", throttleWaypointApproach);
                	
                    plane.setEngineThrottle(throttleWaypointApproach);
                    
                    waypointApproach = true;
                    
                } else if ( !waypointApproach && plane.getEngine0Throttle() != throttleFlight ) {
                    
                    //far enough away from the previous waypoint and not close enough to the next
                    //throttle up to max if we haven't already
                    plane.setEngineThrottle(throttleFlight);
                }
                
                // check altitude first, if we're in a nose dive that needs to be corrected first
                FlightUtilities.altitudeCheck(plane, maxAltitudeDeviation, targetAltitude);

                // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                // you're facing a 5000ft mountain

                stabilizeCheck(plane, nextWaypointBearing,
                   	maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement
                );                      
                
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

                if(LOGGER.isTraceEnabled()) {
                	LOGGER.trace("Telemetry Read: {}", telemetryReadOut(plane, nextWaypoint, nextWaypointBearing));
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
	
	private static void transitionToWaypoint(F35B2 plane, WaypointPosition targetWaypointPosition) {
		//TODO: move functionality from runFlight to here
	}

    /**
     * Build a String to help log plane telemetry.
     * 
     * @param plane
     * @param position
     * @param targetBearing
     * @return
     */
    public static String telemetryReadOut(F35B2 plane, WaypointPosition position, double targetBearing) {
        
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
            String.format("\nEnv Temp: %f", plane.getTemperature()) + 
            String.format("\nEngine 1 Throttle: %f", plane.getEngine0Throttle()) +
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
    
    public static void stabilizeCheck(
    	F35B2 plane, 
    	double waypointBearing,
    	double maxRoll,
    	double targetRoll,
    	double maxPitch,
    	double targetPitch,
    	double courseAdjustmentIncrement
    ) throws IOException {
        if( 
            !FlightUtilities.withinRollThreshold(plane, maxRoll, targetRoll) ||
            !FlightUtilities.withinPitchThreshold(plane, maxPitch, targetPitch) ||
            !FlightUtilities.withinHeadingThreshold(plane, courseAdjustmentIncrement, waypointBearing)
        ) 
        {
        	//if the current heading is too far from the waypoint bearing, adjust by courseAdjustmentIncrement
        	double currentHeading = plane.getHeading();
        	double intermediateHeading = FlightUtilities.determineCourseChangeAdjustment(currentHeading, courseAdjustmentIncrement, waypointBearing);

        	if( currentHeading != intermediateHeading) {
        		LOGGER.info("Adjusting heading to intermediate heading: {}", intermediateHeading);
        		waypointBearing = intermediateHeading;
        	} else {
        		if(LOGGER.isTraceEnabled()) {
        			LOGGER.trace("Sticking to current heading: {}", currentHeading);
        		}
        		waypointBearing = currentHeading;
        	}
        	
            plane.forceStabilize(waypointBearing, targetPitch, targetRoll, false);
        }
    }
}
