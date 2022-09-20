package org.jason.fgcontrol.flight.position;

public abstract class LatLonPosition {

	protected double latitude;
	protected double longitude;
	
	public LatLonPosition(double latitude, double longitude) {
		//TODO: enforce 5 decimal precision
		
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}
