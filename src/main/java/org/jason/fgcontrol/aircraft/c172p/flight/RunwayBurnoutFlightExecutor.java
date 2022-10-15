package org.jason.fgcontrol.aircraft.c172p.flight;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PFields;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launch with the plane on a runway. Start the engine, apply the parking brake, and boost throttle to 100%.
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
	
	public static void runFlight(C172P plane) throws IOException, AircraftStartupException, InterruptedException {
        
        //start the engine up to start consuming fuel
        plane.startupPlane(true);

        //so the plane doesn't move- not that it really matters.
        //has to happen after startup
        plane.setParkingBrake(true);
        
        //engine should be running at this point but it's not ready  
        //wait for startup to complete and telemetry reads to arrive
        Thread.sleep(POST_STARTUP_SLEEP);
        
        //throttle and mixture up to consume faster
        //mixture has to be set first, then the throttle
        plane.setMixture(0.95);
        
        //step up the throttle. autostart sets it at 20% on the ground
        plane.setThrottle(0.25);
        plane.setThrottle(0.35);
        plane.setThrottle(0.45);
        plane.setThrottle(0.55);
        plane.setThrottle(0.65);
        plane.setThrottle(0.75);
        plane.setThrottle(0.85);
        plane.setThrottle(0.95);

        //highest it goes at 100%.
        plane.setThrottle(C172PFields.THROTTLE_MAX);
        
        //have seen this set the throttle back to 20
        //possible that we're not waiting on the next state read after writing the input to the socket
        //highest it goes at 100%. set after throttle
        //plane.setMixture(1);
                    
        //speed up time in the simulator
        //full tank 32x - 752s -> not a 2x speedup from 16x, possibly overwhelms the cpu
        //full tank 16x - 824s
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
        
        //at higher speedups the simulator window is unusable, so return it to something usable
        plane.setSimSpeedUp(1);
        
        LOGGER.info("Final Telemetry Read: {}", telemetryReadOut(plane));
        
        LOGGER.info("Completed burnout in {}s", (System.currentTimeMillis() - startTime)/1000);
	}
	
    private static String telemetryReadOut(C172P plane) {
        
        return 
            String.format("\nFuel level: %f", plane.getFuelLevel()) +
            String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
            String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
            String.format("\nTime Elapsed: %f", plane.getTimeElapsed()) +
            String.format("\nTime Local: %f", plane.getLocalDaySeconds()) +
            String.format("\nFuel flow: %f", plane.getFuelFlow()) +
            String.format("\nOil pressure: %f", plane.getOilPressure()) +
            String.format("\nOil temperature: %f", plane.getOilTemperature()) +
            String.format("\nMpOsi: %f", plane.getMpOsi()) +
            String.format("\nThrottle: %f", plane.getThrottle()) +
            String.format("\nMixture: %f", plane.getMixture()) +
            String.format("\nParking Brake: %d", plane.getParkingBrake()) +
            String.format("\nEngine running: %d", plane.getEngineRunning());
    }
}
