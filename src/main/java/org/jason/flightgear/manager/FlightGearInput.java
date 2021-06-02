package org.jason.flightgear.manager;

/**
 * An input schema and a port
 */
public class FlightGearInput {
	
	private int port;
	private String[] schema;
	
	public FlightGearInput(String[] schema, int port) {
		this.schema = schema;
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public String[] getSchema() {
		return schema;
	}
}
