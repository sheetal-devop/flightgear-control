package org.jason.flightgear.planes.util;

import org.jason.flightgear.planes.FlightGearPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightUtilities {
	
    private static Logger logger = LoggerFactory.getLogger(FlightUtilities.class);

    private final static int ORIENTATION_CHANGE_SLEEP = 2500;
	
    public static void altitudeCheck(FlightGearPlane plane, int maxDifference, double targetAltitude) {
    	altitudeCheck(plane, maxDifference, targetAltitude, false);
    }
    
    public static void altitudeCheck(FlightGearPlane plane, int maxDifference, double targetAltitude, boolean trailingSleep) {
        double currentAltitude = plane.getAltitude();
        
        logger.info("Altitude check. Current {} vs target {}", currentAltitude, targetAltitude);
        
        //correct if too high or too low
        if(targetAltitude - maxDifference > currentAltitude || 
            targetAltitude + maxDifference < currentAltitude ) {
            
            logger.info("Correcting altitude to target: {}", targetAltitude);
            
            plane.setPause(true);
            plane.setAltitude(targetAltitude);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                logger.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void headingCheck(FlightGearPlane plane, int maxDifference, double targetHeading) {
    	headingCheck(plane, maxDifference, targetHeading, false);
    }
    
    public static void headingCheck(FlightGearPlane plane, int maxDifference, double targetHeading, boolean trailingSleep) {
        
        double currentHeading = plane.getHeading();
        double currentHeadingSin = Math.sin( Math.toRadians(currentHeading) );
        
        //heading is 0 to 360, both values are true/mag north
        
        logger.info("Heading check. Current {} vs target {}", currentHeading, targetHeading);
        
        double minHeadingSin = Math.sin( Math.toRadians(targetHeading - maxDifference) );
//        if (minHeading < 0) {
//            //-1 deg heading results in heading of 359
//            minHeading += 360;
//        }
        
        //target heading of 355 with a maxDifference of 10, is a min of 345 and a max of 5
        double maxHeadingSin = Math.sin( Math.toRadians(targetHeading + maxDifference) );
        
        logger.info("Target heading sin range {} to {}, current: {}", minHeadingSin, maxHeadingSin, currentHeadingSin);
        
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
            
            logger.info("Correcting heading to target: {}", targetHeading);
            
            plane.setPause(true);
            plane.setHeading(targetHeading);
            
            //force this here? or count on the flight app to handle the specific context
//            setPitch(0);
//            setRoll(0);
//            setYaw(0);
            
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                logger.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void pitchCheck(FlightGearPlane plane, int maxDifference, double targetPitch) {
    	pitchCheck(plane, maxDifference, targetPitch, false);
    }
    
    public static void pitchCheck(FlightGearPlane plane, int maxDifference, double targetPitch, boolean trailingSleep) {
        //read pitch
        //if pitch is too far from target in +/- directions, set to target
        
        double currentPitch = plane.getPitch();
        
        //pitch is -180 to 180
        
        logger.info("Pitch check. Current {} vs target {}", currentPitch, targetPitch);
        
        if( Math.abs(currentPitch) - Math.abs(targetPitch) > maxDifference) {
            
            logger.info("Correcting pitch to target: {}", targetPitch);

            plane.setPause(true);
            plane.setPitch(targetPitch);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                logger.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
    
    public static void rollCheck(FlightGearPlane plane, int maxDifference, double targetRoll) {
    	rollCheck(plane, maxDifference, targetRoll, false);
    }
    
    public static void rollCheck(FlightGearPlane plane, int maxDifference, double targetRoll, boolean trailingSleep) {
        double currentRoll = plane.getRoll();
        
        //roll is +180 to -180
        
        logger.info("Roll check. Current {} vs target {}", currentRoll, targetRoll);
        
        if( Math.abs(currentRoll) - Math.abs(targetRoll) > maxDifference) {
            logger.info("Correcting roll to target: {}", targetRoll);
            
            plane.setPause(true);
            plane.setRoll(targetRoll);
            plane.setPause(false);
            
            if(trailingSleep) {
	            //trailing sleep only if we made a change
	            try {
	                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
	            } catch (InterruptedException e) {
	                logger.warn("Trailing sleep interrupted", e);
	            }
            }
        }
    }
}
