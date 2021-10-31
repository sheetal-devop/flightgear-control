package org.jason.flightgear.planes.c172p.app;

import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.c172p.C172P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitRunningOnRunway {
		
	private final static int POST_STARTUP_SLEEP = 3000;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SitRunningOnRunway.class);
	
	private static String telemetryReadOut(C172P plane) {
		
		return 
			String.format("\nFuel level: %f", plane.getFuelLevel()) +
			String.format("\nTime Elapsed: %f", plane.getTimeElapsed()) +
			String.format("\nTime Local: %f", plane.getLocalDaySeconds()) +
			String.format("\nFuel flow: %f", plane.getFuelFlow()) +
			String.format("\nOil pressure: %f", plane.getOilPressure()) +
			String.format("\nOil temperature: %f", plane.getOilTemperature()) +
			String.format("\nMpOsi: %f", plane.getMpOsi()) +
			String.format("\nThrottle: %f", plane.getThrottle()) +
			String.format("\nMixture: %f", plane.getMixture()) +
			String.format("\nEngine running: %d", plane.getEngineRunning());
	}
	
	public static void main(String [] args) {
		
		int maxRuntime = 20 * 60 * 1000;
		int runtime = 0;
		
		//need a low sleep time because we'll crank up the simulator time reference
		int runtimeSleep = 250;
		
		C172P plane = null;
		
		try {
			plane = new C172P();
			
			//refill in case a previous run emptied it
			plane.refillFuelTank();
			
			//start the engine up to start consuming stuff
			plane.startupPlane();

			//can set these early since they don't seem to impact the engine
			
			//probably not going to happen but do it anyway
			plane.setDamageEnabled(false);
			
			//so the plane doesn't move- not that it really matters.
			plane.setParkingBrake(true);
			
			//engine should be running at this point but it's not ready  
			//wait for startup to complete and telemetry reads to arrive
			Thread.sleep(POST_STARTUP_SLEEP);
			
			//a full fuel tank will take a while
			plane.setFuelTankLevel(5);
			
			//throttle and mixture up to consume faster
			//mixture has to be set first, then the throttle
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
			
			//have seen this set the throttle back to 20
			//possible that we're not waiting on the next state read after writing the input to the socket
			//highest it goes at 100%. set after throttle
			//plane.setMixture(1);
						
			//speed up time in the simulator
			//full tank 32x - 752s 
			//full tank 16x - 824s
			plane.setSpeedUp(16);
			
			long startTime = System.currentTimeMillis();
			
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
			
			//at higher speedups the simulator window is unusable, so return it to something usable
			plane.setSpeedUp(1);
			
			LOGGER.info("Final Telemetry Read: {}", telemetryReadOut(plane));
			
			LOGGER.info("Completed burnout in {}s", (System.currentTimeMillis() - startTime)/1000);
			
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
