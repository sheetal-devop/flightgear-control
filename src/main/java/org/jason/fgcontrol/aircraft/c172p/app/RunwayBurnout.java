package org.jason.fgcontrol.aircraft.c172p.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PConfig;
import org.jason.fgcontrol.aircraft.c172p.flight.RunwayBurnoutFlightExecutor;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunwayBurnout {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunwayBurnout.class);
    
    public static void main(String [] args) throws IOException {
        
    	//5 gallons
    	int testFuelAmount = 5;
    	
    	C172P plane = null;
    	
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
            
            //refill in case a previous run emptied it
            plane.refillFuel();

            //probably not going to happen but do it anyway
            plane.setDamageEnabled(false);
            
            //a full fuel tank will take a while
            plane.setFuelTank0Level(testFuelAmount);
            plane.setFuelTank1Level(testFuelAmount);
            
            RunwayBurnoutFlightExecutor.runFlight(plane);
            
            LOGGER.debug("Exiting runtime loop");
            
            //at higher speedups the simulator window is unusable, so return it to something usable
            plane.setSimSpeedUp(1);
            
        } catch (FlightGearSetupException e) {
            LOGGER.error("FlightGearSetupException caught", e);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException caught", e);
        } catch (AircraftStartupException e) {
            LOGGER.error("AircraftStartupException caught", e);
		}
        finally {
            if(plane != null) {
                plane.shutdown();
            }
        }
    }
}
