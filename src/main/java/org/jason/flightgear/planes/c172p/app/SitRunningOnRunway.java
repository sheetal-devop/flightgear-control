package org.jason.flightgear.planes.c172p.app;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;
import org.jason.flightgear.planes.c172p.C172PFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitRunningOnRunway {
		
	private static Logger logger = LoggerFactory.getLogger(SitRunningOnRunway.class);
	
	private static boolean isEngineRunning(C172P plane) {
		return plane.getEngineRunning() == C172PFields.ENGINE_RUNNING_INT_TRUE;
	}
	
	public static void main(String [] args) throws InvalidTelnetOptionException, Exception {
		
		StringBuilder telemetryRead;
		
		int maxRuntime = 60 * 60 * 1000;
		int runtime = 0;
		
		//need a low sleep time because we'll crank up the simulator time reference
		int runtimeSleep = 100;
		
		C172P plane = new C172P();
		
		//a full fuel tank will take a while
		plane.setFuelTankLevel(10);
		
		plane.setDamageEnabled(false);
		
		plane.startupPlane();

		//so the plane doesn't move
		plane.setParkingBrake(true);
		
		//wait for startup to complete and telemetry reads to arrive
		
		while( !isEngineRunning(plane) ) {
			try {
				logger.info("Waiting for c172p engine to start");
				Thread.sleep(runtimeSleep);
			} catch (InterruptedException e) {
				logger.warn("Engine start sleep interrupted", e);
			}			
		}
		
		plane.setSpeedUp(32);
		
		while( isEngineRunning(plane) && runtime < maxRuntime ) {
			
			logger.info("======================\nCycle start.");
						
			telemetryRead = new StringBuilder();
			
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
				.append("\nEngine running: ")
				.append(plane.getEngineRunning())
				.append("\n=======\n");
			
			logger.info("Telemetry Read: {}", telemetryRead.toString());
			
			try {
				Thread.sleep(runtimeSleep);
			} catch (InterruptedException e) {
				logger.warn("Runtime sleep interrupted", e);
			}
			
			runtime += runtimeSleep;
			
			logger.info("Cycle end\n======================");
		}
		
		logger.info("Exiting runtime loop. Engine running: {}", isEngineRunning(plane));
		
		plane.shutdown();
	}
}
