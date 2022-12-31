package org.jason.fgcontrol.aircraft.c172p.flight;

/**
 * Config class for C172P flight parameters with largely-proven default values.
 * 
 * @author jason
 *
 * TODO: sanity check as many parameters as possible
 */
public class C172PFlightParameters {

    private final static double WAYPOINT_ADJUST_MIN_DIST = 0.75 * 5280.0; 
    
    private final static double SIM_SPEEDUP = 8.0;
    
    private final static double TARGET_ALTITUDE = 6000.0;
    private final static double MAX_ALTITUDE_DEVIATION = 500.0;
    
    private final static double FLIGHT_MIXTURE = 0.95;
    private final static double FLIGHT_THROTTLE = 0.95;
    
    private final static double MAX_HEADING_CHANGE = 20.0;
    
    //adjust in smaller increments than MAX_HEADING_CHANGE, since course changes can be radical
    //TODO: lower this value after waypoint interpolation is implemented
    private final static double COURSE_ADJUSTMENT_INCREMENT = 12.5;
    
    private final static double TARGET_ROLL = 0.0;
    private final static double FLIGHT_ROLL_MAX = 2.0;
    
    private final static double TARGET_PITCH = 2.0;
    private final static double FLIGHT_PITCH_MAX = 4.0;  
    
    //adjust this according to the route. packed together waypoints can result in the plane orbiting where it wants to go
    private final static double WAYPOINT_ARRIVAL_THRESHOLD = 0.5 * 5280.0;
    
    private final static double LOW_FUEL_AMOUNT_THRESHOLD = 16.0;
    private final static double LOW_BATTERY_AMOUNT_THRESHOLD = 0.25;
    
    private final static int BEARING_RECALC_CYCLE_INTERVAL = 5;
    
    private final static long BEARING_RECALC_CYCLE_SLEEP = 5L;
    
    private final static int STABILIZATION_CYCLE_COUNT = 5;
    private final static long STABILIZATION_CYCLE_SLEEP = 10L;

    //TODO: document each
    //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
	private double waypointAdjustMinimumDistance;
    private double simSpeedup;
    private double targetAltitude;
    private double maxAltitudeDeviation;
    private double flightMixture;
    private double flightThrottle;
    private double maxHeadingChange;
    private double courseAdjustmentIncrement;
    private double targetRoll;
    private double flightRollMax;
    private double targetPitch;
    private double flightPitchMax;
    private double waypointArrivalThreshold;
   
    //evenly distributed fuel tank levels on the airframe generally assists stable flight
    private double lowFuelAmountThreshold;
    private double lowBatteryAmountThreshold;
    
    private int bearingRecalculationCycleInterval;
    private long bearingRecalculationCycleSleep;
    
    private int stabilizationCycleCount;
    private long stabilizationCycleSleep;
    
	/**
	 * Generate our config with default values. Invoker expected to override as needed.
	 * 
	 */
	public C172PFlightParameters() {
		waypointAdjustMinimumDistance = WAYPOINT_ADJUST_MIN_DIST;
		simSpeedup = SIM_SPEEDUP;
		targetAltitude = TARGET_ALTITUDE;
		maxAltitudeDeviation = MAX_ALTITUDE_DEVIATION;
		flightMixture = FLIGHT_MIXTURE;
		flightThrottle = FLIGHT_THROTTLE;
		maxHeadingChange = MAX_HEADING_CHANGE;
		courseAdjustmentIncrement = COURSE_ADJUSTMENT_INCREMENT;
		targetRoll = TARGET_ROLL;
		flightRollMax = FLIGHT_ROLL_MAX;
		targetPitch = TARGET_PITCH;
		flightPitchMax = FLIGHT_PITCH_MAX;
		waypointArrivalThreshold = WAYPOINT_ARRIVAL_THRESHOLD;
		lowFuelAmountThreshold = LOW_FUEL_AMOUNT_THRESHOLD;
		lowBatteryAmountThreshold = LOW_BATTERY_AMOUNT_THRESHOLD;
		bearingRecalculationCycleInterval = BEARING_RECALC_CYCLE_INTERVAL;
		bearingRecalculationCycleSleep = BEARING_RECALC_CYCLE_SLEEP;
		stabilizationCycleCount = STABILIZATION_CYCLE_COUNT;
		stabilizationCycleSleep = STABILIZATION_CYCLE_SLEEP;
	}

	public double getLowFuelAmountThreshold() {
		return lowFuelAmountThreshold;
	}

	public void setLowFuelAmountThreshold(double lowFuelAmountThreshold) {
		this.lowFuelAmountThreshold = lowFuelAmountThreshold;
	}

	public double getLowBatteryAmountThreshold() {
		return lowBatteryAmountThreshold;
	}

	public void setLowBatteryAmountThreshold(double lowBatteryAmountThreshold) {
		this.lowBatteryAmountThreshold = lowBatteryAmountThreshold;
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

	public double getFlightMixture() {
		return flightMixture;
	}

	public void setFlightMixture(double flightMixture) {
		this.flightMixture = flightMixture;
	}

	public double getFlightThrottle() {
		return flightThrottle;
	}

	public void setFlightThrottle(double flightThrottle) {
		this.flightThrottle = flightThrottle;
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
		return "C172PFlightParameters [waypointAdjustMinimumDistance=" + waypointAdjustMinimumDistance + ", simSpeedup="
				+ simSpeedup + ", targetAltitude=" + targetAltitude + ", maxAltitudeDeviation=" + maxAltitudeDeviation
				+ ", flightMixture=" + flightMixture + ", flightThrottle=" + flightThrottle + ", maxHeadingChange="
				+ maxHeadingChange + ", courseAdjustmentIncrement=" + courseAdjustmentIncrement + ", targetRoll="
				+ targetRoll + ", flightRollMax=" + flightRollMax + ", targetPitch=" + targetPitch + ", flightPitchMax="
				+ flightPitchMax + ", waypointArrivalThreshold=" + waypointArrivalThreshold
				+ ", lowFuelAmountThreshold=" + lowFuelAmountThreshold + ", lowBatteryAmountThreshold="
				+ lowBatteryAmountThreshold + ", bearingRecalculationCycleInterval=" + bearingRecalculationCycleInterval
				+ ", bearingRecalculationCycleSleep=" + bearingRecalculationCycleSleep + ", stabilizationCycleCount="
				+ stabilizationCycleCount + ", stabilizationCycleSleep=" + stabilizationCycleSleep + "]";
	}
}
