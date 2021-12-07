package org.jason.flightgear.planes.c172p;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.connection.sockets.FlightGearInputConnection;
import org.jason.flightgear.connection.sockets.FlightGearTelemetryConnection;
import org.jason.flightgear.connection.telnet.FlightGearTelnetConnection;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.jason.flightgear.planes.FlightGearPlane;
import org.jason.flightgear.planes.FlightGearPlaneFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C172P extends FlightGearPlane{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(C172P.class);

    private final static int AUTOSTART_COMPLETION_SLEEP = 5000;
    
    private final static int SOCKET_WRITE_WAIT_SLEEP = 100;
    
    private FlightGearTelemetryConnection socketsTelemetryConnection;
    
    private FlightGearInputConnection consumeablesInputConnection;
    private FlightGearInputConnection controlInputConnection;
    private FlightGearInputConnection fdmInputConnection;
    private FlightGearInputConnection orientationInputConnection;
    private FlightGearInputConnection positionInputConnection;
    private FlightGearInputConnection simInputConnection;
    private FlightGearInputConnection simFreezeInputConnection;
    private FlightGearInputConnection simSpeedupInputConnection;
    private FlightGearInputConnection velocitiesInputConnection;
               
    public C172P() throws FlightGearSetupException {
        this(new C172PConfig());
    }
    
    public C172P(C172PConfig config) throws FlightGearSetupException  {
        super();
        
        LOGGER.info("Loading C172P...");
        
        //setup known ports, and the telemetry socket. start the telemetry retrieval thread.
        setup(config);
        
        //TODO: implement. possibly add to superclass. depends on superclass init and setup
        launchSimulator();
                
        LOGGER.info("C172P setup completed");
    }
    
    private void launchSimulator() {
        //run script, wait for telemetry port and first read
    }
    
    private void setup(C172PConfig config) throws FlightGearSetupException {
        LOGGER.info("setup called");
        
        //TODO: invoke port setters in superclass per config
        //networkConfig.setConsumeablesPort(config.getConsumeablesPort);
        
        try {
        	LOGGER.info("Establishing input socket connections.");
        	
        	consumeablesInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getConsumeablesPort());
			controlInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getControlsPort());
			fdmInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getFdmPort());
			orientationInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getOrientationPort());
			positionInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getPositionPort());
			simInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimPort());
			simFreezeInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimFreezePort());
			simSpeedupInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimSpeedupPort());
			velocitiesInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getVelocitiesPort());
			
			LOGGER.info("Input socket connections established.");
		} catch (SocketException | UnknownHostException e) {
            LOGGER.error("Exception occurred establishing control input connections", e);
            
            throw new FlightGearSetupException(e);
		}
        
        //TODO: check that any dynamic config reads result in all control input ports being defined
        
        //TODO: consider a separate function so this can be started/restarted externally
        //launch thread to update telemetry

        try {
            socketsTelemetryConnection = new FlightGearTelemetryConnection(networkConfig.getTelemetryOutputHost(), networkConfig.getTelemetryOutputPort());
            
            //launch this after the fgsockets connection is initialized, because the telemetry reads depends on this
            launchTelemetryThread();
        } catch (SocketException | UnknownHostException e) {
            
            LOGGER.error("Exception occurred during setup", e);
            
            throw new FlightGearSetupException(e);
        }
        
        LOGGER.info("setup returning");
    }
    
    public void startupPlane() throws FlightGearSetupException {
        
        LOGGER.info("Starting up the C172P");
                
        FlightGearTelnetConnection planeStartupTelnetSession = null; 
		//nasal script to autostart from c172p menu
        try {
        	planeStartupTelnetSession = new FlightGearTelnetConnection(networkConfig.getTelnetHost(), networkConfig.getTelnetPort());
        	
            //execute the startup nasal script
            planeStartupTelnetSession.runNasal("c172p.autostart();");
            
            LOGGER.debug("Startup nasal script was executed. Sleeping for completion.");
            
            //startup may be asynchronous so we have to wait for the next prompt 
            //TODO: maybe run another telnet command as a check
            try {
                Thread.sleep(AUTOSTART_COMPLETION_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("Startup wait interrupted", e);
            }        
            
            LOGGER.debug("Startup nasal script execution completed");
        } catch (IOException | InvalidTelnetOptionException e) {
            LOGGER.error("Exception running startup nasal script", e);
            throw new FlightGearSetupException("Could not execute startup nasal script");
        } finally {
            //disconnect the telnet connection because we only use it to run the nasal script
            //to start the plane. it's not used again.
            if(planeStartupTelnetSession.isConnected()) {
                planeStartupTelnetSession.disconnect();
            }
        }
        
        int startupWait = 0;
        int sleep = 250;
        
        while(!this.isEngineRunning() && startupWait < AUTOSTART_COMPLETION_SLEEP) {
            LOGGER.debug("Waiting for engine to complete startup");
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                LOGGER.warn("Engine startup wait interrupted", e);
            }
            
            startupWait += sleep;
        }
        
        if(!this.isEngineRunning()) {
            throw new FlightGearSetupException("Engine was not running after startup. Bailing out. Not literally.");
        }
    
        LOGGER.info("Startup completed");
    }
    
    /**
     * Write an input hash to a simulator input socket.
     * 
     * @param inputHash
     * @param port
     * @throws IOException 
     */
    @Override
    protected synchronized void writeControlInput(LinkedHashMap<String, String> inputHash, FlightGearInputConnection socketConnection) throws IOException {
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
            socketConnection.writeControlInput(inputHash);
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
    
    public void forceStabilize(double heading, double altitude, double roll, double pitch) throws IOException {
        
        LOGGER.info("forceStabilize called");
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> orientationFields = copyStateFields(FlightGearPlaneFields.ORIENTATION_INPUT_FIELDS);
        
        setPause(true);
        orientationFields.put(FlightGearPlaneFields.HEADING_FIELD, String.valueOf(heading) ) ;
        orientationFields.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(pitch) );
        orientationFields.put(FlightGearPlaneFields.ROLL_FIELD, String.valueOf(roll) );

        //TODO: altitude check?
        
        writeControlInput(orientationFields, this.orientationInputConnection);

        setPause(false);
    }
    
    @Override
    public synchronized void setFuelTankLevel(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(C172PFields.FUEL_TANK_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);
    }
    
    public synchronized void setFuelTankWaterContamination(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(C172PFields.WATER_CONTAMINATION_FIELD, "" + amount);
        
        LOGGER.info("Setting fuel tank water contamination: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setBatterySwitch(boolean switchOn) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        if(switchOn) {
            inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_TRUE);
        }
        else {
            inputHash.put(C172PFields.BATTERY_SWITCH_FIELD, C172PFields.BATTERY_SWITCH_FALSE);
        }
        
        LOGGER.info("Setting battery switch to {}", switchOn);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setElevator(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.ELEVATOR_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setAileron(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.AILERON_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setAutoCoordination(boolean enabled) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        if(enabled) {
            inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_TRUE);
        }
        else {
            inputHash.put(C172PFields.AUTO_COORDINATION_FIELD, C172PFields.AUTO_COORDINATION_FALSE);
        }

        LOGGER.info("Setting autocoordination to {}", enabled);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setFlaps(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.FLAPS_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting flaps to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setRudder(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.RUDDER_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setThrottle(double throttle ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.THROTTLE_FIELD, String.valueOf(throttle));
        
        LOGGER.info("Setting throttle to {}", throttle);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setMixture(double mixture ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(C172PFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(C172PFields.MIXTURE_FIELD, String.valueOf(mixture));
        
        LOGGER.info("Setting mixture to {}", mixture);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void resetControlSurfaces() throws IOException {
        
        LOGGER.info("Resetting control surfaces");
        
        setElevator(C172PFields.ELEVATOR_DEFAULT);
        setAileron(C172PFields.AILERON_DEFAULT);
        setFlaps(C172PFields.FLAPS_DEFAULT);
        setRudder(C172PFields.RUDDER_DEFAULT);
        
        LOGGER.info("Reset of control surfaces completed");
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) throws IOException {
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
        writeControlInput(inputHash, this.fdmInputConnection);
    }
    
    @Override
    public synchronized void setAltitude(double targetAltitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.ALTITUDE_FIELD, String.valueOf(targetAltitude));
        
        LOGGER.info("Setting altitude to {}", targetAltitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setLatitude(double targetLatitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.LATITUDE_FIELD, String.valueOf(targetLatitude));
        
        LOGGER.info("Setting latitude to {}", targetLatitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setLongitude(double targetLongitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.LONGITUDE_FIELD, String.valueOf(targetLongitude));
        
        LOGGER.info("Setting longitude to {}", targetLongitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setParkingBrake(boolean brakeEnabled) throws IOException {
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
        
        writeControlInput(inputHash, this.simInputConnection);
    }
    
    @Override
    public synchronized void setAirSpeed(double targetSpeed) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.AIRSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting air speed to {}", targetSpeed);
        
        writeControlInput(inputHash, this.velocitiesInputConnection);
    }
    
    @Override
    public synchronized void setVerticalSpeed(double targetSpeed) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearPlaneFields.VERTICALSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting vertical speed to {}", targetSpeed);
        
        writeControlInput(inputHash, this.velocitiesInputConnection);
    }
    
    ///////////////
    
    public void shutdown() {
        
        LOGGER.info("C172P Shutdown invoked");
        
        //shuts down telemetry thread
        super.shutdown();
        
        //close input sockets
        try {
			consumeablesInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing consumeables input socket", e);
		}
        try {
			controlInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing control input socket", e);
		}
        try {
			fdmInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing fdm input socket", e);
		}
        try {
			orientationInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing orientation input socket", e);
		}
        try {
			positionInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing position input socket", e);
		}
        try {
			simInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing sim input socket", e);
		}
        try {
			simFreezeInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing sim freeze input socket", e);
		}
        try {
			simSpeedupInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing sim speedup input socket", e);
		}
        try {
			velocitiesInputConnection.close();
		} catch (IOException e) {
			LOGGER.error("Exception closing velocities input socket", e);
		}
        
        LOGGER.info("C172P Shutdown completed");
    }

    @Override
    protected String readTelemetryRaw() throws IOException {
        return socketsTelemetryConnection.readTelemetry();
        
    }

    @Override
    public synchronized double getFuelTankCapacity() {
        return this.getCapacity_gal_us();
    }

    @Override
    public synchronized double getFuelLevel() {
        return getLevel_gal_us();
        
    }

    ///////////////
    //socket connection writing
    
	@Override
	protected void writeConsumeablesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.consumeablesInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeControlInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.controlInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeFdmInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.fdmInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeOrientationInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.orientationInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writePositionInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.positionInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.simFreezeInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.simSpeedupInputConnection.writeControlInput(inputHash);
	}

	@Override
	protected void writeVelocitiesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		this.velocitiesInputConnection.writeControlInput(inputHash);
	}
}
