package org.jason.fgcontrol.aircraft.c172p.flight;

import org.jason.fgcontrol.aircraft.flight.AircraftFlightParameters;

/**
 * Config class for C172P flight parameters with largely-proven default values.
 * 
 * @author jason
 *
 * TODO: sanity check as many parameters as possible
 */
public class C172PFlightParameters extends AircraftFlightParameters {

    private final static double WAYPOINT_ADJUST_MIN_DIST = 0.75 * 5280.0; 
    
    private final static double SIM_SPEEDUP = 1.0;
    
    private final static double TARGET_ALTITUDE = 6000.0;
    private final static double MAX_ALTITUDE_DEVIATION = 500.0;
    
    private final static double FLIGHT_MIXTURE = 0.95;
    private final static double FLIGHT_THROTTLE = 0.95;
    
    //max deviation from a target heading before triggering a course change
    private final static double MAX_HEADING_DEVIATION = 6.0;
    
    //adjust in larger increments than MAX_HEADING_DEVIATION otherwise the plane will deviate
    //farther than can be corrected
    private final static double COURSE_ADJUSTMENT_INCREMENT = 8.0;
    
    private final static double TARGET_ROLL = 0.0;
    private final static double FLIGHT_ROLL_MAX = 2.0;
    
    private final static double TARGET_PITCH = 2.0;
    private final static double FLIGHT_PITCH_MAX = 6.0;  
    
    //adjust this according to the route. packed together waypoints can result in the plane orbiting where it wants to go
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 0.5 * 5280.0;
    
    private final static double FUEL_LEVEL_REFILL_THRESHOLD_PERCENT = 0.9;
    private final static double LOW_BATTERY_AMOUNT_THRESHOLD = 0.25;
    
    private final static int BEARING_RECALC_CYCLE_INTERVAL = 5;
    
    private final static long BEARING_RECALC_CYCLE_SLEEP = 5L;
    
    private final static int STABILIZATION_CYCLE_COUNT = 5;
    private final static long STABILIZATION_CYCLE_SLEEP = 10L;

	protected double lowBatteryAmountThreshold;
    protected double mixtureFlight;
    
	/**
	 * Generate our config with default values. Invoker expected to override as needed.
	 * 
	 */
	public C172PFlightParameters() {
		super();
	}

	@Override
	protected void setDefaultValues() {
		waypointAdjustMinimumDistance = WAYPOINT_ADJUST_MIN_DIST;
		simSpeedup = SIM_SPEEDUP;
		targetAltitude = TARGET_ALTITUDE;
		maxAltitudeDeviation = MAX_ALTITUDE_DEVIATION;
		throttleFlight = FLIGHT_MIXTURE;
		throttleFlight = FLIGHT_THROTTLE;
		maxHeadingDeviation = MAX_HEADING_DEVIATION;
		courseAdjustmentIncrement = COURSE_ADJUSTMENT_INCREMENT;
		targetRoll = TARGET_ROLL;
		flightRollMax = FLIGHT_ROLL_MAX;
		targetPitch = TARGET_PITCH;
		flightPitchMax = FLIGHT_PITCH_MAX;
		waypointArrivalThreshold = WAYPOINT_ARRIVAL_THRESHOLD;
		fuelLevelRefillThresholdPercent = FUEL_LEVEL_REFILL_THRESHOLD_PERCENT;
		lowBatteryAmountThreshold = LOW_BATTERY_AMOUNT_THRESHOLD;
		bearingRecalculationCycleInterval = BEARING_RECALC_CYCLE_INTERVAL;
		bearingRecalculationCycleSleep = BEARING_RECALC_CYCLE_SLEEP;
		stabilizationCycleCount = STABILIZATION_CYCLE_COUNT;
		stabilizationCycleSleep = STABILIZATION_CYCLE_SLEEP;
	}
	
    public double getLowBatteryAmountThreshold() {
		return lowBatteryAmountThreshold;
	}

	public void setLowBatteryAmountThreshold(double lowBatteryAmountThreshold) {
		this.lowBatteryAmountThreshold = lowBatteryAmountThreshold;
	}

	public double getMixtureFlight() {
		return mixtureFlight;
	}

	public void setMixtureFlight(double mixtureFlight) {
		this.mixtureFlight = mixtureFlight;
	}

	@Override
	public String toString() {
		return "C172PFlightParameters [lowBatteryAmountThreshold=" + lowBatteryAmountThreshold + ", mixtureFlight="
				+ mixtureFlight + ", waypointAdjustMinimumDistance=" + waypointAdjustMinimumDistance + ", simSpeedup="
				+ simSpeedup + ", targetAltitude=" + targetAltitude + ", maxAltitudeDeviation=" + maxAltitudeDeviation
				+ ", throttleFlight=" + throttleFlight + ", throttleWaypointApproach=" + throttleWaypointApproach
				+ ", throttleCourseChange=" + throttleCourseChange + ", fuelLevelRefillThresholdPercent="
				+ fuelLevelRefillThresholdPercent + ", maxHeadingDeviation=" + maxHeadingDeviation
				+ ", courseAdjustmentIncrement=" + courseAdjustmentIncrement + ", targetRoll=" + targetRoll
				+ ", flightRollMax=" + flightRollMax + ", targetPitch=" + targetPitch + ", flightPitchMax="
				+ flightPitchMax + ", waypointArrivalThreshold=" + waypointArrivalThreshold
				+ ", waypointDepartureThreshold=" + waypointDepartureThreshold + ", bearingRecalculationCycleInterval="
				+ bearingRecalculationCycleInterval + ", bearingRecalculationCycleSleep="
				+ bearingRecalculationCycleSleep + ", stabilizationCycleCount=" + stabilizationCycleCount
				+ ", stabilizationCycleSleep=" + stabilizationCycleSleep + "]";
	}
	
	
}
