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
    private final static int SOCKETS_INPUT_VELOCITIES_PORT = 6607;
    private final static int SOCKETS_INPUT_FDM_PORT = 6608;
    
    private final static int TELEMETRY_READ_SLEEP = 250;
    private final static int AUTOSTART_COMPLETION_SLEEP = 5000;
    private final static int ORIENTATION_CHANGE_SLEEP = 2500;

    private FlightGearManagerTelnet fgTelnet;
    private FlightGearManagerSockets fgSockets;
    
    private boolean runTelemetryThread;
    
    private Thread telemetryThread;
    
    private Map<String, String> currentState;
    
    //writing telemetry
    private AtomicBoolean stateWriting;
    
    //reading telemetry from socket
    private AtomicBoolean stateReading;
    
    //TODO: move all these into static fields in another class
    private final String[] ORIENTATION_FIELDS = 
    {
        "/orientation/alpha-deg",
        "/orientation/beta-deg",
        "/orientation/heading-deg",
        "/orientation/heading-magnetic-deg",
        "/orientation/pitch-deg",
        "/orientation/roll-deg",
        "/orientation/track-magnetic-deg",
        "/orientation/yaw-deg",
        "/orientation/yaw-rate-degps"
    };
    
    private final String[] POSITION_FIELDS = 
    {
        "/position/altitude-ft",
        "/position/ground-elev-ft",
        "/position/latitude-deg",
        "/position/longitude-deg"
    };
    
    private final String[] SIM_FIELDS = {
    	"/sim/model/c172p/brake-parking"	
    };
    
    private final String[] CONTROL_FIELDS = 
    {
        "/controls/electric/battery-switch",
        "/controls/engines/current-engine/mixture",
        "/controls/engines/current-engine/throttle",
        "/controls/flight/aileron",
        "/controls/flight/auto-coordination",
        "/controls/flight/auto-coordination-factor",
        "/controls/flight/elevator",
        "/controls/flight/flaps",
        "/controls/flight/rudder",
        "/controls/flight/speedbrake",
        "/controls/gear/brake-parking",
        "/controls/gear/gear-down"
    };
    
