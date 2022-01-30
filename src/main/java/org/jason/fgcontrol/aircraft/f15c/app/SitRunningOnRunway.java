package org.jason.fgcontrol.aircraft.f15c.app;

import java.io.IOException;

import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CFields;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitRunningOnRunway {

    private final static Logger LOGGER = LoggerFactory.getLogger(SitRunningOnRunway.class);

    private final static int POST_STARTUP_SLEEP = 3000;

    private static String telemetryReadOut(F15C plane) {

        return String.format("\nFuel level: %f", plane.getFuelLevel())
                + String.format("\nTime Elapsed: %f", plane.getTimeElapsed())
                + String.format("\nTime Local: %f", plane.getLocalDaySeconds())
                + String.format("\nFuel flow: %f", plane.getEngine0FuelFlow())
                + String.format("\nOil pressure: %f", plane.getEngine0OilPressure())
                + String.format("\nThrottle: %f", plane.getEngine0Throttle())
                + String.format("\nMixture: %f", plane.getEngine0Mixture())
                + String.format("\nEngine running: %d", plane.getEngineRunning());
    }

    public static void main(String [] args) throws IOException {
        int maxRuntime = 20 * 60 * 1000;
        int runtime = 0;
        
        //need a low sleep time because we'll crank up the simulator time reference
        int runtimeSleep = 250;
        
        F15C plane = null;
        
        try {
            plane = new F15C();
            
            //refill in case a previous run emptied it
            plane.refillFuel();
            
            //a clean sim can start with the engines running depending on the sim autosave, and we don't want that
            plane.setEngine0Cutoff(true);
            plane.setEngine1Cutoff(true);
            
            //start the engine up to start consuming stuff
            plane.startupPlane();
            
            //probably not going to happen but do it anyway
            plane.setDamageEnabled(false);
            
            //so the plane doesn't move- not that it really matters.
            plane.setParkingBrake(true);
            
            if(plane.getParkingBrake() != F15CFields.PARKING_BRAKE_INT_TRUE) {
                throw new FlightGearSetupException("Parking brake failed to set");
            }
                        
            //engine should be running at this point but it's not ready  
            //wait for startup to complete and telemetry reads to arrive
            Thread.sleep(POST_STARTUP_SLEEP);
            
            //at high throttle fuel goes fairly quickly
            plane.setFuelTank0Level(40);
            
            //stop short of 100% or it will overwhelm the parking brake
            plane.setEngine0Throttle(0.25);
            plane.setEngine0Throttle(0.5);
            plane.setEngine0Throttle(0.75);
            plane.setEngine0Throttle(0.90);
                        
            //speed up time in the simulator
            //full tank 16x - 353s
            plane.setSimSpeedUp(16);
            
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
            plane.setSimSpeedUp(1);
            
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
