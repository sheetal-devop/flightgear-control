package org.jason.flightgear.planes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.connection.sockets.FlightGearInputConnection;
import org.jason.flightgear.connection.telnet.FlightGearTelnetConnection;
import org.jason.flightgear.flight.WaypointPosition;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlightGearPlane {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearPlane.class);
    
    private final static int TELEMETRY_READ_WAIT_SLEEP = 100;
    private final static int TELEMETRY_READ_TRAILING_SLEEP = 250;
    private final static int TELEMETRY_WRITE_WAIT_SLEEP = 100;
    
    private final static int POST_PAUSE_SLEEP = 250;
    
    private boolean runTelemetryThread;
    
    private Thread telemetryThread;
    
    protected Map<String, String> currentState;
    
    //writing telemetry
    protected AtomicBoolean stateWriting;
    
    //reading telemetry from socket
    protected AtomicBoolean stateReading;
    
    protected NetworkConfig networkConfig;

    public FlightGearPlane() {
    	
    	networkConfig = new NetworkConfig();
    	
        //linkedhashmap to match the xml schema loaded into the simulator
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
        
        //wait for the first read to arrive
        while(currentState.size() == 0) {
        	LOGGER.debug("Waiting for first telemetry read to complete after thread start");
        	
            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        LOGGER.debug("launchTelemetryThread returning");
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
    
    /**
     * Get the telemetry map. Useful if we need a lot of fields at once.
     * 
     * @return a copy of the telemetry map
     */
    public synchronized Map<String, String> getTelemetry() {
        Map<String, String> retval = new HashMap<>();
                
        //TODO: time this out, trigger some kind of reset or clean update
        while(stateWriting.get()) {
            LOGGER.trace("Waiting for state writing to complete");

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
                
        stateReading.set(true);
        retval.putAll(currentState);
        stateReading.set(false);
        
        return retval;
    }
    
    /**
     * Get a single telemetry field
     * 
     * @param fieldName    Field name to lookup
     * @return    value of the field, null if it isn't in the map
     */
    public synchronized String getTelemetryField(String fieldName) {
        
        String retval = null;
        
        //TODO: time this out, trigger some kind of reset or clean update
        while(stateWriting.get()) {
            LOGGER.trace("Waiting for state writing to complete");

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        stateReading.set(true);
        retval = currentState.get(fieldName);
        stateReading.set(false);
        
        return retval;
    }
    
    public void shutdown() {
        LOGGER.debug("Plane shutdown invoked");
        
        //stop telemetry read
        runTelemetryThread = false;
                
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
        
        LOGGER.debug("Telemetry thread terminated. isAlive: {}", telemetryThread.isAlive());
        
        LOGGER.info("Plane shutdown completed");
    }
    
    //internal telemetry retrieval thread
    private void readTelemetry() {
        
        //enable pause on sim freeze? how would we know when the simulator was unpaused without an update?
        String telemetryRead = null;
        while(runningTelemetryThread()) {
            
        	LOGGER.trace("Begin telemetry read cycle");
        	
            //wait for any state read operations to finish
            //TODO: max wait on this
            while(stateReading.get()) {
                LOGGER.trace("Waiting for state reading to complete");

                try {
                    Thread.sleep(TELEMETRY_READ_WAIT_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Polling state read sleep interrupted", e);
                }
            }
            
            stateWriting.set(true);
            //read from socket connection. retrieves json string. write state to map
            //TODO: make this not awful

            try {
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
                LOGGER.error("JSON Error parsing telemetry. Received:\n{}\n===", telemetryRead, jsonException);
            } catch (IOException ioException) {
                LOGGER.error("IOException parsing telemetry. Received:\n{}\n===", telemetryRead, ioException);
            }
            finally {
                stateWriting.set(false);
                
                //sleep before next update. successful or not
                try {
                    Thread.sleep(TELEMETRY_READ_TRAILING_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Trailing state read sleep interrupted", e);
                }
                
            	LOGGER.trace("End telemetry read cycle");
            }
        }
        
        LOGGER.debug("readTelemetry returning");
    }
    
    /////////////////
    //simulator management
    
    public void resetSimulator() throws IOException, InvalidTelnetOptionException {
    	
    	LOGGER.debug("Simulator reset invoked");
    	
    	FlightGearTelnetConnection telnetSession = null;

		try {
			telnetSession = new FlightGearTelnetConnection(networkConfig.getTelnetHost(), networkConfig.getTelnetPort());
			telnetSession.resetSimulator();

	    	LOGGER.info("Simulator reset completed");
		} catch (IOException e) {
			LOGGER.error("Exception resetting simulator", e);
			throw e;
		} catch (InvalidTelnetOptionException e) {
			LOGGER.error("Exception resetting simulator", e);
			throw e;
		} finally {
			if (telnetSession != null && telnetSession.isConnected()) {
				telnetSession.disconnect();
			}
		}
    }
    
    public void terminateSimulator() throws IOException, InvalidTelnetOptionException {
    	
    	LOGGER.debug("Simulator termination invoked");
    	
    	FlightGearTelnetConnection telnetSession = null;

		try {
			telnetSession = new FlightGearTelnetConnection(networkConfig.getTelnetHost(), networkConfig.getTelnetPort());
			telnetSession.terminateSimulator();

			LOGGER.info("Simulator termination completed");
		} catch (IOException e) {
			LOGGER.error("Exception terminating simulator", e);
			throw e;
		} catch (InvalidTelnetOptionException e) {
			LOGGER.error("Exception terminating simulator", e);
			throw e;
		} finally {
			if (telnetSession != null && telnetSession.isConnected()) {
				telnetSession.disconnect();
			}
		}
	}
    
    /////////////////
    
    protected abstract String readTelemetryRaw() throws IOException;
    
    protected abstract void writeControlInput(LinkedHashMap<String, String> inputHash, FlightGearInputConnection socketConnection) throws IOException;
    
    /////////////////
    
    protected abstract void writeConsumeablesInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeControlInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeFdmInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeOrientationInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writePositionInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSystemInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeVelocitiesInput(LinkedHashMap<String, String> inputHash) throws IOException;

    //////////////////
    //May not be generic, since planes may have multiple tanks, so the subclass handles this details. 
    //expected FG property setters/getters
    //defined in plane subclass since socket io is managed there
    
    //public abstract void setFuelTank0Level(double fuelTankCapacity) throws IOException;
    
    public abstract double getFuelLevel();

    public abstract double getFuelTankCapacity();
    
    //////////////////
    
    /**
     * May not be generic, since planes may have multiple engines, so the subclass handles the details. 
     */
    public abstract boolean isEngineRunning();
    
    //////////////////
    //generic position
    
    public abstract void setLatitude(double targetLatitude) throws IOException;
    
    public abstract void setLongitude(double targetLongitude) throws IOException;
    
    public abstract void setAltitude(double targetAltitude) throws IOException;
    
    //////////////////
    //generic orientation
    
    /**
     * 
     * 
     * @param heading    Degrees from north, clockwise. 0=360 => North. 90 => East. 180 => South. 270 => West 
     * @throws IOException 
     */
    public synchronized void setHeading(double heading) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.ORIENTATION_INPUT_FIELDS);
        
        //get telemetry hash
        
        inputHash.put(FlightGearFields.HEADING_FIELD, String.valueOf(heading));
        
        LOGGER.info("Setting heading to {}", heading);
        
        writeOrientationInput(inputHash);
    }
    
    public synchronized void setPitch(double targetPitch) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.ORIENTATION_INPUT_FIELDS);
        
        inputHash.put(FlightGearFields.PITCH_FIELD, String.valueOf(targetPitch));
        
        LOGGER.info("Setting pitch to {}", targetPitch);
        
        writeOrientationInput(inputHash);
    }
    
    public synchronized void setRoll(double targetRoll) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.ORIENTATION_INPUT_FIELDS);
                
        inputHash.put(FlightGearFields.ROLL_FIELD, String.valueOf(targetRoll));
        
        LOGGER.info("Setting roll to {}", targetRoll);
        
        writeOrientationInput(inputHash);
    }
    
    //////////////////
    //generic velocity
    
    public abstract void setAirSpeed(double targetSpeed) throws IOException;
    
    public abstract void setVerticalSpeed(double targetSpeed) throws IOException;
    
    //////////////////
    //generic sim management
    
    public synchronized boolean isPaused() {
    	return getSimFreezeClock() == FlightGearFields.SIM_FREEZE_INT_TRUE &&
    			getSimFreezeMaster() == FlightGearFields.SIM_FREEZE_INT_TRUE;
    }
    
    /**
     * Pause the simulator. Does not consider existing state.
     * 
     * @param isPaused true to pause, false to unpause. 
     * @throws IOException
     */
    public synchronized void setPause(boolean isPaused) throws IOException {

        // TODO: check telemetry if already paused

        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.SIM_FREEZE_FIELDS);

        // oh get fucked. requires an int value for the bool, despite the schema specifying a bool.
        if (isPaused) {
            LOGGER.info("Pausing simulation");
            inputHash.put(FlightGearFields.SIM_FREEZE_CLOCK_FIELD, FlightGearFields.SIM_FREEZE_TRUE);
            inputHash.put(FlightGearFields.SIM_FREEZE_MASTER_FIELD, FlightGearFields.SIM_FREEZE_TRUE);
        } else {
            LOGGER.info("Unpausing simulation");
            inputHash.put(FlightGearFields.SIM_FREEZE_CLOCK_FIELD, FlightGearFields.SIM_FREEZE_FALSE);
            inputHash.put(FlightGearFields.SIM_FREEZE_MASTER_FIELD, FlightGearFields.SIM_FREEZE_FALSE);
        }

        // clock and master are the only two fields, no need to retrieve from the
        // current state
        // order matters. defined in input xml schema

        // socket writes typically require pauses so telemetry/state aren't out of date
        // however this is an exception
        writeSimFreezeInput(inputHash);

        // trailing sleep, so that the last real telemetry read arrives
        try {
            Thread.sleep(POST_PAUSE_SLEEP);
        } catch (InterruptedException e) {
            LOGGER.warn("setPause trailing sleep interrupted", e);
        }
    }
    
    /**
     * The speed at which the flight dynamics model is run.
     * 
     * @param targetSpeedup	Speedup factor. Acceptable values are 0.5,1,2,4,8,16,32. 32 is pretty slow. 
     * @throws IOException
     */
    public synchronized void setSpeedUp(double targetSpeedup) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.SIM_SPEEDUP_FIELDS);
        
        LOGGER.info("Setting speedup: {}", targetSpeedup);
        
        inputHash.put(FlightGearFields.SIM_SPEEDUP_FIELD, String.valueOf(targetSpeedup));
        
        writeSimSpeedupInput(inputHash);
    }
    
    ///////////////////
    
    /**
     * Refill necessary fuel tanks to capacity
     * @throws IOException 
     */
    public abstract void refillFuel() throws IOException;
    
    public synchronized WaypointPosition getPosition() {
    	return new WaypointPosition(getLatitude(), getLongitude(), getAltitude(), "Current position");
    }
    
    ///////////////////
    //environment
    
    public double getDewpoint() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.DEWPOINT_FIELD));
    }
    
    public double getEffectiveVisibility() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.EFFECTIVE_VISIBILITY_FIELD));
    }
    
    public double getPressure() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.PRESSURE_FIELD));
    }
    
    public double getRelativeHumidity() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.RELATIVE_HUMIDITY_FIELD));
    }
    
    public double getTemperature() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.TEMPERATURE_FIELD));
    }
    
    public double getVisibility() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.VISIBILITY_FIELD));
    }
    
    public double getWindFromDown() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.WIND_FROM_DOWN_FIELD));
    }
    
    public double getWindFromEast() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.WIND_FROM_EAST_FIELD));
    }
    
    public double getWindFromNorth() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.WIND_FROM_NORTH_FIELD));
    }
    
    public double getWindspeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.WINDSPEED_FIELD));
    }
    
    ///////////////////
    //fdm
    
    public int getDamageRepairing() {
        return Character.getNumericValue( getTelemetryField(FlightGearFields.FDM_DAMAGE_REPAIRING_FIELD).charAt(0));
    }
    
    public boolean isDamageRepairing() {
        return getDamageRepairing() == FlightGearFields.FDM_DAMAGE_REPAIRING_INT_TRUE;
    }
    
    //fbx
    public double getFbxAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_AERO_FIELD));
    }
    
    public double getFbxExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_EXTERNAL_FIELD));
    }
    
    public double getFbxGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_GEAR_FIELD));
    }
    
    public double getFbxPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_PROP_FIELD));
    }
    
    public double getFbxTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_TOTAL_FIELD));
    }
    
    public double getFbxWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBX_WEIGHT_FIELD));
    }
    
    //fby
    public double getFbyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_AERO_FIELD));
    }
    
    public double getFbyExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_EXTERNAL_FIELD));
    }
    
    public double getFbyGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_GEAR_FIELD));
    }
    
    public double getFbyPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_PROP_FIELD));
    }
    
    public double getFbyTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_TOTAL_FIELD));
    }
    
    public double getFbyWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBY_WEIGHT_FIELD));
    }
    
    //fbz
    public double getFbzAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_AERO_FIELD));
    }
    
    public double getFbzExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_EXTERNAL_FIELD));
    }
    
    public double getFbzGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_GEAR_FIELD));
    }
    
    public double getFbzPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_PROP_FIELD));
    }
    
    public double getFbzTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_TOTAL_FIELD));
    }
    
    public double getFbzWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FBZ_WEIGHT_FIELD));
    }
    
    //fsx
    public double getFsxAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FSX_AERO_FIELD));
    }
    
    //fsy
    public double getFsyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FSY_AERO_FIELD));
    }
    
    //fsz
    public double getFszAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FSZ_AERO_FIELD));
    }
    
    //fwy
    public double getFwyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FWY_AERO_FIELD));
    }
    
    //fwz
    public double getFwzAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_FWZ_AERO_FIELD));
    }
    
    //load factor
    public double getLoadFactor() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_LOAD_FACTOR_FIELD));
    }
    
    public double getLodNorm() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_LOD_NORM_FIELD));
    }
    
    //damage
    
    public int getDamage() {
        return Character.getNumericValue(getTelemetryField(FlightGearFields.FDM_DAMAGE_FIELD).charAt(0));
    }
    
    public boolean isDamageEnabled() {
        return getDamage() == FlightGearFields.FDM_DAMAGE_ENABLED_INT_TRUE;
    }

    public double getLeftWingDamage() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_LEFT_WING_DAMAGE_FIELD));
    }
    
    public double getRightWingDamage() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_RIGHT_WING_DAMAGE_FIELD));
    }
    
    ///////////////////
    //orientation
    
    public double getAlpha() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.ALPHA_FIELD));
    }
    
    public double getBeta() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.BETA_FIELD));
    }
    
    public double getHeading() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.HEADING_FIELD));
    }
    
    public double getHeadingMag() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.HEADING_MAG_FIELD));
    }
    
    public double getPitch() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.PITCH_FIELD));
    }
    
    public double getRoll() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.ROLL_FIELD));
    }
    
    public double getTrack() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.TRACK_MAG_FIELD));
    }
    
    public double getYaw() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.YAW_FIELD));
    }
    
    public double getYawRate() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.YAW_RATE_FIELD));
    }

    ///////////////////
    //position
    
    public double getAltitude() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.ALTITUDE_FIELD));
    }
    
    public double getGroundElevation() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.GROUND_ELEVATION_FIELD));
    }
    
    public double getLatitude() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.LATITUDE_FIELD));
    }
    
    public double getLongitude() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.LONGITUDE_FIELD));
    }
    
    ///////////////////
    //sim
    
    public abstract void setParkingBrake(boolean brakeEnabled) throws IOException;
    
    public int getSimFreezeClock() {
        return Character.getNumericValue(getTelemetryField(FlightGearFields.SIM_FREEZE_CLOCK_FIELD).charAt(0));
    }
    
    public int getSimFreezeMaster() {
        return Character.getNumericValue(getTelemetryField(FlightGearFields.SIM_FREEZE_MASTER_FIELD).charAt(0));
    }
    
    public double getSimSpeedUp() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.SIM_SPEEDUP_FIELD));
    }
    
    public double getTimeElapsed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.SIM_TIME_ELAPSED_FIELD));
    }
    
    public double getLocalDaySeconds() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.SIM_LOCAL_DAY_SECONDS_FIELD));
    }
    
    public double getMpClock() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.SIM_MP_CLOCK_FIELD));
    }
    
    ///////////////////
    //velocities
    public double getAirSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.AIRSPEED_FIELD));
    }
    
    public double getGroundSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.GROUNDSPEED_FIELD));
    }
    
    public double getVerticalSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.VERTICALSPEED_FIELD));
    }
    
    public double getUBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.U_BODY_FIELD));
    }
    
    public double getVBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.V_BODY_FIELD));
    }
    
    public double getWBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.W_BODY_FIELD));
    }
}
