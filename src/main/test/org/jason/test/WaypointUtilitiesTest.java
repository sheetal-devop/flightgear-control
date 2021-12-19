package org.jason.test;

import java.math.BigDecimal;

import org.jason.flightgear.flight.waypoints.KnownPositions;
import org.jason.flightgear.flight.waypoints.WaypointPosition;
import org.jason.flightgear.flight.waypoints.WaypointUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WaypointUtilitiesTest {

	@Test (enabled = false)
	public void testDistanceBetween() {

		WaypointPosition p1 = KnownPositions.LONSDALE_QUAY;
		WaypointPosition p2 = KnownPositions.RIT;
		
		//2185.377 miles converted to feet
		double expected = 2185.377 * 5280;
		
		double actual = WaypointUtilities.distanceBetween(p1, p2);
		
		System.out.println("testDistanceBetween");
		System.out.println("Actual Distance:\t\t" + new BigDecimal(actual).toPlainString() );
		System.out.println("Expected Distance:\t\t" + new BigDecimal(expected).toPlainString() );

		
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testDistanceBetweenGPSCoordinates() {
		
		double errorMargin = 100d;
		
		WaypointPosition p1 = KnownPositions.LONSDALE_QUAY;
		WaypointPosition p2 = KnownPositions.RIT;
		
		//2185.267 miles converted to feet
		double expected = 2185.267 * 5280;
		
		double actual = WaypointUtilities.distanceBetweenPositions(p1, p2);
		
		System.out.println("testDistanceBetweenGPS");
		System.out.println("Actual Distance:\t\t" + new BigDecimal(actual).toPlainString() );
		System.out.println("Expected Distance:\t\t" + new BigDecimal(expected).toPlainString() );

		double difference = Math.abs( actual - expected);
		
		Assert.assertTrue( difference < errorMargin, 
				"Difference is less than errorMargin " + errorMargin +": " + new BigDecimal(difference).toPlainString() ) ;
	}
	
	@Test
	public void testBearingToGPSCoordinates() {
		
		double errorMargin = 0.05d;
		
		//lonsdale quay
		WaypointPosition p1 = new WaypointPosition(49.31004, -123.08439);
		
		//RIT
		WaypointPosition p2 = new WaypointPosition(43.08389, -77.67956);
		
		//degrees
		double expected = 83.797;
		
		double actual = WaypointUtilities.calcBearingToGPSCoordinates(p1, p2);
		
		System.out.println("testheadingToGPS");
		System.out.println("Actual bearing:\t\t" + new BigDecimal(actual).toPlainString() );
		System.out.println("Expected bearing:\t\t" + new BigDecimal(expected).toPlainString() );

		double difference = Math.abs( actual - expected);
		
		Assert.assertTrue( difference < errorMargin, "Difference is less than errorMargin: " + errorMargin ) ;
	}
}
