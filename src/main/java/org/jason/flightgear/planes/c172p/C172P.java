package org.jason.flightgear.planes.c172p;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.FlightGearPlane;
import org.jason.flightgear.sockets.FlightGearManagerSockets;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P extends FlightGearPlane{
    
    private Logger logger = LoggerFactory.getLogger(C172P.class);
    
    //TODO: read from config file
    private final static String FG_SOCKETS_HOST = "localhost";
    private final static int FG_SOCKETS_TELEM_PORT = 6501;
    
    private final static String FG_TELNET_HOST = "localhost";
    private final static int FG_TELNET_PORT = 5501;
    
    private final static int SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    private final static int SOCKETS_INPUT_CONTROLS_PORT = 6602;
    private final static int SOCKETS_INPUT_ORIENTATION_PORT = 6603;
    private final static int SOCKETS_INPUT_POSITION_PORT = 6604;
    private final static int SOCKETS_INPUT_SIM_PORT = 6605;
    private final static int SOCKETS_INPUT_SIM_FREEZE_PORT = 6606;
    private final static int SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6607;
    private final static int SOCKETS_INPUT_VELOCITIES_PORT = 6608;
    private final static int SOCKETS_INPUT_FDM_PORT = 6609;
    
    private final static int TELEMETRY_READ_SLEEP = 250;
    private final static int AUTOSTART_COMPLETION_SLEEP = 5000;
    
    private FlightGearManagerTelnet fgTelnet;
    private FlightGearManagerSockets fgSockets;
    
    private boolean runTelemetryThread;
    
    private Thread telemetryThread;
    
    private Map<String, String> currentState;
    
    //writing telemetry
    private AtomicBoolean stateWriting;
    
    //reading telemetry from socket
    private AtomicBoolean stateReading;
             
    public C172P() throws InvalidTelnetOptionException, IOException {
        logger.info("Loading C172P...");
        
        fgTelnet = new FlightGearManagerTelnet(FG_TELNET_HOST, FG_TELNET_PORT);
        fgSockets = new FlightGearManagerSockets(FG_SOCKETS_HOST, FG_SOCKETS_TELEM_PORT);
        
        currentState = Collections.synchronizedMap(new LinkedHashMap<String, String>());
        
        stateWriting = new AtomicBoolean(false);
        stateReading = new AtomicBoolean(false);
        
        setup();
    }
    
    private LinkedHashMap<String, String> copyStateFields(String[] fields) {
        LinkedHashMap<String, String> retval = new LinkedHashMap<>();
        
        for(String field : fields) {
            if(currentState.containsKey(field)) {
                retval.put(field, currentState.get(field));
            }
            else
            {
                logger.warn("Current state missing field: " + field);
            }
        }
        
        return retval;
    }
    
    private void setup() {
        logger.info("setup called");
        
        //TODO: check that any dynamic config reads result in all control input ports being defined
        
        //TODO: consider a separate function so this can be started/restarted externally
        //launch thread to update telemetry
        runTelemetryThread = true;
        
        telemetryThread = new Thread() {
            @Override
            public void run() {
                logger.trace("Telemetry thread started");
                
                readTelemetry();
                
                logger.trace("Telemetry thread returning");
            }
        };
        telemetryThread.start();
        
        logger.info("setup returning");
    }
    
    //internal telemetry retrieval thread
    private void readTelemetry() {
        
        //TODO: enable pause on sim freeze
        while(runTelemetryThread) {
            
            //wait for any state read operations to finish
            while(stateReading.get()) {
                try {
                    logger.trace("Waiting for state reading to complete");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.warn("Polling state read sleep interrupted", e);
                }
            }
            
            stateWriting.set(true);
            //read from socket connection. retrieves json string. write state to map
            //TODO: make this not awful
            
            //TODO: init outside the loop
            String telemetryRead = ""; 
            try {
                telemetryRead = fgSockets.readTelemetry();
                
                //if for some reason telemetryRead is not proper json, the update is dropped
                //TODO: move init outside the loop
                JSONObject jsonTelemetry = new JSONObject(telemetryRead);
                
                jsonTelemetry.keySet().forEach( 
                    keyStr -> {
                        currentState.put(keyStr, jsonTelemetry.get(keyStr).toString());
                    }    
                );
            } catch (JSONException jsonException) {
                logger.error("JSON Error parsing telemetry. Received:\n" + telemetryRead + "\n===", jsonException);
            } catch (IOException ioException) {
                logger.error("IOException parsing telemetry. Received:\n" + telemetryRead + "\n===", ioException);
            }
            finally {
                stateWriting.set(false);
                
                //sleep before next update. successful or not
                try {
                    Thread.sleep(TELEMETRY_READ_SLEEP);
                } catch (InterruptedException e) {
                    logger.warn("Trailing state read sleep interrupted", e);
                }
            }
        }
        
        logger.info("readTelemetry returning");
    }
    
    public synchronized Map<String, String> getTelemetry() {
        Map<String, String> retval = new HashMap<>();
        
        while(stateWriting.get()) {
            try {
                logger.debug("Waiting for state writing to complete");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        stateReading.set(true);
        retval.putAll(currentState);
        stateReading.set(false);
        
        return retval;
    }
    
    public void startupPlane() throws Exception {
        
        logger.info("Starting up the plane");
        
        //may not need to wait on state read/write
        
        //nasal script to autostart from c172p menu
        fgTelnet.runNasal("c172p.autostart();");
        
        logger.info("Startup nasal script executed. Sleeping for completion.");
        
        //startup may be asynchronous so we have to wait for the next prompt 
        try {
            Thread.sleep(AUTOSTART_COMPLETION_SLEEP);
        } catch (InterruptedException e) {
            logger.warn("Startup wait interrupted", e);
        }

        //TODO: verify from telemetry read engines are running
        //from currentstate
        
        
        logger.info("Startup completed");
    }
    
    /**
     * 
     * public so that high-level input (advanced maneuvers) can be written in one update externally
     * 
     * @param inputHash
     * @param port
     */
    public synchronized void writeSocketInput(LinkedHashMap<String, String> inputHash, int port) {
//        while(stateReading.get()) {
//            try {
//                logger.debug("Waiting for state reading to complete");
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                logger.warn("writeSocketInput: Socket write wait interrupted", e);
//            }
//        }
    	
    	
    	//TODO: check if paused, and pause if not
    	//expect pause before and after invocation of this
    	//
        
    	//TODO: wait for stateread to end
    	
        //TODO: lock on write
        fgSockets.writeInput(inputHash, port);
        
        //TODO: unpause if pause issued in this function

    }
    
    //////////////
    //telemetry accessors
    
    ///////////////////
    //consumables
    
    public double getCapacity_gal_us() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FUEL_TANK_CAPACITY_FIELD));
    }
    
    public double getLevel_gal_us() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FUEL_TANK_LEVEL_FIELD));
    }
    
    public double getWaterContamination() {
        return Double.parseDouble(getTelemetry().get(C172PFields.WATER_CONTAMINATION_FIELD));
    }
    
    ///////////////////
    //controls
    
    public int getBatterySwitch() {
        return Character.getNumericValue( getTelemetry().get(C172PFields.BATTERY_SWITCH_FIELD).charAt(0));
    }
    
    public boolean isBatterySwitchEnabled() {
    	return getBatterySwitch() == C172PFields.BATTERY_SWITCH_INT_TRUE;
    }
    
    public double getMixture() {
        return Double.parseDouble(getTelemetry().get(C172PFields.MIXTURE_FIELD));
    }
    
    public double getThrottle() {
        return Double.parseDouble(getTelemetry().get(C172PFields.THROTTLE_FIELD));
    }
    
    public double getAileron() {
        return Double.parseDouble(getTelemetry().get(C172PFields.AILERON_FIELD));
    }
    
    public double getAutoCoordination() {
        return Double.parseDouble(getTelemetry().get(C172PFields.AUTO_COORDINATION_FIELD));
    }
    
    public double getAutoCoordinationFactor() {
        return Double.parseDouble(getTelemetry().get(C172PFields.AUTO_COORDINATION_FACTOR_FIELD));
    }
    
    public double getElevator() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ELEVATOR_FIELD));
    }
    
    public double getFlaps() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FLAPS_FIELD));
    }
    
    public double getRudder() {
        return Double.parseDouble(getTelemetry().get(C172PFields.RUDDER_FIELD));
    }
    
    public double getSpeedbrake() {
        return Double.parseDouble(getTelemetry().get(C172PFields.SPEED_BRAKE_FIELD));
    }
    
    public int getParkingBrakeEnabled() {
    	//TODO: this and other fields can be missing if the protocol files are incorrect- safeguard against.
    	
    	//returned as a double like 0.000000, just look at the first character
        return Character.getNumericValue( getTelemetry().get(C172PFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public boolean isParkingBrakeEnabled() {
        return getParkingBrakeEnabled() == C172PFields.SIM_PARKING_BRAKE_INT_TRUE;
    }
    
    public int getGearDown() {
        return Character.getNumericValue( getTelemetry().get(C172PFields.GEAR_DOWN_FIELD).charAt(0));
    }
    
    public boolean isGearDown() {
    	return getGearDown() == C172PFields.GEAR_DOWN_INT_TRUE;
    }
    
    ///////////////////
    //engine
    
    public double getCowlingAirTemperature() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_COWLING_AIR_TEMPERATURE_FIELD));
    }

    public double getExhaustGasTemperature() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_EXHAUST_GAS_TEMPERATURE_FIELD));
    }
    
    public double getExhaustGasTemperatureNormalization() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_EXHAUST_GAS_TEMPERATURE_NORM_FIELD));
    }
    
    public double getFuelFlow() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_FUEL_FLOW_FIELD));
    }
    
    public double getMpOsi() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_MP_OSI_FIELD));
    }
    
    public double getOilPressure() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_OIL_PRESSURE_FIELD));
    }
    
    public double getOilTemperature() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_OIL_TEMPERATURE_FIELD));
    }
    
    public double getEngineRpms() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ENGINE_RPM_FIELD));
    }
    
    public int getEngineRunning() {
        return Character.getNumericValue( getTelemetry().get(C172PFields.ENGINE_RUNNING_FIELD).charAt(0));
    }
    
    public boolean isEngineRunning() {
    	return getEngineRunning() == C172PFields.ENGINE_RUNNING_INT_TRUE;
    }
    
    ///////////////////
    //environment
    
    public double getDewpoint() {
        return Double.parseDouble(getTelemetry().get(C172PFields.DEWPOINT_FIELD));
    }
    
    public double getEffectiveVisibility() {
        return Double.parseDouble(getTelemetry().get(C172PFields.EFFECTIVE_VISIBILITY_FIELD));
    }
    
    public double getPressure() {
        return Double.parseDouble(getTelemetry().get(C172PFields.PRESSURE_FIELD));
    }
    
    public double getRelativeHumidity() {
        return Double.parseDouble(getTelemetry().get(C172PFields.RELATIVE_HUMIDITY_FIELD));
    }
    
    public double getTemperature() {
        return Double.parseDouble(getTelemetry().get(C172PFields.TEMPERATURE_FIELD));
    }
    
    public double getVisibility() {
        return Double.parseDouble(getTelemetry().get(C172PFields.VISIBILITY_FIELD));
    }
    
    public double getWindFromDown() {
        return Double.parseDouble(getTelemetry().get(C172PFields.WIND_FROM_DOWN_FIELD));
    }
    
    public double getWindFromEast() {
        return Double.parseDouble(getTelemetry().get(C172PFields.WIND_FROM_EAST_FIELD));
    }
    
    public double getWindFromNorth() {
        return Double.parseDouble(getTelemetry().get(C172PFields.WIND_FROM_NORTH_FIELD));
    }
    
    public double getWindspeed() {
        return Double.parseDouble(getTelemetry().get(C172PFields.WINDSPEED_FIELD));
    }
    
    ///////////////////
    //fdm
    
    public int getDamageRepairing() {
        return Character.getNumericValue( getTelemetry().get(C172PFields.FDM_DAMAGE_REPAIRING_FIELD).charAt(0));
    }
    
    public boolean isDamageRepairing() {
    	return getDamageRepairing() == C172PFields.FDM_DAMAGE_REPAIRING_INT_TRUE;
    }
    
    //fbx
    public double getFbxAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_AERO_FIELD));
    }
    
    public double getFbxExternalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_EXTERNAL_FIELD));
    }
    
    public double getFbxGearForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_GEAR_FIELD));
    }
    
    public double getFbxPropForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_PROP_FIELD));
    }
    
    public double getFbxTotalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_TOTAL_FIELD));
    }
    
    public double getFbxWeightForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBX_WEIGHT_FIELD));
    }
    
    //fby
    public double getFbyAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_AERO_FIELD));
    }
    
    public double getFbyExternalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_EXTERNAL_FIELD));
    }
    
    public double getFbyGearForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_GEAR_FIELD));
    }
    
    public double getFbyPropForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_PROP_FIELD));
    }
    
    public double getFbyTotalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_TOTAL_FIELD));
    }
    
    public double getFbyWeightForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBY_WEIGHT_FIELD));
    }
    
    //fbz
    public double getFbzAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_AERO_FIELD));
    }
    
    public double getFbzExternalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_EXTERNAL_FIELD));
    }
    
    public double getFbzGearForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_GEAR_FIELD));
    }
    
    public double getFbzPropForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_PROP_FIELD));
    }
    
    public double getFbzTotalForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_TOTAL_FIELD));
    }
    
    public double getFbzWeightForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FBZ_WEIGHT_FIELD));
    }
    
    //fsx
    public double getFsxAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FSX_AERO_FIELD));
    }
    
    //fsy
    public double getFsyAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FSY_AERO_FIELD));
    }
    
    //fsz
    public double getFszAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FSZ_AERO_FIELD));
    }
    
    //fwy
    public double getFwyAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FWY_AERO_FIELD));
    }
    
    //fwz
    public double getFwzAeroForce() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_FWZ_AERO_FIELD));
    }
    
    //load factor
    public double getLoadFactor() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_LOAD_FACTOR_FIELD));
    }
    
    public double getLodNorm() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_LOD_NORM_FIELD));
    }
    
    //damage
    
    public int getDamage() {
        return Character.getNumericValue(getTelemetry().get(C172PFields.FDM_DAMAGE_FIELD).charAt(0));
    }
    
    public boolean isDamageEnabled() {
    	return getDamage() == C172PFields.FDM_DAMAGE_ENABLED_INT_TRUE;
    }

    public double getLeftWingDamage() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_LEFT_WING_DAMAGE_FIELD));
    }
    
    public double getRightWingDamage() {
        return Double.parseDouble(getTelemetry().get(C172PFields.FDM_RIGHT_WING_DAMAGE_FIELD));
    }
    
    ///////////////////
    //orientation
    
    public double getAlpha() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.ALPHA_FIELD));
    }
    
    public double getBeta() {
        return Double.parseDouble(getTelemetry().get(C172PFields.BETA_FIELD));
    }
    
    public double getHeading() {
        return Double.parseDouble(getTelemetry().get(C172PFields.HEADING_FIELD));
    }
    
    public double getHeadingMag() {
        return Double.parseDouble(getTelemetry().get(C172PFields.HEADING_MAG_FIELD));
    }
    
    public double getPitch() {
        return Double.parseDouble(getTelemetry().get(C172PFields.PITCH_FIELD));
    }
    
    public double getRoll() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ROLL_FIELD));
    }
    
    public double getTrack() {
        return Double.parseDouble(getTelemetry().get(C172PFields.TRACK_MAG_FIELD));
    }
    
    public double getYaw() {
        return Double.parseDouble(getTelemetry().get(C172PFields.YAW_FIELD));
    }
    
    public double getYawRate() {
        return Double.parseDouble(getTelemetry().get(C172PFields.YAW_RATE_FIELD));
    }
    
    ///////////////////
    //position
    
    public double getAltitude() {
        return Double.parseDouble(getTelemetry().get(C172PFields.ALTITUDE_FIELD));
    }
    
    public double getGroundElevation() {
        return Double.parseDouble(getTelemetry().get(C172PFields.GROUND_ELEVATION_FIELD));
    }
    
    public double getLatitude() {
        return Double.parseDouble(getTelemetry().get(C172PFields.LATITUDE_FIELD));
    }
    
    public double getLongitude() {
        return Double.parseDouble(getTelemetry().get(C172PFields.LONGITUDE_FIELD));
    }
    
    ///////////////////
    //sim
    
    public int getParkingBrake() {   	
        return Character.getNumericValue(getTelemetry().get(C172PFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public int getSimFreezeClock() {
        return Character.getNumericValue(getTelemetry().get(C172PFields.SIM_FREEZE_CLOCK_FIELD).charAt(0));
    }
    
    public int getSimFreezeMaster() {
        return Character.getNumericValue(getTelemetry().get(C172PFields.SIM_FREEZE_MASTER_FIELD).charAt(0));
    }
    
    public double getSimSpeedUp() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.SIM_SPEEDUP_FIELD));
    }
    
    public double getTimeElapsed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.SIM_TIME_ELAPSED_FIELD));
    }
    
    public double getLocalDaySeconds() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.SIM_LOCAL_DAY_SECONDS_FIELD));
    }
    
    public double getMpClock() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.SIM_MP_CLOCK_FIELD));
    }
    
    ///////////////////
    //velocities
    public double getAirSpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.AIRSPEED_FIELD));
    }
    
    public double getGroundSpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.GROUNDSPEED_FIELD));
    }
    
    public double getVerticalSpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.VERTICALSPEED_FIELD));
    }
    
    public double getUBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.U_BODY_FIELD));
    }
    
    public double getVBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.V_BODY_FIELD));
    }
    
    public double getWBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(C172PFields.W_BODY_FIELD));
    }
    
    //////////////
    //telemetry modifiers
    
    public void forceStabilize(double heading, double altitude, double roll, double pitch, double yaw) {
        
        logger.info("forceStablize called");
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> orientationFields = copyStateFields(C172PFields.ORIENTATION_FIELDS);
        
        //get telemetry hash
        
        //TODO: String.valueOf for these and similar
        orientationFields.put(C172PFields.HEADING_FIELD, "" + heading);
        orientationFields.put(C172PFields.PITCH_FIELD, "" + pitch);
        orientationFields.put(C172PFields.ROLL_FIELD, "" + roll);
        
        writeSocketInput(orientationFields, SOCKETS_INPUT_ORIENTATION_PORT);
        
        //setAltitude(altitude);
        C172PFlightUtilities.altitudeCheck(this, 500, altitude);
    }
    
    public synchronized void refillFuelTank() {
    	setFuelTankLevel(getCapacity_gal_us());
    }
       
    public synchronized void setFuelTankLevel(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_FIELDS);
    	
        inputHash.put(C172PFields.FUEL_TANK_LEVEL_FIELD, "" + amount);
        
        logger.info("Setting fuel tank level: {}", amount);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    }
    
    public synchronized void setFuelTankWaterContamination(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_FIELDS);
    	
        inputHash.put(C172PFields.WATER_CONTAMINATION_FIELD, "" + amount);
        
        logger.info("Setting fuel tank water contamination: {}", amount);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    	
    }
    
    public synchronized void setBatterySwitch(boolean switchOn) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        if(switchOn) {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, String.valueOf(C172PFields.BATTERY_SWITCH_INT_TRUE));
        }
        else {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, String.valueOf(C172PFields.BATTERY_SWITCH_INT_FALSE));
        }
        
        logger.info("Setting battery switch to {}", switchOn);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setElevator(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.ELEVATOR_FIELD, String.valueOf(orientation));

        logger.info("Setting elevator to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAileron(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.AILERON_FIELD, String.valueOf(orientation));

        logger.info("Setting aileron to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setFlaps(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.FLAPS_FIELD, String.valueOf(orientation));

        logger.info("Setting flaps to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setRudder(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.RUDDER_FIELD, String.valueOf(orientation));

        logger.info("Setting rudder to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void resetControlSurfaces() {
    	
    	logger.info("Resetting control surfaces");
    	
    	setElevator(C172PFields.ELEVATOR_DEFAULT);
    	setAileron(C172PFields.AILERON_DEFAULT);
    	setFlaps(C172PFields.FLAPS_DEFAULT);
    	setRudder(C172PFields.RUDDER_DEFAULT);
    }
        
	public synchronized void setPause(boolean isPaused) {

		// TODO: check telemetry if already paused

		// resolve sim_freeze port
		// if(controlInputs.containsKey(PAUSE_INPUT)) {
		// FlightGearInput input = controlInputs.get(PAUSE_INPUT);

		LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.SIM_PAUSE_FIELDS);

		// oh get fucked. requires an int value for the bool, despite the schema
		// specifying a bool.
		// hardcode the string values because i don't want to have to deal with parse*
		// calls or casting here
		if (isPaused) {
			logger.info("Pausing simulation");
			inputHash.put(C172PFields.SIM_FREEZE_CLOCK_FIELD, C172PFields.SIM_FREEZE_TRUE);
			inputHash.put(C172PFields.SIM_FREEZE_MASTER_FIELD, C172PFields.SIM_FREEZE_TRUE);
		} else {
			logger.info("Unpausing simulation");
			inputHash.put(C172PFields.SIM_FREEZE_CLOCK_FIELD, C172PFields.SIM_FREEZE_FALSE);
			inputHash.put(C172PFields.SIM_FREEZE_MASTER_FIELD, C172PFields.SIM_FREEZE_FALSE);
		}

		// clock and master are the only two fields, no need to retrieve from the
		// current state
		// order matters. defined in input xml schema

		// socket writes typically require pauses so telemetry/state aren't out of date
		// however this is an exception
		writeSocketInput(inputHash, SOCKETS_INPUT_SIM_FREEZE_PORT);

		// trailing sleep, so that the last real telemetry read arrives
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			logger.warn("setPause trailing sleep interrupted", e);
		}
	}
    
    public synchronized void setSpeedUp(double speedup) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.SIM_SPEEDUP_FIELDS);
        
        logger.info("Setting speedup: {}", speedup);
        
        inputHash.put(C172PFields.SIM_SPEEDUP_FIELD, "" + speedup);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_SIM_SPEEDUP_PORT);
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) {
        LinkedHashMap<String, String> inputHash = new LinkedHashMap<String, String>();
        
        //requires an int value for the bool
        if(!damageEnabled) {
            inputHash.put(C172PFields.FDM_DAMAGE_FIELD, C172PFields.FDM_DAMAGE_ENABLED_FALSE);
        }
        else {
            inputHash.put(C172PFields.FDM_DAMAGE_FIELD, C172PFields.FDM_DAMAGE_ENABLED_TRUE);
        }
        
        logger.info("Toggling damage enabled: {}", damageEnabled);
        
        //socket writes typically require pauses so telemetry/state aren't out of date
        //however this is an exception
        writeSocketInput(inputHash, SOCKETS_INPUT_FDM_PORT);
    }
    
    /**
     * 
     * 
     * @param heading    Degrees from north, clockwise. 0=360 => North. 90 => East. 180 => South. 270 => West 
     */
    public synchronized void setHeading(double heading) {
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.ORIENTATION_FIELDS);
        
        //get telemetry hash
        
        inputHash.put(C172PFields.HEADING_FIELD, "" + heading);
        
        logger.info("Setting heading to {}", heading);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setPitch(double pitch) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.ORIENTATION_FIELDS);
        
        inputHash.put(C172PFields.PITCH_FIELD, "" + pitch);
        
        logger.info("Setting pitch to {}", pitch);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setRoll(double roll) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.ORIENTATION_FIELDS);
                
        inputHash.put(C172PFields.ROLL_FIELD, "" + roll);
        
        logger.info("Setting roll to {}", roll);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setAltitude(double altitude) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.POSITION_FIELDS);
        
        inputHash.put(C172PFields.ALTITUDE_FIELD, "" + altitude);
        
        logger.info("Setting altitude to {}", altitude);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }

