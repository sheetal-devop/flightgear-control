package org.jason.fgcontrol.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.jason.fgcontrol.aircraft.config.ConfigDirectives;
import org.jason.fgcontrol.aircraft.f15c.F15C;
import org.jason.fgcontrol.aircraft.f15c.F15CConfig;
import org.jason.fgcontrol.aircraft.f15c.flight.F15CWaypointFlightExecutor;
import org.jason.fgcontrol.aircraft.f15c.flight.RunwayBurnoutFlightExecutor;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.flight.position.KnownRoutes;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class F15CService {

	private final static Logger LOGGER = LoggerFactory.getLogger(F15CService.class);
	
	//TODO: dedicated repository/manager autowired bean implementation
	//for now, package-private and inline initialized is the only way to guarantee initialization
	HashMap<String, F15C> activeAircraft = new HashMap<String, F15C>();
	HashMap<String, Properties> availableAircraft = new HashMap<String, Properties>();
	
	@Autowired
	FlightPlanExecutor flightplanExecutor;

	private final static long SHUTDOWN_SLEEP = 4 * 1000L;
	
	private final static int MAX_AIRCRAFT_NAME_LEN = 20;
	
	//need the relative paths for the classloader
	//TODO: directory
	private final static String[] KNOWN_AIRCRAFT_CONF_FILES = {
		"conf/f15c/f15c_alpha_flight.properties",
		"conf/f15c/f15c_alpha_runway.properties",
		"conf/f15c/f15c_beta_flight.properties",
		"conf/f15c/f15c_beta_runway.properties",
		"conf/f15c/f15c_delta_flight.properties",
		"conf/f15c/f15c_delta_runway.properties",
		"conf/f15c/f15c_epsilon_flight.properties",
		"conf/f15c/f15c_epsilon_runway.properties",
		"conf/f15c/f15c_gamma_flight.properties",
		"conf/f15c/f15c_gamma_runway.properties",
		"conf/f15c/f15c_zeta_flight.properties",
		"conf/f15c/f15c_zeta_runway.properties"
	};
	
	@PostConstruct
	private void postConstruct() {
    	LOGGER.info("Constructing F15CService");
    	
    	//load availableAircraft with our packaged properties files
    	for( String confFile : KNOWN_AIRCRAFT_CONF_FILES) {
    		Properties props = new Properties();
    		
    		try {
    			InputStream confFileIn = this.getClass().getClassLoader().getResourceAsStream(confFile);
    			
    			if(confFileIn != null) {
					props.load(confFileIn);
					
					String name = props.getProperty( ConfigDirectives.AIRCRAFT_NAME_DIRECTIVE );
					
					if(name.length() > MAX_AIRCRAFT_NAME_LEN) {
						LOGGER.error("Aircraft name {} too long. Skipping...");
					}
					else if( !availableAircraft.containsKey(name) ) {
						availableAircraft.put(name, props);
						
						LOGGER.info("Loaded aircraft config for {}", name);
					} else {
						LOGGER.warn("Available aircraft already had {} defined. Skipping...", name);
					}
    			} else {
    				LOGGER.error("Config file {} was not found by the classloader. Skipping...", confFile);
    			}
			} catch (IOException e) {
				LOGGER.error("IOException loading config file {}. Continuing...", confFile, e);
			}
    	}
    	
		for( String iAircraftName : availableAircraft.keySet()) {
    		LOGGER.debug("Available Aircraft: {}", iAircraftName);
    	}
    	
	}
	
    @PreDestroy
    private void onDestroy() {
    	LOGGER.info("Destroying F15CService");
        
    	LOGGER.info("Shutting down active F15Cs");
    	//shutdown our active aircraft
    	if(activeAircraft != null) {
			//shutdown all f15cs
			for ( Entry<String, F15C> entry : activeAircraft.entrySet()) {
				entry.getValue().shutdown();
			}
    	}
    	
    	LOGGER.info("Shutting down F15CService flightplan executor");
    	try {
			Thread.sleep(SHUTDOWN_SLEEP);
		} catch (InterruptedException e) {
			LOGGER.error("Shutdown sleep1 Interrupted", e);
		}
    	
    	//shutdown our plan executor running flightplans
    	flightplanExecutor.shutdown();
    	
    	try {
			Thread.sleep(SHUTDOWN_SLEEP);
		} catch (InterruptedException e) {
			LOGGER.error("Shutdown sleep2 Interrupted", e);
		}
    	
    	LOGGER.info("F15CService preDestroy completed");
    }
	
    public Set<String> listAvailableAircraft() {
    	return availableAircraft.keySet();
    }
    
    public Set<String> listActiveAircraft() {
    	return activeAircraft.keySet();
    }
    
	/**
	 * 
	 * @param name	The name of the aircraft. Must be a known aircraft name and not running
	 * @return 
	 * 
	 * @throws FlightGearSetupException		If an exception occurs during aircraft setup 
	 */
	public boolean buildF15C(String name) throws FlightGearSetupException {
		
		boolean success = false;
		
		//TODO: return a result object with success/failure message. ie if we're trying to doubly-build planes, return error message
		//TODO: only build if we're beneath a maximum. not all hardware can run many simulator instances. 
		// possibly value read from application.properties 
				
		//check that we arent running this aircraft already
		if(!activeAircraft.containsKey(name)) {
			
			//check that this is a known aircraft
			if(availableAircraft.containsKey(name)) {
			
				//get the properties mapped to the aircraft name
				Properties propFile = availableAircraft.get(name);
				
				//create an aircraft config
				F15CConfig config = new F15CConfig(propFile);
				
				//build the aircraft. started later by a specific action
				activeAircraft.put(name, new F15C(config));
				
				success = true;
			}
			else {
				LOGGER.error("Error: Unknown Aircraft name {}", name);
			}
		}
		else {
			LOGGER.error("Error: Active aircraft with name {} already exists", name);
		}
		
		return success;
	}
	
	public void stopF15C(String name) throws FlightGearSetupException {
		
		//TODO: return a result object with success/failure message. ie if we're trying to doubly-build planes, return error message
		//TODO: only build if we're beneath a maximum. possibly value read from application.properties 
		
		//vvvv these maps are null for some reason despite being initialized above
		
		//check that we arent running this aircraft already
		if(activeAircraft.containsKey(name)) {
			
			//check that this is a known aircraft
			if(availableAircraft.containsKey(name)) {
							
				//shutdown aircraft
				//C15c aircraft = activeAircraft.get(name);
				//if(aircraft != null) {
					
					LOGGER.info("Aircraft {} has been shut down", name);
					
					//executor invokes FlightGearAircraft.shutdown on the runnable
					flightplanExecutor.stop(name);
					
				//} else {
				//	LOGGER.error("Error: found a null aircraft when attempting shutdown: {}", name);
				//}
	
				//build the aircraft. started later by a specific action
				activeAircraft.remove(name);
			}
			else {
				LOGGER.error("Error: Unknown Aircraft name {}", name);
			}
		}
		else {
			LOGGER.error("Error: Active aircraft with name {} does not exist", name);
		}
	}

	public void runRunwayPlan(String name) {
		
		//TODO: supply user-defined FlightParameters to flightexecutor
		//TODO: prevent doubly-running aircraft. likely resolved with a better executor for flightplans
		
		if(activeAircraft.containsKey(name)) {
			
			FlightPlanRunnable myRunnable = new FlightPlanRunnable() {
				public void run() {
					try {
						LOGGER.debug("Executing flightplan for {}", name);
						RunwayBurnoutFlightExecutor.runFlight(activeAircraft.get(name));
						LOGGER.debug("Completed flightplan for {}", name);
					} catch (IOException | AircraftStartupException | InterruptedException | FlightGearSetupException e) {
						LOGGER.error("Exeception operating aircraft {}", name, e);
					}
					finally {
						
						LOGGER.debug("Removing aircraft {} from active status", name);
						
						//no longer active
						activeAircraft.remove(name);
					}
				}

				@Override
				protected void shutdownFlightPlan() {
					activeAircraft.get(name).shutdown();
					
					//remove from active
					activeAircraft.remove(name);
				}
			};
			
			myRunnable.setName(name);
			
			flightplanExecutor.run(myRunnable);
		}
		else {
			LOGGER.error("Error: Aircraft {} is not active", name);
		}
	}
	
	public void runFlightPlan(String name) {
		
		//TODO: supply user-defined FlightParameters to flightexecutor
		//TODO: prevent doubly-running aircraft. likely resolved with a better executor for flightplans
		
		if(activeAircraft.containsKey(name)) {
			
			FlightPlanRunnable myRunnable = new FlightPlanRunnable() {
				public void run() {
					try {
						
						//TODO: hardcode for now, make dynamic later
						activeAircraft.get(name).setWaypoints( KnownRoutes.BC_SOUTH_DEMO );
						
						LOGGER.debug("Executing flightplan for {}", name);
						F15CWaypointFlightExecutor.runFlight(activeAircraft.get(name));
						LOGGER.debug("Completed flightplan for {}", name);
					} catch (IOException e) {
						LOGGER.error("Exeception operating aircraft {}", name, e);
					}
					finally {
						
						// flight plan done - pause simulator so the plane doesn't drift off
						try {
							activeAircraft.get(name).setPause(true);
						} catch (IOException e) {
							LOGGER.error("IOException pausing simulator after flightplan was complete: {}", name, e);
						}
						
						LOGGER.debug("Removing aircraft {} from active status", name);
						
						//no longer active
						activeAircraft.remove(name);
					}
				}

				@Override
				protected void shutdownFlightPlan() {
					activeAircraft.get(name).shutdown();
					
					//remove from active
					activeAircraft.remove(name);
				}
			};
			
			myRunnable.setName(name);
			
			flightplanExecutor.run(myRunnable);
		}
		else {
			LOGGER.error("Error: Aircraft {} is not active", name);
		}
	}
	
	public Map<String, String> getTelemetry(String name) {
		Map<String, String> retval = null;
		if(activeAircraft.containsKey(name)) {
			retval = activeAircraft.get(name).getTelemetry();
			LOGGER.debug("Retrieved telemetry for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
		return retval;
	}
		
	public void addWaypoint(String name, double lat, double lon) {
		if(activeAircraft.containsKey(name)) {
			activeAircraft.get(name).addWaypoint(lat, lon);
			LOGGER.debug("Added new waypoint for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	}  
	
	public void removeWaypoint(String name, double lat, double lon) {
		if(activeAircraft.containsKey(name)) {
			activeAircraft.get(name).removeWaypoints(lat, lon);
			LOGGER.debug("Added new waypoint for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	} 
	
	public void setFlightPlan(String name, String flightPlanName) {
		if(activeAircraft.containsKey(name)) {
			
			ArrayList<WaypointPosition> waypoints = KnownRoutes.lookupKnownRoute(flightPlanName);
			
			if(waypoints != null) {
				activeAircraft.get(name).clearWaypoints();
				activeAircraft.get(name).setWaypoints(waypoints);
				LOGGER.debug("Added new flightplan for f15c {}", name);
			} else {
				LOGGER.error("Failed to resolve new flightplan for f15c {}", name);
			}

		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	}
	
	public void clearFlightPlan(String name, String flightPlanName) {
		if(activeAircraft.containsKey(name)) {
			activeAircraft.get(name).clearWaypoints();
			LOGGER.debug("Added new waypoint for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	}
	
	public void resetSimulator(String name) {
		if(activeAircraft.containsKey(name)) {
			try {
				activeAircraft.get(name).resetSimulator();
			} catch (IOException e) {
				LOGGER.error("IOException: Resetting simulator for f15c {}", name, e);
			} catch (InvalidTelnetOptionException e) {
				LOGGER.error("InvalidTelnetOptionException: Resetting simulator for f15c {}", name, e);
			}
			LOGGER.debug("Reset simulator for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	}
	
	public void terminateSimulator(String name) {
		if(activeAircraft.containsKey(name)) {
			try {
				activeAircraft.get(name).terminateSimulator();;
			} catch (IOException e) {
				LOGGER.error("IOException: Terminating simulator for f15c {}", name, e);
			} catch (InvalidTelnetOptionException e) {
				LOGGER.error("InvalidTelnetOptionException: Terminating simulator for f15c {}", name, e);
			}
			LOGGER.debug("Terminating simulator for f15c {}", name);
		}
		else {
			LOGGER.error("Error: Active Aircraft with name {} does not exist", name);
		}
	}
}
