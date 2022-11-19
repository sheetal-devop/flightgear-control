package org.jason.fgcontrol.aircraft.c172p;

import java.util.Properties;

import org.jason.fgcontrol.aircraft.config.ConfigDirectives;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;

public class C172PConfig extends SimulatorConfig {
   
	private final static String DEFAULT_AIRCRAFT_NAME = "C172P_Default";
	private String aircraftName;
	
	public C172PConfig() {
        super(
        	SimulatorConfig.DEFAULT_TELNET_HOST, 
        	SimulatorConfig.DEFAULT_TELNET_PORT, 
        	SimulatorConfig.DEFAULT_SOCKETS_TELEM_HOST, 
        	SimulatorConfig.DEFAULT_SOCKETS_TELEM_PORT
        );
        
        aircraftName = DEFAULT_AIRCRAFT_NAME;
    }
    
    public C172PConfig(String telnetHostname, int telnetPort, String socketsHostname, int socketsPort) {
        super(
        		telnetHostname,
        		telnetPort,
        		socketsHostname,
        		socketsPort
        );
        
        aircraftName = DEFAULT_AIRCRAFT_NAME;
    }
    
    public C172PConfig(Properties configProperties) {
    	super(configProperties);
    	
    	//any c172-specific config processing happens here
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
