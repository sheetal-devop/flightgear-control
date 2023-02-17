package org.jason.fgcontrol.aircraft.f35b2.flight;

import org.jason.fgcontrol.aircraft.f15c.F15CFields;

/**
 * Config class for F35B2 flight parameters with largely-proven default values.
 * 
 * @author jason
 *
 * TODO: sanity check as many parameters as possible
 */
public class F35B2FlightParameters {

    //private final static double MAX_HEADING_CHANGE = 12.0;
    private final static double MAX_HEADING_CHANGE = 3.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    //private final static double COURSE_ADJUSTMENT_INCREMENT = 3.5;
    private final static double COURSE_ADJUSTMENT_INCREMENT = 2.5;

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
    
    //TODO: document each
    //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
	private double waypointAdjustMinimumDistance;
    private double simSpeedup;
    private double targetAltitude;
    private double maxAltitudeDeviation;
    private double throttleFlight;
    private double throttleWaypointApproach;
    private double throttleCourseChange;
    private double fuelLevelRefillThresholdPercent;
    private double maxHeadingChange;
    private double courseAdjustmentIncrement;
    private double targetRoll;
    private double flightRollMax;
    private double targetPitch;
    private double flightPitchMax;
    private double waypointArrivalThreshold;
    private int bearingRecalculationCycleInterval;
    private long bearingRecalculationCycleSleep;
    private int stabilizationCycleCount;
    private long stabilizationCycleSleep;
    
