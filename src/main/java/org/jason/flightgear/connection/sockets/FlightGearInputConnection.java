package org.jason.flightgear.connection.sockets;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control a virtual plane in a flightgear sim through a socket connection
 *  
 *
 */
//TODO: support optionally setting a schema and enforce it on writes. Then require a schema read from the protocol xml file.
public class FlightGearInputConnection implements Closeable {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearInputConnection.class);
    
    private String host;
    private InetAddress hostInet;
    
    private int port;
    
    private final static int SOCKET_TIMEOUT = 5000;
    
    private final static int MAX_WRITE_LEN = 300;
    
    private final static long DEFAULT_BENCH_TIME = 0L;
    
    //TODO: default values overridable, or used to generate protocol files from templates
    private final static String FG_SOCKET_PROTOCOL_VAR_SEP = ",";
    private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
    
    private Charset utf8_charset;
    
    private DatagramSocket fgInputSocket;

    public FlightGearInputConnection(String host,  int port) 
            throws SocketException, UnknownHostException {
        
        if(Charset.isSupported(StandardCharsets.UTF_8.name())) {
            this.utf8_charset = StandardCharsets.UTF_8;
        } else {
            throw new SocketException("Required charset: " + StandardCharsets.UTF_8.name() + " not supported in this environment");
        }
        
        this.host = host;
        this.hostInet = InetAddress.getByName(this.host);
        
        if( port <= 0 ) {
            throw new SocketException("Invalid port");
        }
        
        this.port = port;
        
        //setup socket
        this.fgInputSocket = new DatagramSocket();
        this.fgInputSocket.setSoTimeout(SOCKET_TIMEOUT);
        
    }
    
    public synchronized void writeControlInput(LinkedHashMap<String, String> inputHash) throws IOException {
        
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
                LOGGER.error("Missing field value for field: {}", entry.getKey());
                    
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
				writeToSocket(controlInput.toString());
				
	            if(LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Wrote control input to socket: {}", controlInput.toString());
	                
	                StringBuilder output = new StringBuilder();
	 
	                for( Entry<String, String> field : inputHash.entrySet()) {
	                    output.append(String.format("%s => %s\n", field.getKey(), field.getValue()));
	                }
	                    
	                LOGGER.debug("Wrote field data to socket:\n{}\n=======", output.toString() );    
	            }
			} catch (IOException e) {
				LOGGER.error("Socket write failed", e);
				throw e;
			}
        }
        else
        {
            LOGGER.error("Error writing control input. Missing field values");
        }
    }
    
    private synchronized void writeToSocket(String input) throws IOException {

    	if(input.length() > MAX_WRITE_LEN) {
    		LOGGER.error("Socket input data too large. Abandoning write.");
    		throw new SocketException("Socket input data size above threshold.");
    	}
    	
    	long startTime = DEFAULT_BENCH_TIME, endTime = DEFAULT_BENCH_TIME;
    	
    	if(LOGGER.isTraceEnabled()) {
    		startTime = System.currentTimeMillis();
    	}
    	
    	byte[] fgInputPayload = input.getBytes(utf8_charset);

        LOGGER.debug("Sending input to {}:{}", this.host, this.port);

        try {
            DatagramPacket fgInputPacket = new DatagramPacket(
                fgInputPayload, 
                fgInputPayload.length, 
                this.hostInet, 
                this.port
            );

            fgInputPacket.setData(fgInputPayload);

            fgInputSocket.send(fgInputPacket);

            LOGGER.debug("Completed input write to {}:{}", this.host, this.port);
        } 
        catch (SocketException e) {
            //a subclass of IOException. timeouts are thrown here 
            LOGGER.error("SocketException writing control input", e);
            throw e;
        }
        catch (IOException e) {
            //thrown on send()
            LOGGER.error("IOException writing control input", e);
            throw e;
        }
        
        if(LOGGER.isTraceEnabled()) {
        	endTime = System.currentTimeMillis();
        	if(startTime != DEFAULT_BENCH_TIME ) {
        		LOGGER.trace("writeToSocket executed write in " + (endTime-startTime) + "ms");
        	} else {
        		LOGGER.warn("Attempted to benchmark, but startTime was not setCorrectly");
        	}
        }
        
        LOGGER.debug("writeControlInput for {}:{} returning", this.host, this.port);
    }

	@Override
	public void close() throws IOException {
		LOGGER.info("Shutting down socket for {}:{} ", this.host, this.port);
		
        if (fgInputSocket != null && !fgInputSocket.isClosed()) {
            fgInputSocket.close();
        }
	}
}

