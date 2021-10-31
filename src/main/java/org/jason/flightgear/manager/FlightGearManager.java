package org.jason.flightgear.manager;

import org.jason.flightgear.connection.sockets.FlightGearSocketsConnection;
import org.jason.flightgear.connection.telnet.FlightGearTelnetConnection;

/**
 * 
 * Manage both socket and telnet connections to control a plane
 *
 */
public class FlightGearManager {
	
	private FlightGearTelnetConnection telnetConnection;
	private FlightGearSocketsConnection socketsConnection;
	
	public void connect() {
		
	}
	
	public void shutdown() {
		
	}
}
