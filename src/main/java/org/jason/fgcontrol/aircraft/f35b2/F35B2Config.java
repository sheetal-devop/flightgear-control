package org.jason.fgcontrol.aircraft.f35b2;

import java.util.Properties;

import org.jason.fgcontrol.aircraft.config.ConfigDirectives;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;

public class F35B2Config extends SimulatorConfig {
   
	private final static String DEFAULT_AIRCRAFT_NAME = "F35B2_Default";
	private String aircraftName;
	
    public F35B2Config() {
        super(
            SimulatorConfig.DEFAULT_TELNET_HOST, 
            SimulatorConfig.DEFAULT_TELNET_PORT, 
            SimulatorConfig.DEFAULT_SOCKETS_TELEM_HOST, 
            SimulatorConfig.DEFAULT_SOCKETS_TELEM_PORT
        );
        
        aircraftName = DEFAULT_AIRCRAFT_NAME;
    }
    
    public F35B2Config(String telnetHostname, int telnetPort, String socketsHostname, int socketsPort) {
        super(
        	telnetHostname,
        	telnetPort,
        	socketsHostname,
        	socketsPort
        );

        aircraftName = DEFAULT_AIRCRAFT_NAME;
    }
    
    public F35B2Config(Properties configProperties) {
    	super(configProperties);
    	
    	//any f15c-specific config processing happens here
    	if(configProperties.containsKey(ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE)) {
    		aircraftName = configProperties.getProperty(ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE);
    	} 
    	else {
    		aircraftName = DEFAULT_AIRCRAFT_NAME;
    	}
    }
    
    public String getAircraftName() {
		return aircraftName;
	}

	public void setAircraftName(String aircraftName) {
		this.aircraftName = aircraftName;
	}    
}
