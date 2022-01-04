package org.jason.fgcontrol.connection.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
    
    private DatagramPacket fgTelemetryPacket;
    
    private final static int SOCKET_TIMEOUT = 5000;
    
    //keep an eye on this. this can silently truncate a telemetry read resulting in consistent read failures 
    private final static int MAX_RECEIVE_BUFFER_LEN = 8192;
    
    //TODO: default values overridable, or used to generate protocol files from templates
    private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
    
    private Pattern telemetryLinePattern;
    
    public FlightGearTelemetryConnection(String host,  int telemetryPort) 
            throws SocketException, UnknownHostException {
        
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
            
            LOGGER.trace("Raw telemetry successfully received from socket.");
            
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
                //only care about these warnings if we're looking at trace level stuff
                if(LOGGER.isTraceEnabled()) {
                    LOGGER.warn("Dropping malformed telemetry line: {}", line);
                }
            }
        }
        
        LOGGER.trace("readTelemetry returning. Read {} lines", telemetryLineCount);
                
        
        //return after adding json braces
        return cleanOutput.insert(0, "{").append("}").toString();
    }
}

