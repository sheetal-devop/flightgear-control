package org.jason.fgcontrol.aircraft;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;
import org.jason.fgcontrol.aircraft.fields.FlightGearFields;
import org.jason.fgcontrol.connection.sockets.FlightGearInputConnection;
import org.jason.fgcontrol.connection.telnet.FlightGearTelnetConnection;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.flight.position.TrackPosition;
import org.jason.fgcontrol.flight.position.WaypointManager;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.jason.fgcontrol.flight.util.FlightLog;
import org.jason.fgcontrol.sshd.SSHDServer;
import org.jason.fgcontrol.view.CameraViewer;
import org.jason.fgcontrol.view.mjpeg.MJPEGStreamer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlightGearAircraft {

    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearAircraft.class);
    
    private final static long TELEMETRY_FIRST_READ_TIMEOUT = 3000L;
    private final static long TELEMETRY_READ_WAIT_SLEEP = 100L;
    private final static long TELEMETRY_READ_TRAILING_SLEEP = 100L;
    private final static long TELEMETRY_WRITE_WAIT_SLEEP = 100L;
    
    //private final static int MAX_TELEMETRY_READ_LEN = 16384;
    
    private final static int POST_PAUSE_SLEEP = 125;
    
    private boolean runTelemetryThread;
    
    private Thread readTelemetryThread;
    
    protected Map<String, String> currentState;
    
    //writing telemetry
    protected AtomicBoolean telemetryStateWriting;
    
    //reading telemetry from socket
    protected AtomicBoolean telemetryStateReading;
    
    protected SimulatorConfig simulatorConfig;
    
    /////////
    
    protected WaypointManager waypointManager;
    protected WaypointPosition currentWaypointTarget;
    
    protected FlightLog flightLog;
    
    protected AtomicBoolean abandonCurrentWaypoint;

    protected boolean enabledCameraStreamer;   
	
    protected String cameraViewHost;
	protected int cameraViewPort;
	protected String cameraViewProto;
	protected String cameraViewResource;

	private MJPEGStreamer cameraStreamer;
	
	protected boolean enabledSSHDServer;
	private SSHDServer sshdServer;

    public FlightGearAircraft() {
        this(new SimulatorConfig());
    }
    
    public FlightGearAircraft(SimulatorConfig config) {
    	simulatorConfig = config;
    	
        //linkedhashmap to match the xml schema loaded into the simulator
        currentState = Collections.synchronizedMap(new LinkedHashMap<String, String>());
        
        telemetryStateWriting = new AtomicBoolean(false);
        telemetryStateReading = new AtomicBoolean(false);
        
        abandonCurrentWaypoint = new AtomicBoolean(false);
        
        enabledCameraStreamer = false;
        enabledSSHDServer = false;
        
        ///////////////
        //if the camera view host is not defined in the config, don't build the camera viewer
        
        //just catch the exception and proceed without enabling the camera viewer if we can't access the camera feed
        if(simulatorConfig.isCameraStreamEnabled()) {
        	LOGGER.info("Configuring camera streaming.");
        	
        	String cameraViewHost = simulatorConfig.getCameraViewerHost();
        	
        	int cameraViewPort = simulatorConfig.getCameraViewerPort();
        	int streamerPort = simulatorConfig.getCameraStreamPort();
        	
        	CameraViewer cameraViewer = null;
        	
        	try {
				cameraViewer = new CameraViewer(cameraViewHost, cameraViewPort);
				
				cameraStreamer = new MJPEGStreamer(cameraViewer, streamerPort);
				
	        	//will likely need to be available for edge apps to set uri for external viewing
	        	this.cameraViewHost = cameraViewHost;
	        	this.cameraViewPort = cameraViewPort;
	        	this.cameraViewProto = MJPEGStreamer.CAMERA_VIEW_HTTP_ENDPOINT_PROTO;
	        	this.cameraViewResource = MJPEGStreamer.CAMERA_VIEW_HTTP_ENDPOINT;
	        	
				
				enabledCameraStreamer = true;
				
				LOGGER.info("Camera streaming configuration completed successfully.");
			} catch (URISyntaxException e) {
				LOGGER.error("URI exception setting up camera streaming", e);
			} catch (IOException e) {
				LOGGER.error("IOException setting up camera streaming", e);
			} finally {
				if(!enabledCameraStreamer) {
					LOGGER.error("Camera Streaming setup failed. Continuing");
				} 
			}
        }
        else {
        	LOGGER.info("Proceeding with Camera streaming disabled");
        }
        
        //sshd server
        
        if(simulatorConfig.isSSHDServerEnabled()) {
        	LOGGER.info("Configuring SSHD Server.");
        	
        	String user = simulatorConfig.getSshdUser();
        	String pass = simulatorConfig.getSshdPass();
        	String homeDir = simulatorConfig.getSshdHomeDir();
        	int port = simulatorConfig.getSshdPort();
        	
        	sshdServer = new SSHDServer(user,pass,port,homeDir);
        	enabledSSHDServer = true;
        	
        	LOGGER.info("SSHD Server configuration completed successfully.");
        }
        else {
        	LOGGER.info("Proceeding with SSHD disabled");
        }
        
        //no caltrops client, handled by application
        //config comes from the app, so directives are available there
        ///////////////
        
        initWaypointManager();
        initFlightLog();
    }

    ////////////
    //waypoint management
    
    protected void initWaypointManager() {
    	waypointManager = new WaypointManager();
    }
    
    //add new waypoint to the end of the flightplan
    public void addWaypoint(double lat, double lon) {
        waypointManager.addWaypoint(new WaypointPosition(lat, lon));
    }
    
    //add new waypoint to the end of the flightplan
    public void addWaypoint(WaypointPosition newWaypoint) {        
    	waypointManager.addWaypoint(newWaypoint);
    }
    
    public void addNextWaypoint(double lat, double lon) {      
    	waypointManager.addNextWaypoint(new WaypointPosition(lat, lon));
    }
    
    public void addNextWaypoint(WaypointPosition newWaypoint) {      
    	waypointManager.addNextWaypoint(newWaypoint);
    }
    
    public WaypointPosition getNextWaypoint() {
        return waypointManager.getNextWaypoint();
    }
    
    public WaypointPosition getAndRemoveNextWaypoint() {
        return waypointManager.getAndRemoveNextWaypoint();
    }
    
    public int getWaypointCount() {
        return waypointManager.getWaypointCount();
    }

    public List<WaypointPosition> getWaypoints() {
        return waypointManager.getWaypoints();
    }
    
    public void setWaypoints(List<WaypointPosition> waypoints) {
    	waypointManager.setWaypoints(waypoints);
    }
    
    public void removeWaypoints(double lat, double lon) {
    	waypointManager.removeWaypoints(lat, lon);
    }
    
    public void clearWaypoints() {
    	waypointManager.reset();
    }
    
    public void setCurrentWaypointTarget(WaypointPosition waypointTarget) {
    	this.currentWaypointTarget = waypointTarget;
    }
    
    public WaypointPosition getCurrentWaypointTarget() {
    	return currentWaypointTarget;
    }
    
    /**
     * Signal to the plane to abandon the current target waypoint. Depending on the flightplan implementation, this 
     * may not be immediate.
     */
    public void abandonCurrentWaypoint() {
    	abandonCurrentWaypoint.set(true);
    }
    
    public void resetAbandonCurrentWaypoint() {
    	abandonCurrentWaypoint.set(false);
    }
    
    public boolean shouldAbandonCurrentWaypoint() {
    	return abandonCurrentWaypoint.get();
    }
    
    ////////////
    //flight log
    
    protected void initFlightLog() {
    	flightLog = new FlightLog();
    }
    
    public void addWaypointToFlightLog(WaypointPosition newWaypoint) {
    	flightLog.addWaypoint(newWaypoint);
    }
    
    public void addTrackPositionToFlightLog(TrackPosition trackPosition) {
    	flightLog.addTrackPosition(trackPosition);	
    }
    
    public void writeFlightLogGPX(String fileName) {
    	//TODO: return a Document and let the invoker write it how/where they want
    	flightLog.writeGPXFile(fileName);
    }
    
    public int getFlightLogTrackPositionCount() {
    	return flightLog.getTrackPositionCount();
    }
    
    public int getFlightLogWaypointCount() {
    	return flightLog.getTrackPositionCount();
    }
    
    ////////////
    //background threads
    
    //telemetry thread
    protected void launchTelemetryThread() throws FlightGearSetupException {
        
    	boolean initialReadCompleted = false;
    	long waitTime = 0L;
    	
    	runTelemetryThread = true;
        
        readTelemetryThread = new Thread() {
            @Override
            public void run() {
            	if(LOGGER.isTraceEnabled()) {
            		LOGGER.trace("Telemetry thread started");
            	}
                
                readTelemetry();
                
                if(LOGGER.isTraceEnabled()) {
                	LOGGER.trace("Telemetry thread returning");
                }
            }
        };
        readTelemetryThread.start();
        
        //TODO: fail gracefully if this never succeeds
        //wait for the first read to arrive, it may take a bit
        while( waitTime <= TELEMETRY_FIRST_READ_TIMEOUT) {
        	
        	if( currentState.size() > 0 ) {
        		initialReadCompleted = true;
        		break;
        	}
        	
        	if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Waiting for first telemetry read to complete after thread start");
        	}
            
            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            	waitTime += TELEMETRY_WRITE_WAIT_SLEEP;
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        if(initialReadCompleted) {
        	LOGGER.info("Launched telemetry thread and received initial read");
        }
        else {
        	LOGGER.error("Could not complete initial telemetry read. Aborting.");
        	
        	//stop the telemetry thread
        	runTelemetryThread = false;
        	throw new FlightGearSetupException("Could not complete initial telemetry read. Check that the simulator is running and listening on the correct ports.");
        }
    }
    
    //cam feed thread
    protected void launchCameraStreamer() {
    	
    	if(!enabledCameraStreamer || cameraStreamer == null) {
    		LOGGER.error("launchCameraViewThread found a null cameraViewer. aborting launch of camera viewer.");
    		return;
    	}

        LOGGER.info("Camera Streaming starting...");
    	
        //null check done earlier in function
    	//underlying http server internally launched in background thread, so don't create one here
        cameraStreamer.start();

        LOGGER.info("Camera Streaming started.");
    }
    
    protected void launchSSHDServer() throws IOException {
    	if(!enabledSSHDServer || sshdServer == null ) {
    		LOGGER.error("launchSSHDServerThread found a null SSHDServer. aborting launch of SSHD Server.");
    		return;
    	}
    	
    	LOGGER.info("SSHD Server starting...");
    	
        //null check done earlier in function
    	//underlying sshd server internally launched in background thread, so don't create one here
		sshdServer.start();
		
		LOGGER.info("SSHD Server started.");
    }
    
    ////////////
    
    protected LinkedHashMap<String, String> copyStateFields(String[] fields) {
        LinkedHashMap<String, String> retval = new LinkedHashMap<>();
        
        while(telemetryStateWriting.get()) {
        	if(LOGGER.isTraceEnabled()) {
        		LOGGER.trace("Waiting for state writing to complete");
        	}

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("copyStateFields: Socket read wait interrupted", e);
            }
        }
        
        telemetryStateReading.set(true);
        for(String field : fields) {
            if(currentState.containsKey(field)) {
                retval.put(field, currentState.get(field));
            }
            else
            {
                LOGGER.warn("Current state missing field: " + field);
            }
        }
        telemetryStateReading.set(false);
        
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
        Map<String, String> retval = new HashMap<String, String>();
                
        //TODO: time this out, trigger some kind of reset or clean update
        while(telemetryStateWriting.get()) {
        	if(LOGGER.isTraceEnabled()) {
        		LOGGER.trace("Waiting for state writing to complete");
        	}

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
                
        telemetryStateReading.set(true);
        retval.putAll(currentState);
        telemetryStateReading.set(false);
        
        return retval;
    }
    
    /**
     * Get a multiple telemetry fields to cut down on locking
     * 
     * Not working ATM, just pasted pocs
     * 
     * @param fieldName    Field names to lookup
     * @return    an ordered list of values for the fields. nulls if the field isn't in the map.
     */
    public synchronized List<String> getTelemetryFields(List<String> fieldNames) {
    	
    	//TODO: implement
    	
        List<String> retval = new ArrayList<String>(fieldNames.size());
        
        Set<String> keyList = new HashSet<String>();
        keyList.addAll(fieldNames);
        
        //TODO: time this out, trigger some kind of reset or clean update
        while(telemetryStateWriting.get()) {
        	if(LOGGER.isTraceEnabled()) {
        		LOGGER.trace("Waiting for state writing to complete");
        	}

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        telemetryStateReading.set(true);
        
      //TODO: get working for types
//        retval = currentState.entrySet()
//                .stream()
//                .filter(ent -> keyList.contains(ent.getKey()))
//                .map(Map.Entry::getValue)
//                .collect(Collectors.toList());
        
        telemetryStateReading.set(false);
        
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
        while(telemetryStateWriting.get()) {
        	if(LOGGER.isTraceEnabled()) {
        		LOGGER.trace("Waiting for state writing to complete");
        	}

            try {
                Thread.sleep(TELEMETRY_WRITE_WAIT_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("getTelemetry: Socket read wait interrupted", e);
            }
        }
        
        telemetryStateReading.set(true);
        retval = currentState.get(fieldName);
        telemetryStateReading.set(false);
        
        return retval;
    }
    
    /**
     * Shut down the aircraft, simulator connections, and subcomponents.
     */
    public void shutdown() {
    	LOGGER.info("Aircraft shutdown initiated.");
        
    	LOGGER.info("Telemetry thread shutting down...");
    	
        //stop telemetry read thread
        runTelemetryThread = false;
                
        int waitTime = 0;
        int interval = 250;
        int maxWait = 4000;
        while(readTelemetryThread.isAlive()) {
        	LOGGER.debug("waiting on telemetry thread to terminate");
            
            if(waitTime >= maxWait) {
            	LOGGER.warn("interrupting readTelemetryThread as part of shutdown");
                readTelemetryThread.interrupt();
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

    	LOGGER.debug("Telemetry thread terminated. isAlive: {}", readTelemetryThread.isAlive());
    	
    	LOGGER.info("Telemetry thread shut down completed.");
    	
    	//stop camera streamer
    	if(enabledCameraStreamer) {
    		LOGGER.info("Shutting down camera streamer...");
    		cameraStreamer.shutdown();
    		LOGGER.info("Camera streamer shutdown completed.");
    	}
    	
    	//stop sshd server
    	if(enabledSSHDServer) {
    		LOGGER.info("Shutting down SSHD server...");
    		sshdServer.shutdown();
    		LOGGER.info("SSHD server completed.");
    	}
    	
    	LOGGER.info("Aircraft shutdown completed.");
    }
    
    
    /**
     * Internal telemetry retrieval process.
     * 
     * TODO: time out initial lock seek.
     * TODO: switch from atomicboolean to an actual lock
     * TODO: Stringbuffer implementation
     * TODO: length check incoming telemetry 
     * 
     */
    protected void readTelemetry() {
        
        String telemetryRead = null;
        while(runningTelemetryThread()) {
            
        	if(LOGGER.isTraceEnabled()) {
        		LOGGER.trace("Begin telemetry read cycle");
        	}
            
            //wait for any state read operations to finish
            //TODO: max wait on this
            while(telemetryStateReading.get()) {
            	if(LOGGER.isTraceEnabled()) {
            		LOGGER.trace("Waiting for state reading to complete");
            	}

                try {
                    Thread.sleep(TELEMETRY_READ_WAIT_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Polling state read sleep interrupted", e);
                }
            }
            
            telemetryStateWriting.set(true);
            
            //read from socket connection. retrieves json string. write state to map
            //TODO: make this not awful

            try {
            	//flush read buffer and read in the new telemetry data
            	telemetryRead = null;
                telemetryRead = readTelemetryRaw();
              
                //TODO: incomplete updates happen infrequently, attempt to clean and correct
                
                if(telemetryRead != null && telemetryRead.charAt(0) == '{') {
                    //if for some reason telemetryRead is not proper json, the update is dropped
                    final JSONObject jsonTelemetry = new JSONObject(telemetryRead);
                    
                    if(jsonTelemetry != null) {
                        jsonTelemetry.keySet().forEach( 
                            keyStr -> {
                                currentState.put(keyStr, jsonTelemetry.get(keyStr).toString());
                            }    
                        );
                        
                        if(LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Read {} telemetry fields", jsonTelemetry.keySet().size());
                        }
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
                telemetryStateWriting.set(false);
                
                //sleep before next update. successful or not
                try {
                    Thread.sleep(TELEMETRY_READ_TRAILING_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.warn("Trailing state read sleep interrupted", e);
                }
                
                if(LOGGER.isTraceEnabled()) {
                	LOGGER.trace("End telemetry read cycle");
                }
            }
        }
        
    	if(LOGGER.isTraceEnabled()) {
    		LOGGER.trace("readTelemetry returning");
    	}
    }
    
    /////////////////
    //simulator management
    
    public void resetSimulator() throws IOException, InvalidTelnetOptionException {
    	LOGGER.debug("Simulator reset invoked");
        
        FlightGearTelnetConnection telnetSession = null;

        try {
            telnetSession = new FlightGearTelnetConnection(simulatorConfig.getTelnetHost(), simulatorConfig.getTelnetPort());
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
            telnetSession = new FlightGearTelnetConnection(simulatorConfig.getTelnetHost(), simulatorConfig.getTelnetPort());
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
    protected abstract void writeEnginesInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeFdmInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeOrientationInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writePositionInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimFreezeInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimModelInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimSpeedupInput(LinkedHashMap<String, String> inputHash) throws IOException;
    protected abstract void writeSimTimeInput(LinkedHashMap<String, String> inputHash) throws IOException;
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
    
    public synchronized void setCurrentView(int viewNumber) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.SIM_INPUT_FIELDS);
        
        inputHash.put(FlightGearFields.SIM_CURRENT_VIEW_NUMBER, String.valueOf(viewNumber));
        
        LOGGER.info("Setting current view to {}", viewNumber);
        
        writeSimInput(inputHash);
    }
    
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
     * @param targetSpeedup    Speedup factor. Acceptable values are 0.5,1,2,4,8,16,32. 32 is pretty slow. 
     * @throws IOException
     */
    public synchronized void setSimSpeedUp(double targetSpeedup) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.SIM_SPEEDUP_FIELDS);
        
        LOGGER.info("Setting sim speedup: {}", targetSpeedup);
        
        inputHash.put(FlightGearFields.SIM_SPEEDUP_FIELD, String.valueOf(targetSpeedup));
        
        writeSimSpeedupInput(inputHash);
    }
    
    /**
     * Set the date time of the sim. useful for avoiding icing problems by flying in summer, or ensuring daytime light"
     * examples: "2020-07-03T12:00:00" => July 3, 2020 at 12pm, "2021-12-31T23:59:59" => Dec 31, 11:59:59pm.
     * 
     * 
     * @param dateTime    The datetime string in GMT of the new time in format "yyyy-mm-ddThh:mm:ss"
     * @throws IOException
     */
    public synchronized void setGMT(String dateTime) throws IOException {
        LinkedHashMap<String, String> inputHash = copyStateFields(FlightGearFields.SIM_TIME_INPUT_FIELDS);
        
        LOGGER.info("Setting sim GMT datetime: {}", dateTime);
        
        //TODO: sim itself drops bad datetimes, but enforce it here too
        
        inputHash.put(FlightGearFields.SIM_TIME_GMT_FIELD, dateTime);
        
        writeSimTimeInput(inputHash);
    }
    
    ///////////////////
    
    /**
     * Refill necessary fuel tanks to capacity
     * @throws IOException 
     */
    public abstract void refillFuel() throws IOException;
    
    /**
     * Refill necessary fuel tanks to specified level
     * @throws IOException 
     */
    public abstract void refillFuel(double level) throws IOException;
    
    public synchronized TrackPosition getPosition() {
        return new TrackPosition(getLatitude(), getLongitude(), getAltitude(), getGMT());
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
    
    //weight
    public double getWeight() {
        return Double.parseDouble(getTelemetryField(FlightGearFields.FDM_WEIGHT));
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
    
    public String getGMT() {
        return getTelemetryField(FlightGearFields.SIM_TIME_GMT_FIELD);
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
