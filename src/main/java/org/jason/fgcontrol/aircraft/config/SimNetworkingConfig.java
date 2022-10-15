package org.jason.fgcontrol.aircraft.config;

import java.util.Properties;

public class SimNetworkingConfig {

    protected final static String DEFAULT_SOCKETS_TELEM_HOST = "localhost";
    protected final static int DEFAULT_SOCKETS_TELEM_PORT = 6501;
    
    protected final static String DEFAULT_SOCKETS_INPUT_HOST = "localhost";
    
    protected final static String DEFAULT_TELNET_HOST = "localhost";
    protected final static int DEFAULT_TELNET_PORT = 5501;

    protected final static int DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    protected final static int DEFAULT_SOCKETS_INPUT_CONTROLS_PORT = 6602;
    protected final static int DEFAULT_SOCKETS_INPUT_ENGINES_PORT = 6603;
    protected final static int DEFAULT_SOCKETS_INPUT_FDM_PORT = 6604;
    protected final static int DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT = 6605;
    protected final static int DEFAULT_SOCKETS_INPUT_POSITION_PORT = 6606;
    protected final static int DEFAULT_SOCKETS_INPUT_SIM_PORT = 6607;
    protected final static int DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT = 6608;
    protected final static int DEFAULT_SOCKETS_INPUT_SIM_MODEL_PORT = 6609;
    protected final static int DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6610;
    protected final static int DEFAULT_SOCKETS_INPUT_SIM_TIME_PORT = 6611;
    protected final static int DEFAULT_SOCKETS_INPUT_SYSTEM_PORT = 6612;
    protected final static int DEFAULT_SOCKETS_INPUT_VELOCITIES_PORT = 6613;

    protected String telemetryOutputHost;
    protected int telemetryOutputPort;

    protected String telnetHost;
    protected int telnetPort;
    
    //expect the same host for all inputs. hard to imagine a plane in a sim on multiple hosts
    protected String socketInputHost;

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
    
	public SimNetworkingConfig() {
        
        telemetryOutputHost = DEFAULT_SOCKETS_TELEM_HOST;
        telemetryOutputPort = DEFAULT_SOCKETS_TELEM_PORT;
        
        telnetHost = DEFAULT_TELNET_HOST;
        telnetPort = DEFAULT_TELNET_PORT;
        
        socketInputHost = DEFAULT_SOCKETS_INPUT_HOST;
        
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
    }
	
	//simple option for simple sim setups
	//default control input ports are still set but the sim probably doesn't have them open
    public SimNetworkingConfig(String telnetHost, int telnetPort, String socketsTelemHost, int socketsTelemPort) 
    {
    	this();
    	
        this.telemetryOutputHost = socketsTelemHost;
        this.telemetryOutputPort = socketsTelemPort;
        
        this.telnetHost = telnetHost;
        this.telnetPort = telnetPort;
	}
    
    public SimNetworkingConfig(Properties configProperties) {
    	//set defaults and let propertiesFile overwrite
    	this();
    	
    	//override the defaults with what the config file defines
    	
    	//telemetry output
    	if(configProperties.containsKey(ConfigDirectives.TELEM_OUTPUT_HOST_DIRECTIVE)) {
    		telemetryOutputHost = configProperties.getProperty(ConfigDirectives.TELEM_OUTPUT_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.TELEM_OUTPUT_PORT_DIRECTIVE)) {
    		telemetryOutputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.TELEM_OUTPUT_PORT_DIRECTIVE));
    	}
    	
    	//telnet server
    	if(configProperties.containsKey(ConfigDirectives.TELNET_HOST_DIRECTIVE)) {
    		telnetHost = configProperties.getProperty(ConfigDirectives.TELNET_HOST_DIRECTIVE);
    	}
    	
    	if(configProperties.containsKey(ConfigDirectives.TELNET_PORT_DIRECTIVE)) {
    		telnetPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.TELNET_PORT_DIRECTIVE));
    	}
    	
    	//consumeables
    	if(configProperties.containsKey(ConfigDirectives.CONSUMEABLES_PORT_DIRECTIVE)) {
    		consumeablesInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CONSUMEABLES_PORT_DIRECTIVE));
    	}
    	
    	//controls
    	if(configProperties.containsKey(ConfigDirectives.CONTROL_PORT_DIRECTIVE)) {
    		controlsInputPort = Integer.parseInt(configProperties.getProperty(ConfigDirectives.CONTROL_PORT_DIRECTIVE));
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
    }

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

    public String getSocketInputHost() {
        return socketInputHost;
    }

    public void setSocketInputHost(String socketInputHost) {
        this.socketInputHost = socketInputHost;
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
    
    @Override
	public String toString() {
		return "SimNetworkingConfig [" + 
			"telemetryOutputHost=" + telemetryOutputHost + 
			", telemetryOutputPort="+ telemetryOutputPort + 
			", telnetHost=" + telnetHost + 
			", telnetPort=" + telnetPort +
			", socketInputHost=" + socketInputHost + 
			", consumeablesInputPort=" + consumeablesInputPort + 
			", controlsInputPort=" + controlsInputPort + 
			", enginesInputPort=" + enginesInputPort + 
			", fdmInputPort=" + fdmInputPort + 
			", orientationInputPort=" + orientationInputPort + 
			", positionInputPort=" + positionInputPort + 
			", simInputPort=" + simInputPort + 
			", simFreezeInputPort=" + simFreezeInputPort + 
			", simModelInputPort=" + simModelInputPort + 
			", simSpeedupInputPort=" + simSpeedupInputPort + 
			", simTimeInputPort=" + simTimeInputPort + 
			", systemsInputPort=" + systemsInputPort + 
			", velocitiesInputPort=" + velocitiesInputPort + "]";
	}
}
