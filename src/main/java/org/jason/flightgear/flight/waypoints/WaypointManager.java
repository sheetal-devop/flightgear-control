package org.jason.flightgear.flight.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jason.flightgear.flight.WaypointPosition;

/**
 * Dumbly manage a list of waypoints. 
 * 
 * The plane needs to internally compensate for a radical course change, and interpolate intermediate points as needed.
 * 
 * @author jason
 *
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
	public void addWaypoint(double lat, double lon) {
		addWaypoint(new WaypointPosition(lat, lon));
	}
	
	//add new waypoint to the end of the flightplan
	public void addWaypoint(WaypointPosition pos) {
		int index = 0;
		
		if(waypoints.size() > 0) {
			index = waypoints.size() - 1;
		}
		
		waypoints.add(index, pos);
	}
	
	public WaypointPosition getNextWaypoint() {
		return waypoints.get(0);
	}
	
	public WaypointPosition getAndRemoveNextWaypoint() {
		return waypoints.remove(0);
	}
	
	public int getWaypointCount() {
		return waypoints.size();
	}
	


}
