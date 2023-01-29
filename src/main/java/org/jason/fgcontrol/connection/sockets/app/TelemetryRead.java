package org.jason.fgcontrol.connection.sockets.app;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jason.fgcontrol.connection.sockets.FlightGearTelemetryConnection;

/**
 * Simple telemetry read from the flightgear sockets telemetry port. Doesn't start or modify the state of the plane.
 * 
 * @author jason
 *
 */
public class TelemetryRead {

    private FlightGearTelemetryConnection fgSocketsClient;
    
    public TelemetryRead(String host, int port) throws SocketException, UnknownHostException  {
        fgSocketsClient = new FlightGearTelemetryConnection(host, port);
    }
    
    public String readTelemetry() throws IOException {
        return fgSocketsClient.readTelemetry();
    }
        
    public static void main(String[] args) {
        
        TelemetryRead telemetryReader = null;
        
        int port = -1;
        
        if(args.length != 2) {
        	System.err.println("Usage: TelemetryRead [host] [port]");
        	System.exit(-1);
        }
        
        String host = args[0];
        
        try {
        	port = Integer.parseInt(args[1]);
        } catch (Exception e) {
        	
        } finally {
        	if(port == -1) {
        		System.err.println("Invalid port");
        		System.exit(-1);
        	}
        }
        
        int readSleep = 3000;
        int maxCycles = 50;
        
        try {
            telemetryReader = new TelemetryRead(host, port);
            
            for(int i = 0; i< maxCycles; i++) {
                System.out.println("========\n" + telemetryReader.readTelemetry() + "\n========");
                
                try {
                    Thread.sleep(readSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        	//underlying FlightGearTelemetryConnection doesn't require external close
        }
    }
}
