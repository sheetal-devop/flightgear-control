package org.jason.fgcontrol.gateway;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runnable for a FlightPlanExecutor to support graceful external shutdown and a better thread model for concurrent execution
 * 
 * Implementor handles start() and shutdown()
 */
public abstract class FlightPlanRunnable implements Runnable {

	private final static Logger LOGGER = LoggerFactory.getLogger(FlightPlanRunnable.class);
	
	private final static String DEFAULT_NAME = "DEFAULT_FlightPlanRunnable";
	
	private String name = DEFAULT_NAME;
	
	private final static int MAX_NAME_LEN = 100;
	
	private Future<?> myFuture;
	
	/**
	 * Gracefully shut down flight plan resources and cancel its execution. 
	 * 
	 */
	public void stop() {
		LOGGER.info("Stopping runnable execution for {}", name);
		
		shutdownFlightPlan();
		
		if(!myFuture.isCancelled()) {
			if(!myFuture.isDone()) {
				boolean result = myFuture.cancel(true);
				
				LOGGER.info("Execution cancelled for {}: result: {}", name, result);
			}
		}		
	}
	
	/**
	 * Set future from submission to executor
	 * 
	 * @param f
	 */
	public void setFuture(Future<?> f) {
		myFuture = f;
	}
	
	public boolean isDone() {
		if(myFuture == null) {
			LOGGER.error("FlightPlanRunnable had a null future in call to isDone");
			return true;
		}
		
		return myFuture.isDone();
	}
	
	public boolean isCancelled() {
		if(myFuture == null) {
			LOGGER.error("FlightPlanRunnable had a null future in call to isCancelled");
			return true;
		}
		
		return myFuture.isCancelled();
	}
	
	public void setName(String name) {
		if(name != null && !name.equals("") && name.length() < MAX_NAME_LEN) {
			LOGGER.info("Setting FlightPlanRunnable name to {}", name);
			this.name = name;
		} else {
			LOGGER.warn("Rejecting invalid name");
		}
	}
	
	public String getName() {
		return name;
	}
	
	protected abstract void shutdownFlightPlan();
}
