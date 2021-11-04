package org.jason.flightgear.connection.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a virtual plane in a flightgear sim through a socket connection
 *  
 *
 */
public class FlightGearSocketsConnection {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearSocketsConnection.class);
    
    private String host;
    private int telemetryPort;
    
    private DatagramPacket fgTelemetryPacket;
    
    private final static int SOCKET_TIMEOUT = 5000;
    private final static int MAX_RECEIVE_BUFFER_LEN = 4096;
    
    private final static int MAX_WRITE_LEN = 300;
    
    //TODO: default values overridable, or used to generate protocol files from templates
    private final static String FG_SOCKET_PROTOCOL_VAR_SEP = ",";
    private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
    
    //cache this so we don't have to constantly perform lookups when receiving data
    //TODO: replace with StandardCharsets.UTF_8;
    private final static String UTF8_CHARSET_STR = "UTF-8";
    private Charset utf8_charset;
    
    private Pattern telemetryLinePattern;
    
    public FlightGearSocketsConnection(String host,  int telemetryPort) 
            throws SocketException, UnknownHostException {
        
        if(Charset.isSupported(UTF8_CHARSET_STR)) {
            this.utf8_charset = Charset.forName(UTF8_CHARSET_STR);
            //StandardCharsets.UTF_8.name()
        } else {
            throw new SocketException("UTF8 charset not found");
        }
        
        //set this here rather than before the match because we don't want to initialize it with every telemetry read
        this.telemetryLinePattern = Pattern.compile("^[\"/]\\S+[\": ]\\S+[,]?$");
        
        this.host = host;
        
        if( telemetryPort <= 0 ) {
            throw new SocketException("Invalid port");
        }
        
        this.telemetryPort = telemetryPort;
        
        byte[] receivingDataBuffer = new byte[MAX_RECEIVE_BUFFER_LEN];
        
        this.fgTelemetryPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
    }
    
    /**
     * Read output fields from the flightgear output socket.
     * 
     * @return    A JSON string of telemetry data.
     * 
     * @throws IOException
     */
    public String readTelemetry() throws IOException {
        
        LOGGER.trace("Telemetry called for {}:{}", host, telemetryPort);
        
        //empty string by default
        String output = "";
        
        DatagramSocket fgTelemetrySocket = null;

        try {
            //technically a server connection. we connect to the fg port, which starts sending us data
            fgTelemetrySocket = new DatagramSocket( telemetryPort, InetAddress.getByName(host) );
            fgTelemetrySocket.setSoTimeout(SOCKET_TIMEOUT);
            
            fgTelemetrySocket.receive(fgTelemetryPacket);
            
            output = new String(fgTelemetryPacket.getData()).trim();  
            
            LOGGER.trace("Raw telemetry was received from socket.");
            
        } catch (IOException e) {
            //comms errors connecting to fg telemetry socket
            
            //e.printStackTrace();
            LOGGER.warn("IOException reading raw telemetry from socket", e);

            throw e;
        }
        finally {
            if( fgTelemetrySocket != null && !fgTelemetrySocket.isClosed() ) {
                fgTelemetrySocket.close();
            }
            else
            {
                LOGGER.warn("Attempted to close fgTelemetrySocket, but was already closed or null");
            }
        }
                
        /*
         occasionally see this. 
         not sure where those extra digits and whitespace are coming from before the closing brace
         
        ...
        "/velocities/groundspeed-kt": 8.734266,
        "/velocities/vertical-speed-fps": -303.683014
        874}
        
        
        ...
        "/velocities/vertical-speed-fps": -163.828430
        8


        0}    
        */
        
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("=========================\nRaw telemetry received:\n{}\n=========================\n", output);
        }
        
        String[] lines = output.split(FG_SOCKET_PROTOCOL_LINE_SEP);
        
        //TODO: count accepted lines and compare against schema
        
        LOGGER.trace("Parsing up raw telemetry data");
        
        int telemetryLineCount = 0;
        StringBuilder cleanOutput = new StringBuilder();
        for(String line : lines) {
            
            //only match lines that look like telemetry
            if( telemetryLinePattern.matcher(line).matches() ) 
            {
                cleanOutput.append(line);
                telemetryLineCount++;
            } else {
                LOGGER.warn("Dropping malformed telemetry line: {}", line);
            }
        }
        
        LOGGER.trace("readTelemetry returning. Read {} lines", telemetryLineCount);
                
        
        //return after adding json braces
        return cleanOutput.insert(0, "{").append("}").toString();
    }
    
    public synchronized void writeControlInput(LinkedHashMap<String, String> inputHash, int port) {
        
        boolean validFieldCount = true;
        
        StringBuilder controlInput = new StringBuilder();
        controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
        
        //foreach key, write the value into a simple unquoted csv string. fail socket write on missing values
        for( Entry<String, String> entry : inputHash.entrySet()) {
 
        	//TODO: more checks around value
            if( !entry.getValue().equals( "" )) {
                controlInput.append(entry.getValue());
            }
            else {
                LOGGER.error("Missing field value: {}" + entry.getKey());
                    
                //field count check later
                validFieldCount = false;
                break;
            }
            controlInput.append(FG_SOCKET_PROTOCOL_VAR_SEP);
        }
        
        //trailing commas appear to be okay
        
        if(validFieldCount) {
            
            controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
        
            try {
				writeToSocket(controlInput.toString(), port);
				
	            if(LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Wrote control input to socket: {}", controlInput.toString());
	                
	                StringBuilder output = new StringBuilder();
	 
	                for( Entry<String, String> field : inputHash.entrySet()) {
	                    output.append(String.format("%s => %s\n", field.getKey(), field.getValue()));
	                }
	                    
	                LOGGER.debug("Wrote field data to socket:\n{}\n=======", output.toString() );    
	            }
			} catch (SocketException e) {
				LOGGER.error("Socket write failed", e);
			}
        }
        else
        {
            LOGGER.error("Error writing control input. Missing field values");
        }
    }
    
    private synchronized void writeToSocket(String input, int port) throws SocketException {

    	if(input.length() > MAX_WRITE_LEN) {
    		LOGGER.error("Socket input data too large. Abandoning write.");
    		throw new SocketException("Socket input data size above threshold.");
    	}
    	
    	byte[] fgInputPayload = input.getBytes(utf8_charset);
        
        DatagramSocket fgInputSocket = null;

        LOGGER.debug("Sending input to {}:{}", host, port);

        //TODO: maybe time the write
        try {
            DatagramPacket fgInputPacket = new DatagramPacket(
                fgInputPayload, 
                fgInputPayload.length, 
                InetAddress.getByName(host), 
                port
            );

            fgInputPacket.setData(input.getBytes(utf8_charset));

            fgInputSocket = new DatagramSocket();
            
            fgInputSocket.setSoTimeout(SOCKET_TIMEOUT);

            fgInputSocket.send(fgInputPacket);

            LOGGER.debug("Completed input write to {}:{}", host, port);
        } 
        catch (SocketException e) {
            //a subclass of IOException. timeouts are thrown here 
            LOGGER.error("SocketException writing control input", e);
            throw e;
        }
        catch (IOException e) {
            //thrown on send()
            LOGGER.error("IOException writing control input", e);
        } finally {
            if (fgInputSocket != null && !fgInputSocket.isClosed()) {
                fgInputSocket.close();
            }
            
            LOGGER.debug("writeControlInput for {}:{} returning", host, port);
        }
    }
    
//no stream resources to close
//    public void shutdown() {
//        
//    }
}

