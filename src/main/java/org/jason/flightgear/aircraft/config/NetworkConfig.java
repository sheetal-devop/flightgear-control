package org.jason.flightgear.aircraft.config;

public class NetworkConfig {

	private final static String DEFAULT_SOCKETS_TELEM_HOST = "localhost";
    private final static int DEFAULT_SOCKETS_TELEM_PORT = 6501;
    
    private final static String DEFAULT_SOCKETS_INPUT_HOST = "localhost";
    
	private final static String DEFAULT_TELNET_HOST = "localhost";
    private final static int DEFAULT_TELNET_PORT = 5501;

    private final static int DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    private final static int DEFAULT_SOCKETS_INPUT_CONTROLS_PORT = 6602;
    private final static int DEFAULT_SOCKETS_INPUT_ENGINES_PORT = 6603;
    private final static int DEFAULT_SOCKETS_INPUT_FDM_PORT = 6604;
    private final static int DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT = 6605;
    private final static int DEFAULT_SOCKETS_INPUT_POSITION_PORT = 6606;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_PORT = 6607;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT = 6608;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_MODEL_PORT = 6609;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6610;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_TIME_PORT = 6611;
    private final static int DEFAULT_SOCKETS_INPUT_SYSTEM_PORT = 6612;
    private final static int DEFAULT_SOCKETS_INPUT_VELOCITIES_PORT = 6613;

	private String telemetryOutputHost;
	private int telemetryOutputPort;

	private String telnetHost;
    private int telnetPort;
    
    //expect the same host for all inputs. hard to imagine a plane in a sim on multiple hosts
	private String socketInputHost;

	private int consumeablesInputPort;
    private int controlsInputPort;
    private int enginesInputPort;
    private int fdmInputPort;
    private int orientationInputPort;
    private int positionInputPort;
    private int simInputPort;
    private int simFreezeInputPort;
    private int simModelInputPort;
    private int simSpeedupInputPort;
    private int simTimeInputPort;
    private int systemsInputPort;
    private int velocitiesInputPort;
    
	public NetworkConfig() {
		
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
}
