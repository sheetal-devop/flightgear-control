package org.jason.flightgear.planes.c172p.app;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.flightgear.planes.c172p.C172P;
import org.jason.flightgear.sockets.FlightGearManagerSockets;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;
import org.json.JSONException;
import org.json.JSONObject;

public class C172P_Input_Test {
	
	private final static String FG_SOCKETS_HOST = "localhost";
	private final static String FG_SOCKET_PROTOCOL_VAR_SEP = ",";
	private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
	
	private final static int FG_SOCKETS_TELEM_PORT = 6501;
	//private final static int FG_SOCKETS_INPUT_PORT = 6601;
	
	private final static int FG_SOCKETS_ORIENTATION_INPUT_PORT = 6603;

	private LinkedHashMap<String, String> controlSchema;

	
	private FlightGearManagerSockets fgSocketsClient;
	
	private C172P plane;
	
	public C172P_Input_Test() throws InvalidTelnetOptionException, IOException {
		fgSocketsClient = new FlightGearManagerSockets(FG_SOCKETS_HOST, FG_SOCKETS_TELEM_PORT);
		
		plane = new C172P();
		
		loadControlSchema();
	}
	
	private void loadControlSchema() {
		//TODO: generate this from the xml file
		//key order definitely matters
		controlSchema = new LinkedHashMap<String, String>();
//		controlSchema.put("/consumables/fuel/tank/level-gal_us", "");
//		controlSchema.put("/consumables/fuel/tank/water-contamination", "");
//		controlSchema.put("/consumables/fuel/tank[1]/level-gal_us", "");
//		controlSchema.put("/consumables/fuel/tank[1]/water-contamination", "");
//		controlSchema.put("/controls/electric/battery-switch", "");
//		controlSchema.put("/controls/flight/aileron", "");
//		controlSchema.put("/controls/flight/auto-coordination", "");
//		controlSchema.put("/controls/flight/auto-coordination-factor", "");
//		controlSchema.put("/controls/flight/elevator", "");
//		controlSchema.put("/controls/flight/flaps", "");
//		controlSchema.put("/controls/flight/rudder", "");
//		controlSchema.put("/controls/flight/speedbrake", "");
//		controlSchema.put("/controls/gear/brake-parking", "");
//		controlSchema.put("/controls/gear/gear-down", "");
		controlSchema.put("/orientation/alpha-deg", "");
		controlSchema.put("/orientation/beta-deg", "");
		controlSchema.put("/orientation/heading-deg", "");
		controlSchema.put("/orientation/heading-magnetic-deg", "");
		controlSchema.put("/orientation/pitch-deg", "");
		controlSchema.put("/orientation/roll-deg", "");
		controlSchema.put("/orientation/track-magnetic-deg", "");
		controlSchema.put("/orientation/yaw-deg", "");
//		controlSchema.put("/position/altitude-ft", "");
//		controlSchema.put("/position/ground-elev-ft", "");
//		controlSchema.put("/position/latitude-deg", "");
//		controlSchema.put("/position/longitude-deg", "");
//		controlSchema.put("/sim/speed-up", "");
//		controlSchema.put("/sim/freeze/clock", "");
//		controlSchema.put("/sim/freeze/fuel", "");
//		controlSchema.put("/sim/freeze/master", "");
//		controlSchema.put("/velocities/airspeed-kt", "");
//		controlSchema.put("/velocities/groundspeed-kt", "");
//		controlSchema.put("/velocities/vertical-speed-fps", "");
	}
	
	public LinkedHashMap<String, String> copyControlSchema() {
		return new LinkedHashMap<String, String>(controlSchema);
	}
	
//	public synchronized String readTelemetry() throws IOException {
//		return fgSocketsClient.readTelemetry();
//	}
//	
//	public JSONObject readTelemetryJSON() throws JSONException, IOException {
//		return new JSONObject(readTelemetry());
//	}
	
	public void writeInput(LinkedHashMap<String, String> inputHash) {
		
		boolean validFieldCount = true;
		
		StringBuilder controlInput = new StringBuilder();
		controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
		
		//foreach key, write the value into a simple unquoted csv string. fail socket write on missing values
		for( Entry<String, String> entry : inputHash.entrySet()) {
			if(controlSchema.containsKey(entry.getKey())) {
				if(!entry.getValue().equals( "" )) {
					controlInput.append(entry.getValue());
				}
				else {
					//field count check later
					validFieldCount = false;
					break;
				}
				controlInput.append(FG_SOCKET_PROTOCOL_VAR_SEP);
			}
			else {
				System.out.println("Ignoring unknown key: " + entry.getKey());
				validFieldCount = false;
				break;
			}
		}
		controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
		
		if(validFieldCount) {
			int controlInputFields = controlInput.toString().split(FG_SOCKET_PROTOCOL_VAR_SEP).length;
			
			//System.out.println
			
			if( controlInputFields - 1 == controlSchema.size())
			{
				//debug output
				
				System.out.println("Writing control input: " + controlInput.toString());
				
				fgSocketsClient.writeControlInput(controlInput.toString(), FG_SOCKETS_ORIENTATION_INPUT_PORT);
				
			}
			else
			{
				System.err.println("Control input had an unexpected number of fields: " + controlInputFields);
			}
		}
		else {
			System.err.println("Control input had an unexpected number of fields: " + validFieldCount);
		}
	}
	
	public void shutdown() {
		//plane shutdown
		if(plane != null) {
			plane.shutdown();
			
		}
	}
	
	public void changeHeading90DegCC() {
		//"/orientation/heading-deg": 279.981689,
		
		//get current heading
		
		LinkedHashMap<String, String> myInput = copyControlSchema();
		
		//myInput.put("/position/altitude-ft", "1000");
		myInput.put("/orientation/heading-deg", "90");
		writeInput(myInput);
		
		//check visually
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//myInput.put("/position/altitude-ft", "1000");
		myInput.put("/orientation/heading-deg", "180");
		writeInput(myInput);
		
		//check visually
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//myInput.put("/position/altitude-ft", "1000");
		myInput.put("/orientation/heading-deg", "270");
		writeInput(myInput);
		
		//check visually
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//myInput.put("/position/altitude-ft", "1000");
		myInput.put("/orientation/heading-deg", "0");
		writeInput(myInput);
		
		//check visually
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//myInput.put("/position/altitude-ft", "1000");
		myInput.put("/orientation/heading-deg", "0");
		writeInput(myInput);
		
	}
	
	public static void main(String[] args) {
		
		//fork execution of shell script and wait for telnet port to open
		//use shell script because we may want to run it from elsewhere
		
		
		C172P_Input_Test plane = null;
		
		try {
			plane = new C172P_Input_Test();
			
			//rotate plane 90 degrees
			plane.changeHeading90DegCC();
			
			
			//try 4 times confirm heading each time

		} catch (InvalidTelnetOptionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (plane != null) {
				plane.shutdown();
			}
		}
	}
}
