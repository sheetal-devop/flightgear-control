package org.jason.flightgear.planes.c172p;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.connection.sockets.FlightGearSocketsConnection;
import org.jason.flightgear.connection.telnet.FlightGearTelnetConnection;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.FlightGearPlane;
import org.jason.flightgear.planes.FlightGearPlaneFields;
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
    private final static int SOCKET_WRITE_WAIT_SLEEP = 100;
    
    private FlightGearTelnetConnection fgTelnet;
    private FlightGearSocketsConnection fgSockets;
               
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
			fgSockets = new FlightGearSocketsConnection(config.getSocketsHostname(), config.getSocketsPort());
			
	        //launch this after the fgsockets connection is initialized, because the telemetry reads depends on this
			//
	        launchTelemetryThread();
			
			fgTelnet = new FlightGearTelnetConnection(config.getTelnetHostname(), config.getTelnetPort());

		} catch (SocketException | UnknownHostException | InvalidTelnetOptionException e) {
			
			LOGGER.error("Exception occurred during setup", e);
			
			throw new FlightGearSetupException(e);
		} catch (IOException e) {
			
			LOGGER.error("IOException occurred during setup", e);
			
			throw new FlightGearSetupException(e);
		}
        
        LOGGER.info("setup returning");
    }
    
    public void startupPlane() throws FlightGearSetupException {
        
        LOGGER.info("Starting up the plane");
                
        //nasal script to autostart from c172p menu
        try {
        	//execute the startup nasal script
			fgTelnet.runNasal("c172p.autostart();");
			
	        LOGGER.debug("Startup nasal script was executed. Sleeping for completion.");
	        
	        //startup may be asynchronous so we have to wait for the next prompt 
	        //TODO: maybe run another telnet command as a check
	        try {
	            Thread.sleep(AUTOSTART_COMPLETION_SLEEP);
	        } catch (InterruptedException e) {
	            LOGGER.warn("Startup wait interrupted", e);
	        }		
	        
	        LOGGER.debug("Startup nasal script execution completed");
		} catch (IOException e) {
			LOGGER.error("Exception running startup nasal script", e);
        	throw new FlightGearSetupException("Could not execute startup nasal script");
		} finally {
	        //disconnect the telnet connection because we only use it to run the nasal script
	        //to start the plane. it's not used again.
	        if(fgTelnet.isConnected()) {
	            fgTelnet.disconnect();
	        }
		}
        
        int startupWait = 0;
        int maxWait = AUTOSTART_COMPLETION_SLEEP;
        int sleep = 250;
        
        while(!this.isEngineRunning() && startupWait < maxWait) {
        	LOGGER.debug("Waiting for engine to complete startup");
        	try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
	            LOGGER.warn("Engine startup wait interrupted", e);
			}
        }
        
        if(!this.isEngineRunning()) {
        	throw new FlightGearSetupException("Engine was not running after startup. Bailing out-not literally.");
        }
    
        LOGGER.info("Startup completed");
    }
    
    /**
     * Write an input hash to the simulator.
     * 
     * @param inputHash
     * @param port
     */
    @Override
    protected synchronized void writeControlInput(LinkedHashMap<String, String> inputHash, int port) {
    	//wait for state write to finish. don't care about state reads
    	//may not actually need this since this is a synchronized function
    	//TODO: test^^^
        while(stateWriting.get()) {
            try {
            	LOGGER.debug("writeSocketInput: Waiting for previous state write to complete");
                Thread.sleep(SOCKET_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
            	LOGGER.warn("writeSocketInput: Socket write wait interrupted", e);
            }
        }
    	
        try {
        	stateWriting.set(true);
        	fgSockets.writeInput(inputHash, port);
        }
        finally {
            stateWriting.set(false);
        }
    }
    
    //////////////
    //telemetry accessors
    
    ///////////////////
    //consumables
    
    public double getCapacity_gal_us() {
        return Double.parseDouble(getTelemetryField(C172PFields.FUEL_TANK_CAPACITY_FIELD));
    }
    
    public double getLevel_gal_us() {
        return Double.parseDouble(getTelemetryField(C172PFields.FUEL_TANK_LEVEL_FIELD));
    }
    
    public double getWaterContamination() {
        return Double.parseDouble(getTelemetryField(C172PFields.WATER_CONTAMINATION_FIELD));
    }
    
    ///////////////////
    //controls
    
    public int getBatterySwitch() {
        return Character.getNumericValue( getTelemetryField(C172PFields.BATTERY_SWITCH_FIELD).charAt(0));
    }
    
    public boolean isBatterySwitchEnabled() {
    	return getBatterySwitch() == C172PFields.BATTERY_SWITCH_INT_TRUE;
    }
    
    public double getMixture() {
        return Double.parseDouble(getTelemetryField(C172PFields.MIXTURE_FIELD));
    }
    
    public double getThrottle() {
        return Double.parseDouble(getTelemetryField(C172PFields.THROTTLE_FIELD));
    }
    
    public double getAileron() {
        return Double.parseDouble(getTelemetryField(C172PFields.AILERON_FIELD));
    }
    
    public int getAutoCoordination() {
        return Integer.parseInt(getTelemetryField(C172PFields.AUTO_COORDINATION_FIELD));
    }
    
    public double getAutoCoordinationFactor() {
        return Double.parseDouble(getTelemetryField(C172PFields.AUTO_COORDINATION_FACTOR_FIELD));
    }
    
    public double getElevator() {
        return Double.parseDouble(getTelemetryField(C172PFields.ELEVATOR_FIELD));
    }
    
    public double getFlaps() {
        return Double.parseDouble(getTelemetryField(C172PFields.FLAPS_FIELD));
    }
    
    public double getRudder() {
        return Double.parseDouble(getTelemetryField(C172PFields.RUDDER_FIELD));
    }
    
    public double getSpeedbrake() {
        return Double.parseDouble(getTelemetryField(C172PFields.SPEED_BRAKE_FIELD));
    }
    
    public int getParkingBrakeEnabled() {
    	//TODO: this and other fields can be missing if the protocol files are incorrect- safeguard against.
    	
    	//returned as a double like 0.000000, just look at the first character
        return Character.getNumericValue( getTelemetryField(C172PFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public boolean isParkingBrakeEnabled() {
        return getParkingBrakeEnabled() == C172PFields.SIM_PARKING_BRAKE_INT_TRUE;
    }
    
    public int getParkingBrake() {   	
        return Character.getNumericValue(getTelemetryField(C172PFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public int getGearDown() {
        return Character.getNumericValue( getTelemetryField(C172PFields.GEAR_DOWN_FIELD).charAt(0));
    }
    
    public boolean isGearDown() {
    	return getGearDown() == C172PFields.GEAR_DOWN_INT_TRUE;
    }
    
    ///////////////////
    //engine
    
    public double getCowlingAirTemperature() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_COWLING_AIR_TEMPERATURE_FIELD));
    }

    public double getExhaustGasTemperature() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_EXHAUST_GAS_TEMPERATURE_FIELD));
    }
    
    public double getExhaustGasTemperatureNormalization() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_EXHAUST_GAS_TEMPERATURE_NORM_FIELD));
    }
    
    public double getFuelFlow() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_FUEL_FLOW_FIELD));
    }
    
    public double getMpOsi() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_MP_OSI_FIELD));
    }
    
    public double getOilPressure() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_OIL_PRESSURE_FIELD));
    }
    
    public double getOilTemperature() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_OIL_TEMPERATURE_FIELD));
    }
    
    public double getEngineRpms() {
        return Double.parseDouble(getTelemetryField(C172PFields.ENGINE_RPM_FIELD));
    }
    
    public int getEngineRunning() {
        return Character.getNumericValue( getTelemetryField(C172PFields.ENGINE_RUNNING_FIELD).charAt(0));
    }
    
    @Override
    public boolean isEngineRunning() {
    	return getEngineRunning() == C172PFields.ENGINE_RUNNING_INT_TRUE;
    }
    
    ///////////////////
    //sim
    
    public int getSimParkingBrake() {   	
        return Character.getNumericValue(getTelemetryField(C172PFields.SIM_PARKING_BRAKE_FIELD).charAt(0));
    }
    
    //////////////
    //telemetry modifiers
    
    public void forceStabilize(double heading, double altitude, double roll, double pitch) {
        
        LOGGER.info("forceStablize called");
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> orientationFields = copyStateFields(FlightGearPlaneFields.ORIENTATION_INPUT_FIELDS);
        
        setPause(true);
        orientationFields.put(FlightGearPlaneFields.HEADING_FIELD, String.valueOf(heading) ) ;
        orientationFields.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(pitch) );
        orientationFields.put(FlightGearPlaneFields.ROLL_FIELD, String.valueOf(roll) );
        orientationFields.put(FlightGearPlaneFields.ALTITUDE_FIELD, String.valueOf(altitude) );

        writeControlInput(orientationFields, SOCKETS_INPUT_ORIENTATION_PORT);

        setPause(false);
    }
    
    @Override
    public synchronized void setFuelTankLevel(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_INPUT_FIELDS);
    	
        inputHash.put(C172PFields.FUEL_TANK_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank level: {}", amount);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    }
    
    public synchronized void setFuelTankWaterContamination(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_INPUT_FIELDS);
    	
        inputHash.put(C172PFields.WATER_CONTAMINATION_FIELD, "" + amount);
        
        LOGGER.info("Setting fuel tank water contamination: {}", amount);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
    	
    }
    
    public synchronized void setBatterySwitch(boolean switchOn) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        if(switchOn) {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_TRUE);
        }
        else {
        	inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_FALSE);
        }
        
        LOGGER.info("Setting battery switch to {}", switchOn);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setElevator(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.ELEVATOR_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator to {}", orientation);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAileron(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.AILERON_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron to {}", orientation);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAutoCoordination(boolean enabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        if(enabled) {
        	inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_TRUE);
        }
        else {
        	inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_FALSE);
        }

        LOGGER.info("Setting autocoordination to {}", enabled);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setFlaps(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.FLAPS_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting flaps to {}", orientation);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setRudder(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.RUDDER_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder to {}", orientation);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setThrottle(double throttle ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.THROTTLE_FIELD, String.valueOf(throttle));
        
        LOGGER.info("Setting throttle to {}", throttle);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setMixture(double mixture ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.MIXTURE_FIELD, String.valueOf(mixture));
        
        LOGGER.info("Setting mixture to {}", mixture);
        
        writeControlInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
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
		writeControlInput(inputHash, SOCKETS_INPUT_SIM_FREEZE_PORT);

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
        
        writeControlInput(inputHash, SOCKETS_INPUT_SIM_SPEEDUP_PORT);
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.FDM_INPUT_FIELDS);
        
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
        writeControlInput(inputHash, SOCKETS_INPUT_FDM_PORT);
    }
    
    /**
     * 
     * 
     * @param heading    Degrees from north, clockwise. 0=360 => North. 90 => East. 180 => South. 270 => West 
     */
    @Override
    public synchronized void setHeading(double heading) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_INPUT_FIELDS);
        
        //get telemetry hash
        
        inputHash.put(FlightGearPlaneFields.HEADING_FIELD, String.valueOf(heading));
        
        LOGGER.info("Setting heading to {}", heading);
        
        writeControlInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setPitch(double targetPitch) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(targetPitch));
        
        LOGGER.info("Setting pitch to {}", targetPitch);
        
        writeControlInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    @Override
    public synchronized void setRoll(double targetRoll) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_INPUT_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.ROLL_FIELD, String.valueOf(targetRoll));
        
        LOGGER.info("Setting roll to {}", targetRoll);
        
        writeControlInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    @Override
    public synchronized void setAltitude(double targetAltitude) {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.ALTITUDE_FIELD, String.valueOf(targetAltitude));
        
        LOGGER.info("Setting altitude to {}", targetAltitude);
        
        writeControlInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }
    
    @Override
    public synchronized void setLatitude(double targetLatitude) {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.LATITUDE_FIELD, String.valueOf(targetLatitude));
        
        LOGGER.info("Setting latitude to {}", targetLatitude);
        
        writeControlInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }
    
    @Override
    public synchronized void setLongitude(double targetLongitude) {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.LONGITUDE_FIELD, String.valueOf(targetLongitude));
        
        LOGGER.info("Setting longitude to {}", targetLongitude);
        
        writeControlInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }
    
    @Override
    public synchronized void setAirSpeed(double targetSpeed) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.AIRSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting air speed to {}", targetSpeed);
        
        writeControlInput(inputHash, SOCKETS_INPUT_VELOCITIES_PORT);
    }
    
    @Override
    public synchronized void setVerticalSpeed(double targetSpeed) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.VERTICALSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting vertical speed to {}", targetSpeed);
        
        writeControlInput(inputHash, SOCKETS_INPUT_VELOCITIES_PORT);
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
        
        writeControlInput(inputHash, SOCKETS_INPUT_SIM_PORT);
    }
    
///////////////
    
    public void shutdown() {
        
        LOGGER.info("C172P Shutdown invoked");
        
        //shuts down telemetry thread
        super.shutdown();
        
        //no io resources in the socket manager to shutdown

        //FGM shutdown
        //telnet client should be shutdown in the setup method but have another look anyway
        if(fgTelnet != null && fgTelnet.isConnected()) {
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
