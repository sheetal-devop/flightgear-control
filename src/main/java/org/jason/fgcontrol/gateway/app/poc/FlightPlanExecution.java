package org.jason.fgcontrol.gateway.app.poc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PConfig;
import org.jason.fgcontrol.exceptions.AircraftStartupException;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.jason.fgcontrol.gateway.FlightPlanExecutor;
import org.jason.fgcontrol.gateway.FlightPlanRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightPlanExecution {

	private final static Logger LOGGER = LoggerFactory.getLogger(FlightPlanExecution.class);

	private static C172P c172p = null;

	private final static long EXECUTION_SLEEP = 60 * 1000L;

	private final static long CYCLE_SLEEP = 2 * 1000L;
	
	private static boolean isDone;
	private static boolean isRunning;
	
	public static void main(String[] args) {

		FlightPlanExecutor executor = null;
		isRunning = true;
		isDone = false;

		C172PConfig c172pConfig = null;
		if (args.length >= 1) {
			// only care about the first arg for the sim config
			String confFile = args[0];

			Properties simProperties = new Properties();
			try {
				simProperties.load(new FileInputStream(confFile));
				c172pConfig = new C172PConfig(simProperties);
				LOGGER.info("Using config:\n{}", c172pConfig.toString());
			} catch (IOException e) {
				System.err.println("Error loading sim config");
				e.printStackTrace();

				System.exit(1);
			}
		}

		try {
			// start a FlightPlanRunnable that reads fields a few times, then signals that it's finished
			
			executor = new FlightPlanExecutor();
			executor.launch();
			c172p = new C172P(c172pConfig);

			////////////////////////////
			FlightPlanRunnable runnable = new FlightPlanRunnable() {

				@Override
				public void run() {
					
					try {
						// refill in case a previous run emptied it
						c172p.refillFuel();

						// probably not going to happen but do it anyway
						c172p.setDamageEnabled(false);

						// a full fuel tank will take a while
						c172p.setFuelTanksLevel(10.0);

						Thread.sleep(5000);
						
				        //start the engine up to start consuming fuel
						c172p.startupPlane(true);

				        //so the plane doesn't move- not that it really matters.
				        //has to happen after startup
						c172p.setParkingBrake(true);
				        
				        //engine should be running at this point but it's not ready  
				        //wait for startup to complete and telemetry reads to arrive
				        Thread.sleep(3000L);
					} catch (InterruptedException e) {
						LOGGER.error("InterruptedException during startup", e);
					} catch (IOException e) {
						LOGGER.error("IOException during startup", e);
					} catch (AircraftStartupException e) {
						LOGGER.error("AircraftStartupException during startup", e);
					}
					
					while (isRunning) {
						// simple plan. print the telemetry stream a few times

						LOGGER.info("=========================\nTelemetry Read:\nTank 0: {}\nTank 1: {}\n", 
							c172p.getFuelTank0Level(),
							c172p.getFuelTank1Level()
						);

						try {
							Thread.sleep(CYCLE_SLEEP);
						} catch (InterruptedException e) {
							LOGGER.error("Cycle sleep interrupted", e);
						}
					}

					isDone = true;
				}

				@Override
				protected void shutdownFlightPlan() {
					
					LOGGER.info("In shutdownFlightPlan");
					
					if (c172p != null) {
						c172p.shutdown();
					}
				}

			};
			////////////////////////////

			//set the runnable name
			runnable.setName(c172pConfig.getAircraftName());
			
			//submit our runnable to the executor
			executor.run(runnable);
			
			//allow our runnable to run for a set periods
			Thread.sleep(EXECUTION_SLEEP);
			
			//signal Runnable execution to break
			isRunning = false;
			
			LOGGER.info("Waiting for runnable termination");
			while(!isDone) {
				LOGGER.info("Waiting for runnable termination");
				
				Thread.sleep(500L);
			}
			
			LOGGER.info("Runnable is terminated");

		} catch (FlightGearSetupException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (executor != null) {
				executor.shutdown();
			}

			//TODO: getting here since nothing sets the aircraft object to null. may have to use a shutdown flag
			// shouldnt get here
//			if (c172p != null) {
//				LOGGER.warn("C172P shutdown invoked from main");
//				c172p.shutdown();
//			}
		}

	}

}
