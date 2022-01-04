package org.jason.flightgear.aircraft.alouette3;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.jason.flightgear.aircraft.FlightGearAircraft;
import org.jason.flightgear.aircraft.alouette3.config.Alouette3Config;
import org.jason.flightgear.connection.sockets.FlightGearInputConnection;
import org.jason.flightgear.connection.sockets.FlightGearTelemetryConnection;
import org.jason.flightgear.exceptions.FlightGearSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alouette3 extends FlightGearAircraft {

    private final static Logger LOGGER = LoggerFactory.getLogger(Alouette3Config.class);

    private final static int AUTOSTART_COMPLETION_SLEEP = 5000;
    
    private final static int SOCKET_WRITE_WAIT_SLEEP = 100;
    
    private FlightGearTelemetryConnection socketsTelemetryConnection;
	
    private FlightGearInputConnection consumeablesInputConnection;
    private FlightGearInputConnection orientationInputConnection;
    private FlightGearInputConnection positionInputConnection;
    private FlightGearInputConnection simFreezeInputConnection;
    
    public Alouette3() throws FlightGearSetupException {
        this(new Alouette3Config());
    }
    
    public Alouette3(Alouette3Config config) throws FlightGearSetupException  {
        super();
        
        LOGGER.info("Loading Alouette3...");
        
        //setup known ports, and the telemetry socket. start the telemetry retrieval thread.
        setup(config);
        
        //TODO: implement. possibly add to superclass. depends on superclass init and setup
        launchSimulator();
                
        LOGGER.info("Alouette3 setup completed");
    }
    
    private void launchSimulator() {
        //run script, wait for telemetry port and first read
    }
    
    private void setup(Alouette3Config config) throws FlightGearSetupException {
        LOGGER.info("setup called");
        
        //TODO: invoke port setters in superclass per config
        //networkConfig.setConsumeablesPort(config.getConsumeablesPort);
        
        try {
        	LOGGER.info("Establishing input socket connections.");
        	
        	consumeablesInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getConsumeablesInputPort());
//			controlInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getControlsInputPort());
//			enginesInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getEnginesInputPort());
//			fdmInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getFdmInputPort());
			orientationInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getOrientationInputPort());
			positionInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getPositionInputPort());
//			simInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimInputPort());
			simFreezeInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimFreezeInputPort());
//			simModelInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimModelInputPort());
//			simSpeedupInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimSpeedupInputPort());
//			simTimeInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSimTimeInputPort());
//			systemsInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getSystemsInputPort());
//			velocitiesInputConnection = new FlightGearInputConnection(networkConfig.getSocketInputHost(), networkConfig.getVelocitiesInputPort());
			
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

	@Override
	protected String readTelemetryRaw() throws IOException {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	protected void writeConsumeablesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeControlInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeFdmInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeOrientationInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writePositionInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeSystemInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeVelocitiesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub

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

	@Override
	public boolean isEngineRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLatitude(double targetLatitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLongitude(double targetLongitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAltitude(double targetAltitude) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAirSpeed(double targetSpeed) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticalSpeed(double targetSpeed) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refillFuel() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParkingBrake(boolean brakeEnabled) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEnginesInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeSimTimeInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeSimInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeSimModelInput(LinkedHashMap<String, String> inputHash) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
