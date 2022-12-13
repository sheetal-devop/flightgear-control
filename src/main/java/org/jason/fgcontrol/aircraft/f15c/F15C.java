package org.jason.fgcontrol.aircraft.f15c;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.apache.commons.math3.util.Precision;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.FlightGearAircraft;
import org.jason.fgcontrol.aircraft.fields.FlightGearFields;
import org.jason.fgcontrol.connection.sockets.FlightGearInputConnection;
import org.jason.fgcontrol.connection.sockets.FlightGearTelemetryConnection;
import org.jason.fgcontrol.connection.telnet.FlightGearTelnetConnection;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class F15C extends FlightGearAircraft {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(F15C.class);

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
    private FlightGearInputConnection simModelInputConnection;
    private FlightGearInputConnection simSpeedupInputConnection;
    private FlightGearInputConnection simTimeInputConnection;
    private FlightGearInputConnection velocitiesInputConnection;
    
    public F15C() throws FlightGearSetupException {
        this(new F15CConfig());
    }
    
    public F15C(F15CConfig config) throws FlightGearSetupException  {
        super(config);
        
        LOGGER.info("Loading F15C...");
        
        //setup known ports, and the telemetry socket. start the telemetry retrieval thread.
        setup(config);
        
        //TODO: implement. possibly add to superclass. depends on superclass init and setup
        //launchSimulator();
                
        LOGGER.info("F15C setup completed");
    }
    
//    private void launchSimulator() {
//        //run script, wait for telemetry port and first read
//    }
    
    private void setup(F15CConfig config) throws FlightGearSetupException {
        LOGGER.info("setup called");
        
        //TODO: invoke port setters in superclass per config
        //simulatorConfig.setConsumeablesPort(config.getConsumeablesPort);
        
        try {
            LOGGER.info("Establishing input socket connections.");
            
            consumeablesInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getConsumeablesInputPort());
            controlInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getControlsInputPort());
            fdmInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getFdmInputPort());
            orientationInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getOrientationInputPort());
            positionInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getPositionInputPort());
            simInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getSimInputPort());
            simFreezeInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getSimFreezeInputPort());
            simModelInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getSimModelInputPort());
            simSpeedupInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getSimSpeedupInputPort());
            simTimeInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getSimTimeInputPort());
            velocitiesInputConnection = new FlightGearInputConnection(simulatorConfig.getSocketInputHost(), simulatorConfig.getVelocitiesInputPort());
            
            LOGGER.info("Input socket connections established.");
        } catch (SocketException | UnknownHostException e) {
            LOGGER.error("Exception occurred establishing control input connections", e);
            
            throw new FlightGearSetupException(e);
        }
        
        //TODO: check that any dynamic config reads result in all control input ports being defined
        
        //TODO: consider a separate function so this can be started/restarted externally
        //launch thread to update telemetry

        try {
            socketsTelemetryConnection = new FlightGearTelemetryConnection(
            	simulatorConfig.getTelemetryOutputHost(), 
            	simulatorConfig.getTelemetryOutputPort()
            );
            
            //launch this after the fgsockets connection is initialized, because the telemetry reads depends on this
            launchTelemetryThread();
        } catch (SocketException | UnknownHostException e) {
            
            LOGGER.error("Exception occurred during setup", e);
            
            throw new FlightGearSetupException(e);
        }
        
        LOGGER.info("setup returning");
    }
    
    public void startupPlane() throws FlightGearSetupException {
        
        LOGGER.info("Starting up the F15C");
                
        FlightGearTelnetConnection planeStartupTelnetSession = null; 
        //nasal script to autostart from F15C menu
        try {
            planeStartupTelnetSession = new FlightGearTelnetConnection(simulatorConfig.getTelnetHost(), simulatorConfig.getTelnetPort());
            
            //execute the startup nasal script
            planeStartupTelnetSession.runNasal("aircraft.quickstart();");
            
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
        while(telemetryStateWriting.get()) {
            try {
                LOGGER.debug("writeSocketInput: Waiting for previous state write to complete");
                Thread.sleep(SOCKET_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("writeSocketInput: Socket write wait interrupted", e);
            }
        }
        
        try {
            telemetryStateWriting.set(true);
            socketConnection.writeControlInput(inputHash);
        }
        finally {
            telemetryStateWriting.set(false);
        }
    }
    
    //////////////
    //telemetry accessors
    
    ///////////////////
    //consumables
    
    public double getLevel_gal_us() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_TOTAL_LEVEL_FIELD));
    }
    
    public double getFuelTank0Capacity() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_0_CAPACITY_FIELD));
    }
    
    public double getFuelTank0Level() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_0_LEVEL_FIELD));
    }
    
    public double getFuelTank1Capacity() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_1_CAPACITY_FIELD));
    }
    
    public double getFuelTank1Level() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_1_LEVEL_FIELD));
    }
    
    public double getFuelTank2Capacity() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_2_CAPACITY_FIELD));
    }
    
    public double getFuelTank2Level() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_2_LEVEL_FIELD));
    }
    
    public double getFuelTank3Capacity() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_3_CAPACITY_FIELD));
    }
    
    public double getFuelTank3Level() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_3_LEVEL_FIELD));
    }
    
    public double getFuelTank4Capacity() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_4_CAPACITY_FIELD));
    }
    
    public double getFuelTank4Level() {
        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_4_LEVEL_FIELD));
    }
    
