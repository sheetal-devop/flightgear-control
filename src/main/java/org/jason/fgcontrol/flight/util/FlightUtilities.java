package org.jason.fgcontrol.flight.util;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.FlightGearAircraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightUtilities {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightUtilities.class);

    public static final int HEADING_NO_ADJUST = 0;
    public static final int HEADING_CW_ADJUST = 1;
    public static final int HEADING_CCW_ADJUST = -1;
    
    public static final double DEGREES_ZERO = 0.0;
    public static final double DEGREES_CIRCLE = 360.0;
    public static final double DEGREES_HALF_CIRCLE = 180.0;
    
    public static final double COMPARATOR_REFERENCE_DEGREES = 90.0;
    
    //TODO: maybe make these functions boolean so we can easily determine if a change was made

    //TODO: this affects altitude. don't want a subsequent altitude check teleporting the plane into a mountain
//    public static void groundElevationCheck(FlightGearPlane plane, int maxDifference, double targetAltitude) {
//        groundElevationCheck(plane, maxDifference, targetAltitude, false);
//    }


    //TODO: better implementation that considers all points in the path of the plane under some distance
    public static boolean withinGroundElevationThreshold(FlightGearAircraft plane, double maxGroundElevation) {
        double currentGroundElevation = plane.getGroundElevation();
        double currentAltitude = plane.getAltitude();
        
        LOGGER.debug("Ground elevation check. Current {} vs altitude {}", currentGroundElevation, currentAltitude);

        return currentAltitude - maxGroundElevation > currentGroundElevation;
    }
    
    public static boolean withinAltitudeThreshold(FlightGearAircraft plane, double maxDifference, double targetAltitude) {
        double currentAltitude = plane.getAltitude();
        
        LOGGER.debug("Altitude check. Current {} vs target {}", currentAltitude, targetAltitude);
        
        return ( Math.abs(plane.getAltitude() - targetAltitude ) < maxDifference);
    }
    
    public static void altitudeCheck(FlightGearAircraft plane, double maxDifference, double targetAltitude) throws IOException {
        
        //correct if too high or too low
        if( !withinAltitudeThreshold(plane, maxDifference, targetAltitude) ) {
            
            LOGGER.info("Correcting altitude to target: {}", targetAltitude);
            
            plane.setPause(true);
            plane.setAltitude(targetAltitude);
            plane.setPause(false);
        }
    }
    
    /**
     * Determine the direction of shortest adjustment for a plane's current heading to reach the target heading.
     * 
     * 
     * 
     * @param plane
     * @param targetHeading    0-360 degrees
     * 
     * @return    -1 for clockwise, 0 if none, 1 for counterclockwise
     */
    public static int headingCompareTo(FlightGearAircraft plane, double targetHeading) {        
        return headingCompareTo( plane.getHeading(), targetHeading);
    }
    
    /**
     * Determine the direction of shortest adjustment for a given heading to reach the target heading.
     * 
     * 
     * 
     * @param currentHeading
     * @param targetHeading    0-360 degrees
     * 
     * @return    -1 for clockwise, 0 if none, 1 for counterclockwise
     */
    public static int headingCompareTo(double currentHeading, double targetHeading) {
        
        int retval = HEADING_NO_ADJUST;
        
        if(currentHeading < DEGREES_ZERO) currentHeading += DEGREES_CIRCLE;    
        if(currentHeading >= DEGREES_CIRCLE) currentHeading %= DEGREES_CIRCLE;
        if(targetHeading < DEGREES_ZERO) targetHeading += DEGREES_CIRCLE;    
        if(targetHeading >= DEGREES_CIRCLE) targetHeading %= DEGREES_CIRCLE;

        //if we're already on our way in the right direction
        if(currentHeading == targetHeading) {
            retval =  HEADING_NO_ADJUST;
        } 
        else if( withinHeadingThreshold( (currentHeading + COMPARATOR_REFERENCE_DEGREES) % DEGREES_CIRCLE, COMPARATOR_REFERENCE_DEGREES, targetHeading) ) {
            //is the targetHeading within the semicircle to the right of the current heading?
            retval = HEADING_CW_ADJUST;
        } 
        else {
            retval = HEADING_CCW_ADJUST;
        }
        
        LOGGER.debug("Comparing headings {} : {} => {}", currentHeading, targetHeading, retval);
        
        return retval;
    }
    
    public static double determineCourseChangeAdjustment(double currentHeading, double courseAdjustmentIncrement, double targetHeading) {
    	
    	double intermediateHeading = currentHeading;
    	
    	if(withinHeadingThreshold(currentHeading, courseAdjustmentIncrement, targetHeading)) {
    		//within threshold -> stick with current heading
    	} else {
        	int headingComparisonResult = headingCompareTo(currentHeading, targetHeading);
        	
            //adjust clockwise or counter? 
            //this may actually change in the middle of the transition itself
            
            if(headingComparisonResult == FlightUtilities.HEADING_NO_ADJUST) {
                LOGGER.warn("Found no adjustment needed");
                
                //nothing, default intermediateHeading is currentHeading as assigned above
            } else if(headingComparisonResult == FlightUtilities.HEADING_CW_ADJUST) {
                //1: adjust clockwise
                intermediateHeading = (intermediateHeading + courseAdjustmentIncrement ) % FlightUtilities.DEGREES_CIRCLE;
            } else {
                //-1: adjust counterclockwise
                intermediateHeading -= courseAdjustmentIncrement;
                
                //normalize 0-360
                if(intermediateHeading < 0) intermediateHeading += FlightUtilities.DEGREES_CIRCLE;
            }
    	}
        
        return intermediateHeading;
    }
    
    public static boolean withinHeadingThreshold(FlightGearAircraft plane, double maxDifference, double targetHeading) {
        return withinHeadingThreshold(plane.getHeading(), maxDifference, targetHeading);
    }    
    
    public static boolean withinHeadingThreshold(double currentHeading, double maxDifference, double targetHeading) {
        
        /*
         * heading is 0 to 360, both values are true/mag north
         * 
         * target 0, maxDif 5, min 355, max 5
         * 
         * target 90, maxDif 10, min 80, max 100
         * 
         * target 355, maxDif 10, min 345, max 5
         */
        if(maxDifference >= DEGREES_HALF_CIRCLE) {
            LOGGER.warn("withinHeadingThreshold called with an impractically large threshold");
            return true;
        }

        double theta1 = Math.toRadians(currentHeading);
        double theta2 = Math.toRadians(targetHeading);
        double thetaMaxDiff = Math.toRadians(maxDifference);
        
        double cosDeltaTheta = Math.cos( theta2 - theta1);
        double cosMaxDiff = Math.cos(thetaMaxDiff);
        
        return cosDeltaTheta > cosMaxDiff;
    }
    
    public static void headingCheck(FlightGearAircraft plane, double maxDifference, double targetHeading) throws IOException {
        
        if( !withinHeadingThreshold( plane, maxDifference, targetHeading) ) {
            
            LOGGER.info("Correcting heading to target: {}: ({} degrees)", targetHeading);
            
            plane.setPause(true);
            plane.setHeading(targetHeading);
            plane.setPause(false);
        }
    }
    
    public static boolean withinPitchThreshold(FlightGearAircraft plane, double maxDifference, double targetPitch) {

        double currentPitch = plane.getPitch();
        
        //pitch is -180 to 180
        
        LOGGER.debug("Pitch check. Current {} vs target {}", currentPitch, targetPitch);
        
        return Math.abs(currentPitch - targetPitch) < maxDifference;
    }
    
    public static void pitchCheck(FlightGearAircraft plane, double maxDifference, double targetPitch) throws IOException {
        //read pitch
        //if pitch is too far from target in +/- directions, set to target
        
        if( !withinPitchThreshold(plane, maxDifference, targetPitch)) {
            
            LOGGER.info("Correcting pitch to target: {}", targetPitch);

            plane.setPause(true);
            plane.setPitch(targetPitch);
            plane.setPause(false);
        }
    }
    
    public static boolean withinRollThreshold(FlightGearAircraft plane, double maxDifference, double targetRoll) {
        double currentRoll = plane.getRoll();
        
        //roll is +180 to -180
        
        LOGGER.debug("Roll check. Current {} vs target {}", currentRoll, targetRoll);
        
        return Math.abs(currentRoll - targetRoll) < maxDifference;
    }
    
    public static void rollCheck(FlightGearAircraft plane, double maxDifference, double targetRoll) throws IOException {

        if( !withinRollThreshold(plane, maxDifference, targetRoll) ) {
            LOGGER.info("Correcting roll to target: {}", targetRoll);
            
            plane.setPause(true);
            plane.setRoll(targetRoll);
            plane.setPause(false);  
        }
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
    public static void airSpeedCheck(FlightGearAircraft plane, double maxDifference, double targetAirspeed) throws IOException {
        double currentAirSpeed = plane.getAirSpeed();
        
        //set while not paused. this functions more like a boost- 
        //the plane can be acceled or deceled to the specified speed, 
        //but then the fdm takes over and stabilizes the air speed
        
        LOGGER.debug("Airspeed check. Current {} vs target {}", currentAirSpeed, targetAirspeed);
        
        if( Math.abs(currentAirSpeed - targetAirspeed) > maxDifference) {
            LOGGER.info("Correcting airspeed to target: {}", targetAirspeed);
            
            plane.setPause(true);
            plane.setAirSpeed(targetAirspeed);
            plane.setPause(false);
        }
    }
}