    public F35B2FlightParameters() {
    	waypointAdjustMinimumDistance = WAYPOINT_ADJUST_MIN_DIST;
    	simSpeedup = SIM_SPEEDUP;
    	targetAltitude = TARGET_ALTITUDE;
    	maxAltitudeDeviation = MAX_ALTITUDE_DEVIATION;
    	throttleFlight = THROTTLE_FLIGHT;
    	throttleWaypointApproach = THROTTLE_WAYPOINT_APPROACH;
    	throttleCourseChange = THROTTLE_COURSE_CHANGE;
    	fuelLevelRefillThresholdPercent = FUEL_LEVEL_REFILL_THRESHOLD_PERCENT;
    	maxHeadingChange = MAX_HEADING_CHANGE;
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

	public double getWaypointAdjustMinimumDistance() {
		return waypointAdjustMinimumDistance;
	}

	public void setWaypointAdjustMinimumDistance(double waypointAdjustMinimumDistance) {
		this.waypointAdjustMinimumDistance = waypointAdjustMinimumDistance;
	}

	public double getSimSpeedup() {
		return simSpeedup;
	}

	public void setSimSpeedup(double simSpeedup) {
		this.simSpeedup = simSpeedup;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public void setTargetAltitude(double targetAltitude) {
		this.targetAltitude = targetAltitude;
	}

	public double getMaxAltitudeDeviation() {
		return maxAltitudeDeviation;
	}

	public void setMaxAltitudeDeviation(double maxAltitudeDeviation) {
		this.maxAltitudeDeviation = maxAltitudeDeviation;
	}

	public double getThrottleFlight() {
		return throttleFlight;
	}

	public void setThrottleFlight(double throttleFlight) {
		this.throttleFlight = throttleFlight;
	}

	public double getThrottleWaypointApproach() {
		return throttleWaypointApproach;
	}

	public void setThrottleWaypointApproach(double throttleWaypointApproach) {
		this.throttleWaypointApproach = throttleWaypointApproach;
	}

	public double getThrottleCourseChange() {
		return throttleCourseChange;
	}

	public void setThrottleCourseChange(double throttleCourseChange) {
		this.throttleCourseChange = throttleCourseChange;
	}

	public double getMaxHeadingChange() {
		return maxHeadingChange;
	}

	public void setMaxHeadingChange(double maxHeadingChange) {
		this.maxHeadingChange = maxHeadingChange;
	}

	public double getCourseAdjustmentIncrement() {
		return courseAdjustmentIncrement;
	}

	public void setCourseAdjustmentIncrement(double courseAdjustmentIncrement) {
		this.courseAdjustmentIncrement = courseAdjustmentIncrement;
	}

	public double getTargetRoll() {
		return targetRoll;
	}

	public void setTargetRoll(double targetRoll) {
		this.targetRoll = targetRoll;
	}

	public double getFlightRollMax() {
		return flightRollMax;
	}

	public void setFlightRollMax(double flightRollMax) {
		this.flightRollMax = flightRollMax;
	}

	public double getTargetPitch() {
		return targetPitch;
	}

	public void setTargetPitch(double targetPitch) {
		this.targetPitch = targetPitch;
	}

	public double getFlightPitchMax() {
		return flightPitchMax;
	}

	public void setFlightPitchMax(double flightPitchMax) {
		this.flightPitchMax = flightPitchMax;
	}

	public double getWaypointArrivalThreshold() {
		return waypointArrivalThreshold;
	}

	public void setWaypointArrivalThreshold(double waypointArrivalThreshold) {
		this.waypointArrivalThreshold = waypointArrivalThreshold;
	}

	public int getBearingRecalculationCycleInterval() {
		return bearingRecalculationCycleInterval;
	}

	public void setBearingRecalculationCycleInterval(int bearingRecalculationCycleInterval) {
		this.bearingRecalculationCycleInterval = bearingRecalculationCycleInterval;
	}

	public long getBearingRecalculationCycleSleep() {
		return bearingRecalculationCycleSleep;
	}

	public void setBearingRecalculationCycleSleep(long bearingRecalculationCycleSleep) {
		this.bearingRecalculationCycleSleep = bearingRecalculationCycleSleep;
	}

	public double getFuelLevelRefillThresholdPercent() {
		return fuelLevelRefillThresholdPercent;
	}

	public void setFuelLevelRefillThresholdPercent(double fuelLevelRefillThresholdPercent) {
		this.fuelLevelRefillThresholdPercent = fuelLevelRefillThresholdPercent;
	}

	public int getStabilizationCycleCount() {
		return stabilizationCycleCount;
	}

	public void setStabilizationCycleCount(int stabilizationCycleCount) {
		this.stabilizationCycleCount = stabilizationCycleCount;
	}

	public long getStabilizationCycleSleep() {
		return stabilizationCycleSleep;
	}

	public void setStabilizationCycleSleep(long stabilizationCycleSleep) {
		this.stabilizationCycleSleep = stabilizationCycleSleep;
	}

	@Override
	public String toString() {
		return "F35B2FlightParameters [waypointAdjustMinimumDistance=" + waypointAdjustMinimumDistance + ", simSpeedup="
				+ simSpeedup + ", targetAltitude=" + targetAltitude + ", maxAltitudeDeviation=" + maxAltitudeDeviation
				+ ", throttleFlight=" + throttleFlight + ", throttleWaypointApproach=" + throttleWaypointApproach
				+ ", throttleCourseChange=" + throttleCourseChange + ", fuelLevelRefillThresholdPercent="
				+ fuelLevelRefillThresholdPercent + ", maxHeadingChange=" + maxHeadingChange
				+ ", courseAdjustmentIncrement=" + courseAdjustmentIncrement + ", targetRoll=" + targetRoll
				+ ", flightRollMax=" + flightRollMax + ", targetPitch=" + targetPitch + ", flightPitchMax="
				+ flightPitchMax + ", waypointArrivalThreshold=" + waypointArrivalThreshold
				+ ", bearingRecalculationCycleInterval=" + bearingRecalculationCycleInterval
				+ ", bearingRecalculationCycleSleep=" + bearingRecalculationCycleSleep + ", stabilizationCycleCount="
				+ stabilizationCycleCount + ", stabilizationCycleSleep=" + stabilizationCycleSleep + "]";
	}
}
