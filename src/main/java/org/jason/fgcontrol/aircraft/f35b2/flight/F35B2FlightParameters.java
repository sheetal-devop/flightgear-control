package org.jason.fgcontrol.aircraft.f35b2.flight;

import org.jason.fgcontrol.aircraft.f15c.F15CFields;
import org.jason.fgcontrol.aircraft.flight.AircraftFlightParameters;

/**
 * Config class for F35B2 flight parameters with largely-proven default values.
 * 
 * @author jason
 *
 * TODO: sanity check as many parameters as possible
 */
public class F35B2FlightParameters extends AircraftFlightParameters {

	//max deviation from a target heading before triggering a course change
    //private final static double MAX_HEADING_DEVIATION = 12.0;
    private final static double MAX_HEADING_DEVIATION = 3.5;
    
    //adjust in larger increments than MAX_HEADING_DEVIATION otherwise the plane will deviate
    //farther than can be corrected
    //private final static double COURSE_ADJUSTMENT_INCREMENT = 3.5;
    private final static double COURSE_ADJUSTMENT_INCREMENT = 5.5;

    private final static double SIM_SPEEDUP = 1.0;
    
    //if the sim is steering the plane by forcing positional constraints,
    //then the plane is essentially a missile at typical f15c speeds so we need a wide margin of error
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 6.0 * 5280.0;
    
    //beyond this distance, increase throttle to crusing level (MAX)
    private final static double WAYPOINT_ADJUST_MIN_DIST = 8.0 * 5280.0; 
        
    private final static double THROTTLE_FLIGHT = F15CFields.THROTTLE_MAX;
    private final static double THROTTLE_WAYPOINT_APPROACH = 0.75;
    private final static double THROTTLE_COURSE_CHANGE = 0.6;
    
    private final static double FUEL_LEVEL_REFILL_THRESHOLD_PERCENT = 0.9;
    
    private final static double TARGET_ROLL = 0.0;
    private final static double FLIGHT_ROLL_MAX = 2.0;
    
    private final static double TARGET_PITCH = 0.0;
    private final static double FLIGHT_PITCH_MAX = 2.0;    
    

    private final static double TARGET_ALTITUDE = 9000.0;
    private final static double MAX_ALTITUDE_DEVIATION = 1000.0;

    
    private final static int BEARING_RECALC_CYCLE_INTERVAL = 5;
    
    private final static long BEARING_RECALC_CYCLE_SLEEP = 5L;
    
    private final static int STABILIZATION_CYCLE_COUNT = 10;
    private final static long STABILIZATION_CYCLE_SLEEP = 10L;
    

    
    public F35B2FlightParameters() {

    }

	@Override
	protected void setDefaultValues() {
    	waypointAdjustMinimumDistance = WAYPOINT_ADJUST_MIN_DIST;
    	simSpeedup = SIM_SPEEDUP;
    	targetAltitude = TARGET_ALTITUDE;
    	maxAltitudeDeviation = MAX_ALTITUDE_DEVIATION;
    	throttleFlight = THROTTLE_FLIGHT;
    	throttleWaypointApproach = THROTTLE_WAYPOINT_APPROACH;
    	throttleCourseChange = THROTTLE_COURSE_CHANGE;
    	fuelLevelRefillThresholdPercent = FUEL_LEVEL_REFILL_THRESHOLD_PERCENT;
    	maxHeadingDeviation = MAX_HEADING_DEVIATION;
    	courseAdjustmentIncrement = COURSE_ADJUSTMENT_INCREMENT;
    	targetRoll = TARGET_ROLL;
    	flightRollMax = FLIGHT_ROLL_MAX;
    	targetPitch = TARGET_PITCH;
    	flightPitchMax = FLIGHT_PITCH_MAX;
    	waypointArrivalThreshold = WAYPOINT_ARRIVAL_THRESHOLD;
    	bearingRecalculationCycleInterval = BEARING_RECALC_CYCLE_INTERVAL;
    	bearingRecalculationCycleSleep = BEARING_RECALC_CYCLE_SLEEP;
    	stabilizationCycleCount = STABILIZATION_CYCLE_COUNT;
    	stabilizationCycleSleep = STABILIZATION_CYCLE_SLEEP;
	}
}
