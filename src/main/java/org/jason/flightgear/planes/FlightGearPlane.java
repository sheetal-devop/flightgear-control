package org.jason.flightgear.planes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlightGearPlane {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearPlane.class);
	
    private final static int TELEMETRY_READ_TRAILING_SLEEP = 250;
    
    private boolean runTelemetryThread;
    
    private Thread telemetryThread;
    
    protected Map<String, String> currentState;
    
    //writing telemetry
    protected AtomicBoolean stateWriting;
    
    //reading telemetry from socket
    protected AtomicBoolean stateReading;
	
	public FlightGearPlane() {
		
        currentState = Collections.synchronizedMap(new LinkedHashMap<String, String>());
        
        stateWriting = new AtomicBoolean(false);
        stateReading = new AtomicBoolean(false);	
	}
	
	protected void launchTelemetryThread() {
        runTelemetryThread = true;
        
        telemetryThread = new Thread() {
            @Override
            public void run() {
                LOGGER.trace("Telemetry thread started");
                
                readTelemetry();
                
                LOGGER.trace("Telemetry thread returning");
            }
        };
        telemetryThread.start();
	}
	
    protected LinkedHashMap<String, String> copyStateFields(String[] fields) {
        LinkedHashMap<String, String> retval = new LinkedHashMap<>();
        
        for(String field : fields) {
            if(currentState.containsKey(field)) {
                retval.put(field, currentState.get(field));
            }
            else
            {
                LOGGER.warn("Current state missing field: " + field);
            }
        }
        
        return retval;
    }

    public boolean runningTelemetryThread() {
    	return runTelemetryThread;
    }
    
    public synchronized Map<String, String> getTelemetry() {
        Map<String, String> retval = new HashMap<>();
        
        while(stateWriting.get()) {
            try {
                LOGGER.debug("Waiting for state writing to complete");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        stateReading.set(true);
        retval.putAll(currentState);
        stateReading.set(false);
        
        return retval;
    }
    
    public void shutdown() {
    	LOGGER.debug("Plane shutdown invoked");
    	
        //stop telemetry read
        runTelemetryThread = false;
        
        //TODO: ensure thread exits within timeout
        
        int waitTime = 0;
        int interval = 250;
        int maxWait = 2000;
        while(telemetryThread.isAlive()) {
            LOGGER.debug("waiting on telemetry thread to terminate");
            
            if(waitTime >= maxWait) {
                telemetryThread.interrupt();
            }
            else {
                waitTime += interval;
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOGGER.warn("Telemetry thread wait interrupted", e);
                }
            }
        }
        
        LOGGER.debug("Telemetry thread terminated");
        
    	LOGGER.debug("Plane shutdown completed");
    }
    
    //internal telemetry retrieval thread
    private void readTelemetry() {
        
        //TODO: enable pause on sim freeze
    	String telemetryRead = null;
        while(runningTelemetryThread()) {
            
            //wait for any state read operations to finish
        	//TODO: max wait on this
            while(stateReading.get()) {
                try {
                    LOGGER.trace("Waiting for state reading to complete");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.warn("Polling state read sleep interrupted", e);
                }
            }
            
            stateWriting.set(true);
            //read from socket connection. retrieves json string. write state to map
            //TODO: make this not awful
            
            //TODO: init outside the loop
             
            try {
                //telemetryRead = fgSockets.readTelemetry();
            	telemetryRead = readTelemetryRaw();
                
            	if(telemetryRead != null) {
	                //if for some reason telemetryRead is not proper json, the update is dropped
	                final JSONObject jsonTelemetry = new JSONObject(telemetryRead);
	                
	                if(jsonTelemetry != null) {
		                jsonTelemetry.keySet().forEach( 
		                    keyStr -> {
		                        currentState.put(keyStr, jsonTelemetry.get(keyStr).toString());
		                    }    
		                );
	                }
            	}
            	else {
            		LOGGER.warn("Raw telemetry read was null. Bailing on update.");
            	}
            } catch (JSONException jsonException) {
                LOGGER.error("JSON Error parsing telemetry. Received:\n" + telemetryRead + "\n===", jsonException);
            } catch (IOException ioException) {
                LOGGER.error("IOException parsing telemetry. Received:\n" + telemetryRead + "\n===", ioException);
            }
            finally {
                stateWriting.set(false);
                
                //sleep before next update. successful or not
                try {
                    Thread.sleep(TELEMETRY_READ_TRAILING_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Trailing state read sleep interrupted", e);
                }
            }
        }
        
        LOGGER.info("readTelemetry returning");
    }
    
    protected abstract String readTelemetryRaw() throws IOException;
    
    
    /**
     * Refill the fuel tanks to capacity
     */
    public synchronized void refillFuelTank() {
    	setFuelTankLevel(getFuelTankCapacity());
    }
    
    protected abstract void writeSocketInput(LinkedHashMap<String, String> inputHash, int port);

    
    //////////////////
    //expected FG property setters/getters
    //defined in plane subclass since socket io is managed there
    
    public abstract void setFuelTankLevel(double fuelTankCapacity);
    
    public abstract double getFuelLevel();

	/**
	 * May not be generic, since planes may be multiple tanks, so the subclass handles this details. 
	 */
	public abstract double getFuelTankCapacity();
    
	public abstract double getAltitude();

	public abstract void setPause(boolean isPaused);

	public abstract void setAltitude(double targetAltitude);

	public abstract double getHeading();

	public abstract void setHeading(double targetHeading);

	public abstract double getPitch();

	public abstract void setPitch(double targetPitch);

	public abstract double getRoll();

	public abstract void setRoll(double targetRoll);

	public abstract void setSpeedUp(double targetSpeedup);

}
