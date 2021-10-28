package org.jason.flightgear.planes.c172p.app;

import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.c172p.C172P;

public class C172P_Flight {
	public static void main(String[] args) {
		
		//TODO: possibly fork execution of shell script and wait for telnet port to open
		//use shell script because we may want to run it from elsewhere
		//maybe do this in C172 class itself
		
		
		C172P plane = null;
		
		try {
			plane = new C172P();
			
			//plane.startupPlane();
			
			//plane.flyPlane();
			
			Thread.sleep(10 * 1000);
			

		} catch (FlightGearSetupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (plane != null) {
				plane.shutdown();
			}
		}
	}
}
