package org.jason.fgcontrol.gateway;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Execute flight plans.
 * 
 * Support canceling a flight plan during operation and gracefully shutting down the underlying aircraft.
 * 
 * A uncanceled, terminating flight plan needs to remove the associated entry from tasks. Use internal Watchdog runnable?
 * 
 * Tasks should be checked for isDone or isCanceled before modifying.
 * 
 */
@Component
public class FlightPlanExecutor {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(FlightPlanExecutor.class);
	
	private final static int TPOOL_SIZE = 12;
	
	ExecutorService planExecutor = Executors.newFixedThreadPool(TPOOL_SIZE);
	
	HashMap<String, FlightPlanRunnable> tasks = new HashMap<>();

	private final static long WATCHDOG_CYCLE_SLEEP = 1 * 1000L;
	
	Future<?> watchdogFuture = null;
	boolean watchDogRunning = false;
	
	//in shutdown state to start
	boolean shutdown = true;
	
	Thread watchdog = new Thread() {
		@Override
		public void run() {
			LOGGER.debug("Watchdog thread starting");
			
			while(watchDogRunning) {
				for( Entry<String, FlightPlanRunnable> task : tasks.entrySet()) {
					
					if (task.getValue().isCancelled()) {
						tasks.remove(task.getKey());
						LOGGER.info("Watchdog removing cancelled task {}", task.getKey());
					} else if (task.getValue().isDone()) {
						LOGGER.info("Watchdog removing done task {}", task.getKey());
						tasks.remove(task.getKey());
					} else if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Watchdog reporting running task: {}", task.getKey());
					}
				}
				
				try {
					Thread.sleep(WATCHDOG_CYCLE_SLEEP);
				} catch (InterruptedException e) {
					LOGGER.warn("Watchdog sleep interrupted", e);
					
					watchDogRunning = false;
				}
				
				LOGGER.debug("Watchdog cycle completed");
			}
			
			LOGGER.debug("Watchdog thread returning");
		}
	};
	
	public void launch() {
		LOGGER.info("Launching FlightPlanExecutor");
		
		watchDogRunning = true;
		
    	watchdogFuture = planExecutor.submit(watchdog);
    	
    	//with watchdog running, signal no longer in shutdown state
    	shutdown = false;
    	
    	//do not add watchdog future to tasks. manage runnable directly.
	}
	
	@PostConstruct
	private void postConstruct() {
    	LOGGER.info("Constructing FlightPlanExecutor");
    	
    	launch();
	}
	
	public String run(FlightPlanRunnable runnable) {
		String name = null;
		
		if(tasks.containsKey(runnable.getName())) {
			LOGGER.error("Name conflict - Flightplan with name \"{}\" already executing");
		} else {
			name = runnable.getName();
			
			LOGGER.info("Beginning execution of flightplan runnable {}", name);
			
			Future<?> future = planExecutor.submit(runnable);
			
			runnable.setFuture(future);
			
			tasks.put(name, runnable);
		}
		
		return name;
	}
	
	/**
	 * Stop one of our executing flight plans
	 * 
	 * If task doesn't honour interrupts and it has already started, it will run to completion.
	 * 
	 * @param id
	 */
	public void stop(String id) {
		if(tasks.containsKey(id)) {
    		LOGGER.info("Terminating task {}", id);
    		
    		tasks.get(id).stop();
		} else {
			LOGGER.warn("Failed to terminate task {} - not a known task");
		}
	}
	
	public boolean isShutdown() {
		return shutdown;
	}
	
	public void shutdown() {
		LOGGER.info("Shutting down FlightPlanExecutor");
		
		if(!isShutdown()) {
			shutdown = true;
			
	    	//terminate watchdog task first
	    	if(watchdogFuture != null) {
		    	if( watchdogFuture.cancel(true) ) {
		    		LOGGER.info("Successfully terminated watchdog task");
		    	} else {
		    		LOGGER.warn("Failed to terminate watchdog task");
		    	}
	    	} else {
	    		LOGGER.warn("Watchdog task future was null");
	    	}
	    	
	    		
	    	//terminate flightplans
	    	//trigger graceful shutdown of underlying aircraft
	    	for( Entry<String, FlightPlanRunnable> task : tasks.entrySet()) {
	    		
	    		LOGGER.info("Terminating task {}", task.getKey());
	    		
	    		task.getValue().stop();
	    	}
	    	
	    	//clear our task collection
	    	tasks.clear();
	    	
	    	//shutdown executor itself
	    	planExecutor.shutdown();
	    	
	    	
		} else {
			LOGGER.warn("Skipping shutdown of FlightPlanExecutor. Already in shutdown state.");
		}
		
		LOGGER.info("FlightPlanExecutor shutdown completed");
	}

	@PreDestroy
    private void onDestroy() {
    	LOGGER.info("Destroying FlightPlanExecutor");
    	
    	shutdown();
    	
    	LOGGER.info("FlightPlanExecutor preDestroy completed");
    }
	

}