//can't modify the yaw
//    public synchronized void setYaw(double yaw) {
//
//        //TODO: check if paused
//        
//        LinkedHashMap<String, String> inputHash = copyStateFields(ORIENTATION_FIELDS);
//                
//        inputHash.put("/orientation/yaw-deg", "" + yaw);
//        
//        logger.info("Setting yaw to {}", yaw);
//        
//        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
//    }
    
    public synchronized void setAirSpeed(double speed) {
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.VELOCITIES_FIELDS);
                
        inputHash.put(C172PFields.AIRSPEED_FIELD, "" + speed);
        
        logger.info("Setting air speed to {}", speed);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_VELOCITIES_PORT);
    }
    
    public synchronized void setThrottle(double throttle ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.THROTTLE_FIELD, "" + throttle);
        
        logger.info("Setting throttle to {}", throttle);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setMixture(double mixture ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.MIXTURE_FIELD, "" + mixture);
        
        logger.info("Setting mixture to {}", mixture);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setParkingBrake(boolean brakeEnabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.SIM_FIELDS);
            
        //visual: in the cockpit, if the brake arm is:
        //pushed in => brake is not engaged (disabled => 0)
        //pulled out => brake is engaged (enabled => 1)
        
        //requires an double value for the bool
        if(brakeEnabled) {
            inputHash.put(C172PFields.SIM_PARKING_BRAKE_FIELD, "1.0");
        }
        else {
            inputHash.put(C172PFields.SIM_PARKING_BRAKE_FIELD, "0.0");
        }
        
        logger.info("Setting parking brake to {}", brakeEnabled);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_SIM_PORT);
    }
    
