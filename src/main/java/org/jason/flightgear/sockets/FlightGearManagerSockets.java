package org.jason.flightgear.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.jason.flightgear.manager.FlightGearInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a virtual plane in a flightgear sim through a socket connection
 *  
 *
 */
public class FlightGearManagerSockets {
	
	private Logger logger = LoggerFactory.getLogger(FlightGearManagerSockets.class);
	
	private String host;
	private int controlInputPort;
	private int telemetryPort;
	
	private DatagramPacket fgTelemetryPacket;
	
	private byte[] receivingDataBuffer;
	
	//TODO: default values overridable
	private final static int SOCKET_TIMEOUT = 5000;
	private final static int MAX_RECEIVE_BUFFER_LEN = 4096;
	
	private final static String PAUSE_INPUT = "PAUSE";
	
	//TODO: default values overridable
	private final static String FG_SOCKET_PROTOCOL_VAR_SEP = ",";
	private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
	
	//cache this so we don't have to constantly perform lookups when receiving data
	private Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	private HashMap<String, FlightGearInput> controlInputs;

	private Pattern telemetryLinePattern;
	
	public FlightGearManagerSockets(String host,  int telemetryPort) 
			throws SocketException, UnknownHostException {
		
		//set this here rather than before the match because we don't want to initialize it with every telemetry read
		telemetryLinePattern = Pattern.compile("^[\"/]\\S+[\": ]\\S+[,]?$");
		
		this.host = host;
		this.telemetryPort = telemetryPort;
		
		receivingDataBuffer = new byte[MAX_RECEIVE_BUFFER_LEN];
		
		fgTelemetryPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
		
		controlInputs = new HashMap<>();
	}
	
	/**
	 * Need a way of resolving an input type to a schema and port
	 * 
	 * @param key
	 * @param input
	 */
	public void registerInput(String key, FlightGearInput input) {
		
		logger.info("Registering control input: {}, on port: {}", key, input.getPort());
		
		controlInputs.put(key, input);
	}
	
	
	/**
	 * Read output fields from the flightgear output socket.
	 * 
	 * @return	A JSON string of telemetry data.
	 * 
	 * @throws IOException
	 */
	public String readTelemetry() throws IOException {
		
		logger.trace("Telemetry called for {}:{}", host, telemetryPort);
		
		String output = "";
		
		DatagramSocket fgTelemetrySocket = null;

		
		try {

			//technically a server connection. we connect to the fg port, which starts sending us data
			fgTelemetrySocket = new DatagramSocket( telemetryPort, InetAddress.getByName(host) );
			fgTelemetrySocket.setSoTimeout(SOCKET_TIMEOUT);
			
			fgTelemetrySocket.receive(fgTelemetryPacket);
			
			output = new String(fgTelemetryPacket.getData()).trim();  
			
			logger.trace("Raw telemetry was received from socket.");
			
		} catch (IOException e) {
			//comms errors connecting to fg telemetry socket
			
			//e.printStackTrace();
			logger.warn("IOException reading raw telemetry from socket", e);

			throw e;
		}
		finally {
			if( fgTelemetrySocket != null && !fgTelemetrySocket.isClosed() ) {
				fgTelemetrySocket.close();
			}
			else
			{
				logger.warn("Attempted to close fgTelemetrySocket, but was already closed or null");
			}
		}
				
		/*
		 occasionally see this. not sure where those extra digits are coming from
		...
		"/velocities/groundspeed-kt": 8.734266,
		"/velocities/vertical-speed-fps": -303.683014
		874}
		
		
		...
		"/velocities/vertical-speed-fps": -163.828430
		8


		0}	
		*/
		
		logger.trace("=========================\nRaw telemetry received:\n{}\n=========================\n", output);
		
		String[] lines = output.split(FG_SOCKET_PROTOCOL_LINE_SEP);
		
		//TODO: count accepted lines and compare against schema
		
		logger.trace("Cleaning up raw telemetry data");
		
		int telemetryLineCount = 0;
		StringBuilder cleanOutput = new StringBuilder();
		for(String line : lines) {
			
			//only match lines that look like telemetry
			if( telemetryLinePattern.matcher(line).matches() ) 
			{
				cleanOutput.append(line);
				telemetryLineCount++;
			} else {
				logger.warn("Dropping malformed telemetry line: {}", line);
			}
		}
		
		logger.debug("readTelemetry returning. Read {} lines", telemetryLineCount);
				
		
		//return after adding json braces
		return cleanOutput.insert(0, "{").append("}").toString();
	}
	
	public synchronized void writeInput(LinkedHashMap<String, String> inputHash, int port) {
		
		boolean validFieldCount = true;
		
		StringBuilder controlInput = new StringBuilder();
		controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
		
		//foreach key, write the value into a simple unquoted csv string. fail socket write on missing values
		for( Entry<String, String> entry : inputHash.entrySet()) {
				if(!entry.getValue().equals( "" )) {
					controlInput.append(entry.getValue());
				}
				else {
					logger.error("Missing field value: {}" + entry.getKey());
					
					//field count check later
					validFieldCount = false;
					break;
				}
				controlInput.append(FG_SOCKET_PROTOCOL_VAR_SEP);
		}
		
		//trailing commas appear to be okay
		
		if(validFieldCount) {
			
			controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
		

			logger.debug("Writing control input: {}", controlInput.toString());
				
			writeControlInput(controlInput.toString(), port);
		}
		else
		{
			logger.error("Error writing control input. Missing field values");
		}

	}
	
	public synchronized void writeControlInput(String input, int port) {
		byte[] fgInputPayload = input.getBytes(UTF8_CHARSET);

		DatagramSocket fgInputSocket = null;

		logger.debug("Sending input to {}:{}", host, port);
		
		try {
			DatagramPacket fgInputPacket = new DatagramPacket(
				fgInputPayload, 
				fgInputPayload.length, 
				InetAddress.getByName(host), 
				port
			);

			fgInputPacket.setData(input.getBytes(UTF8_CHARSET));

			fgInputSocket = new DatagramSocket();
			
			fgInputSocket.setSoTimeout(SOCKET_TIMEOUT);

			fgInputSocket.send(fgInputPacket);

			logger.debug("Completed input write to {}:{}", host, port);
		} 
		catch (SocketException e) {
			//a subclass of IOException. timeouts are thrown here 
			logger.warn("SocketException writing control input", e);
		}
		catch (IOException e) {
			//thrown on send()
			logger.warn("IOException writing control input", e);
		} finally {
			if (fgInputSocket != null && !fgInputSocket.isClosed()) {
				fgInputSocket.close();
			}
			
			logger.debug("writeControlInput for {}:{} returning", host, port);
		}
	}
	
//no stream resources to close
//	public void shutdown() {
//		
//	}
}