//    public double getFuelTank5Level() {
//        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_5_LEVEL_FIELD));
//    }
//    
//    public double getFuelTank6Level() {
//        return Double.parseDouble(getTelemetryField(F15CFields.FUEL_TANK_6_LEVEL_FIELD));
//    }
   
    ///////////////////
    //controls
    
    public int getBatterySwitch() {
        return Character.getNumericValue( getTelemetryField(F15CFields.BATTERY_SWITCH_FIELD).charAt(0));
    }
    
    public boolean isBatterySwitchEnabled() {
        return getBatterySwitch() == F15CFields.BATTERY_SWITCH_INT_TRUE;
    }
    
    public int getEngine0Cutoff() {
        return Character.getNumericValue( getTelemetryField(F15CFields.ENGINE_0_CUTOFF_FIELD).charAt(0));
    }
    
    public boolean isEngine0Cutoff() {
        return getEngine0Cutoff() == F15CFields.ENGINE_CUTOFF_INT_TRUE;
    }
    
    public boolean isEngine1Cutoff() {
        return getEngine1Cutoff() == F15CFields.ENGINE_CUTOFF_INT_TRUE;
    }
    
    public int getEngine1Cutoff() {
        return Character.getNumericValue( getTelemetryField(F15CFields.ENGINE_0_CUTOFF_FIELD).charAt(0));
    }
    
    public double getEngine0Mixture() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_MIXTURE_FIELD));
    }
    
    public double getEngine1Mixture() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_MIXTURE_FIELD));
    }
    
    public double getEngine0Throttle() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_THROTTLE_FIELD));
    }
    
    public double getEngine1Throttle() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_THROTTLE_FIELD));
    }

    public double getAileron() {
        return Double.parseDouble(getTelemetryField(F15CFields.AILERON_FIELD));
    }
    
    public double getAileronTrim() {
        return Double.parseDouble(getTelemetryField(F15CFields.AILERON_TRIM_FIELD));
    }
    
    public int getAutoCoordination() {
        return Integer.parseInt(getTelemetryField(F15CFields.AUTO_COORDINATION_FIELD));
    }
    
    public double getAutoCoordinationFactor() {
        return Double.parseDouble(getTelemetryField(F15CFields.AUTO_COORDINATION_FACTOR_FIELD));
    }
    
    public double getElevator() {
        return Double.parseDouble(getTelemetryField(F15CFields.ELEVATOR_FIELD));
    }
    
    public double getElevatorTrim() {
        return Double.parseDouble(getTelemetryField(F15CFields.ELEVATOR_TRIM_FIELD));
    }
    
    public double getFlaps() {
        return Double.parseDouble(getTelemetryField(F15CFields.FLAPS_FIELD));
    }
    
    public double getRudder() {
        return Double.parseDouble(getTelemetryField(F15CFields.RUDDER_FIELD));
    }
    
    public double getRudderTrim() {
        return Double.parseDouble(getTelemetryField(F15CFields.RUDDER_TRIM_FIELD));
    }
    
    public double getSpeedbrake() {
        return Double.parseDouble(getTelemetryField(F15CFields.SPEED_BRAKE_FIELD));
    }
    
    public int getSpeedBrakeEnabled() {
        //returned as a double like 0.000000, just look at the first character
        return Character.getNumericValue( getTelemetryField(F15CFields.SPEED_BRAKE_FIELD).charAt(0));
    }
    
    public boolean isSpeedBrakeEnabled() {
        return getSpeedBrakeEnabled() == F15CFields.SPEED_BRAKE_INT_TRUE;
    }
    
    public int getParkingBrakeEnabled() {
        //TODO: this and other fields can be missing if the protocol files are incorrect- safeguard against.
        
        //returned as a double like 0.000000, just look at the first character
        return Character.getNumericValue( getTelemetryField(F15CFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public boolean isParkingBrakeEnabled() {
        return getParkingBrakeEnabled() == F15CFields.PARKING_BRAKE_INT_TRUE;
    }
    
    public int getParkingBrake() {       
        return Character.getNumericValue(getTelemetryField(F15CFields.PARKING_BRAKE_FIELD).charAt(0));
    }
    
    public int getGearDown() {
        return Character.getNumericValue( getTelemetryField(F15CFields.GEAR_DOWN_FIELD).charAt(0));
    }
    
    public boolean isGearDown() {
        return getGearDown() == F15CFields.GEAR_DOWN_INT_TRUE;
    }
    
    ///////////////////
    //engine - two engines on the f15c

    public double getEngine0ExhaustGasTemperature() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_EXHAUST_GAS_TEMPERATURE_FIELD));
    }
    
    public double getEngine1ExhaustGasTemperature() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_EXHAUST_GAS_TEMPERATURE_FIELD));
    }
    
    public double getEngine0ExhaustGasTemperatureNormalization() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_EXHAUST_GAS_TEMPERATURE_NORM_FIELD));
    }
    
    public double getEngine1ExhaustGasTemperatureNormalization() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_EXHAUST_GAS_TEMPERATURE_NORM_FIELD));
    }
    
    public double getEngine0FuelFlow() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_FUEL_FLOW_FIELD));
    }
    
    public double getEngine1FuelFlow() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_FUEL_FLOW_FIELD));
    }
    
    public double getEngine0OilPressure() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_OIL_PRESSURE_FIELD));
    }
    
    public double getEngine1OilPressure() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_OIL_PRESSURE_FIELD));
    }
    
    public double getEngine0Thrust() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_0_THRUST_FIELD));
    }
    
    public double getEngine1Thrust() {
        return Double.parseDouble(getTelemetryField(F15CFields.ENGINE_1_THRUST_FIELD));
    }
    
    /**
     * The state of engine 1 (and not engine 0) determines the running and cutoff states.
     * 
     * @return
     */
    public int getEngineRunning() {
        return Character.getNumericValue( getTelemetryField(F15CFields.ENGINE_1_RUNNING_FIELD).charAt(0));
    }
    
    public int getEngine0Running() {
    	return Character.getNumericValue( getTelemetryField(F15CFields.ENGINE_0_RUNNING_FIELD).charAt(0));
    }
    
    public int getEngine1Running() {
    	return getEngineRunning();
    }
    
    /**
     *    
     */
    @Override
    public boolean isEngineRunning() {
        return getEngineRunning() == F15CFields.ENGINE_RUNNING_INT_TRUE;
    }
    
    ///////////////////
    //sim model
    
    public int getArmamentAGMCount() {
    	//integer value that the sim stores as a float
        return (int)Double.parseDouble(getTelemetryField(F15CFields.ARMAMENT_AGM_COUNT));
    }
    
    public int getArmamentSystemRunning() {
        return Character.getNumericValue(getTelemetryField(F15CFields.ARMAMENT_SYSTEM_RUNNING).charAt(0));
    }
    
    //////////////
    //telemetry modifiers
    
    private void forceStablizationWrite(double targetHeading, double targetRoll, double targetPitch) throws IOException {
    	LinkedHashMap<String, String> orientationFields = copyStateFields(FlightGearFields.ORIENTATION_INPUT_FIELDS);
    	
        orientationFields.put(FlightGearFields.HEADING_FIELD, String.valueOf(targetHeading) ) ;
        orientationFields.put(FlightGearFields.PITCH_FIELD, String.valueOf(targetPitch) );
        orientationFields.put(FlightGearFields.ROLL_FIELD, String.valueOf(targetRoll) );
        
        writeControlInput(orientationFields, this.orientationInputConnection);
        
        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Force stablizing to {}", orientationFields.entrySet().toString());
        }
    }
    
    public void forceStabilize(double targetHeading, double targetRoll, double targetPitch) throws IOException {
    	forceStabilize(targetHeading, targetRoll, targetPitch, true);
    }
    
    public void forceStabilize(double targetHeading, double targetRoll, double targetPitch, boolean pauseSim) throws IOException {
        
    	if(LOGGER.isDebugEnabled()) {
    		LOGGER.debug("forceStabilize called");
    	}
                
        //pause before copyStateFields so we're not changing an orientation in the past
        //
        //for most fields we need to be careful about overwriting fields, but for forcibly 
        //re-orienting the plane we care less about orientation/roll/pitch
        if(pauseSim) {
        	try {
        		setPause(true);
        	
        		forceStablizationWrite(targetHeading, targetRoll, targetPitch);
        	} finally {
        		setPause(false);
        	}
        } else {
        	forceStablizationWrite(targetHeading, targetRoll, targetPitch);
        }
               
        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("forceStabilize returning");
        }
    }
    
    @Override
    public synchronized void refillFuel() throws IOException {
        
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        //write as one big update, otherwise we'll have to wait for the next telemetry read to react to the refuel of each tank
        inputHash.put(F15CFields.FUEL_TANK_0_LEVEL_FIELD, String.valueOf(this.getFuelTank0Capacity()));
        inputHash.put(F15CFields.FUEL_TANK_1_LEVEL_FIELD, String.valueOf(this.getFuelTank1Capacity()));
        inputHash.put(F15CFields.FUEL_TANK_2_LEVEL_FIELD, String.valueOf(this.getFuelTank2Capacity()));
        inputHash.put(F15CFields.FUEL_TANK_3_LEVEL_FIELD, String.valueOf(this.getFuelTank3Capacity()));
        inputHash.put(F15CFields.FUEL_TANK_4_LEVEL_FIELD, String.valueOf(this.getFuelTank4Capacity()));
        
        LOGGER.info("Refilling fuel tanks {}", inputHash.entrySet().toString());
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    @Override
    public synchronized void refillFuel(double level) throws IOException {
        
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        //write as one big update, otherwise we'll have to wait for the next telemetry read to react to the refuel of each tank
        inputHash.put(F15CFields.FUEL_TANK_0_LEVEL_FIELD, String.valueOf(level));
        inputHash.put(F15CFields.FUEL_TANK_1_LEVEL_FIELD, String.valueOf(level));
        inputHash.put(F15CFields.FUEL_TANK_2_LEVEL_FIELD, String.valueOf(level));
        inputHash.put(F15CFields.FUEL_TANK_3_LEVEL_FIELD, String.valueOf(level));
        inputHash.put(F15CFields.FUEL_TANK_4_LEVEL_FIELD, String.valueOf(level));
        
        LOGGER.info("Refilling fuel tanks {}", inputHash.entrySet().toString());
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setFuelTank0Level(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FUEL_TANK_0_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank 0 level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setFuelTank1Level(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FUEL_TANK_1_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank 1 level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setFuelTank2Level(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FUEL_TANK_2_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank 2 level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setFuelTank3Level(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FUEL_TANK_3_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank 3 level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
    public synchronized void setFuelTank4Level(double amount) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FUEL_TANK_4_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank 4 level: {}", amount);
        
        writeControlInput(inputHash, this.consumeablesInputConnection);   
    }
    
//    public synchronized void setFuelTank5Level(double amount) throws IOException {
//        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
//        
//        inputHash.put(F15CFields.FUEL_TANK_5_LEVEL_FIELD, String.valueOf(amount));
//        
//        LOGGER.info("Setting fuel tank 5 level: {}", amount);
//        
//        writeControlInput(inputHash, this.consumeablesInputConnection);   
//    }
//    
//    public synchronized void setFuelTank6Level(double amount) throws IOException {
//        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_INPUT_FIELDS);
//        
//        inputHash.put(F15CFields.FUEL_TANK_6_LEVEL_FIELD, String.valueOf(amount));
//        
//        LOGGER.info("Setting fuel tank 6 level: {}", amount);
//        
//        writeControlInput(inputHash, this.consumeablesInputConnection);   
//    }
    
    public synchronized void setBatterySwitch(boolean switchOn) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        if(switchOn) {
            inputHash.put(F15CFields.BATTERY_SWITCH_FIELD, F15CFields.BATTERY_SWITCH_TRUE);
        }
        else {
            inputHash.put(F15CFields.BATTERY_SWITCH_FIELD, F15CFields.BATTERY_SWITCH_FALSE);
        }
        
        LOGGER.info("Setting battery switch to {}", switchOn);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine0Cutoff(boolean cutoffState) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        if(cutoffState) {
            inputHash.put(F15CFields.ENGINE_0_CUTOFF_FIELD, F15CFields.ENGINE_CUTOFF_TRUE);
        }
        else {
            inputHash.put(F15CFields.ENGINE_0_CUTOFF_FIELD, F15CFields.ENGINE_CUTOFF_FALSE);
        }
        
        LOGGER.info("Setting engine 0 cutoff to {}", cutoffState);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine1Cutoff(boolean cutoffState) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        if(cutoffState) {
            inputHash.put(F15CFields.ENGINE_1_CUTOFF_FIELD, F15CFields.ENGINE_CUTOFF_TRUE);
        }
        else {
            inputHash.put(F15CFields.ENGINE_1_CUTOFF_FIELD, F15CFields.ENGINE_CUTOFF_FALSE);
        }
        
        LOGGER.info("Setting engine 1 cutoff to {}", cutoffState);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setAileron(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.AILERON_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setAileronTrim(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.AILERON_TRIM_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron trim to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setAutoCoordination(boolean enabled) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        if(enabled) {
            inputHash.put(F15CFields.AUTO_COORDINATION_FIELD, F15CFields.AUTO_COORDINATION_TRUE);
        }
        else {
            inputHash.put(F15CFields.AUTO_COORDINATION_FIELD, F15CFields.AUTO_COORDINATION_FALSE);
        }

        LOGGER.info("Setting autocoordination to {}", enabled);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setElevator(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ELEVATOR_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setElevatorTrim(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ELEVATOR_TRIM_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator trim to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setFlaps(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.FLAPS_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting flaps to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setRudder(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.RUDDER_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setRudderTrim(double orientation) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.RUDDER_TRIM_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder trim to {}", orientation);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    /**
     * Set both throttles in unison
     * 
     * @param throttle
     * @throws IOException
     */
    public synchronized void setEngineThrottles( double throttle ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        String finalThrottle = "";
        
        if(throttle < F15CFields.THROTTLE_MIN ) {
            finalThrottle = String.valueOf( F15CFields.THROTTLE_MIN ); 
        } else if(throttle > F15CFields.THROTTLE_MAX) {
            finalThrottle = String.valueOf( F15CFields.THROTTLE_MAX );
        } else {
            finalThrottle = String.valueOf(Precision.round(throttle, 2));
        }
        
        inputHash.put(F15CFields.ENGINE_0_THROTTLE_FIELD, finalThrottle );
        inputHash.put(F15CFields.ENGINE_1_THROTTLE_FIELD, finalThrottle );

        
        LOGGER.info("Setting engine throttles to {}", finalThrottle);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine0Throttle( double throttle ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ENGINE_0_THROTTLE_FIELD, String.valueOf( Precision.round(throttle, 2) ));
        
        LOGGER.info("Setting engine 0 throttle to {}", throttle);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine0Mixture( double mixture ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ENGINE_0_MIXTURE_FIELD, String.valueOf(mixture));
        
        LOGGER.info("Setting engine 0 mixture to {}", mixture);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine1Throttle( double throttle ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ENGINE_1_THROTTLE_FIELD, String.valueOf(Precision.round(throttle, 2)));
        
        LOGGER.info("Setting engine 1 throttle to {}", throttle);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setEngine1Mixture( double mixture ) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ENGINE_1_MIXTURE_FIELD, String.valueOf(mixture));
        
        LOGGER.info("Setting engine 1 mixture to {}", mixture);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setGearDown(boolean isGearDown) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
        
        if(isGearDown) {
            inputHash.put(F15CFields.GEAR_DOWN_FIELD, F15CFields.GEAR_DOWN_TRUE);
        }
        else {
            inputHash.put(F15CFields.GEAR_DOWN_FIELD, F15CFields.GEAR_DOWN_FALSE);
        }

        LOGGER.info("Setting gear down to {}", isGearDown);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    public synchronized void setArmamentAGMCount(int count) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.SIM_MODEL_INPUT_FIELDS);
        
        inputHash.put(F15CFields.ARMAMENT_AGM_COUNT, String.valueOf(count));
        
        LOGGER.info("Setting armament AGM count to {}", count);
        
        writeControlInput(inputHash, this.simModelInputConnection);
    }
    
    public synchronized void setArmamentSystemRunning(boolean running) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.SIM_MODEL_INPUT_FIELDS);
        
        if(running) {
            inputHash.put(F15CFields.ARMAMENT_SYSTEM_RUNNING, F15CFields.ARMAMENT_SYSTEM_RUNNING_TRUE);
        }
        else {
            inputHash.put(F15CFields.ARMAMENT_SYSTEM_RUNNING, F15CFields.ARMAMENT_SYSTEM_RUNNING_FALSE);
        }
        
        
        LOGGER.info("Setting armament AGM system running to {}", running);
        
        writeControlInput(inputHash, this.simModelInputConnection);
    }
    
    public synchronized void resetControlSurfaces() throws IOException {
        
        LOGGER.info("Resetting control surfaces");
               
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);

        inputHash.put(F15CFields.AILERON_FIELD, String.valueOf(F15CFields.AILERON_DEFAULT));
        inputHash.put(F15CFields.AILERON_TRIM_FIELD, String.valueOf(F15CFields.AILERON_TRIM_DEFAULT));
        
        inputHash.put(F15CFields.ELEVATOR_FIELD, String.valueOf(F15CFields.ELEVATOR_DEFAULT));
        inputHash.put(F15CFields.ELEVATOR_TRIM_FIELD, String.valueOf(F15CFields.ELEVATOR_TRIM_DEFAULT));
        
        inputHash.put(F15CFields.FLAPS_FIELD, String.valueOf(F15CFields.FLAPS_DEFAULT));
        
        inputHash.put(F15CFields.RUDDER_FIELD, String.valueOf(F15CFields.RUDDER_DEFAULT));
        inputHash.put(F15CFields.RUDDER_TRIM_FIELD, String.valueOf(F15CFields.RUDDER_DEFAULT));
        
        writeControlInput(inputHash, this.controlInputConnection);
        
        LOGGER.info("Reset of control surfaces completed");
    }
    
    public synchronized void syncEngines() throws IOException {

        //step throttle down to 80%, then back up to 100%
        //step up to full throttle or the engines will have divergent thrust outputs
        double startThrottleMin = 0.8;
        double throttleStep = 0.01;
        
        LOGGER.info("syncEngines invoked");
        
        setEngine0Throttle(F15CFields.THROTTLE_MAX);
        setEngine1Throttle(F15CFields.THROTTLE_MAX);
        

        for(double throttleInc = F15CFields.THROTTLE_MAX; throttleInc >= startThrottleMin; throttleInc-=throttleStep)
        {
            setEngine0Throttle(throttleInc);
            setEngine1Throttle(throttleInc);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("syncEngine sleep interrupted", e);
            }
        }
        
        for(double throttleInc = startThrottleMin; throttleInc <= F15CFields.THROTTLE_MAX; throttleInc+=throttleStep)
        {
            setEngine0Throttle(throttleInc);
            setEngine1Throttle(throttleInc);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("syncEngine sleep interrupted", e);
            }
        }
        
        LOGGER.info("syncEngines completed");
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.FDM_INPUT_FIELDS);
        
        //requires an int value for the bool
        if(!damageEnabled) {
            inputHash.put(FlightGearFields.FDM_DAMAGE_FIELD, FlightGearFields.FDM_DAMAGE_ENABLED_FALSE);
        }
        else {
            inputHash.put(FlightGearFields.FDM_DAMAGE_FIELD, FlightGearFields.FDM_DAMAGE_ENABLED_TRUE);
        }
        
        LOGGER.info("Toggling damage enabled: {}", damageEnabled);
        
        //socket writes typically require pauses so telemetry/state aren't out of date
        //however this is an exception
        writeControlInput(inputHash, this.fdmInputConnection);
    }
    
    @Override
    public synchronized void setAltitude(double targetAltitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearFields.ALTITUDE_FIELD, String.valueOf(targetAltitude));
        
        LOGGER.info("Setting altitude to {}", targetAltitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setLatitude(double targetLatitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearFields.LATITUDE_FIELD, String.valueOf(targetLatitude));
        
        LOGGER.info("Setting latitude to {}", targetLatitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setLongitude(double targetLongitude) throws IOException {        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.POSITION_INPUT_FIELDS);
        
        inputHash.put(FlightGearFields.LONGITUDE_FIELD, String.valueOf(targetLongitude));
        
        LOGGER.info("Setting longitude to {}", targetLongitude);
        
        writeControlInput(inputHash, this.positionInputConnection);
    }
    
    @Override
    public synchronized void setParkingBrake(boolean brakeEnabled) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_INPUT_FIELDS);
            
        //visual: in the cockpit, if the display on the lower right shows the right center light bar as:
        //lit -> brake is on
        //unlit -> brake is off
        
        //requires an double value for the bool
        if(brakeEnabled) {
            inputHash.put(F15CFields.PARKING_BRAKE_FIELD, F15CFields.PARKING_BRAKE_TRUE);
        }
        else {
            inputHash.put(F15CFields.PARKING_BRAKE_FIELD, F15CFields.PARKING_BRAKE_FALSE);
        }
        
        LOGGER.info("Setting parking brake to {}", brakeEnabled);
        
        writeControlInput(inputHash, this.controlInputConnection);
    }
    
    @Override
    public synchronized void setAirSpeed(double targetSpeed) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearFields.AIRSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting air speed to {}", targetSpeed);
        
        writeControlInput(inputHash, this.velocitiesInputConnection);
    }
    
    @Override
    public synchronized void setVerticalSpeed(double targetSpeed) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.VELOCITIES_INPUT_FIELDS);
                
        inputHash.put(FlightGearFields.VERTICALSPEED_FIELD, String.valueOf(targetSpeed));
        
        LOGGER.info("Setting vertical speed to {}", targetSpeed);
        
        writeControlInput(inputHash, this.velocitiesInputConnection);
    }
    
    ///////////////
    
    public void shutdown() {
        
        LOGGER.info("F15C Shutdown invoked");
        
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
            simModelInputConnection.close();
        } catch (IOException e) {
            LOGGER.error("Exception closing sim model input socket", e);
        }
        try {
            simSpeedupInputConnection.close();
        } catch (IOException e) {
            LOGGER.error("Exception closing sim speedup input socket", e);
        }
        try {
            simTimeInputConnection.close();
        } catch (IOException e) {
            LOGGER.error("Exception closing sim time input socket", e);
        }
        try {
            velocitiesInputConnection.close();
        } catch (IOException e) {
            LOGGER.error("Exception closing velocities input socket", e);
        }
        
        LOGGER.info("F15C Shutdown completed");
    }

    @Override
    protected String readTelemetryRaw() throws IOException {
        return socketsTelemetryConnection.readTelemetry();
        
    }

    @Override
    public synchronized double getFuelTankCapacity() {
        return this.getFuelTank0Capacity();
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
    protected void writeEnginesInput(LinkedHashMap<String, String> inputHash) throws IOException {
        // None as of now
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
    protected void writeSimInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.simInputConnection.writeControlInput(inputHash);
    }
    
    @Override
    protected void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.simFreezeInputConnection.writeControlInput(inputHash);
    }
    
    @Override
    protected void writeSimModelInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.simModelInputConnection.writeControlInput(inputHash);
    }

    @Override
    protected void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.simSpeedupInputConnection.writeControlInput(inputHash);
    }
    
    @Override
    protected void writeSimTimeInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.simTimeInputConnection.writeControlInput(inputHash);
    }
    
    @Override
    protected void writeSystemInput(LinkedHashMap<String, String> inputHash) throws IOException {
        // None as of now
    }

    @Override
    protected void writeVelocitiesInput(LinkedHashMap<String, String> inputHash) throws IOException {
        this.velocitiesInputConnection.writeControlInput(inputHash);
    }
}
