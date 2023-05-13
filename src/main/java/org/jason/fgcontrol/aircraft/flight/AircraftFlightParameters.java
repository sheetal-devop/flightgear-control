package org.jason.fgcontrol.aircraft.flight;

public abstract class AircraftFlightParameters {
	
    //TODO: document each
    //needs to be tuned depending on aircraft speed, sim speedup, and waypoint closeness
	protected double waypointAdjustMinimumDistance;
	protected double simSpeedup;
	protected double targetAltitude;
	protected double maxAltitudeDeviation;
	protected double throttleFlight;
	protected double throttleWaypointApproach;
	protected double throttleCourseChange;
	protected double fuelLevelRefillThresholdPercent;
	protected double maxHeadingDeviation;
	protected double courseAdjustmentIncrement;
	protected double targetRoll;
	protected double flightRollMax;
	protected double targetPitch;
	protected double flightPitchMax;
	protected double waypointArrivalThreshold;
	protected double waypointDepartureThreshold;
	protected int bearingRecalculationCycleInterval;
	protected long bearingRecalculationCycleSleep;
	protected int stabilizationCycleCount;
	protected long stabilizationCycleSleep;
	
	protected AircraftFlightParameters() {
		setDefaultValues();
	}
	
	abstract protected void setDefaultValues(); 
	
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

	public double getMaxHeadingDeviation() {
		return maxHeadingDeviation;
	}

	public void setMaxHeadingDeviation(double maxHeadingDeviation) {
		this.maxHeadingDeviation = maxHeadingDeviation;
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
	
	public double getWaypointDepartureThreshold() {
		return waypointDepartureThreshold;
	}

	public void setWaypointDepartureThreshold(double waypointDepartureThreshold) {
		this.waypointDepartureThreshold = waypointDepartureThreshold;
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
		return "AircraftFlightParameters [waypointAdjustMinimumDistance=" + waypointAdjustMinimumDistance + ", simSpeedup="
				+ simSpeedup + ", targetAltitude=" + targetAltitude + ", maxAltitudeDeviation=" + maxAltitudeDeviation
				+ ", throttleFlight=" + throttleFlight + ", throttleWaypointApproach=" + throttleWaypointApproach
				+ ", throttleCourseChange=" + throttleCourseChange + ", fuelLevelRefillThresholdPercent="
				+ fuelLevelRefillThresholdPercent + ", maxHeadingDeviation=" + maxHeadingDeviation
				+ ", courseAdjustmentIncrement=" + courseAdjustmentIncrement + ", targetRoll=" + targetRoll
				+ ", flightRollMax=" + flightRollMax + ", targetPitch=" + targetPitch + ", flightPitchMax="
				+ flightPitchMax + ", waypointArrivalThreshold=" + waypointArrivalThreshold
				+ ", bearingRecalculationCycleInterval=" + bearingRecalculationCycleInterval
				+ ", bearingRecalculationCycleSleep=" + bearingRecalculationCycleSleep + ", stabilizationCycleCount="
				+ stabilizationCycleCount + ", stabilizationCycleSleep=" + stabilizationCycleSleep + "]";
	}
}
