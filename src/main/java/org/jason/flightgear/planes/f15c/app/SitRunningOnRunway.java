package org.jason.flightgear.planes.f15c.app;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.f15c.F15C;
import org.jason.flightgear.planes.f15c.F15CFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitRunningOnRunway {
        
    private static Logger logger = LoggerFactory.getLogger(SitRunningOnRunway.class);
    
    private static boolean isEngineRunning(F15C plane) {
        return plane.getEngineRunning() == F15CFields.ENGINE_RUNNING_INT_TRUE;
    }
    
    public static void main(String [] args) throws InvalidTelnetOptionException, Exception {
        
        StringBuilder telemetryRead;
        
        //20 minutes
        int maxRuntime = 20 * 60 * 1000;
        
        int runtime = 0;
        
        //need a low sleep time because we'll crank up the simulator time reference
        int runtimeSleep = 100;
        
        F15C plane = new F15C();
        
        //a full fuel tank will take a while
        plane.setFuelTankLevel(50);
        
        plane.setDamageEnabled(false);
        
        plane.startupPlane();

        //so the plane doesn't move
        plane.setParkingBrake(true);
        
        //wait for startup to complete and telemetry reads to arrive
        
        while( !isEngineRunning(plane) ) {
            try {
                logger.info("Waiting for f15c engine to start");
                Thread.sleep(runtimeSleep);
            } catch (InterruptedException e) {
                logger.warn("Engine start sleep interrupted", e);
            }            
        }
        
        //fast
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
                .append(plane.getFuelLevel())
                .append("\nOil pressure: ")
                .append(plane.getOilPressure())
                .append("\nOil temperature: ")
                .append(plane.getOilPressure())
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
