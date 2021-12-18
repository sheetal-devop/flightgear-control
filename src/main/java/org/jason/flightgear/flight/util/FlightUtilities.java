package org.jason.flightgear.flight.util;

import java.io.IOException;

import org.jason.flightgear.aircraft.FlightGearAircraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightUtilities {
	
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightUtilities.class);

    private final static int ORIENTATION_CHANGE_SLEEP = 2500;
    private final static int POSITION_CHANGE_SLEEP = 2500;
    private final static int VELOCITIES_CHANGE_SLEEP = 2500;
	
    //TODO: maybe make these functions boolean so we can easily determine if a change was made

    //TODO: this affects altitude. don't want a subsequent altitude check teleporting the plane into a mountain
//	public static void groundElevationCheck(FlightGearPlane plane, int maxDifference, double targetAltitude) {
//		groundElevationCheck(plane, maxDifference, targetAltitude, false);
//    }
//	
//	public static void groundElevationCheck(FlightGearPlane plane, int maxDifference, double targetAltitude, boolean trailingSleep) {
//        double currentAltitude = plane.getAltitude();
//        
//        LOGGER.info("Ground elevation check. Current {} vs target {}", currentAltitude, targetAltitude);
//        
//        //correct if too high or too low
//        if(targetAltitude - maxDifference > currentAltitude || 
//            targetAltitude + maxDifference < currentAltitude ) {
//            
//            LOGGER.info("Correcting altitude to target: {}", targetAltitude);
//            
//            plane.setPause(true);
//            plane.setAltitude(targetAltitude);
//            plane.setPause(false);
//            
//            if(trailingSleep) {
//	            //trailing sleep only if we made a change
//	            try {
//	                Thread.sleep(POSITION_CHANGE_SLEEP);
//	            } catch (InterruptedException e) {
//	                LOGGER.warn("Trailing sleep interrupted", e);
//	            }
//            }
//        }
//    }
    
	public static void altitudeCheck(FlightGearAircraft plane, int maxDifference, double targetAltitude) throws IOException {
    	altitudeCheck(plane, maxDifference, targetAltitude, false);
    }
    
    public static void altitudeCheck(FlightGearAircraft plane, int maxDifference, double targetAltitude, boolean trailingSleep) throws IOException {
        double currentAltitude = plane.getAltitude();
        
        LOGGER.info("Altitude check. Current {} vs target {}", currentAltitude, targetAltitude);
        
        //correct if too high or too low
        if( Math.abs(currentAltitude - targetAltitude ) > maxDifference) {
            
            LOGGER.info("Correcting altitude to target: {}", targetAltitude);
            
            plane.setPause(true);
            plane.setAltitude(targetAltitude);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(POSITION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                LOGGER.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void headingCheck(FlightGearAircraft plane, int maxDifference, double targetHeading) throws IOException {
    	headingCheck(plane, maxDifference, targetHeading, false);
    }
    
    public static void headingCheck(FlightGearAircraft plane, int maxDifference, double targetHeading, boolean trailingSleep) throws IOException {
        
        double currentHeading = plane.getHeading();
        double currentHeadingSin = Math.sin( Math.toRadians(currentHeading) );
        
        //heading is 0 to 360, both values are true/mag north
        
        LOGGER.info("Heading check. Current {} vs target {}", currentHeading, targetHeading);
        
        double minHeadingSin = Math.sin( Math.toRadians(targetHeading - maxDifference) );
        
        //target heading of 355 with a maxDifference of 10, is a min of 345 and a max of 5
        double maxHeadingSin = Math.sin( Math.toRadians(targetHeading + maxDifference) );
        
        LOGGER.info("Target heading sin range {} to {}, current: {}", minHeadingSin, maxHeadingSin, currentHeadingSin);
        
        /*
         * Might be easier to normal 
         * 
         * target 0, maxDif 5, min 355, max 5
         * 
         * target 90, maxDif 10, min 80, max 100
         * 
         * target 355, maxDif 10, min 345, max 5
         */
        
        if(currentHeadingSin > maxHeadingSin || currentHeadingSin < minHeadingSin) {
            
            LOGGER.info("Correcting heading to target: {}", targetHeading);
            
            double finalHeading = targetHeading;
//            double finalHeading = MAX_HEADING_ADJUSTMENT;
//            
//            if( currentHeadingSin > Math.sin( Math.toRadians(targetHeading)) ) {
//            	LOGGER.debug("Adjusting lower");
//            	finalHeading = currentHeadingSin - Math.sin( Math.toRadians(MAX_HEADING_ADJUSTMENT) );
//            } else if (currentHeadingSin < Math.sin( Math.toRadians(targetHeading))) {
//            	LOGGER.debug("Adjusting higher");
//            	finalHeading = currentHeadingSin + Math.sin( Math.toRadians(MAX_HEADING_ADJUSTMENT) );
//            }
            
            plane.setPause(true);
            plane.setHeading(finalHeading);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                LOGGER.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void pitchCheck(FlightGearAircraft plane, int maxDifference, double targetPitch) throws IOException {
    	pitchCheck(plane, maxDifference, targetPitch, false);
    }
    
    public static void pitchCheck(FlightGearAircraft plane, int maxDifference, double targetPitch, boolean trailingSleep) throws IOException {
        //read pitch
        //if pitch is too far from target in +/- directions, set to target
        
        double currentPitch = plane.getPitch();
        
        //pitch is -180 to 180
        
        LOGGER.info("Pitch check. Current {} vs target {}", currentPitch, targetPitch);
        
        if( Math.abs(currentPitch - targetPitch) > maxDifference) {
            
            LOGGER.info("Correcting pitch to target: {}", targetPitch);

            plane.setPause(true);
            plane.setPitch(targetPitch);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                LOGGER.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void rollCheck(FlightGearAircraft plane, int maxDifference, double targetRoll) throws IOException {
    	rollCheck(plane, maxDifference, targetRoll, false);
    }
    
    public static void rollCheck(FlightGearAircraft plane, int maxDifference, double targetRoll, boolean trailingSleep) throws IOException {
        double currentRoll = plane.getRoll();
        
        //roll is +180 to -180
        
        LOGGER.info("Roll check. Current {} vs target {}", currentRoll, targetRoll);
        
        if( Math.abs(currentRoll - targetRoll) > maxDifference) {
            LOGGER.info("Correcting roll to target: {}", targetRoll);
            
            plane.setPause(true);
            plane.setRoll(targetRoll);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                LOGGER.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void airSpeedCheck(FlightGearAircraft plane, int maxDifference, double targetAirspeed) throws IOException {
    	airSpeedCheck(plane, maxDifference, targetAirspeed, false);
    }
    
    /**
     * Enforce an airspeed on a plane. Use sparingly, since boosting speed on an adjusting plane can destabilize flight.
     * 
     * @param plane
     * @param maxDifference
     * @param targetAirspeed
     * @param trailingSleep
     * @throws IOException
     */
    public static void airSpeedCheck(FlightGearAircraft plane, int maxDifference, double targetAirspeed, boolean trailingSleep) throws IOException {
        double currentAirSpeed = plane.getAirSpeed();
        
		//set while not paused. this functions more like a boost- 
		//the plane can be acceled or deceled to the specified speed, 
		//but then the fdm takes over and stabilizes the air speed
        
        LOGGER.info("Airspeed check. Current {} vs target {}", currentAirSpeed, targetAirspeed);
        
        if( Math.abs(currentAirSpeed - targetAirspeed) > maxDifference) {
            LOGGER.info("Correcting airspeed to target: {}", targetAirspeed);
            
            plane.setPause(true);
            plane.setAirSpeed(targetAirspeed);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(VELOCITIES_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                LOGGER.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    
}
