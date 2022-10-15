package org.jason.fgcontrol.aircraft.c172p.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PConfig;
import org.jason.fgcontrol.aircraft.c172p.C172PFields;
import org.jason.fgcontrol.aircraft.c172p.flight.WaypointFlightExecutor;
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
        C172P plane = null;
        
        long startTime = System.currentTimeMillis();
        
        //local tour
        //C172P script launches from YVR
        ArrayList<WaypointPosition> route = KnownRoutes.VANCOUVER_TOUR ;
        
        //bc tour - much longer
        //C172P script launches from YVR
        //ArrayList<WaypointPosition> route = KnownRoutes.BC_SOUTH_TOUR;
        
        //for fun, mix it up, but check your launch heading
        //java.util.Collections.reverse(route);       
        
		C172PConfig c172pConfig = null;
		if (args.length >= 1) {
			//only care about the first arg for the sim config
			String confFile = args[0];
			
			Properties simProperties = new Properties();
			try {
				simProperties.load(new FileInputStream(confFile));
				c172pConfig = new C172PConfig(simProperties);
				LOGGER.info("Using config:\n{}", c172pConfig.toString());
			} catch (IOException e) {	
				System.err.println("Error loading sim config");
				e.printStackTrace();
			
				System.exit(1);
			}
		}


		try {
			// build our plane
			if(c172pConfig != null ) {
				plane = new C172P(c172pConfig);
			}
			else {
				LOGGER.info("Using default simulator configuration");
				plane = new C172P();
			}
	
			// prep plane
			plane.setWaypoints(route);
	
			// set chase view
			plane.setCurrentView(2);
	
			plane.setDamageEnabled(false);
			plane.setComplexEngineProcedures(false);
			plane.setWinterKitInstalled(true);
			plane.setGMT(LAUNCH_TIME_GMT);
	
			// in case we get a previously lightly-used environment
			plane.refillFuel();
			plane.setBatteryCharge(C172PFields.BATTERY_CHARGE_MAX);
			
			//kick off our flight in the main thread
			WaypointFlightExecutor.runFlight(plane);
	            
	        //pause so the plane doesn't list from its heading and crash
	        plane.setPause(true);
        } catch (FlightGearSetupException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
            if(plane != null) {
                
                plane.shutdown();
                
                try {
                    plane.terminateSimulator();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidTelnetOptionException e) {
                    e.printStackTrace();
                }
            }
            
            plane.writeFlightLogGPX(System.getProperty("user.dir") + "/c172p_"+System.currentTimeMillis() + ".gpx");
        }
        
        long tripTime = (System.currentTimeMillis() - startTime);
        
        LOGGER.info("Completed course in: {}ms => {} minutes", tripTime, ( ((double)tripTime / 1000.0) / 60.0 ) );
    }
}

