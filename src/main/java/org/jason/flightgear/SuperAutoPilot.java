package org.jason.flightgear;

import java.util.ArrayList;

public class SuperAutoPilot {
    
    private ArrayList<WayPoint> waypoints;
    
    public SuperAutoPilot() {
        waypoints = new ArrayList<WayPoint>();
        waypoints.add( new WayPoint(49.198333333, -123.195833333, 500.0) );
        waypoints.add( new WayPoint(49.270833333, -123.286944444, 500.0) );
        waypoints.add( new WayPoint(49.305000000, -123.176666667, 500.0) );
        waypoints.add( new WayPoint(49.380555556, -123.017500000, 500.0) );
        waypoints.add( new WayPoint(49.290555556, -122.858055556, 500.0) );
        waypoints.add( new WayPoint(49.190555556, -123.195555556, 500.0) );
    }
    
    private class WayPoint {
        public double getLat() {
            return lat;
        }
        public void setLat(double lat) {
            this.lat = lat;
        }
        public double getLon() {
            return lon;
        }
        public void setLon(double lon) {
            this.lon = lon;
        }
        public double getAlt() {
            return alt;
        }
        public void setAlt(double alt) {
            this.alt = alt;
        }
        public WayPoint(double lat, double lon, double alt) {
            super();
            this.lat = lat;
            this.lon = lon;
            this.alt = alt;
        }
        private double lat;
        private double lon;
        private double alt;
    }
    
    public static void main(String[] args) {
        
        /*
         * type    latitude        longitude        altitude (m)    sym            name    desc
         * W    49.198333333    -123.195833333    500.0            Waypoint    Start    
         * W    49.270833333    -123.286944444    500.0            Waypoint    UBC    
         * W    49.305000000    -123.176666667    500.0            Waypoint    stan park    
         * W    49.380555556    -123.017500000    500.0            Waypoint    Lynn Headwaters    
         * W    49.290555556    -122.858055556    500.0            Waypoint    Port Moody    
         * W    49.190555556    -123.195555556    500.0            Waypoint    End    
         */
        

        
        //put a place at a place and keep it aloft by constantly resetting altitude, bearing, and other state
        //lat
        //lon
        //heading
        //altitude
        //speed
        //fuel
        //gravity accel
        
        //starting point and target point -> keep moving it toward the target
        //waypoint and sub waypoints
        
        //edge app to alert events on deviation
        //possibly prompt user to invoke edge service to reset trouble parameter
    }
}
