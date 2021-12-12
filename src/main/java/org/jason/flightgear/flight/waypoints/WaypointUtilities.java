package org.jason.flightgear.flight.waypoints;

import java.awt.geom.Point2D;

import org.geotools.referencing.GeodeticCalculator;
import org.jason.flightgear.flight.WaypointPosition;

public abstract class WaypointUtilities {

	//radius of the earth in km
	private final static double EARTH_RADIUS = 6371.0088;
	
	//have we arrived at a waypoint? threshold in feet.
	//might not want this too precise because a narrow miss might result in the plane orbiting endlessly
	public final static double ARRIVAL_THRESHOLD = 2640d;
			
	private WaypointUtilities() {}
	
	public static boolean hasArrivedAtWaypoint(WaypointPosition current, WaypointPosition target) {
		return distanceBetweenPositions(current, target) < ARRIVAL_THRESHOLD;
	}
	
	/**
	 * Orthodromic distance between two points, as the earth is not a perfect sphere.
	 * Does not consider altitude.
	 * 	 
	 * @param current
	 * @param target
	 * 
	 * @return	Distance in feet
	 */
	public static double distanceBetweenPositions(WaypointPosition current, WaypointPosition target) {
		
		GeodeticCalculator calc = new GeodeticCalculator();
		
		Point2D startPoint = new Point2D.Double(current.getLongitude(), current.getLatitude());
		Point2D targetPoint = new Point2D.Double(target.getLongitude(), target.getLatitude());
		
		calc.setStartingGeographicPoint(startPoint );
		calc.setDestinationGeographicPoint( targetPoint );
		
		//meters convert to feet
		return calc.getOrthodromicDistance() * 3.28084;
	}
	
	/**
	 * Heading from current position to target position.
	 * Does not consider altitude.
	 * 
	 * @param current
	 * @param target
	 * 
	 * @return	Heading in degrees -180 to 180. 0 degrees is due North.
	 */
	public static double calcBearingToGPSCoordinates(WaypointPosition current, WaypointPosition target) {
		GeodeticCalculator calc = new GeodeticCalculator();
		
		Point2D startPoint = new Point2D.Double(current.getLongitude(), current.getLatitude());
		Point2D targetPoint = new Point2D.Double(target.getLongitude(), target.getLatitude());
		
		calc.setStartingGeographicPoint(startPoint );
		calc.setDestinationGeographicPoint( targetPoint );
		
		//getAzimuth for bearing - the angle in degrees (clockwise) between North and the direction to the destination.
		//
		//This formula is for the initial bearing (sometimes referred to as forward azimuth) which if followed in a 
		//straight line along a great-circle arc will take you from the start point to the end point:1
		return calc.getAzimuth();
	}
	
	public static double distanceBetween(WaypointPosition current, WaypointPosition target) {
		//convert result from km to feet
		return haversine(current, target) * 3280.84;
	}
	
	public static boolean hasArrivedAtWaypoint(WaypointPosition current, WaypointPosition target, double distance) {
		return distanceBetween(current, target) < distance;
	}
	
	/**
	 * Definitely stolen from the internet. hopefully there's a commons implementation.
	 * 
	 * @param current
	 * @param target
	 * @return
	 */
	public static double getHeadingToTarget(WaypointPosition current, WaypointPosition target) {
		double deltaLongitude = target.getLongitude() - current.getLongitude(); 
		
		//X = cos(b.lat)* sin(dL)
		double xVal = Math.cos(target.getLatitude()) * deltaLongitude;
		
		//Y = cos(a.lat)*sin(b.lat) - sin(a.lat)*cos(b.lat)* cos(dL)
		double yVal = (Math.cos(current.getLatitude()) * Math.sin(target.getLatitude())) -
				(Math.sin(current.getLatitude()) * target.getLatitude()) * 
				Math.cos(deltaLongitude);
		
		return Math.toDegrees( Math.atan2(xVal, yVal) );
	}

	private static double haversine(WaypointPosition p1, WaypointPosition p2) {
		return haversine(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
	}
	
	/**
	 * Definitely stolen from the internet. hopefully there's a commons implementation.
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	private static double haversine(double lat1, double lon1, double lat2, double lon2) {
		// distance between latitudes and longitudes
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		// convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		
		double c = 2 * Math.asin(Math.sqrt(a));
		return EARTH_RADIUS * c;
	}
}
