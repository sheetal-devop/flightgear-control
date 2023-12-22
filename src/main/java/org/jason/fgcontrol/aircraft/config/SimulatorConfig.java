package org.jason.fgcontrol.aircraft.config;

import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorConfig {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(SimulatorConfig.class);
	
	public final static int UNCONFIG_PORT = -1;

	//telemetry
	public final static String DEFAULT_SOCKETS_TELEM_HOST = "localhost";
    public final static int DEFAULT_SOCKETS_TELEM_PORT = 6501;
    
    //camera view
    //port is unconfigured since it may not always run
    public final static String DEFAULT_CAMERA_VIEW_HOST = null;   
    public final static int DEFAULT_CAMERA_VIEW_PORT = UNCONFIG_PORT;   
    
    //camera stream - 
    //port is unconfigured since it may not always run
    public final static String DEFAULT_CAMERA_STREAM_HOST = null;   
    public final static int DEFAULT_CAMERA_STREAM_PORT = UNCONFIG_PORT;  
    
    //telnet
    public final static String DEFAULT_TELNET_HOST = "localhost";
    public final static int DEFAULT_TELNET_PORT = 5501;

    //socket input
    public final static String DEFAULT_SOCKETS_INPUT_HOST = "localhost";
    public final static int DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    public final static int DEFAULT_SOCKETS_INPUT_CONTROLS_PORT = 6602;
    public final static int DEFAULT_SOCKETS_INPUT_ENGINES_PORT = 6603;
    public final static int DEFAULT_SOCKETS_INPUT_FDM_PORT = 6604;
    public final static int DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT = 6605;
    public final static int DEFAULT_SOCKETS_INPUT_POSITION_PORT = 6606;
    public final static int DEFAULT_SOCKETS_INPUT_SIM_PORT = 6607;
    public final static int DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT = 6608;
    public final static int DEFAULT_SOCKETS_INPUT_SIM_MODEL_PORT = 6609;
    public final static int DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6610;
    public final static int DEFAULT_SOCKETS_INPUT_SIM_TIME_PORT = 6611;
    public final static int DEFAULT_SOCKETS_INPUT_SYSTEM_PORT = 6612;
    public final static int DEFAULT_SOCKETS_INPUT_VELOCITIES_PORT = 6613;

    //internal sshd - host will likely always be localhost
    //port is unconfigured since it may not always run
    public final static String DEFAULT_SSHD_HOME_DIR = "/tmp/flightgear-control";
    public final static int DEFAULT_SSHD_PORT = UNCONFIG_PORT;
	public final static String DEFAULT_SSHD_USER = "edge";
	public final static String DEFAULT_SSHD_PASS = "twxedge";
	
	public final static String DEFAULT_AIRCRAFT_NAME = "Aircraft_Default";
    
    protected String telemetryOutputHost;
    protected int telemetryOutputPort;

    protected String telnetHost;
    protected int telnetPort;
    
    //expect the same host for all inputs. hard to imagine a plane in a sim on multiple hosts
    protected String controlInputHost;

    protected int consumeablesInputPort;
    protected int controlsInputPort;
    protected int enginesInputPort;
    protected int fdmInputPort;
    protected int orientationInputPort;
    protected int positionInputPort;
    protected int simInputPort;
    protected int simFreezeInputPort;
    protected int simModelInputPort;
    protected int simSpeedupInputPort;
    protected int simTimeInputPort;
    protected int systemsInputPort;
    protected int velocitiesInputPort;
    
    protected String cameraViewHost;
    protected int cameraViewPort;
    
    protected String cameraStreamHost;
    protected int cameraStreamPort;
    
    protected int sshdPort;
    protected String sshdHomeDir;
    protected String sshdUser;
    protected String sshdPass;
    
    protected String flightPlanName;
    
    protected String aircraftName;

	private final static int MAX_STR_INPUT_LEN = 1024;
    
	/**
	 * Simple option for simple sim setups and testing. default control input ports are still 
     * set but the sim probably doesn't have them open
	 * 
	 */
	public SimulatorConfig() {
        
        telemetryOutputHost = DEFAULT_SOCKETS_TELEM_HOST;
        telemetryOutputPort = DEFAULT_SOCKETS_TELEM_PORT;
        
        telnetHost = DEFAULT_TELNET_HOST;
        telnetPort = DEFAULT_TELNET_PORT;
        
        controlInputHost = DEFAULT_SOCKETS_INPUT_HOST;
        
        consumeablesInputPort = DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT;
        controlsInputPort = DEFAULT_SOCKETS_INPUT_CONTROLS_PORT;
        enginesInputPort = DEFAULT_SOCKETS_INPUT_ENGINES_PORT;
        fdmInputPort = DEFAULT_SOCKETS_INPUT_FDM_PORT;
        orientationInputPort = DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT;
        positionInputPort = DEFAULT_SOCKETS_INPUT_POSITION_PORT;
        simInputPort = DEFAULT_SOCKETS_INPUT_SIM_PORT;
        simFreezeInputPort = DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT;
        simModelInputPort = DEFAULT_SOCKETS_INPUT_SIM_MODEL_PORT;
        simSpeedupInputPort = DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT;
        simTimeInputPort = DEFAULT_SOCKETS_INPUT_SIM_TIME_PORT;
        systemsInputPort = DEFAULT_SOCKETS_INPUT_SYSTEM_PORT;
        velocitiesInputPort = DEFAULT_SOCKETS_INPUT_VELOCITIES_PORT;
        
        //defaults to signal not to configure- config must define all
        cameraViewHost = DEFAULT_CAMERA_VIEW_HOST;
        cameraViewPort = DEFAULT_CAMERA_VIEW_PORT;

        //defaults to signal not to configure- config must define all
        cameraStreamHost = DEFAULT_CAMERA_STREAM_HOST;
        cameraStreamPort = DEFAULT_CAMERA_STREAM_PORT;
        
        //port dictates whether or not to configure an sshd server
        sshdPort = DEFAULT_SSHD_PORT;
        sshdHomeDir = DEFAULT_SSHD_HOME_DIR;
        sshdUser = DEFAULT_SSHD_USER;
        sshdPass = DEFAULT_SSHD_PASS;
        
        flightPlanName = null;
        
        aircraftName = DEFAULT_AIRCRAFT_NAME;
    }

    /**
     * Another simple option for simple sim setups and testing. default control input ports are still 
     * set but the sim probably doesn't have them open
     * 
     * @param telnetHost
     * @param telnetPort
     * @param socketsTelemHost
     * @param socketsTelemPort
     */
    public SimulatorConfig(String telnetHost, int telnetPort, String socketsTelemHost, int socketsTelemPort) 
    {
    	//set defaults and let propertiesFile overwrite
    	this();
    	
        this.telemetryOutputHost = socketsTelemHost;
        this.telemetryOutputPort = socketsTelemPort;
        
        this.telnetHost = telnetHost;
        this.telnetPort = telnetPort;
	}
    
    /**
     * Read in config from a properties object. Common usage for project- expect most of these to be defined.
     * 
     * @param configProperties
     */
    public SimulatorConfig(Properties configProperties) {
    	//set defaults and let propertiesFile overwrite
    	this();
    	
    	LOGGER.debug("Loading SimulatorConfig from properties");
    	
    	//override the defaults with what the config file defines
    	load(configProperties);
    }
    
    /**
     * Read in config from a JSON string.
     * 
     * @param configJSONStr
     */
    public SimulatorConfig(String configJSONStr) {
    	this();
    	
    	LOGGER.debug("Loading SimulatorConfig from JSON string");
    	
    	if(configJSONStr.length() > MAX_STR_INPUT_LEN) {
    		LOGGER.error("Successfully processed config JSON string");
    		
    		//error out since truncating will likely not result in valid json
    		throw new RuntimeException("Config JSON string larger than maximum");
    	}
    	else if(configJSONStr != null && configJSONStr.charAt(0) == '{') {
            JSONObject configJSON = new JSONObject(configJSONStr);
            
            Properties configProperties = new Properties();
            
            String value;
            for( String key : configJSON.keySet() ) {
            	if(ConfigDirectives.KNOWN_CONFIG_DIRECTIVES.contains(key)) {
            		        
            		value = String.valueOf(configJSON.get(key));
     		
            		configProperties.setProperty(key, value);
            		LOGGER.debug(key + " => " + value);
            	}
            }
            	
        	LOGGER.debug("Successfully processed config JSON string");
            
            load(configProperties);
        } else {
        	LOGGER.error("Failed to read config JSON string");
        	throw new RuntimeException("Failed to read config JSON string");
        }
    }
    
    /////////////
    private void load(Properties configProperties) {
    	
    	////////////////
    	//telemetry output
    	if(configProperties.containsKey(ConfigDirectives.TELEM_OUTPUT_HOST_DIRECTIVE)) {
    		telemetryOutputHost = configProperties.getProperty(ConfigDirectives.TELEM_OUTPUT_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.TELEM_OUTPUT_PORT_DIRECTIVE)) {
    		telemetryOutputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.TELEM_OUTPUT_PORT_DIRECTIVE));
    	}
    	
    	////////////////
    	//telnet server
    	if(configProperties.containsKey(ConfigDirectives.TELNET_HOST_DIRECTIVE)) {
    		telnetHost = configProperties.getProperty(ConfigDirectives.TELNET_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.TELNET_PORT_DIRECTIVE)) {
    		telnetPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.TELNET_PORT_DIRECTIVE));
    	}
    	
    	////////////////
    	//httpd server with camera view
    	if(configProperties.containsKey(ConfigDirectives.CAMERA_VIEW_HOST_DIRECTIVE)) {
    		cameraViewHost = configProperties.getProperty(ConfigDirectives.CAMERA_VIEW_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.CAMERA_VIEW_PORT_DIRECTIVE)) {
    		cameraViewPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CAMERA_VIEW_PORT_DIRECTIVE));
    	}
    	
    	////////////////
    	//application camera view
    	//TODO: framerate or between-frame sleep config param
    	
    	if(configProperties.containsKey(ConfigDirectives.CAMERA_STREAM_HOST_DIRECTIVE)) {
    		cameraStreamHost = configProperties.getProperty(ConfigDirectives.CAMERA_STREAM_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.CAMERA_STREAM_PORT_DIRECTIVE)) {
    		cameraStreamPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CAMERA_STREAM_PORT_DIRECTIVE));
    	}
    	
    	////////////////
    	//control input
    	if(configProperties.containsKey(ConfigDirectives.CONTROL_INPUT_HOST_DIRECTIVE)) {
    		controlInputHost = configProperties.getProperty(ConfigDirectives.CONTROL_INPUT_HOST_DIRECTIVE);
    	}
    	
    	//consumeables
    	if(configProperties.containsKey(ConfigDirectives.CONSUMEABLES_PORT_DIRECTIVE)) {
    		consumeablesInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CONSUMEABLES_PORT_DIRECTIVE));
    	}
    	
    	//controls
    	if(configProperties.containsKey(ConfigDirectives.CONTROLS_PORT_DIRECTIVE)) {
    		controlsInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CONTROLS_PORT_DIRECTIVE));
    	}
    	
    	//engines
    	if(configProperties.containsKey(ConfigDirectives.ENGINES_PORT_DIRECTIVE)) {
    		enginesInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.ENGINES_PORT_DIRECTIVE));
    	}
    	
    	//fdm
    	if(configProperties.containsKey(ConfigDirectives.FDM_PORT_DIRECTIVE)) {
    		fdmInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.FDM_PORT_DIRECTIVE));
    	}
    	
    	//orientation
    	if(configProperties.containsKey(ConfigDirectives.ORIENTATION_PORT_DIRECTIVE)) {
    		orientationInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.ORIENTATION_PORT_DIRECTIVE));
    	}
    	
    	//position
    	if(configProperties.containsKey(ConfigDirectives.POSITION_PORT_DIRECTIVE)) {
    		positionInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.POSITION_PORT_DIRECTIVE));
    	}
    	
    	//sim
    	if(configProperties.containsKey(ConfigDirectives.SIM_PORT_DIRECTIVE)) {
    		simInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SIM_PORT_DIRECTIVE));
    	}
    	
    	//sim freeze
    	if(configProperties.containsKey(ConfigDirectives.SIM_FREEZE_PORT_DIRECTIVE)) {
    		simFreezeInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SIM_FREEZE_PORT_DIRECTIVE));
    	}
    	
    	//sim model
    	if(configProperties.containsKey(ConfigDirectives.SIM_MODEL_PORT_DIRECTIVE)) {
    		simModelInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SIM_MODEL_PORT_DIRECTIVE));
    	}
    	
    	//sim speedup
    	if(configProperties.containsKey(ConfigDirectives.SIM_SPEEDUP_PORT_DIRECTIVE)) {
    		simSpeedupInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SIM_SPEEDUP_PORT_DIRECTIVE));
    	}
    	
    	//sim time
    	if(configProperties.containsKey(ConfigDirectives.SIM_TIME_PORT_DIRECTIVE)) {
    		simTimeInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SIM_TIME_PORT_DIRECTIVE));
    	}
    	
    	//systems
    	if(configProperties.containsKey(ConfigDirectives.SYSTEMS_PORT_DIRECTIVE)) {
    		systemsInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SYSTEMS_PORT_DIRECTIVE));
    	}
    	
    	//velocities
    	if(configProperties.containsKey(ConfigDirectives.VELOCITIES_PORT_DIRECTIVE)) {
    		velocitiesInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.VELOCITIES_PORT_DIRECTIVE));
    	}
    	
    	////////////////
    	//embedded ssh server
    	
    	if(configProperties.containsKey(ConfigDirectives.SSHD_HOME_DIR_DIRECTIVE)) {
    		sshdHomeDir = configProperties.getProperty(ConfigDirectives.SSHD_HOME_DIR_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.SSHD_PORT_DIRECTIVE)) {
    		sshdPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.SSHD_PORT_DIRECTIVE));
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.SSHD_USER_DIRECTIVE)) {
    		sshdUser = configProperties.getProperty(ConfigDirectives.SSHD_USER_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.SSHD_PASS_DIRECTIVE)) {
    		sshdPass = configProperties.getProperty(ConfigDirectives.SSHD_PASS_DIRECTIVE);
    	}
    	
    	////////////////
    	//flight plan
    	if(configProperties.containsKey(ConfigDirectives.FLIGHT_PLAN_DIRECTIVE)) {
    		flightPlanName = configProperties.getProperty(ConfigDirectives.FLIGHT_PLAN_DIRECTIVE);
    	}
    	
    	////////////////
    	//aircraft name
    	if(configProperties.containsKey(ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE)) {
    		aircraftName = configProperties.getProperty(ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE);
    	}
    }
    
    /////////////
    //subcomponent enable flags
    
    public boolean isSSHDServerEnabled() {
    	return sshdPort != UNCONFIG_PORT;
    }

    public boolean isCameraStreamEnabled() {
    	return (
    		cameraViewHost != null &&
    		cameraViewPort != UNCONFIG_PORT &&
    		cameraStreamHost != null &&
    		cameraStreamPort != UNCONFIG_PORT
    	);
    }
    
    public boolean hasDefinedFlightPlan() { 
    	return flightPlanName != null;
    }
    
    /////////////
    //getters/setters
    
	public String getTelemetryOutputHost() {
        return telemetryOutputHost;
    }

    public void setTelemetryOutputHost(String telemetryOutputHost) {
        this.telemetryOutputHost = telemetryOutputHost;
    }

    public String getTelnetHost() {
        return telnetHost;
    }

    public void setTelnetHost(String telnetHost) {
        this.telnetHost = telnetHost;
    }
    
    public int getTelemetryOutputPort() {
        return telemetryOutputPort;
    }

    public void setTelemetryOutputPort(int telemetryOutputPort) {
        this.telemetryOutputPort = telemetryOutputPort;
    }

    public int getTelnetPort() {
        return telnetPort;
    }

    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }

    public String getControlInputHost() {
        return controlInputHost;
    }

    public void setControlInputHost(String controlInputHost) {
        this.controlInputHost = controlInputHost;
    }
    
    public int getConsumeablesInputPort() {
        return consumeablesInputPort;
    }

    public void setConsumeablesInputPort(int port) {
        this.consumeablesInputPort = port;
    }

    public int getControlsInputPort() {
        return controlsInputPort;
    }

    public void setControlsInputPort(int port) {
        this.controlsInputPort = port;
    }

    public int getEnginesInputPort() {
        return enginesInputPort;
    }

    public void setEnginesInputPort(int port) {
        this.enginesInputPort = port;
    }
    
    public int getFdmInputPort() {
        return fdmInputPort;
    }

    public void setFdmInputPort(int port) {
        this.fdmInputPort = port;
    }

    public int getOrientationInputPort() {
        return orientationInputPort;
    }

    public void setOrientationInputPort(int port) {
        this.orientationInputPort = port;
    }

    public int getPositionInputPort() {
        return positionInputPort;
    }

    public void setPositionInputPort(int port) {
        this.positionInputPort = port;
    }

    public int getSimInputPort() {
        return simInputPort;
    }

    public void setSimInputPort(int port) {
        this.simInputPort = port;
    }

    public int getSimFreezeInputPort() {
        return simFreezeInputPort;
    }

    public void setSimFreezeInputPort(int port) {
        this.simFreezeInputPort = port;
    }

    public int getSimModelInputPort() {
        return simModelInputPort;
    }

    public void setSimModelInputPort(int port) {
        this.simModelInputPort = port;
    }

	public int getSimSpeedupInputPort() {
        return simSpeedupInputPort;
    }

    public void setSimSpeedupInputPort(int port) {
        this.simSpeedupInputPort = port;
    }
    
    public int getSimTimeInputPort() {
        return simTimeInputPort;
    }

    public void setSimTimeInputPort(int port) {
        this.simTimeInputPort = port;
    }
    
    public int getSystemsInputPort() {
        return systemsInputPort;
    }

    public void setSystemsInputPort(int port) {
        this.systemsInputPort = port;
    }

    public int getVelocitiesInputPort() {
        return velocitiesInputPort;
    }

    public void setVelocitiesInputPort(int port) {
        this.velocitiesInputPort = port;
    }
    
    public int getSshdPort() {
		return sshdPort;
	}

	public void setSshdPort(int sshdPort) {
		this.sshdPort = sshdPort;
	}

	public String getSshdHomeDir() {
		return sshdHomeDir;
	}

	public void setSshdHomeDir(String sshdHomeDir) {
		this.sshdHomeDir = sshdHomeDir;
	}

	public String getSshdUser() {
		return sshdUser;
	}

	public void setSshdUser(String sshdUser) {
		this.sshdUser = sshdUser;
	}

	public String getSshdPass() {
		return sshdPass;
	}

	public void setSshdPass(String sshdPass) {
		this.sshdPass = sshdPass;
	}

	public String getCameraViewerHost() {
		return cameraViewHost;
	}

	public void setCameraViewerHost(String cameraViewerHost) {
		this.cameraViewHost = cameraViewerHost;
	}

	public int getCameraViewerPort() {
		return cameraViewPort;
	}
	
	public void setCameraViewerPort(int cameraViewerPort) {
		this.cameraViewPort = cameraViewerPort;
	}
	
	public String getCameraStreamHost() {
		return cameraStreamHost;
	}
	
	public int getCameraStreamPort() {
		return cameraStreamPort;
	}

	public void setCameraStreamPort(int cameraStreamPort) {
		this.cameraStreamPort = cameraStreamPort;
	}

	public String getFlightPlanName() {
		return flightPlanName;
	}

	public void setFlightPlanName(String flightPlanName) {
		this.flightPlanName = flightPlanName;
	}
	
	public String getAircraftName() {
		return aircraftName;
	}

	public void setAircraftName(String aircraftName) {
		this.aircraftName = aircraftName;
	}

	@Override
	public String toString() {
		return "SimulatorConfig [telemetryOutputHost=" + telemetryOutputHost + ", telemetryOutputPort="
				+ telemetryOutputPort + ", telnetHost=" + telnetHost + ", telnetPort=" + telnetPort
				+ ", controlInputHost=" + controlInputHost + ", consumeablesInputPort=" + consumeablesInputPort
				+ ", controlsInputPort=" + controlsInputPort + ", enginesInputPort=" + enginesInputPort
				+ ", fdmInputPort=" + fdmInputPort + ", orientationInputPort=" + orientationInputPort
				+ ", positionInputPort=" + positionInputPort + ", simInputPort=" + simInputPort
				+ ", simFreezeInputPort=" + simFreezeInputPort + ", simModelInputPort=" + simModelInputPort
				+ ", simSpeedupInputPort=" + simSpeedupInputPort + ", simTimeInputPort=" + simTimeInputPort
				+ ", systemsInputPort=" + systemsInputPort + ", velocitiesInputPort=" + velocitiesInputPort
				+ ", cameraViewHost=" + cameraViewHost + ", cameraViewPort=" + cameraViewPort + ", cameraStreamHost="
				+ cameraStreamHost + ", cameraStreamPort=" + cameraStreamPort + ", sshdPort=" + sshdPort
				+ ", sshdHomeDir=" + sshdHomeDir + ", sshdUser=" + sshdUser + ", sshdPass=" + sshdPass
				+ ", flightPlanName=" + flightPlanName + "]";
	}
	
	public String toJSON() {
		
		JSONObject json = new JSONObject();
		
		//telemetry
		json.put(ConfigDirectives.TELEM_OUTPUT_HOST_DIRECTIVE, this.getTelemetryOutputHost());
		json.put(ConfigDirectives.TELEM_OUTPUT_PORT_DIRECTIVE, this.getTelemetryOutputPort());
		
		//telnet
		json.put(ConfigDirectives.TELNET_HOST_DIRECTIVE, this.getTelnetHost());
		json.put(ConfigDirectives.TELNET_PORT_DIRECTIVE, this.getTelnetPort());
		
		//control input
		json.put(ConfigDirectives.CONTROL_INPUT_HOST_DIRECTIVE, this.getControlInputHost());
		
		//control input ports
		json.put(ConfigDirectives.CONSUMEABLES_PORT_DIRECTIVE, this.getConsumeablesInputPort());
		json.put(ConfigDirectives.CONTROLS_PORT_DIRECTIVE, this.getControlsInputPort());
		json.put(ConfigDirectives.ENGINES_PORT_DIRECTIVE, this.getEnginesInputPort());
		json.put(ConfigDirectives.FDM_PORT_DIRECTIVE, this.getFdmInputPort());
		json.put(ConfigDirectives.ORIENTATION_PORT_DIRECTIVE, this.getOrientationInputPort());
		json.put(ConfigDirectives.POSITION_PORT_DIRECTIVE, this.getPositionInputPort());
		json.put(ConfigDirectives.SIM_PORT_DIRECTIVE, this.getSimInputPort());
		json.put(ConfigDirectives.SIM_FREEZE_PORT_DIRECTIVE, this.getSimFreezeInputPort());
		json.put(ConfigDirectives.SIM_MODEL_PORT_DIRECTIVE, this.getSimModelInputPort());
		json.put(ConfigDirectives.SIM_SPEEDUP_PORT_DIRECTIVE, this.getSimSpeedupInputPort());
		json.put(ConfigDirectives.SIM_TIME_PORT_DIRECTIVE, this.getSimTimeInputPort());
		json.put(ConfigDirectives.SYSTEMS_PORT_DIRECTIVE, this.getSystemsInputPort());
		json.put(ConfigDirectives.VELOCITIES_PORT_DIRECTIVE, this.getVelocitiesInputPort());
		
		//camera view
		json.put(ConfigDirectives.CAMERA_VIEW_HOST_DIRECTIVE, this.getCameraViewerHost());
		json.put(ConfigDirectives.CAMERA_VIEW_PORT_DIRECTIVE, this.getCameraViewerPort());

		//camera stream
		json.put(ConfigDirectives.CAMERA_STREAM_HOST_DIRECTIVE, this.getCameraStreamHost());
		json.put(ConfigDirectives.CAMERA_STREAM_PORT_DIRECTIVE, this.getCameraStreamPort());
		
		//sshd
		json.put(ConfigDirectives.SSHD_PORT_DIRECTIVE, this.getSshdPort());
		json.put(ConfigDirectives.SSHD_USER_DIRECTIVE, this.getSshdUser());
		json.put(ConfigDirectives.SSHD_PASS_DIRECTIVE, this.getSshdPass());
		json.put(ConfigDirectives.SSHD_HOME_DIR_DIRECTIVE, this.getSshdHomeDir());

		//flightplan
		json.put(ConfigDirectives.FLIGHT_PLAN_DIRECTIVE, this.getFlightPlanName());
		
		//aircraft name
		json.put(ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE, this.getAircraftName());
		
		return json.toString();
	}
}
