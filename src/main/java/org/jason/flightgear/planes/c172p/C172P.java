package org.jason.flightgear.planes.c172p;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.FlightGearPlane;
import org.jason.flightgear.planes.FlightGearPlaneFields;
import org.jason.flightgear.sockets.FlightGearManagerSockets;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P extends FlightGearPlane{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(C172P.class);
    
    //TODO: read from config file
    private final static int SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    private final static int SOCKETS_INPUT_CONTROLS_PORT = 6602;
    private final static int SOCKETS_INPUT_FDM_PORT = 6603;
    private final static int SOCKETS_INPUT_ORIENTATION_PORT = 6604;
    private final static int SOCKETS_INPUT_POSITION_PORT = 6605;
    private final static int SOCKETS_INPUT_SIM_PORT = 6606;
    private final static int SOCKETS_INPUT_SIM_FREEZE_PORT = 6607;
    private final static int SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6608;
    private final static int SOCKETS_INPUT_VELOCITIES_PORT = 6609;

    private final static int AUTOSTART_COMPLETION_SLEEP = 5000;
    private final static int POST_PAUSE_SLEEP = 250;
    
    private FlightGearManagerTelnet fgTelnet;
    private FlightGearManagerSockets fgSockets;
               
    public C172P() throws FlightGearSetupException {
    	this(new C172PConfig());
    }
    
    public C172P(C172PConfig config) throws FlightGearSetupException  {
    	super();
    	
        LOGGER.info("Loading C172P...");
        
        //setup the socket and telnet connections. start the telemetry retrieval thread.
        setup(config);
        
        //TODO: implement. possibly add to superclass
        launchSimulator();
                
        LOGGER.info("C172P setup completed");
    }
    
    private void launchSimulator() {
    	//run script, wait for telemetry port and first read
    }
    
    private void setup(C172PConfig config) throws FlightGearSetupException {
        LOGGER.info("setup called");
        
        //TODO: check that any dynamic config reads result in all control input ports being defined
        
        //TODO: consider a separate function so this can be started/restarted externally
        //launch thread to update telemetry

        try {
			fgSockets = new FlightGearManagerSockets(config.getSocketsHostname(), config.getSocketsPort());
			
	        //launch this after the fgsockets connection is initialized, because the telemetry reads depends on this
			//
	        launchTelemetryThread();
			
			fgTelnet = new FlightGearManagerTelnet(config.getTelnetHostname(), config.getTelnetPort());

		} catch (SocketException | UnknownHostException | InvalidTelnetOptionException e) {
			
			LOGGER.error("Exception occurred during setup", e);
			
			throw new FlightGearSetupException(e);
		} catch (IOException e) {
			
			LOGGER.error("IOException occurred during setup", e);
			
			throw new FlightGearSetupException(e);
		}
        
        LOGGER.info("setup returning");
    }
    
    public void startupPlane() throws Exception {
        
        LOGGER.info("Starting up the plane");
        
        //may not need to wait on state read/write
        
        //nasal script to autostart from c172p menu
        fgTelnet.runNasal("c172p.autostart();");
        
        LOGGER.info("Startup nasal script executed. Sleeping for completion.");
        
        //startup may be asynchronous so we have to wait for the next prompt 
        try {
            Thread.sleep(AUTOSTART_COMPLETION_SLEEP);
        } catch (InterruptedException e) {
            LOGGER.warn("Startup wait interrupted", e);
        }
        
        //disconnect the telnet connection because we only use it to run the nasal script
        //to start the plane. it's not used again.
        fgTelnet.disconnect();

        //TODO: verify from telemetry read engines are running
        //from currentstate
        
        LOGGER.info("Startup completed");
    }
    
    /**
     * public so that high-level input (advanced maneuvers) can be written in one update externally
     * 
     * @param inputHash
     * @param port
     */
    @Override
    protected synchronized void writeSocketInput(LinkedHashMap<String, String> inputHash, int port) {
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
    
    public int getAutoCoordination() {
        return Integer.parseInt(getTelemetry().get(C172PFields.AUTO_COORDINATION_FIELD));
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
    
    public int getParkingBrake() {   	
        return Character.getNumericValue(getTelemetry().get(C172PFields.PARKING_BRAKE_FIELD).charAt(0));
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
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.DEWPOINT_FIELD));
    }
    
    public double getEffectiveVisibility() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.EFFECTIVE_VISIBILITY_FIELD));
    }
    
    public double getPressure() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.PRESSURE_FIELD));
    }
    
    public double getRelativeHumidity() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.RELATIVE_HUMIDITY_FIELD));
    }
    
    public double getTemperature() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.TEMPERATURE_FIELD));
    }
    
    public double getVisibility() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.VISIBILITY_FIELD));
    }
    
    public double getWindFromDown() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.WIND_FROM_DOWN_FIELD));
    }
    
    public double getWindFromEast() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.WIND_FROM_EAST_FIELD));
    }
    
    public double getWindFromNorth() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.WIND_FROM_NORTH_FIELD));
    }
    
    public double getWindspeed() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.WINDSPEED_FIELD));
    }
    
    ///////////////////
    //fdm
    
    public int getDamageRepairing() {
        return Character.getNumericValue( getTelemetry().get(FlightGearPlaneFields.FDM_DAMAGE_REPAIRING_FIELD).charAt(0));
    }
    
    public boolean isDamageRepairing() {
    	return getDamageRepairing() == FlightGearPlaneFields.FDM_DAMAGE_REPAIRING_INT_TRUE;
    }
    
    //fbx
    public double getFbxAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_AERO_FIELD));
    }
    
    public double getFbxExternalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_EXTERNAL_FIELD));
    }
    
    public double getFbxGearForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_GEAR_FIELD));
    }
    
    public double getFbxPropForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_PROP_FIELD));
    }
    
    public double getFbxTotalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_TOTAL_FIELD));
    }
    
    public double getFbxWeightForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBX_WEIGHT_FIELD));
    }
    
    //fby
    public double getFbyAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_AERO_FIELD));
    }
    
    public double getFbyExternalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_EXTERNAL_FIELD));
    }
    
    public double getFbyGearForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_GEAR_FIELD));
    }
    
    public double getFbyPropForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_PROP_FIELD));
    }
    
    public double getFbyTotalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_TOTAL_FIELD));
    }
    
    public double getFbyWeightForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBY_WEIGHT_FIELD));
    }
    
    //fbz
    public double getFbzAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_AERO_FIELD));
    }
    
    public double getFbzExternalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_EXTERNAL_FIELD));
    }
    
    public double getFbzGearForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_GEAR_FIELD));
    }
    
    public double getFbzPropForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_PROP_FIELD));
    }
    
    public double getFbzTotalForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_TOTAL_FIELD));
    }
    
    public double getFbzWeightForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FBZ_WEIGHT_FIELD));
    }
    
    //fsx
    public double getFsxAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FSX_AERO_FIELD));
    }
    
    //fsy
    public double getFsyAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FSY_AERO_FIELD));
    }
    
    //fsz
    public double getFszAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FSZ_AERO_FIELD));
    }
    
    //fwy
    public double getFwyAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FWY_AERO_FIELD));
    }
    
    //fwz
    public double getFwzAeroForce() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_FWZ_AERO_FIELD));
    }
    
    //load factor
    public double getLoadFactor() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_LOAD_FACTOR_FIELD));
    }
    
    public double getLodNorm() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_LOD_NORM_FIELD));
    }
    
    //damage
    
    public int getDamage() {
        return Character.getNumericValue(getTelemetry().get(FlightGearPlaneFields.FDM_DAMAGE_FIELD).charAt(0));
    }
    
    public boolean isDamageEnabled() {
    	return getDamage() == FlightGearPlaneFields.FDM_DAMAGE_ENABLED_INT_TRUE;
    }

    public double getLeftWingDamage() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_LEFT_WING_DAMAGE_FIELD));
    }
    
    public double getRightWingDamage() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.FDM_RIGHT_WING_DAMAGE_FIELD));
    }
    
    ///////////////////
    //orientation
    
    public double getAlpha() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.ALPHA_FIELD));
    }
    
    public double getBeta() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.BETA_FIELD));
    }
    
    public double getHeading() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.HEADING_FIELD));
    }
    
    public double getHeadingMag() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.HEADING_MAG_FIELD));
    }
    
    public double getPitch() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.PITCH_FIELD));
    }
    
    public double getRoll() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.ROLL_FIELD));
    }
    
    public double getTrack() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.TRACK_MAG_FIELD));
    }
    
    public double getYaw() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.YAW_FIELD));
    }
    
    public double getYawRate() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.YAW_RATE_FIELD));
    }
    
    ///////////////////
    //position
    
    public double getAltitude() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.ALTITUDE_FIELD));
    }
    
    public double getGroundElevation() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.GROUND_ELEVATION_FIELD));
    }
    
    public double getLatitude() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.LATITUDE_FIELD));
    }
    
    public double getLongitude() {
        return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.LONGITUDE_FIELD));
    }
    
    ///////////////////
    //sim
    
    public int getSimFreezeClock() {
        return Character.getNumericValue(getTelemetry().get(FlightGearPlaneFields.SIM_FREEZE_CLOCK_FIELD).charAt(0));
    }
    
    public int getSimFreezeMaster() {
        return Character.getNumericValue(getTelemetry().get(FlightGearPlaneFields.SIM_FREEZE_MASTER_FIELD).charAt(0));
    }
    
    public int getSimParkingBrake() {   	
        return Character.getNumericValue(getTelemetry().get(C172PFields.SIM_PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public double getSimSpeedUp() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.SIM_SPEEDUP_FIELD));
    }
    
    public double getTimeElapsed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.SIM_TIME_ELAPSED_FIELD));
    }
    
    public double getLocalDaySeconds() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.SIM_LOCAL_DAY_SECONDS_FIELD));
    }
    
    public double getMpClock() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.SIM_MP_CLOCK_FIELD));
    }
    
    ///////////////////
    //velocities
    public double getAirSpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.AIRSPEED_FIELD));
    }
    
    public double getGroundSpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.GROUNDSPEED_FIELD));
    }
    
    public double getVerticalSpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.VERTICALSPEED_FIELD));
    }
    
    public double getUBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.U_BODY_FIELD));
    }
    
    public double getVBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.V_BODY_FIELD));
    }
    
    public double getWBodySpeed() {
    	return Double.parseDouble(getTelemetry().get(FlightGearPlaneFields.W_BODY_FIELD));
    }
    
    //////////////
    //telemetry modifiers
    
    public void forceStabilize(double heading, double altitude, double roll, double pitch) {
        
        LOGGER.info("forceStablize called");
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> orientationFields = copyStateFields(FlightGearPlaneFields.ORIENTATION_FIELDS);
        
        setPause(true);
        orientationFields.put(FlightGearPlaneFields.HEADING_FIELD, String.valueOf(heading) ) ;
        orientationFields.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(pitch) );
        orientationFields.put(FlightGearPlaneFields.ROLL_FIELD, String.valueOf(roll) );
        orientationFields.put(FlightGearPlaneFields.ALTITUDE_FIELD, String.valueOf(altitude) );

        writeSocketInput(orientationFields, SOCKETS_INPUT_ORIENTATION_PORT);

        setPause(false);
    }
    
    @Override
    public synchronized void setFuelTankLevel(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_FIELDS);
    	
        inputHash.put(C172PFields.FUEL_TANK_LEVEL_FIELD, "" + amount);
        
        LOGGER.info("Setting fuel tank level: {}", amount);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    }
    
    public synchronized void setFuelTankWaterContamination(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_FIELDS);
    	
        inputHash.put(C172PFields.WATER_CONTAMINATION_FIELD, "" + amount);
        
        LOGGER.info("Setting fuel tank water contamination: {}", amount);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    	
    }
    
    public synchronized void setBatterySwitch(boolean switchOn) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        if(switchOn) {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_TRUE);
        }
        else {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_FALSE);
        }
        
        LOGGER.info("Setting battery switch to {}", switchOn);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setElevator(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.ELEVATOR_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAileron(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.AILERON_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAutoCoordination(boolean enabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        if(enabled) {
        	inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_TRUE);
        }
        else {
        	inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_FALSE);
        }

        LOGGER.info("Setting autocoordination to {}", enabled);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setFlaps(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.FLAPS_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting flaps to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setRudder(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.RUDDER_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void resetControlSurfaces() {
    	
    	LOGGER.info("Resetting control surfaces");
    	
    	setElevator(C172PFields.ELEVATOR_DEFAULT);
    	setAileron(C172PFields.AILERON_DEFAULT);
    	setFlaps(C172PFields.FLAPS_DEFAULT);
    	setRudder(C172PFields.RUDDER_DEFAULT);
    	
    	LOGGER.info("Reset of control surfaces completed");
    }
        
	public synchronized void setPause(boolean isPaused) {

		// TODO: check telemetry if already paused

		// resolve sim_freeze port
		// if(controlInputs.containsKey(PAUSE_INPUT)) {
		// FlightGearInput input = controlInputs.get(PAUSE_INPUT);

		LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.SIM_PAUSE_FIELDS);

		// oh get fucked. requires an int value for the bool, despite the schema specifying a bool.
		if (isPaused) {
			LOGGER.info("Pausing simulation");
			inputHash.put(FlightGearPlaneFields.SIM_FREEZE_CLOCK_FIELD, FlightGearPlaneFields.SIM_FREEZE_TRUE);
			inputHash.put(FlightGearPlaneFields.SIM_FREEZE_MASTER_FIELD, FlightGearPlaneFields.SIM_FREEZE_TRUE);
		} else {
			LOGGER.info("Unpausing simulation");
			inputHash.put(FlightGearPlaneFields.SIM_FREEZE_CLOCK_FIELD, FlightGearPlaneFields.SIM_FREEZE_FALSE);
			inputHash.put(FlightGearPlaneFields.SIM_FREEZE_MASTER_FIELD, FlightGearPlaneFields.SIM_FREEZE_FALSE);
		}

		// clock and master are the only two fields, no need to retrieve from the
		// current state
		// order matters. defined in input xml schema

		// socket writes typically require pauses so telemetry/state aren't out of date
		// however this is an exception
		writeSocketInput(inputHash, SOCKETS_INPUT_SIM_FREEZE_PORT);

		// trailing sleep, so that the last real telemetry read arrives
		try {
			Thread.sleep(POST_PAUSE_SLEEP);
		} catch (InterruptedException e) {
			LOGGER.warn("setPause trailing sleep interrupted", e);
		}
	}
    
    @Override
    public synchronized void setSpeedUp(double targetSpeedup) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.SIM_SPEEDUP_FIELDS);
        
        LOGGER.info("Setting speedup: {}", targetSpeedup);
        
        inputHash.put(FlightGearPlaneFields.SIM_SPEEDUP_FIELD, String.valueOf(targetSpeedup));
        
        writeSocketInput(inputHash, SOCKETS_INPUT_SIM_SPEEDUP_PORT);
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) {
        LinkedHashMap<String, String> inputHash = new LinkedHashMap<String, String>();
        
        //requires an int value for the bool
        if(!damageEnabled) {
            inputHash.put(FlightGearPlaneFields.FDM_DAMAGE_FIELD, FlightGearPlaneFields.FDM_DAMAGE_ENABLED_FALSE);
        }
        else {
            inputHash.put(FlightGearPlaneFields.FDM_DAMAGE_FIELD, FlightGearPlaneFields.FDM_DAMAGE_ENABLED_TRUE);
        }
        
        LOGGER.info("Toggling damage enabled: {}", damageEnabled);
        
        //socket writes typically require pauses so telemetry/state aren't out of date
        //however this is an exception
        writeSocketInput(inputHash, SOCKETS_INPUT_FDM_PORT);
    }
    
    /**
     * 
     * 
     * @param heading    Degrees from north, clockwise. 0=360 => North. 90 => East. 180 => South. 270 => West 
     */
    @Override
    public synchronized void setHeading(double heading) {
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_FIELDS);
        
        //get telemetry hash
        
        inputHash.put(FlightGearPlaneFields.HEADING_FIELD, String.valueOf(heading));
        
        LOGGER.info("Setting heading to {}", heading);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    @Override
    public synchronized void setPitch(double targetPitch) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(targetPitch));
        
        LOGGER.info("Setting pitch to {}", targetPitch);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    @Override
    public synchronized void setRoll(double targetRoll) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.ROLL_FIELD, String.valueOf(targetRoll));
        
        LOGGER.info("Setting roll to {}", targetRoll);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    @Override
    public synchronized void setAltitude(double targetAltitude) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.ALTITUDE_FIELD, String.valueOf(targetAltitude));
        
        LOGGER.info("Setting altitude to {}", targetAltitude);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }
    
    public synchronized void setAirSpeed(double speed) {
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.VELOCITIES_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.AIRSPEED_FIELD, String.valueOf(speed));
        
        LOGGER.info("Setting air speed to {}", speed);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_VELOCITIES_PORT);
    }
    
    public synchronized void setThrottle(double throttle ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.THROTTLE_FIELD, "" + throttle);
        
        LOGGER.info("Setting throttle to {}", throttle);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setMixture(double mixture ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_FIELDS);
        
        inputHash.put(C172PFields.MIXTURE_FIELD, "" + mixture);
        
        LOGGER.info("Setting mixture to {}", mixture);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setParkingBrake(boolean brakeEnabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.SIM_FIELDS);
            
        //visual: in the cockpit, if the brake arm is:
        //pushed in => brake is not engaged (disabled => 0)
        //pulled out => brake is engaged (enabled => 1)
        
        //requires an double value for the bool
        if(brakeEnabled) {
            inputHash.put(C172PFields.SIM_PARKING_BRAKE_FIELD, C172PFields.SIM_PARKING_BRAKE_TRUE);
        }
        else {
            inputHash.put(C172PFields.SIM_PARKING_BRAKE_FIELD, C172PFields.SIM_PARKING_BRAKE_FALSE);
        }
        
        LOGGER.info("Setting parking brake to {}", brakeEnabled);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_SIM_PORT);
    }
    
///////////////
    
    public void shutdown() {
        
        LOGGER.info("C172P Shutdown invoked");
        
        //shuts down telemetry thread
        super.shutdown();
        
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
            
            LOGGER.debug("FlightGear telnet connection shut down");
        } 
        else {
            LOGGER.warn("FlightGear telnet connection was null at shutdown");
        }
        
        LOGGER.info("C172P Shutdown completed");
    }

	@Override
	protected String readTelemetryRaw() throws IOException {
		return fgSockets.readTelemetry();
		
	}

	@Override
	public synchronized double getFuelTankCapacity() {
		return this.getCapacity_gal_us();
	}

	@Override
	public synchronized double getFuelLevel() {
		return getLevel_gal_us();
		
	}
}
