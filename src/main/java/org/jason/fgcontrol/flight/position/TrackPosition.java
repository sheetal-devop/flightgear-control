package org.jason.fgcontrol.flight.position;

public class TrackPosition extends LatLonPosition {

    private final static String DEFAULT_TIME = "NO_TIME";
    
    private double altitude;
    
    private String time;
    
    public final static double DEFAULT_ALTITUDE = 1000;
    
    public TrackPosition(double latitude, double longitude) {
        this(latitude, longitude, DEFAULT_ALTITUDE, DEFAULT_TIME);
    }
    
    public TrackPosition(double latitude, double longitude, String time) {
        this(latitude, longitude, DEFAULT_ALTITUDE, time);
    }
    
    public TrackPosition(double latitude, double longitude, double altitude) {
        this(latitude, longitude, altitude, DEFAULT_TIME);
    }
    
    public TrackPosition(double latitude, double longitude, double altitude, String time) {
        
        super(latitude, longitude);
        
        this.altitude = altitude;
        this.time = time;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
