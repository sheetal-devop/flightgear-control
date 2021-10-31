package org.jason.flightgear.connection.telnet.app;

import java.io.IOException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.connection.telnet.FlightGearTelnetConnection;

public class TelnetTelemetryLoop {
	public static void main(String [] args) {
		
		String[] properties = {
				"/environment/relative-humidity",
				
				"/environment/temperature-degf",
				"/environment/dewpoint-degc",
				"/environment/pressure-inhg",
				"/environment/visibility-m",
				"/environment/gravitational-acceleration-mps2",
				"/environment/wind-speed-kt",
				"/environment/wind-from-north-fps",
				"/environment/wind-from-east-fps",
				"/environment/wind-from-down-fps",
				"/sim/speed-up",
				"/sim/time/local-day-seconds",
				"/sim/time/elapsed-sec",
				"/orientation/pitch-deg",
				"/orientation/heading-deg", 
				"/orientation/heading-magnetic-deg",
				"/orientation/track-magnetic-deg",
				"/orientation/beta-deg",
				"/orientation/alpha-deg",
				"/position/longitude-deg",
				"/position/latitude-deg",
				"/position/longitude-string",
				"/position/latitude-string",
				"/position/ground-elev-ft",
				"/engines/engine/running",
				"/consumables/fuel/total-fuel-gals",
				"/consumables/fuel/total-fuel-lbs"
			};
		
		int loopIterations = 4;
		
		FlightGearTelnetConnection mgr = null;
		try {
			mgr = new FlightGearTelnetConnection("localhost", 5501);
			
			System.out.println("==========");
			String outputRaw, outputValues, cmdOutput;
			for(int j = 0; j< loopIterations; j++) {
				outputRaw = "";
	            for (int i = 0; i < properties.length; i++) {
	      
	            	cmdOutput = mgr.sendCommandReadRawOutput("get " + properties[i]);
	            	
	            	
	            	outputRaw += "\"" + properties[i] +"\": " + cmdOutput;
	            	
	    			//System.out.println("Got value for property " + properties[i] + ": " + output);
	    			
	    			if(i != properties.length-1) {
	    				//output = output.replaceAll("\n", "") + ",\n";
	    				outputRaw += ",\n";
	    			}
	            }
	            
	            System.out.println("{\n" + outputRaw + "\n}");
			}
			
			System.out.println("-----------");
			
			for(int j = 0; j< loopIterations; j++) {
				outputValues = "";
	            for (int i = 0; i < properties.length; i++) {
	      
	            	cmdOutput = mgr.getPropertyValue(properties[i]);
	            	
	            	
	            	outputValues += "\"" + properties[i] +"\": " + cmdOutput;
	            	
	    			//System.out.println("Got value for property " + properties[i] + ": " + output);
	    			
	    			if(i != properties.length-1) {
	    				outputValues += ",\n";
	    			}
	            }
	            
	            System.out.println("{\n" + outputValues + "\n}");
			}
			
		} catch (InvalidTelnetOptionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(mgr != null) {
				mgr.disconnect();
			}
		}
	}
}