///////////////
    
    public void shutdown() {
        
        logger.info("C172P Shutdown invoked");
        
        //stop telemetry read
        runTelemetryThread = false;
        
        //TODO: ensure thread exits soon
        
        int waitTime = 0;
        int interval = 250;
        int maxWait = 2000;
        while(telemetryThread.isAlive()) {
            logger.debug("waiting on telemetry thread to terminate");
            
            if(waitTime >= maxWait) {
                telemetryThread.interrupt();
            }
            else {
                waitTime += interval;
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    logger.warn("Telemetry thread wait interrupted", e);
                }
            }
        }
        
        logger.debug("Telemetry thread terminated");

        //no io resources in the socket manager to shutdown
        //sockets shutdown
//        if(fgSockets != null) {
//            fgSockets.shutdown();
//            
//            logger.debug("FlightGear sockets connection shut down");
//        }
//        else {
//            logger.warn("FlightGear sockets connection was null at shutdown");
//        }
        
        
        
        //FGM shutdown
        if(fgTelnet != null) {
            //end simulator - needs streams to write commands
            fgTelnet.exit();
            
            //disconnect streams
            fgTelnet.disconnect();
            
            logger.debug("FlightGear telnet connection shut down");
        } 
        else {
            logger.warn("FlightGear telnet connection was null at shutdown");
        }
        
        logger.info("C172P Shutdown completed");
    }
}
