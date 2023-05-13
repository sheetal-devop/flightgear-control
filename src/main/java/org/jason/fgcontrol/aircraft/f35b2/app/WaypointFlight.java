package org.jason.fgcontrol.aircraft.f35b2.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.f35b2.F35B2;
import org.jason.fgcontrol.aircraft.f35b2.F35B2Config;
import org.jason.fgcontrol.aircraft.f35b2.flight.F35B2FlightParameters;
import org.jason.fgcontrol.aircraft.f35b2.flight.F35B2WaypointFlightExecutor;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointFlight {
        
    private final static Logger LOGGER = LoggerFactory.getLogger(WaypointFlight.class);

    //a nice, clear, warm, and bright date/time in western canada
    private final static String LAUNCH_TIME_GMT = "2021-07-01T20:00:00";
            
    public static void main(String [] args) {
        F35B2 plane = null;
        
        long startTime = System.currentTimeMillis();
        
        //bc tour
        //f15c script launches from YVR
        
        //too fast for the local tour
        //ArrayList<WaypointPosition> route = KnownRoutes.VAN_ISLAND_TOUR_SOUTH;
        
        //for fun, mix it up
        //java.util.Collections.reverse(route);
        
        try {
        	
        	String confFile = "./f15c.properties";
        	if(args.length >= 1) {
        		confFile = args[0];	
        	}
        	
        	Properties simProperties = new Properties();
        	simProperties.load(new FileInputStream(confFile) );
        	
        	F35B2Config f35b2Config = new F35B2Config(simProperties); 
        	
        	LOGGER.info("Using config:\n{}", f35b2Config.toString() );
        	
            plane = new F35B2(f35b2Config);
        
    		plane.refillFuel();
            
    		//route set in config
            //plane.setWaypoints(route);
            
            plane.setDamageEnabled(false);
            plane.setGMT(LAUNCH_TIME_GMT);
            
            F35B2FlightParameters parameters = new F35B2FlightParameters();
            parameters.setBearingRecalculationCycleSleep(1000L);
            
            F35B2WaypointFlightExecutor.runFlight(plane, parameters);
            
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
