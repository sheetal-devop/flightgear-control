package org.jason.flightgear.flight.position;

public abstract class LatLonPosition {

	private double latitude;
	private double longitude;
	
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
