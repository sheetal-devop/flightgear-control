package org.jason.fgcontrol.aircraft.f15c.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CConfig;
import org.jason.fgcontrol.aircraft.f15c.app.flight.RunwayBurnoutFlightExecutor;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunwayBurnout {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunwayBurnout.class);

    public static void main(String [] args) throws IOException {

    	//40 gallons - at high throttle fuel goes fairly quickly
    	int testFuelAmount = 40;
        
        F15C plane = null;
        
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
            
            //refill in case a previous run emptied it
            plane.refillFuel();
            
            //a clean sim can start with the engines running depending on the sim autosave, and we don't want that
            plane.setEngine0Cutoff(true);
            plane.setEngine1Cutoff(true);
            
            //probably not going to happen but do it anyway
            plane.setDamageEnabled(false);
            
            //set our test fuel amount
            plane.setFuelTank0Level(0.0);
            plane.setFuelTank1Level(0.0);
            plane.setFuelTank2Level(testFuelAmount);
            plane.setFuelTank3Level(0.0);
            plane.setFuelTank4Level(0.0);
            
            RunwayBurnoutFlightExecutor.runFlight(plane);
            
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
