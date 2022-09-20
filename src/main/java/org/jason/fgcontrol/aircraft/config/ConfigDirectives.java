package org.jason.fgcontrol.aircraft.config;

public interface ConfigDirectives {
	
	public static final String AIRCRAFT_NAME_DIRECTIVE = "aircraftName";
	
	/*
consumeablesInputPort=5002
controlsInputPort=5003
enginesInputPort=5004
fdmInputPort=5005
orientationInputPort=5006
positionInputPort=5007
simInputPort=5008
simFreezeInputPort=5009
simModelInputPort=5010
simSpeedupInputPort=5011
simTimeInputPort=5012
systemsInputPort=5013
velocitiesInputPort=5014
	 * 
	 */
	
	public static final String TELEM_OUTPUT_HOST_DIRECTIVE = "telemetryHost";
	public static final String TELEM_OUTPUT_PORT_DIRECTIVE = "telemetryPort";
	
	public static final String TELNET_HOST_DIRECTIVE = "telnetHost";
	public static final String TELNET_PORT_DIRECTIVE = "telnetPort";
	
	
	
	public static final String CONSUMEABLES_PORT_DIRECTIVE = "consumeablesInputPort";
	public static final String CONTROL_PORT_DIRECTIVE = "controlsInputPort";
	public static final String ENGINES_PORT_DIRECTIVE = "enginesInputPort";
	public static final String FDM_PORT_DIRECTIVE = "fdmInputPort";
	public static final String ORIENTATION_PORT_DIRECTIVE = "orientationInputPort";
	public static final String POSITION_PORT_DIRECTIVE = "positionInputPort";
	public static final String SIM_PORT_DIRECTIVE = "simInputPort";
	public static final String SIM_FREEZE_PORT_DIRECTIVE = "simFreezeInputPort";
	public static final String SIM_MODEL_PORT_DIRECTIVE = "simModelInputPort";
	public static final String SIM_SPEEDUP_PORT_DIRECTIVE = "simSpeedupInputPort";
	public static final String SIM_TIME_PORT_DIRECTIVE = "simTimeInputPort";
	public static final String SYSTEMS_PORT_DIRECTIVE = "systemsInputPort";
	public static final String VELOCITIES_PORT_DIRECTIVE = "velocitiesInputPort";
}