//    private final String[] FDM_FIELDS = {
//        "/fdm/jsbsim/settings/damage"    
//    };
    
    private final String[] VELOCITIES_FIELDS = {
        "/velocities/airspeed-kt",
        "/velocities/groundspeed-kt",
        "/velocities/vertical-speed-fps"
    };
            
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
                logger.debug("Telemetry thread started");
                
                readTelemetry();
                
                logger.debug("Telemetry thread returning");
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
            
            String telemetryRead = ""; 
            try {
                telemetryRead = fgSockets.readTelemetry();
                
                //if for some reason telemetryRead is not proper json, the update is dropped
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
    
    public int getParkingBrakeEnabled() {
    	//TODO: this and other fields can be missing if the protocol files are incorrect- safeguard against.
    	
    	//returned as a double like 0.000000, just look at the first character
        return Character.getNumericValue( getTelemetry().get("/sim/model/c172p/brake-parking").charAt(0));
    }
    
    public boolean isParkingBrakeEnabled() {
        return getParkingBrakeEnabled() == 1;
    }
    
    public double getAltitude() {
        return Double.parseDouble(getTelemetry().get("/position/altitude-ft"));
    }
    
    public void altitudeCheck(int maxDifference, double targetAltitude) {
        double currentAltitude = getAltitude();
        
        logger.info("Altitude check. Current {} vs target {}", currentAltitude, targetAltitude);
        
        //correct if too high or too low
        if(targetAltitude - maxDifference > currentAltitude || 
            targetAltitude + maxDifference < currentAltitude ) {
            
            logger.info("Correcting altitude to target: {}", targetAltitude);
            
            setPause(true);
            setAltitude(targetAltitude);
            setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public double getRoll() {
        return Double.parseDouble(getTelemetry().get("/orientation/roll-deg"));
    }
    
    public double getYaw() {
        return Double.parseDouble(getTelemetry().get("/orientation/yaw-deg"));
    }
    
    public double getYawRate() {
        return Double.parseDouble(getTelemetry().get("/orientation/yaw-rate-degps"));
    }
    
    public void rollCheck(int maxDifference, double targetRoll) {
        double currentRoll = getRoll();
        
        //roll is +180 to -180
        
        logger.info("Roll check. Current {} vs target {}", currentRoll, targetRoll);
        
        if( Math.abs(currentRoll) - Math.abs(targetRoll) > maxDifference) {
            logger.info("Correcting roll to target: {}", targetRoll);
            
            setPause(true);
            setRoll(targetRoll);
            setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    //Turns out this is not mutable
//    public void yawRateCheck(int maxDifference, double targetYawRate) {
//        double currentYaw = getYawRate();
//        
//        //yaw is +180 to -180
//        
//        logger.info("Yaw rate check. Current {} vs target {}", currentYaw, targetYawRate);
//        
//        if( Math.abs(currentYaw) - Math.abs(targetYawRate) > maxDifference) {
//            logger.info("Correcting yaw to target: {}", targetYawRate);
//            
//            setPause(true);
//            setYaw(targetYawRate);
//            setPause(false);
//            
//            //trailing sleep only if we made a change
//            try {
//                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
//            } catch (InterruptedException e) {
//                logger.warn("Trailing sleep interrupted", e);
//            }
//        }
//    }
    
    public double getHeading() {
        return Double.parseDouble(getTelemetry().get("/orientation/heading-deg"));
    }
    
    public void headingCheck(int maxDifference, double targetHeading) {
        
        double currentHeading = getHeading();
        double currentHeadingSin = Math.sin( Math.toRadians(currentHeading) );
        
        //heading is 0 to 360, both values are true/mag north
        
        logger.info("Heading check. Current {} vs target {}", currentHeading, targetHeading);
        
        double minHeadingSin = Math.sin( Math.toRadians(targetHeading - maxDifference) );
//        if (minHeading < 0) {
//            //-1 deg heading results in heading of 359
//            minHeading += 360;
//        }
        
        //target heading of 355 with a maxDifference of 10, is a min of 345 and a max of 5
        double maxHeadingSin = Math.sin( Math.toRadians(targetHeading + maxDifference) );
        
        logger.info("Target heading sin range {} to {}, current: {}", minHeadingSin, maxHeadingSin, currentHeadingSin);
        
        /*
         * Might be easier to normal 
         * 
         * target 0, maxDif 5, min 355, max 5
         * 
         * target 90, maxDif 10, min 80, max 100
         * 
         * target 355, maxDif 10, min 345, max 5
         */
        
        if(currentHeadingSin > maxHeadingSin || currentHeadingSin < minHeadingSin) {
            
            logger.info("Correcting heading to target: {}", targetHeading);
            
            setPause(true);
            setHeading(targetHeading);
            
            //force this here? or count on the flight app to handle the specific context
//            setPitch(0);
//            setRoll(0);
//            setYaw(0);
            
            setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public double getPitch() {
        return Double.parseDouble(getTelemetry().get("/orientation/pitch-deg"));
    }
    
    public void pitchCheck(int maxDifference, double targetPitch) {
        //read pitch
        //if pitch is too far from target in +/- directions, set to target
        
        double currentPitch = getPitch();
        
        //pitch is -180 to 180
        
        logger.info("Pitch check. Current {} vs target {}", currentPitch, targetPitch);
        
        if( Math.abs(currentPitch) - Math.abs(targetPitch) > maxDifference) {
            
            logger.info("Correcting pitch to target: {}", targetPitch);

            setPause(true);
            setPitch(targetPitch);
            setPause(false);
            
            //trailing sleep only if we made a change
            try {
                Thread.sleep(ORIENTATION_CHANGE_SLEEP);
            } catch (InterruptedException e) {
                logger.warn("Trailing sleep interrupted", e);
            }
        }
    }
    
    public void forceStabilize(double heading, double altitude, double roll, double pitch, double yaw) {
        
        logger.info("forceStablize called");
        
        //TODO: check if paused
        
        LinkedHashMap<String, String> orientationFields = copyStateFields(ORIENTATION_FIELDS);
        
        //get telemetry hash
        
        orientationFields.put("/orientation/heading-deg", "" + heading);
        orientationFields.put("/orientation/pitch-deg", "" + pitch);
        orientationFields.put("/orientation/roll-deg", "" + roll);
        
        writeSocketInput(orientationFields, SOCKETS_INPUT_ORIENTATION_PORT);
        
        //setAltitude(altitude);
        altitudeCheck(500, altitude);
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
    
    public void startupPlane() {
        
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
        
        //TODO: lock on write
        fgSockets.writeInput(inputHash, port);

    }
    
    public synchronized void setPause(boolean isPaused) {
        
        //TODO: check telemetry if already paused
        
        //resolve sim_freeze port
        //if(controlInputs.containsKey(PAUSE_INPUT)) {
            //FlightGearInput input = controlInputs.get(PAUSE_INPUT);
            
            LinkedHashMap<String, String> inputHash = new LinkedHashMap<String, String>();
        
            //oh get fucked. requires an int value for the bool, despite the schema specifying a bool.
            //hardcode the string values because i don't want to have to deal with parse* calls or casting here
            if(isPaused) {
                logger.debug("Pausing simulation");
                inputHash.put("/sim/freeze/clock", "1");
                inputHash.put("/sim/freeze/master", "1");
            }
            else {
                logger.debug("Unpausing simulation");
                inputHash.put("/sim/freeze/clock", "0");
                inputHash.put("/sim/freeze/master", "0");
            }
            
            //clock and master are the only two fields, no need to retrieve from the current state
            //order matters. defined in input xml schema

            //socket writes typically require pauses so telemetry/state aren't out of date
            //however this is an exception
            writeSocketInput(inputHash, SOCKETS_INPUT_SIM_FREEZE_PORT);
            
            //trailing sleep, so that the last real telemetry read arrives
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            	logger.warn("setPause trailing sleep interrupted", e);
            }
    }
    
    public synchronized void setDamageEnabled(boolean damageEnabled) {
        LinkedHashMap<String, String> inputHash = new LinkedHashMap<String, String>();
        
        logger.debug("Toggling damage enabled: {}", damageEnabled);
        
        //requires an int value for the bool
        if(!damageEnabled) {
            inputHash.put("/fdm/jsbsim/settings/damage","0");
        }
        else {
            inputHash.put("/fdm/jsbsim/settings/damage","1");
        }
        
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
        
        LinkedHashMap<String, String> inputHash = copyStateFields(ORIENTATION_FIELDS);
        
        //get telemetry hash
        
        inputHash.put("/orientation/heading-deg", "" + heading);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setPitch(double pitch) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(ORIENTATION_FIELDS);
        
        inputHash.put("/orientation/pitch-deg", "" + pitch);
        
        logger.info("Setting pitch to {}", pitch);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setRoll(double roll) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(ORIENTATION_FIELDS);
                
        inputHash.put("/orientation/roll-deg", "" + roll);
        
        logger.info("Setting roll to {}", roll);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setAltitude(double altitude) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(POSITION_FIELDS);
        
        inputHash.put("/position/altitude-ft", "" + altitude);
        
        logger.info("Setting altitude to {}", altitude);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_POSITION_PORT);
    }
    
    public synchronized void setYaw(double yaw) {

        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(ORIENTATION_FIELDS);
                
        inputHash.put("/orientation/yaw-deg", "" + yaw);
        
        logger.info("Setting yaw to {}", yaw);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_ORIENTATION_PORT);
    }
    
    public synchronized void setAirSpeed(double speed) {
        //TODO: check if paused
        
        LinkedHashMap<String, String> inputHash = copyStateFields(VELOCITIES_FIELDS);
                
        inputHash.put("/velocities/airspeed-kt", "" + speed);
        
        logger.info("Setting air speed to {}", speed);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_VELOCITIES_PORT);
    }
    
    public synchronized void setThrottle(double throttle ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(CONTROL_FIELDS);
        
        inputHash.put("/controls/engines/current-engine/throttle", "" + throttle);
        
        logger.info("Setting throttle to {}", throttle);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setMixture(double mixture ) {
        LinkedHashMap<String, String> inputHash = copyStateFields(CONTROL_FIELDS);
        
        inputHash.put("/controls/engines/current-engine/mixture", "" + mixture);
        
        logger.info("Setting mixture to {}", mixture);
        
        writeSocketInput(inputHash, SOCKETS_INPUT_CONTROLS_PORT);
    }
    
    public synchronized void setParkingBrake(boolean brakeEnabled) {
        LinkedHashMap<String, String> inputHash = copyStateFields(SIM_FIELDS);
            
        //visual: in the cockpit, if the brake arm is:
        //pushed in => brake is not engaged (disabled => 0)
        //pulled out => brake is engaged (enabled => 1)
        
        //requires an int value for the bool
        if(brakeEnabled) {
            inputHash.put("/sim/model/c172p/brake-parking", "" + (double)1);
        }
        else {
            inputHash.put("/sim/model/c172p/brake-parking", "" + (double)0);
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
