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
    
    private final static int TELEMETRY_READ_WAIT_SLEEP = 100;
    private final static int TELEMETRY_READ_TRAILING_SLEEP = 250;
    private final static int TELEMETRY_WRITE_WAIT_SLEEP = 100;
    
    private boolean runTelemetryThread;
    
    private Thread telemetryThread;
    
    protected Map<String, String> currentState;
    
    //writing telemetry
    protected AtomicBoolean stateWriting;
    
    //reading telemetry from socket
    protected AtomicBoolean stateReading;
    
    public FlightGearPlane() {
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
            try {
                LOGGER.trace("Waiting for state writing to complete");
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
            try {
                LOGGER.trace("Waiting for state writing to complete");
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
        
        //TODO: enable pause on sim freeze
        String telemetryRead = null;
        while(runningTelemetryThread()) {
            
            //wait for any state read operations to finish
            //TODO: max wait on this
            while(stateReading.get()) {
                try {
                    LOGGER.trace("Waiting for state reading to complete");
                    Thread.sleep(TELEMETRY_READ_WAIT_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Polling state read sleep interrupted", e);
                }
            }
            
            stateWriting.set(true);
            //read from socket connection. retrieves json string. write state to map
            //TODO: make this not awful

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
            }
        }
        
        LOGGER.debug("readTelemetry returning");
    }
    
    protected abstract String readTelemetryRaw() throws IOException;
    
    protected abstract void writeControlInput(LinkedHashMap<String, String> inputHash, int port);
    
    //////////////////
    //expected FG property setters/getters
    //defined in plane subclass since socket io is managed there
    
    public abstract void setFuelTankLevel(double fuelTankCapacity);
    
    public abstract double getFuelLevel();

    /**
     * May not be generic, since planes may have multiple tanks, so the subclass handles this details. 
     */
    public abstract double getFuelTankCapacity();
    
    public abstract void setPause(boolean isPaused);

    public abstract void setAltitude(double targetAltitude);
    
    public abstract void setLatitude(double targetLatitude);
    
    public abstract void setLongitude(double targetLongitude);
    
    public abstract void setRoll(double targetRoll);
    
    public abstract void setHeading(double targetHeading);
    
    public abstract void setPitch(double targetPitch);

    public abstract void setSpeedUp(double targetSpeedup);
    
    public abstract void setAirSpeed(double targetSpeed);
    
    public abstract void setVerticalSpeed(double targetSpeed);
    
    public abstract boolean isEngineRunning();
    
    ///////////////////
    
    /**
     * Refill the fuel tanks to capacity
     */
    public synchronized void refillFuelTank() {
        setFuelTankLevel(getFuelTankCapacity());
    }
    
    ///////////////////
    //environment
    
    public double getDewpoint() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.DEWPOINT_FIELD));
    }
    
    public double getEffectiveVisibility() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.EFFECTIVE_VISIBILITY_FIELD));
    }
    
    public double getPressure() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.PRESSURE_FIELD));
    }
    
    public double getRelativeHumidity() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.RELATIVE_HUMIDITY_FIELD));
    }
    
    public double getTemperature() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.TEMPERATURE_FIELD));
    }
    
    public double getVisibility() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.VISIBILITY_FIELD));
    }
    
    public double getWindFromDown() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.WIND_FROM_DOWN_FIELD));
    }
    
    public double getWindFromEast() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.WIND_FROM_EAST_FIELD));
    }
    
    public double getWindFromNorth() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.WIND_FROM_NORTH_FIELD));
    }
    
    public double getWindspeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.WINDSPEED_FIELD));
    }
    
    ///////////////////
    //fdm
    
    public int getDamageRepairing() {
        return Character.getNumericValue( getTelemetryField(FlightGearPlaneFields.FDM_DAMAGE_REPAIRING_FIELD).charAt(0));
    }
    
    public boolean isDamageRepairing() {
        return getDamageRepairing() == FlightGearPlaneFields.FDM_DAMAGE_REPAIRING_INT_TRUE;
    }
    
    //fbx
    public double getFbxAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_AERO_FIELD));
    }
    
    public double getFbxExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_EXTERNAL_FIELD));
    }
    
    public double getFbxGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_GEAR_FIELD));
    }
    
    public double getFbxPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_PROP_FIELD));
    }
    
    public double getFbxTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_TOTAL_FIELD));
    }
    
    public double getFbxWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBX_WEIGHT_FIELD));
    }
    
    //fby
    public double getFbyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_AERO_FIELD));
    }
    
    public double getFbyExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_EXTERNAL_FIELD));
    }
    
    public double getFbyGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_GEAR_FIELD));
    }
    
    public double getFbyPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_PROP_FIELD));
    }
    
    public double getFbyTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_TOTAL_FIELD));
    }
    
    public double getFbyWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBY_WEIGHT_FIELD));
    }
    
    //fbz
    public double getFbzAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_AERO_FIELD));
    }
    
    public double getFbzExternalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_EXTERNAL_FIELD));
    }
    
    public double getFbzGearForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_GEAR_FIELD));
    }
    
    public double getFbzPropForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_PROP_FIELD));
    }
    
    public double getFbzTotalForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_TOTAL_FIELD));
    }
    
    public double getFbzWeightForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FBZ_WEIGHT_FIELD));
    }
    
    //fsx
    public double getFsxAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FSX_AERO_FIELD));
    }
    
    //fsy
    public double getFsyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FSY_AERO_FIELD));
    }
    
    //fsz
    public double getFszAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FSZ_AERO_FIELD));
    }
    
    //fwy
    public double getFwyAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FWY_AERO_FIELD));
    }
    
    //fwz
    public double getFwzAeroForce() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_FWZ_AERO_FIELD));
    }
    
    //load factor
    public double getLoadFactor() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_LOAD_FACTOR_FIELD));
    }
    
    public double getLodNorm() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_LOD_NORM_FIELD));
    }
    
    //damage
    
    public int getDamage() {
        return Character.getNumericValue(getTelemetryField(FlightGearPlaneFields.FDM_DAMAGE_FIELD).charAt(0));
    }
    
    public boolean isDamageEnabled() {
        return getDamage() == FlightGearPlaneFields.FDM_DAMAGE_ENABLED_INT_TRUE;
    }

    public double getLeftWingDamage() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_LEFT_WING_DAMAGE_FIELD));
    }
    
    public double getRightWingDamage() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.FDM_RIGHT_WING_DAMAGE_FIELD));
    }
    
    ///////////////////
    //orientation
    
    public double getAlpha() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.ALPHA_FIELD));
    }
    
    public double getBeta() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.BETA_FIELD));
    }
    
    public double getHeading() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.HEADING_FIELD));
    }
    
    public double getHeadingMag() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.HEADING_MAG_FIELD));
    }
    
    public double getPitch() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.PITCH_FIELD));
    }
    
    public double getRoll() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.ROLL_FIELD));
    }
    
    public double getTrack() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.TRACK_MAG_FIELD));
    }
    
    public double getYaw() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.YAW_FIELD));
    }
    
    public double getYawRate() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.YAW_RATE_FIELD));
    }

    ///////////////////
    //position
    
    public double getAltitude() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.ALTITUDE_FIELD));
    }
    
    public double getGroundElevation() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.GROUND_ELEVATION_FIELD));
    }
    
    public double getLatitude() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.LATITUDE_FIELD));
    }
    
    public double getLongitude() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.LONGITUDE_FIELD));
    }
    
    ///////////////////
    //sim
    
    public int getSimFreezeClock() {
        return Character.getNumericValue(getTelemetryField(FlightGearPlaneFields.SIM_FREEZE_CLOCK_FIELD).charAt(0));
    }
    
    public int getSimFreezeMaster() {
        return Character.getNumericValue(getTelemetryField(FlightGearPlaneFields.SIM_FREEZE_MASTER_FIELD).charAt(0));
    }
    
    public double getSimSpeedUp() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.SIM_SPEEDUP_FIELD));
    }
    
    public double getTimeElapsed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.SIM_TIME_ELAPSED_FIELD));
    }
    
    public double getLocalDaySeconds() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.SIM_LOCAL_DAY_SECONDS_FIELD));
    }
    
    public double getMpClock() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.SIM_MP_CLOCK_FIELD));
    }
    
    ///////////////////
    //velocities
    public double getAirSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.AIRSPEED_FIELD));
    }
    
    public double getGroundSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.GROUNDSPEED_FIELD));
    }
    
    public double getVerticalSpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.VERTICALSPEED_FIELD));
    }
    
    public double getUBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.U_BODY_FIELD));
    }
    
    public double getVBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.V_BODY_FIELD));
    }
    
    public double getWBodySpeed() {
        return Double.parseDouble(getTelemetryField(FlightGearPlaneFields.W_BODY_FIELD));
    }
}
