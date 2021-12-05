package org.jason.flightgear.planes;

public class NetworkConfig {

	private final static String DEFAULT_SOCKETS_TELEM_HOST = "localhost";
    private final static int DEFAULT_SOCKETS_TELEM_PORT = 6501;
    
    private final static String DEFAULT_SOCKETS_INPUT_HOST = "localhost";
    
	private final static String DEFAULT_TELNET_HOST = "localhost";
    private final static int DEFAULT_TELNET_PORT = 5501;

    private final static int DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT = 6601;
    private final static int DEFAULT_SOCKETS_INPUT_CONTROLS_PORT = 6602;
    private final static int DEFAULT_SOCKETS_INPUT_FDM_PORT = 6603;
    private final static int DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT = 6604;
    private final static int DEFAULT_SOCKETS_INPUT_POSITION_PORT = 6605;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_PORT = 6606;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT = 6607;
    private final static int DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT = 6608;
    private final static int DEFAULT_SOCKETS_INPUT_VELOCITIES_PORT = 6609;

	private String telemetryOutputHost;
	private int telemetryOutputPort;

	private String telnetHost;
    private int telnetPort;
    
    //expect the same host for all inputs. hard to imagine a plane in a sim on multiple hosts
	private String socketInputHost;

	private int consumeablesInputPort;
    private int controlsInputPort;
    private int fdmInputPort;
    private int orientationInputPort;
    private int positionInputPort;
    private int simInputPort;
    private int simFreezeInputPort;
    private int simSpeedupInputPort;
    private int velocitiesInputPort;
    
	public NetworkConfig() {
		
		telemetryOutputHost = DEFAULT_SOCKETS_TELEM_HOST;
		telemetryOutputPort = DEFAULT_SOCKETS_TELEM_PORT;
		
		telnetHost = DEFAULT_TELNET_HOST;
		telnetPort = DEFAULT_TELNET_PORT;
		
		socketInputHost = DEFAULT_SOCKETS_INPUT_HOST;
		
		consumeablesInputPort = DEFAULT_SOCKETS_INPUT_CONSUMABLES_PORT;
		controlsInputPort = DEFAULT_SOCKETS_INPUT_CONTROLS_PORT;
		fdmInputPort = DEFAULT_SOCKETS_INPUT_FDM_PORT;
		orientationInputPort = DEFAULT_SOCKETS_INPUT_ORIENTATION_PORT;
		positionInputPort = DEFAULT_SOCKETS_INPUT_POSITION_PORT;
		simInputPort = DEFAULT_SOCKETS_INPUT_SIM_PORT;
		simFreezeInputPort = DEFAULT_SOCKETS_INPUT_SIM_FREEZE_PORT;
		simSpeedupInputPort = DEFAULT_SOCKETS_INPUT_SIM_SPEEDUP_PORT;
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
    
    public int getConsumeablesPort() {
		return consumeablesInputPort;
	}

	public void setConsumeablesPort(int consumeablesPort) {
		this.consumeablesInputPort = consumeablesPort;
	}

	public int getControlsPort() {
		return controlsInputPort;
	}

	public void setControlsPort(int controlsPort) {
		this.controlsInputPort = controlsPort;
	}

	public int getFdmPort() {
		return fdmInputPort;
	}

	public void setFdmPort(int fdmPort) {
		this.fdmInputPort = fdmPort;
	}

	public int getOrientationPort() {
		return orientationInputPort;
	}

	public void setOrientationPort(int orientationPort) {
		this.orientationInputPort = orientationPort;
	}

	public int getPositionPort() {
		return positionInputPort;
	}

	public void setPositionPort(int positionPort) {
		this.positionInputPort = positionPort;
	}

	public int getSimPort() {
		return simInputPort;
	}

	public void setSimPort(int simPort) {
		this.simInputPort = simPort;
	}

	public int getSimFreezePort() {
		return simFreezeInputPort;
	}

	public void setSimFreezePort(int simFreezePort) {
		this.simFreezeInputPort = simFreezePort;
	}

	public int getSimSpeedupPort() {
		return simSpeedupInputPort;
	}

	public void setSimSpeedupPort(int simSpeedupPort) {
		this.simSpeedupInputPort = simSpeedupPort;
	}

	public int getVelocitiesPort() {
		return velocitiesInputPort;
	}

	public void setVelocitiesPort(int velocitiesPort) {
		this.velocitiesInputPort = velocitiesPort;
	}
}
