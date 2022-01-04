package org.jason.flightgear.flight.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dumbly manage a list of waypoints. 
 * 
 * The plane needs to internally compensate for a radical course change, and interpolate intermediate points as needed.
 * 
 * @author jason
 *
 *
 * TODO: offer function to import from gpx file with granularity parameter
 */
public class WaypointManager {

	private List<WaypointPosition> waypoints;
	
	public WaypointManager() {
		waypoints = Collections.synchronizedList(new ArrayList<WaypointPosition>());
	}
	
	public WaypointManager(ArrayList<WaypointPosition> positions) {	
		waypoints = Collections.synchronizedList(positions);
	}
	
	//add new waypoint to the end of the flightplan
	public synchronized void addWaypoint(double lat, double lon) {
		addWaypoint(new WaypointPosition(lat, lon));
	}
	
	//add new waypoint to the end of the flightplan
	public synchronized void addWaypoint(WaypointPosition pos) {		
		waypoints.add(waypoints.size(), pos);
	}
	
	public synchronized WaypointPosition getNextWaypoint() {
		return waypoints.get(0);
	}
	
	public synchronized WaypointPosition getAndRemoveNextWaypoint() {
		return waypoints.remove(0);
	}
	
	public synchronized int getWaypointCount() {
		return waypoints.size();
	}

	public List<WaypointPosition> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<WaypointPosition> waypoints) {
		this.waypoints = waypoints;
	}

	
	@Override
	public String toString() {
		return "WaypointManager [waypoints=" + waypoints + "]";
	}
}
