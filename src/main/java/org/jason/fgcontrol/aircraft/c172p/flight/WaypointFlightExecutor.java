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

	//TODO: put some of these fields into a F15CFlightParameters and allow override
	
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlightExecutor.class);
	
	/**
	 * Fly the plane. Assume simulator is launched with the plane in the air and the engine started. Plane object should 
	 * be configured by the invoker, and have a set of waypoints loaded. Blocks until flightplan is completed. 
	 * 
	 * @param plane		Planey McC172PFace
	 * @throws IOException 
	 */
	public static void runFlight(C172P plane) throws IOException {
		runFlight(plane, new C172PFlightParameters());
	}
	
	public static void runFlight(C172P plane, C172PFlightParameters parameters) throws IOException {
		
		/////////////////////
		//seeing odd behavior for the c172p in fgfs 2020.03.17
		//if camera view is switched during autostart, the camera actually ends up pointing
		//at the pilot's chair for some reason.
		//TODO: periodically test removing this
		new Thread() {
			public void run() {
				try {
					LOGGER.info("_______________Starting view change hack thread");
					Thread.sleep(15L*1000L);
					plane.setCurrentView(2);
					LOGGER.info("_______________Ending view change hack thread");
				} catch (IOException | InterruptedException e) {
					LOGGER.warn("View change hack sleep exception", e);
				}
			}
		}.start();
		/////////////////////
		
		WaypointPosition startingWaypoint = plane.getNextWaypoint();
		plane.setCurrentWaypointTarget(startingWaypoint);
		
		//figure out the heading of our first waypoint based upon our current position
        double initialBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), startingWaypoint);    
        
        //point the plane at our first waypoint
        //***we really have to trust the sim is launched with the correct initial heading
        
        LOGGER.info("First waypoint is {} and initial target bearing is {}", startingWaypoint.toString(), initialBearing);
        
        /////////////////////////////////
        //stabilize plane from the simulator start state
        
        //engine will spin up after unpause, but isn't considered running until it's fully spin up
        plane.setPause(false);
        
        //sometimes the flaps are down, which causes the plane to drift off course
        plane.resetControlSurfaces();
        
        ////////////////////////////        
        //flight parameters
        //not much of a min, but both tanks largely filled means even weight and more stable flight
        double minFuelGal = parameters.getLowFuelAmountThreshold();
        double minBatteryCharge = parameters.getLowBatteryAmountThreshold();
        
        double maxHeadingChange = parameters.getMaxHeadingChange();
        double courseAdjustmentIncrement = parameters.getCourseAdjustmentIncrement();
        
        double targetAltitude = parameters.getTargetAltitude();
        double maxAltitudeDeviation = parameters.getMaxAltitudeDeviation();
        
        double targetRoll = parameters.getTargetRoll();
        double maxRoll = parameters.getFlightRollMax();
        double targetPitch = parameters.getTargetPitch();
        double maxPitch = parameters.getFlightPitchMax();
        
        int bearingRecalcCycleInterval = parameters.getBearingRecalculationCycleInterval();   
        
        double waypointArrivalThreshold = parameters.getWaypointArrivalThreshold();
        double waypointAdjustMinimum = parameters.getWaypointAdjustMinimumDistance();
        
        long cycleSleep = parameters.getBearingRecalculationCycleSleep();
        
        int stabilizationCycleCount = parameters.getStabilizationCycleCount();
        long stabilizationCycleSleep = parameters.getStabilizationCycleSleep();
        
        //catch the plane launched externally and steer it towards the inital bearing
        //TODO: move the mixture set outside of this function?
        stabilizeLaunch(plane, initialBearing, 
        	parameters.getFlightMixture(),
        	maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement
        );
        
        plane.setBatterySwitch(false);
        plane.setAntiIce(true);
                
        //i'm in a hurry and a c172p only goes so fast
        plane.setSimSpeedUp(parameters.getSimSpeedup());
        
        //set the throttle here otherwise the mixture may cap it
        plane.setThrottle(parameters.getFlightThrottle());
        
        
        ////////////////////////////
        //intermediate values
        int waypointFlightCycles;
        WaypointPosition nextWaypoint;
        TrackPosition currentPosition;
        double distanceToNextWaypoint;
        double nextWaypointBearing = initialBearing;
        
        //main flight loop
        while(plane.getWaypointCount() > 0) {
            
            nextWaypoint = plane.getAndRemoveNextWaypoint();
            plane.setCurrentWaypointTarget(nextWaypoint);
            
            LOGGER.info("Headed to next waypoint: {}", nextWaypoint.toString());
            
            //bearing normalized to 0-360
            nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
            
            LOGGER.info("Bearing to next waypoint: {}", nextWaypointBearing);
            
            ///////////////////////////////
            //transition to a stable path to next waypoint.
            
            //seems to cause problems- the engine may still be spinning up on the first iteration
            //plane.setCurrentView(2);
            
            double currentHeading;
            int headingComparisonResult;
            while(!FlightUtilities.withinHeadingThreshold(plane, maxHeadingChange, nextWaypointBearing)) {
                
                currentHeading = plane.getHeading();
                headingComparisonResult = FlightUtilities.headingCompareTo(plane, nextWaypointBearing);
                
                LOGGER.debug("Easing hard turn from current heading {} to target heading {} for waypoint", 
                		currentHeading, nextWaypointBearing, nextWaypoint.getName());
                
                //adjust clockwise or counter? 
                //this may actually change in the middle of the transition itself
                double intermediateHeading = currentHeading;
                if(headingComparisonResult == FlightUtilities.HEADING_NO_ADJUST) {
                    LOGGER.warn("Found no adjustment needed");
                    //shouldn't happen since we'd be with the heading threshold
                    break;
                } else if(headingComparisonResult == FlightUtilities.HEADING_CW_ADJUST) {
                    //1: adjust clockwise
                    intermediateHeading = (intermediateHeading + courseAdjustmentIncrement ) % FlightUtilities.DEGREES_CIRCLE;
                } else {
                    //-1: adjust counterclockwise
                    intermediateHeading -= courseAdjustmentIncrement;
                    
                    //normalize 0-360
                    if(intermediateHeading < 0) intermediateHeading += FlightUtilities.DEGREES_CIRCLE;
                }
                
                //TODO: add waypoint info to log message
                LOGGER.info("++++Stabilizing to intermediate heading {} from current {} with target {} for next waypoint {}", 
                		intermediateHeading, currentHeading, nextWaypointBearing, nextWaypoint.getName());
                
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
                
                //recalculate bearing since we've moved, normalize to 0-360
                nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
            }
            
            LOGGER.info("Heading change within tolerance");
            
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
                
                //periodically recalculate bearing to next waypoint
                if(    
                    distanceToNextWaypoint >= waypointAdjustMinimum &&
                    waypointFlightCycles % bearingRecalcCycleInterval == 0
                ) 
                {
                    //reset bearing incase we've drifted, not not if we're too close. normalize to 0-360
                    nextWaypointBearing = PositionUtilities.calcBearingToGPSCoordinatesNormalized(plane.getPosition(), nextWaypoint);
                      
                    LOGGER.info("Recalculating bearing to waypoint {}: {}. Distance remaining: {}", 
                    		nextWaypoint.getName(), 
                    		nextWaypointBearing,
                    		distanceToNextWaypoint
                    );   
                }
                
                // check altitude first, if we're in a nose dive that needs to be corrected first
                FlightUtilities.altitudeCheck(plane, maxAltitudeDeviation, targetAltitude);

                // TODO: ground elevation check. it's a problem if your target alt is 5000ft and
                // you're facing a 5000ft mountain

                stabilizeCheck(plane, nextWaypointBearing,
                	maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement
                );                    
                
                if(!plane.isEngineRunning()) {
                    LOGGER.error("Engine found not running.");

                    //TODO: try to recover
//                    LOGGER.error("Engine found not running. Attempting to restart.");
//                    try {
//						plane.startupPlane();
//						
//	                    plane.setMixture(parameters.getFlightMixture());
//	                    
//	                    //increase throttle
//	                    plane.setPause(true);
//	                    plane.setThrottle(parameters.getFlightThrottle());
//	                    plane.setPause(false);
//	                    
//	                    plane.resetControlSurfaces();
//						
//					} catch (AircraftStartupException e) {
//						LOGGER.error("AircraftStartupException attempting to restart aircraft engine", e);
//					}
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
	
    /**
     * Build a String to help log plane telemetry.
     * 
     * @param plane
     * @param nextWaypoint
     * @param waypointBearing
     * @param waypointDistanceRemaining
     * @return
     */
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
    
    private static void stabilizeLaunch(C172P plane, 
    		double bearing, 
    		double mixture,
        	double maxRoll,
        	double targetRoll,
        	double maxPitch,
        	double targetPitch,
        	double courseAdjustmentIncrement
    ) throws IOException {
    	LOGGER.info("============== Stabilizing plane on launch");
    	
    	//TODO: unhardcode these values probably arrived at via trial and error
        int i = 0;
        while(i < 100) {
            
            stabilizeCheck(plane, bearing, maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement);
            
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
            	LOGGER.warn("Launch stabilization phase 1 sleep interrupted", e);
            }
            
            i++;
        }
        
        plane.setMixture(mixture);
        
        //wait for mixture to take effect
        i = 0;
        while(i < 20) {
            
        	stabilizeCheck(plane, bearing, maxRoll, targetRoll, maxPitch, targetPitch, courseAdjustmentIncrement);
            
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
            	LOGGER.warn("Launch stabilization phase 2 sleep interrupted", e);
            }
            
            i++;
        }
        
    	LOGGER.info("============== Completed launch stabilization");
    }
    
    private static void stabilizeCheck(
    	C172P plane, 
    	double bearing, 
    	double maxRoll,
    	double targetRoll,
    	double maxPitch,
    	double targetPitch,
    	double courseAdjustmentIncrement
    ) throws IOException {
        if( 
            !FlightUtilities.withinRollThreshold(plane, maxRoll, targetRoll) ||
            !FlightUtilities.withinPitchThreshold(plane, maxPitch, targetPitch) ||
            !FlightUtilities.withinHeadingThreshold(plane, courseAdjustmentIncrement, bearing)
        ) 
        {
            plane.forceStabilize(bearing, targetRoll, targetPitch, false);
                
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
