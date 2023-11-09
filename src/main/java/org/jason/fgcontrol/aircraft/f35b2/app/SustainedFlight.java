package org.jason.fgcontrol.aircraft.f35b2.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.f35b2.F35B2;
import org.jason.fgcontrol.aircraft.f35b2.F35B2Config;
import org.jason.fgcontrol.aircraft.f35b2.F35B2Fields;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SustainedFlight {

    private static Logger LOGGER = LoggerFactory.getLogger(SustainedFlight.class);
    
    private final static int TARGET_ALTITUDE = 9000;
    
    //0 => N, 90 => E
    private final static int DEFAULT_TARGET_HEADING = 93;
    
    private static String telemetryReadOut(F35B2 plane) {
                
        return 
                String.format("\nCurrent Heading: %f", plane.getHeading()) +
                String.format("\nAir Speed: %f", plane.getAirSpeed()) +
                String.format("\nFuel tank 0 level: %f", plane.getFuelTank0Level()) +
                String.format("\nFuel tank 1 level: %f", plane.getFuelTank1Level()) +
                String.format("\nFuel tank 2 level: %f", plane.getFuelTank2Level()) +
                String.format("\nFuel tank 3 level: %f", plane.getFuelTank3Level()) +
                String.format("\nFuel tank 4 level: %f", plane.getFuelTank4Level()) +
//                String.format("\nFuel tank 5 level: %f", plane.getFuelTank5Level()) +
//                String.format("\nFuel tank 6 level: %f", plane.getFuelTank6Level()) +
                String.format("\nEngine running: %d", plane.getEngineRunning()) + 
                String.format("\nEngine 1 thrust: %f", plane.getEngine0Thrust()) + 
                String.format("\nEnv Temp: %f", plane.getTemperature()) + 
                String.format("\nEngine 1 Throttle: %f", plane.getEngine0Throttle()) +
                String.format("\nAltitude: %f", plane.getAltitude()) +
                String.format("\nLatitude: %f", plane.getLatitude()) + 
                String.format("\nLongitude: %f", plane.getLongitude()) +
                String.format("\nAileron: %f", plane.getAileron()) +
                String.format("\nAileron Trim: %f", plane.getAileronTrim()) +
                String.format("\nElevator: %f", plane.getElevator()) +
                String.format("\nElevator Trim: %f", plane.getElevatorTrim()) +
                String.format("\nFlaps: %f", plane.getFlaps()) +
                String.format("\nRudder: %f", plane.getRudder()) +
                String.format("\nRudder Trim: %f", plane.getRudderTrim()) +
                String.format("\nGear Down: %d", plane.getGearDown()) +
                String.format("\nParking Brake: %d", plane.getParkingBrake()) +
                "\nGMT: " + plane.getGMT();
    }
    
    
    
    public static void main(String[] args) {
    	F35B2 plane = null;
        
        try {
        	double currentHeading = DEFAULT_TARGET_HEADING;
        	
        	String confFile = "./f35b2.properties";
        	if(args.length >= 2) {
        		confFile = args[0];	
        		currentHeading = Double.parseDouble(args[1]);
        	}
        	
        	Properties simProperties = new Properties();
        	simProperties.load(new FileInputStream(confFile) );
        	
        	F35B2Config f35b2Config = new F35B2Config(simProperties); 
        	
        	LOGGER.info("Using config:\n{}", f35b2Config.toString() );
        	
            plane = new F35B2(f35b2Config);
            
            plane.setDamageEnabled(false);
            plane.setGMT("2021-07-01T20:00:00");
            
            //in case we get a previously lightly-used environment
            plane.refillFuel();
            
            //////////////////////////////////////////////
            
            //launched from shell script. starts paused
            
            //check surfaces and orientations
            
            //parking brake check
            if(plane.isParkingBrakeEnabled()) {
                LOGGER.warn("Found parking brake on before throttle step up. Retracting");
                plane.setParkingBrake(false);
            }
            
            plane.resetControlSurfaces();
            
            plane.setPause(false);
            
            //has gear down by default
            if(plane.isGearDown()) {
            	plane.setGearDown(false);
            }
            
            //sleep to let the plane get up to speed
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            
            //////////////////////////////////////////////
            //enter stable flight
            
            double minFuelTank0 = 50.0,
                    minFuelTank1 = 50.0,
                    minFuelTank2 = 50.0,
                    minFuelTank3 = 50.0,
                    minFuelTank4 = 50.0;
                        
            //////////////////////////////////////////////
            
            LOGGER.info("Stepping up engine throttle");
            
            //hopefully smoother flight with fewer heading corrections
            plane.setElevator(-0.01);
            plane.setAileron(0.001);
            plane.setRudder(-0.001);
            
            //ensure we're at the max explicitly
            plane.setEngineThrottle(F35B2Fields.THROTTLE_MAX);
            
            LOGGER.info("Throttle step up completed");
            
            //chase view
            plane.setCurrentView(2);
            
            LOGGER.info("Post startup ending");

            
            //////////////////////////////////////////////
            
            boolean running = true;
            int cycles = 0;
            int maxCycles = 50* 1000;
            
            //tailor the update rate to the speedup
            int cycleSleep = 20;
            
            double maxRoll = 2.0;
            double targetRoll = 0.0;
            double maxPitch = 2.0;
            double targetPitch = 0.0;
            double courseAdjustmentIncrement = 4.0;
            double maxHeadingDeviation = 3.0;
            
            while(running && cycles < maxCycles) {
                
                LOGGER.info("======================\nCycle {} start. Target heading: {} ", cycles, currentHeading);
            
                //check altitude first, if we're in a nose dive that needs to be corrected first
                FlightUtilities.altitudeCheck(plane, 1000, TARGET_ALTITUDE);
                
                FlightUtilities.stabilizeCheck(plane, 
                	currentHeading, courseAdjustmentIncrement, maxHeadingDeviation,
                    maxRoll, targetRoll, 
                    maxPitch, targetPitch 
                );
                
                //refill all tanks for balance
                if (
                    plane.getFuelTank0Level() < minFuelTank0 || 
                    plane.getFuelTank1Level() < minFuelTank1 ||
                    plane.getFuelTank2Level() < minFuelTank2 ||
                    plane.getFuelTank3Level() < minFuelTank3 ||
                    plane.getFuelTank4Level() < minFuelTank4 

                ) {
                    plane.refillFuel();
                }
                

                
                LOGGER.info("Telemetry Read: {}", telemetryReadOut(plane));
                
                //optionally change direction slightly
                //may happen very quickly depending on how quickly telemetry updates completes
//                if(cycles % 100 == 0) {
//                    currentHeading = (currentHeading + 15) % 360;
//                    
//                    if(currentHeading < 0) {
//                        currentHeading += 360;
//                    } 
//                    
//                    LOGGER.info("Adjusting Heading to {}", currentHeading);
//                }
                
                try {
                    Thread.sleep(cycleSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                LOGGER.info("Cycle end\n======================");
                
                cycles++;
            }
            
            LOGGER.info("Trip is finished!");
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
        }
    }

}
