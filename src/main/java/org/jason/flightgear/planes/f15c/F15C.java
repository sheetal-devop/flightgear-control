package org.jason.flightgear.planes.f15c;

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

public class F15C extends FlightGearPlane {

    private final static Logger LOGGER = LoggerFactory.getLogger(F15C.class);
    
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
               
    public F15C() throws FlightGearSetupException {
    	this(new F15CConfig());
    }
    
    public F15C(F15CConfig config) throws FlightGearSetupException  {
    	super();
    	
        LOGGER.info("Loading F15C...");
        
        //setup the socket and telnet connections. start the telemetry retrieval thread.
        setup(config);
        
        //TODO: implement. possibly add to superclass
        launchSimulator();
                
        LOGGER.info("F15C setup completed");
    }
    
    private void launchSimulator() {
    	//run script, wait for telemetry port and first read
    }
    
    private void setup(F15CConfig config) throws FlightGearSetupException {
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
        
        //nasal script to autostart from f15c menu
        fgTelnet.runNasal("aircraft.quickstart();");
        
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
	
	@Override
	public double getAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPause(boolean isPaused) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAltitude(double targetAltitude) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getHeading() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHeading(double targetHeading) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPitch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPitch(double targetPitch) {
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.ORIENTATION_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.PITCH_FIELD, String.valueOf(targetPitch));
        
        LOGGER.info("Setting pitch to {}", targetPitch);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
	}

	@Override
	public double getRoll() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRoll(double targetRoll) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String readTelemetryRaw() throws IOException {
		return fgSockets.readTelemetry();
	}

	@Override
	public void setFuelTankLevel(double amount) {
    	LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONSUMABLES_FIELDS);
    	
        inputHash.put(F15CFields.FUEL_TANK_0_LEVEL_FIELD, String.valueOf(amount));
        inputHash.put(F15CFields.FUEL_TANK_1_LEVEL_FIELD, String.valueOf(amount));
        inputHash.put(F15CFields.FUEL_TANK_2_LEVEL_FIELD, String.valueOf(amount));
        
        LOGGER.info("Setting fuel tank level: {}", amount);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONSUMABLES_PORT);
	}

	@Override
	public double getFuelLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFuelTankCapacity() {
		// TODO Auto-generated method stub
		return 0;
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

    public synchronized void setParkingBrake(boolean brakeEnabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
            
        //visual: in the cockpit, if the brake arm is:
        //pushed in => brake is not engaged (disabled => 0)
        //pulled out => brake is engaged (enabled => 1)
        
        //requires an double value for the bool
        if(brakeEnabled) {
            inputHash.put(F15CFields.PARKING_BRAKE_FIELD, F15CFields.PARKING_BRAKE_TRUE);
        }
        else {
            inputHash.put(F15CFields.PARKING_BRAKE_FIELD, F15CFields.PARKING_BRAKE_FALSE);
        }
        
        LOGGER.info("Setting parking brake to {}", brakeEnabled);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }

    public synchronized void setElevator(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
        
        inputHash.put(F15CFields.ELEVATOR_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting elevator to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAileron(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
        
        inputHash.put(F15CFields.AILERON_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting aileron to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setAutoCoordination(boolean enabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
        
        if(enabled) {
        	inputHash.put(F15CFields.AUTO_COORDINATION_FIELD, F15CFields.AUTO_COORDINATION_TRUE);
        }
        else {
        	inputHash.put(F15CFields.AUTO_COORDINATION_FIELD, F15CFields.AUTO_COORDINATION_FALSE);
        }

        LOGGER.info("Setting autocoordination to {}", enabled);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setFlaps(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
        
        inputHash.put(F15CFields.FLAPS_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting flaps to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setRudder(double orientation) {
        LinkedHashMap<String, String> inputHash = copyStateFields(F15CFields.CONTROL_FIELDS);
        
        inputHash.put(F15CFields.RUDDER_FIELD, String.valueOf(orientation));

        LOGGER.info("Setting rudder to {}", orientation);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void resetControlSurfaces() {
    	
    	LOGGER.info("Resetting control surfaces");
    	
    	setElevator(F15CFields.ELEVATOR_DEFAULT);
    	setAileron(F15CFields.AILERON_DEFAULT);
    	setFlaps(F15CFields.FLAPS_DEFAULT);
    	setRudder(F15CFields.RUDDER_DEFAULT);
    	
    	LOGGER.info("Reset of control surfaces completed");
    }
    
    public synchronized void setSpeedUp(double speedup) {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearPlaneFields.SIM_SPEEDUP_FIELDS);
        
        inputHash.put(FlightGearPlaneFields.SIM_SPEEDUP_FIELD, String.valueOf(speedup));
        
        LOGGER.info("Setting speedup: {}", speedup);        
        
        writeSocketInput(inputHash, SOCKETS_INPUT_SIM_SPEEDUP_PORT);
    }

    public int getEngineRunning() {
        return Character.getNumericValue( getTelemetry().get(F15CFields.ENGINE_RUNNING_FIELD).charAt(0));
    }
    
    public boolean isEngineRunning() {
    	return getEngineRunning() == F15CFields.ENGINE_RUNNING_INT_TRUE;
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

    public double getFuelFlow() {
        return Double.parseDouble(getTelemetry().get(F15CFields.ENGINE_FUEL_FLOW_FIELD));
    }
    
    public double getOilPressure() {
        return Double.parseDouble(getTelemetry().get(F15CFields.ENGINE_OIL_PRESSURE_FIELD));
    }	
}
