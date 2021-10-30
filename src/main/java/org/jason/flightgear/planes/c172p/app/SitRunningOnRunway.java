package org.jason.flightgear.planes.c172p.app;

import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitRunningOnRunway {
		
	private final static int POST_STARTUP_SLEEP = 3000;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SitRunningOnRunway.class);
	
	private static String telemetryReadOut(C172P plane) {
		StringBuilder telemetryRead = new StringBuilder();
		
		//engine should be running and consuming consumables
		telemetryRead.append("\n=======")
			.append("\nFuel level: ")
			.append(plane.getFuelLevel())
			.append("\nTime Elapsed: ")
			.append(plane.getTimeElapsed())
			.append("\nTime Local: ")
			.append(plane.getLocalDaySeconds())
			.append("\nFuel flow: ")
			.append(plane.getFuelFlow())
			.append("\nOil pressure: ")
			.append(plane.getOilPressure())
			.append("\nOil temperature: ")
			.append(plane.getOilTemperature())
			.append("\nMpOsi: ")
			.append(plane.getMpOsi())
			.append("\nThrottle: ")
			.append(plane.getThrottle())
			.append("\nMixture: ")
			.append(plane.getMixture())
			.append("\nEngine running: ")
			.append(plane.getEngineRunning())
			.append("\n=======\n");
		
		return telemetryRead.toString();
	}
	
	public static void main(String [] args) {
		
		
		
		int maxRuntime = 60 * 60 * 1000;
		int runtime = 0;
		
		//need a low sleep time because we'll crank up the simulator time reference
		int runtimeSleep = 100;
		
		C172P plane = null;
		
		try {
			plane = new C172P();
			
			//start the engine up to start consuming stuff
			plane.startupPlane();

			//can set these early since they don't seem to impact the engine
			
			//probably not going to happen but do it anyway
			plane.setDamageEnabled(false);
			
			//so the plane doesn't move
			plane.setParkingBrake(true);
			
			//engine should be running at this point but it's not ready  
			//wait for startup to complete and telemetry reads to arrive

			Thread.sleep(POST_STARTUP_SLEEP);
			
			//throttle and mixture up to consume faster
			//mixture has to be set first, then the throttle stepped up
			plane.setMixture(0.95);
			
			//step up the throttle. autostart sets it at 20%
			plane.setThrottle(0.25);
			plane.setThrottle(0.35);
			plane.setThrottle(0.45);
			plane.setThrottle(0.55);
			plane.setThrottle(0.65);
			plane.setThrottle(0.75);
			plane.setThrottle(0.85);
			plane.setThrottle(0.95);
			
			//highest it goes at 100%.
			plane.setThrottle(1);
			
			//speed up time in the simulator
			plane.setSpeedUp(16);
			
			//a full fuel tank will take a while
			//set this after the engine is running
			//possible that setting this too low drops the fuel flow which causes the engine to seize
			//plane.setFuelTankLevel(5);
			
			while( plane.isEngineRunning() && runtime < maxRuntime ) {
				
				LOGGER.debug("======================\nCycle start.");
							
				LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
				
				try {
					Thread.sleep(runtimeSleep);
				} catch (InterruptedException e) {
					LOGGER.warn("Runtime sleep interrupted", e);
				}
				
				runtime += runtimeSleep;
				
				LOGGER.debug("Cycle end\n======================");
			}
			
			LOGGER.debug("Exiting runtime loop");
			
			LOGGER.info("Final Telemetry Read: {}", telemetryReadOut(plane));
			
		} catch (FlightGearSetupException e) {
			LOGGER.error("FlightGearSetupException caught", e);
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException caught", e);
		}
		finally {
			if(plane != null) {
				plane.shutdown();
			}
		}
	}
}
