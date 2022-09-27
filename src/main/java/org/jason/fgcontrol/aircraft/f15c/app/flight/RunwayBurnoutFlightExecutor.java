package org.jason.fgcontrol.aircraft.f15c.app.flight;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CFields;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launch with the plane on a runway. Start the engine, apply the parking brake, and boost throttle to 90%.
 * Observe the plane state until fuel runs out.
 * 
 * @author jason
 *
 */
public abstract class RunwayBurnoutFlightExecutor {
	
    private final static int POST_STARTUP_SLEEP = 3000;
    
    //need a low sleep time because we'll crank up the simulator time reference
    private final static int RUNTIME_SLEEP = 250;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunwayBurnoutFlightExecutor.class);
	
	public static void runFlight(F15C plane) throws IOException, AircraftStartupException, FlightGearSetupException, InterruptedException {
		
        //start the engine up to start consuming fuel
        plane.startupPlane();
        
        //so the plane doesn't move- not that it really matters.
        plane.setParkingBrake(true);
        
        if(plane.getParkingBrake() != F15CFields.PARKING_BRAKE_INT_TRUE) {
            throw new FlightGearSetupException("Parking brake failed to set");
        }
                    
        //engine should be running at this point but it's not ready  
        //wait for startup to complete and telemetry reads to arrive
        Thread.sleep(POST_STARTUP_SLEEP);
        
        //stop short of 100% or it will overwhelm the parking brake
        //two engines on the f15c, engine 1 throttle is synced to engine 0
        plane.setEngine0Throttle(0.25);
        plane.setEngine0Throttle(0.5);
        plane.setEngine0Throttle(0.75);
        plane.setEngine0Throttle(0.90);
                    
        //speed up time in the simulator
        //full tank 16x - 353s
        plane.setSimSpeedUp(16);
        
        long startTime = System.currentTimeMillis();
        
        while( plane.isEngineRunning() ) {
            
            LOGGER.debug("======================\nCycle start.");
                        
            LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
            
            try {
                Thread.sleep(RUNTIME_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("Runtime sleep interrupted", e);
            }
            
            LOGGER.debug("Cycle end\n======================");
        }
        
        LOGGER.debug("Exiting runtime loop");

        LOGGER.info("Final Telemetry Read: {}", telemetryReadOut(plane));
        
        LOGGER.info("Completed burnout in {}s", (System.currentTimeMillis() - startTime)/1000);
	}
	
    private static String telemetryReadOut(F15C plane) {

        return String.format("\nFuel level Tank 0: %f", plane.getFuelTank0Level())
        		+ String.format("\nFuel level Tank 1: %f", plane.getFuelTank1Level())
        		+ String.format("\nFuel level Tank 2: %f", plane.getFuelTank2Level())
        		+ String.format("\nFuel level Tank 3: %f", plane.getFuelTank3Level())
        		+ String.format("\nFuel level Tank 4: %f", plane.getFuelTank4Level())
        		+ String.format("\nTime Elapsed: %f", plane.getTimeElapsed())
                + String.format("\nTime Local: %f", plane.getLocalDaySeconds())
                + String.format("\nFuel flow - Engine 0: %f", plane.getEngine0FuelFlow())
                + String.format("\nFuel flow - Engine 1: %f", plane.getEngine1FuelFlow())
                + String.format("\nOil pressure - Engine 0: %f", plane.getEngine0OilPressure())
                + String.format("\nOil pressure - Engine 1: %f", plane.getEngine1OilPressure())
                + String.format("\nThrottle: %f", plane.getEngine0Throttle())
                + String.format("\nMixture: %f", plane.getEngine0Mixture())
                + String.format("\nEngine running: %d", plane.getEngineRunning());
    }
}
