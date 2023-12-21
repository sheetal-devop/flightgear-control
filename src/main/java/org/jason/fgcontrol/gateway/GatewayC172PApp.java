package org.jason.fgcontrol.gateway;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.aircraft.c172p.C172PConfig;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class GatewayC172PApp {

	private final static Logger LOGGER = LoggerFactory.getLogger(GatewayC172PApp.class);
	
    private static C172P aircraft;

	//private static ConfigurableApplicationContext ctx;

    
    ///////////////////////////////
    //endpoints
    
    @GetMapping("/fgctl/telem")
    @RequestMapping(value = "/fgctl/telem", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTelemetry() {
    	
    	LOGGER.info("Getting telemetry");
    	
    	//get telemetry from our aircraft
    	
    	String telemJSON = null;
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		telemJSON = new JSONObject(aircraft.getTelemetry()).toString();
    		responseCode = HttpStatus.OK;
    	}
    	catch (Exception e) {
    		LOGGER.error("Exception parsing aircraft telemetry", e);
    	}
    	finally {
        	if(telemJSON == null) {
        		telemJSON = "{}";
        	}
    	}
    	
        return new ResponseEntity<String>(telemJSON, responseCode);
    }
    
    @PostMapping("/fgctl/setCarbIce/{iceAmt}")
    @RequestMapping(value = "/fgctl/setCarbIce", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void setCarbIce(double iceAmt) {
    	try {
			aircraft.setCarbIce(iceAmt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception in setCarbIce", e);
		}
    }
    
    ///////////////////////////////
    
    /**
     * 
     * 
     * @throws Exception
     */
    @PreDestroy
    public void onDestroy() throws Exception {
    	LOGGER.info("Destroying C172P Container");
        
        shutdown();
    }
    
    /**
     * Shuts down the aircraft. Does not affect container.
     * 
     * @return
     */
    @GetMapping("/shutdown") 
    @RequestMapping(value = "/shutdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String shutdown()
    {
    	if(aircraft != null) {
    		LOGGER.info("Shutting down aircraft");
    		
    		aircraft.shutdown();
    	}
    	
    	return "{\"Shutdown\": \"true\"}";
    }

    //////////////////////////
    //main
    
	public static void main(String[] args) {
    	
		//load config from cli args
		
		//load aircraft?
		
		//load endpoints
		
		//wait for shutdown
    	
    	/////////////
    	
    	//hardcode aircraft type for now
    	
		C172PConfig c172pConfig = null;
		if (args.length >= 1) {
			//only care about the first arg for the sim config
			String confFile = args[0];
			
			Properties simProperties = new Properties();
			try {
				simProperties.load(new FileInputStream(confFile));
				c172pConfig = new C172PConfig(simProperties);
				LOGGER.info("Using config:\n{}", c172pConfig.toString());
			} catch (IOException e) {	
				LOGGER.error("Error loading sim config", e);
			
				System.exit(1);
			}
		} else {
			LOGGER.error("Missing config file argument");
			System.exit(1);
		}
		
    	try {

    		
			aircraft = new C172P(c172pConfig);
			
		} catch (FlightGearSetupException e) {
			LOGGER.error("Exception building C172P",e );
		}
    	
    	SpringApplication.run(GatewayC172PApp.class, args);
        
    }
}
