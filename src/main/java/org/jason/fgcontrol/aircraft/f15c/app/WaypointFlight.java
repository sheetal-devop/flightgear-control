package org.jason.fgcontrol.aircraft.f15c.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CConfig;
import org.jason.fgcontrol.aircraft.f15c.flight.F15CFlightParameters;
import org.jason.fgcontrol.aircraft.f15c.flight.WaypointFlightExecutor;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.flight.position.KnownRoutes;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointFlight {
        
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlight.class);

    //a nice, clear, warm, and bright date/time in western canada
    private final static String LAUNCH_TIME_GMT = "2021-07-01T20:00:00";
            
    public static void main(String [] args) {
        F15C plane = null;
        
        long startTime = System.currentTimeMillis();
        
        //bc tour
        //f15c script launches from YVR
        
        //too fast for the local tour
        ArrayList<WaypointPosition> route = KnownRoutes.VAN_ISLAND_TOUR_SOUTH;
        
        //for fun, mix it up
        //java.util.Collections.reverse(route);
        
        try {
        	
        	String confFile = "./f15c.properties";
        	if(args.length >= 1) {
        		confFile = args[0];	
        	}
        	
        	Properties simProperties = new Properties();
        	simProperties.load(new FileInputStream(confFile) );
        	
        	F15CConfig f15cConfig = new F15CConfig(simProperties); 
        	
        	LOGGER.info("Using config:\n{}", f15cConfig.toString() );
        	
            plane = new F15C(f15cConfig);
        
    		plane.refillFuel();
            
            plane.setWaypoints(route);
            
            plane.setDamageEnabled(false);
            plane.setGMT(LAUNCH_TIME_GMT);
            
            F15CFlightParameters parameters = new F15CFlightParameters();
            parameters.setBearingRecalculationCycleSleep(500L);
            
            WaypointFlightExecutor.runFlight(plane);
            
            //since we're done and no longer stabilizing the plane, pause the sim so the plane doesn't fall
            plane.setPause(true);
        } catch (FlightGearSetupException e) {
            LOGGER.error("FlightGearSetupException occurred", e);
        } catch (IOException e) {
            LOGGER.error("IOException occurred", e);
        }
        finally {
            if(plane != null) {
                
                plane.shutdown();
                
                try {
                    plane.terminateSimulator();
                } catch (IOException e) {
                	LOGGER.error("IOException occurred during shutdown", e);
                } catch (InvalidTelnetOptionException e) {
                	LOGGER.error("InvalidTelnetOptionException occurred during shutdown", e);
                }
                
                if(plane.getFlightLogTrackPositionCount() > 0) {
	                plane.writeFlightLogGPX(System.getProperty("user.dir") + "/f15c_"+System.currentTimeMillis() + ".gpx");
                }
                else {
                	LOGGER.warn("No track positions in flightlog");
                }
                
                long tripTime = (System.currentTimeMillis() - startTime);
                
                LOGGER.info("Completed course in: {}ms => {} minutes", tripTime, ( ((double)tripTime / 1000.0) / 60.0 ) );
            }           
        }
    }
}
