package org.jason.flightgear.planes.c172p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172PFlightUtilities {
	
    private static Logger logger = LoggerFactory.getLogger(C172PFlightUtilities.class);

    private final static int ORIENTATION_CHANGE_SLEEP = 2500;
	
    public static void altitudeCheck(C172P plane, int maxDifference, double targetAltitude) {
        double currentAltitude = plane.getAltitude();
        
        logger.info("Altitude check. Current {} vs target {}", currentAltitude, targetAltitude);
        
        //correct if too high or too low
        if(targetAltitude - maxDifference > currentAltitude || 
            targetAltitude + maxDifference < currentAltitude ) {
            
            logger.info("Correcting altitude to target: {}", targetAltitude);
            
            plane.setPause(true);
            plane.setAltitude(targetAltitude);
            plane.setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public static void headingCheck(C172P plane, int maxDifference, double targetHeading) {
        
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
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public static void pitchCheck(C172P plane, int maxDifference, double targetPitch) {
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
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public static void rollCheck(C172P plane, int maxDifference, double targetRoll) {
        double currentRoll = plane.getRoll();
        
        //roll is +180 to -180
        
        logger.info("Roll check. Current {} vs target {}", currentRoll, targetRoll);
        
        if( Math.abs(currentRoll) - Math.abs(targetRoll) > maxDifference) {
            logger.info("Correcting roll to target: {}", targetRoll);
            
            plane.setPause(true);
            plane.setRoll(targetRoll);
            plane.setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    //Turns out this is not mutable
//    public void yawRateCheck(int maxDifference, double targetYawRate) {
//        double currentYaw = getYawRate();
//        
//        //yaw is +180 to -180
//        
//        logger.info("Yaw rate check. Current {} vs target {}", currentYaw, targetYawRate);
//        
//        if( Math.abs(currentYaw) - Math.abs(targetYawRate) > maxDifference) {
//            logger.info("Correcting yaw to target: {}", targetYawRate);
//            
//            setPause(true);
//            setYaw(targetYawRate);
//            setPause(false);
//            
//            //trailing sleep only if we made a change
//            try {
//                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
//            } catch (InterruptedException e) {
//                logger.warn("Trailing sleep interrupted", e);
//            }
//        }
//    }
}
