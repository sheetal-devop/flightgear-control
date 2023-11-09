package org.jason.fgcontrol.flight.position;

import java.util.Objects;

/**
 * Wrapper for a plane position in flight. 
 *
 */
public class WaypointPosition extends LatLonPosition {
    
    private final static String DEFAULT_NAME = "NO_NAME";
    
    private double altitude;
    private String name;
    
    public final static double DEFAULT_ALTITUDE = 1000;
    
    public WaypointPosition(double latitude, double longitude) {
        this(latitude, longitude, DEFAULT_ALTITUDE, DEFAULT_NAME);
    }
    
    public WaypointPosition(double latitude, double longitude, String name) {
        this(latitude, longitude, DEFAULT_ALTITUDE, name);
    }
    
    public WaypointPosition(double latitude, double longitude, double altitude) {
        this(latitude, longitude, altitude, DEFAULT_NAME);
    }
    
    public WaypointPosition(double latitude, double longitude, double altitude, String name) {
        
        super(latitude, longitude);
        this.altitude = altitude;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return "WaypointPosition [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + ", name="
                + name + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(altitude, name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		WaypointPosition other = (WaypointPosition) obj;
		return Double.doubleToLongBits(altitude) == Double.doubleToLongBits(other.altitude)
				&& Objects.equals(name, other.name) && 
				getLatitude() == other.getLatitude() &&
				getLongitude() == other.getLongitude();
	}


}
