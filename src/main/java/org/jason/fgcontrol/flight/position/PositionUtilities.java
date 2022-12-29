package org.jason.fgcontrol.flight.position;

import java.awt.geom.Point2D;

import org.geotools.referencing.GeodeticCalculator;
import org.jason.fgcontrol.flight.util.FlightUtilities;

public abstract class PositionUtilities {

    private final static double METERS_TO_FEET_CONVERSION = 3.28084;
    
    //have we arrived at a waypoint? threshold in feet.
    public final static double WAYPOINT_ARRIVAL_THRESHOLD = 5.0 * 5280.0;
            
    private PositionUtilities() {}
    
    public static boolean hasArrivedAtWaypoint(LatLonPosition current, LatLonPosition target) {
        return hasArrivedAtWaypoint(current, target, WAYPOINT_ARRIVAL_THRESHOLD);
    }
    
    public static boolean hasArrivedAtWaypoint(LatLonPosition current, LatLonPosition target, double arrivalThreshold) {
        return distanceBetweenPositions(current, target) < arrivalThreshold;
    }
    
    /**
     * Orthodromic distance between two points, as the earth is not a perfect sphere.
     * Does not consider altitude.
     *      
     * @param current
     * @param target
     * 
     * @return    Distance in feet
     */
    public static double distanceBetweenPositions(LatLonPosition current, LatLonPosition target) {
        
        GeodeticCalculator calc = new GeodeticCalculator();
        
        Point2D startPoint = new Point2D.Double(current.getLongitude(), current.getLatitude());
        Point2D targetPoint = new Point2D.Double(target.getLongitude(), target.getLatitude());
        
        calc.setStartingGeographicPoint(startPoint );
        calc.setDestinationGeographicPoint(targetPoint);
        
        //meters convert to feet
        return calc.getOrthodromicDistance() * METERS_TO_FEET_CONVERSION;
    }
    
    public static double calcBearingToGPSCoordinates(LatLonPosition current, LatLonPosition target) {
        return calcBearingToGPSCoordinates(current.getLatitude(), current.getLongitude(), target.getLatitude(), target.getLongitude());
    }

	/**
     * Heading from current position to target position.
     * Does not consider altitude.
     * 
     * @param startLat
     * @param startLon
     * @param targetLat
     * @param targetLon
     * 
     * @return    Heading in degrees -180 to 180. 0 degrees is due North.
     */
    public static double calcBearingToGPSCoordinates(double startLat, double startLon, double targetLat, double targetLon) {
        GeodeticCalculator calc = new GeodeticCalculator();
        
        Point2D startPoint = new Point2D.Double(startLon, startLat);
        Point2D targetPoint = new Point2D.Double(targetLon, targetLat);
        
        calc.setStartingGeographicPoint(startPoint);
        calc.setDestinationGeographicPoint(targetPoint);
        
        //getAzimuth for bearing - the angle in degrees (clockwise) between North and the direction to the destination.
        //
        //This formula is for the initial bearing (sometimes referred to as forward azimuth) which if followed in a 
        //straight line along a great-circle arc will take you from the start point to the end point
        return calc.getAzimuth();
    }
    
    public static double calcBearingToGPSCoordinatesNormalized(LatLonPosition current, LatLonPosition target) {
        return calcBearingToGPSCoordinatesNormalized(current.getLatitude(), current.getLongitude(), target.getLatitude(), target.getLongitude());
    }
    
    private static double calcBearingToGPSCoordinatesNormalized(double startLat, double startLon, double targetLat, double targetLon) {
		double bearing = calcBearingToGPSCoordinates(startLat, startLon, targetLat, targetLon);
		
        if(bearing < FlightUtilities.DEGREES_ZERO) {
        	bearing += FlightUtilities.DEGREES_CIRCLE;
        }
		
		return bearing;
	}
}
