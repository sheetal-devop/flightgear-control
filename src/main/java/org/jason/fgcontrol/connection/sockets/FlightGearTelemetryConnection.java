package org.jason.fgcontrol.connection.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a virtual plane in a flightgear sim through a socket connection
 *  
 *
 */
public class FlightGearTelemetryConnection {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearTelemetryConnection.class);
    
    private String host;
    private int telemetryPort;
        
    private final static int SOCKET_TIMEOUT = 5000;
    
    //keep an eye on this. this can silently truncate a telemetry read resulting in consistent
    //read failures if this is smaller than the data retrieved by the telemetry read 
    private final static int MAX_RECEIVE_BUFFER_LEN = 8192;
    
    //TODO: default values overridable, or used to generate protocol files from templates
    private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
    
    private Pattern telemetryLinePattern;

	private InetAddress hostIp;
    
    public FlightGearTelemetryConnection(String host,  int telemetryPort) 
            throws SocketException, UnknownHostException {
               
        this.host = host;
        this.hostIp = InetAddress.getByName(this.host);
        
        if( telemetryPort <= 0 ) {
            throw new SocketException("Invalid port");
        }
        
        this.telemetryPort = telemetryPort;
        
        LOGGER.info("Initializing FlightGearTelemetryConnection for {}({}):{}", this.host, this.hostIp, this.telemetryPort);
        
        //set this here rather than before the match because we don't want to initialize it with every telemetry read
        this.telemetryLinePattern = Pattern.compile("^[\"/]\\S+[\": ]\\S+[,]?$");
        
    	//TODO: if this is configured with the wrong port or host, it won't be known until the first read, 
    	//because the constructor doesn't actually establish a connection. maybe call a throwaway read
    }
    
    /**
     * Read output fields from the flightgear output socket.
     * 
     * @return    A JSON string of telemetry data.
     * 
     * @throws IOException
     */
    public String readTelemetry() throws IOException {
        
    	if(LOGGER.isTraceEnabled()) {
    		LOGGER.trace("Telemetry called for {}:{}", host, telemetryPort);
    	}
        
    	//TODO: move declarations to constructor
        String output = null;
        
        DatagramSocket fgTelemetrySocket = null;

        try {
        	
        	//TODO: find a way to reuse the packet object rather than reallocating new byte arrays for each invocation
        	//however see below when this can cause problems
            DatagramPacket fgTelemetryPacket = new DatagramPacket(new byte[MAX_RECEIVE_BUFFER_LEN], MAX_RECEIVE_BUFFER_LEN);
        	
            //technically a server connection. we connect to the fg port, which starts sending us data
            fgTelemetrySocket = new DatagramSocket( telemetryPort, hostIp );
            fgTelemetrySocket.setSoTimeout(SOCKET_TIMEOUT);
            fgTelemetrySocket.setReceiveBufferSize(MAX_RECEIVE_BUFFER_LEN);
            
            fgTelemetrySocket.receive(fgTelemetryPacket);
                        
            //TODO: switch to new String(byteArray, offset, len, charset), warn message if length doesn't match expectations
            
            //a call to new String(bytes[], charset) seems to be convention for converting the byte[]
            //returned by DatagramPacket.getData() to a string
            output = new String(fgTelemetryPacket.getData(), StandardCharsets.UTF_8).trim();  
            
            if(LOGGER.isTraceEnabled()) {
            	LOGGER.trace("Raw telemetry successfully received from socket.");
            }
        } 
        catch (SocketTimeoutException e) {
            LOGGER.warn("SocketTimeoutException reading raw telemetry from socket", e);

            throw e;
        }        
        catch (IOException e) {
            //comms errors connecting to fg telemetry socket
            
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
            
            //empty string by default
            if(output == null) {
            	output = "";
            }
        }
                
        /*
         hopefully fixed by recreating the byte buffer for each read VVVVV
         
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
        
        if(LOGGER.isTraceEnabled()) {
        	LOGGER.trace("Parsing up raw telemetry data");
        }
        
        int telemetryLineCount = 0;
        StringBuilder cleanOutput = new StringBuilder();
        for(String line : lines) {
            
            //only match lines that look like telemetry
            if( telemetryLinePattern.matcher(line).matches() ) 
            {
                cleanOutput.append(line);
                telemetryLineCount++;
            } else {
                //only care about these warnings if we're looking at trace level stuff
                if(LOGGER.isTraceEnabled()) {
                    LOGGER.warn("Dropping malformed telemetry line: {}", line);
                }
            }
        }
        
        if(LOGGER.isTraceEnabled()) {
        	LOGGER.trace("readTelemetry returning. Read {} lines", telemetryLineCount);
        }        
        
        //return after adding json braces
        return cleanOutput.insert(0, "{").append("}").toString();
    }
}
