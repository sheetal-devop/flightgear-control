package org.jason.flightgear.manager;

import org.jason.flightgear.sockets.FlightGearManagerSockets;
import org.jason.flightgear.telnet.FlightGearManagerTelnet;

/**
 * 
 * Manage both socket and telnet connections to control a plane
 *
 */
public class FlightGearManager {
	
	private FlightGearManagerTelnet telnetConnection;
	private FlightGearManagerSockets socketsConnection;
	
	public void connect() {
		
	}
	
	public void shutdown() {
		
	}
}
