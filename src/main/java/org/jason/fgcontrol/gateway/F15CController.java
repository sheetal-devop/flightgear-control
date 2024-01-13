package org.jason.fgcontrol.gateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class F15CController {

	private final static Logger LOGGER = LoggerFactory.getLogger(F15CController.class);
	
	@Autowired
	F15CService f15cService;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.info("Constructing F15CController");
		
	}
	
    @PreDestroy
    private void onDestroy() {
    	LOGGER.info("Destroying F15CController");
    }
	
    ///////////////////////////////
    //endpoints
        
    @GetMapping("/fgctl/f15c/build")
    @RequestMapping(
    	value = "/fgctl/f15c/build", 
    	method = RequestMethod.GET, 
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> build(@RequestParam("name") String name) {   	
    	
    	String message = "None";
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	JSONObject responseJSON = new JSONObject();
    	
    	if(name != null && !StringUtils.isEmpty(name)) {
    	
	    	try {
				if (f15cService.buildF15C(name) ) {
					message = "F15C built with name: " + name;
					responseCode = HttpStatus.OK;
				} else {
					message = "F15C could not be built with name: " + name;
					responseCode = HttpStatus.OK;
				}
			} catch (FlightGearSetupException e) {
	    		message = "FlightGearSetupException building F15C: " + name;
	    		LOGGER.error(message, e);
			}
    	}
    	else {
    		message = "Cannot build F15C with null or empty name";
    		LOGGER.error(message);
    	}
    	
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    @GetMapping("/fgctl/f15c/stop")
    @RequestMapping(value = "/fgctl/f15c/stop", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> stop(@RequestParam("name") String name) {  
    	
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	String message = "";
    	
    	try {
    		f15cService.stopF15C(name);
    		message = "F15C stopped with name: " + name;
    		responseCode = HttpStatus.OK;
    	} catch (FlightGearSetupException e) {
    		message = "FlightGearSetupException stopping F15C: " + name;
    		LOGGER.error(message, e);
		} 
    	
    	responseJSON.put("message", message);
    	
    	return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    ////////
    // flightplans
    
    @GetMapping("/fgctl/f15c/runRunwayPlan")
    @RequestMapping(
    	value = "/fgctl/f15c/runRunwayPlan", 
    	method = RequestMethod.GET, 
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> runRunwayPlan(@RequestParam("name") String name) {   	
    	
    	String message = "None";
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	JSONObject responseJSON = new JSONObject();
    	
    	//TODO: get result
    	f15cService.runRunwayPlan(name);
		
		responseCode = HttpStatus.OK;
    	
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    @GetMapping("/fgctl/f15c/runFlightPlan")
    @RequestMapping(
    	value = "/fgctl/f15c/runFlightPlan", 
    	method = RequestMethod.GET, 
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> runFlightPlan(@RequestParam("name") String name) {   	
    	
    	String message = "None";
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	JSONObject responseJSON = new JSONObject();
    	
    	//TODO: get result
    	f15cService.runFlightPlan(name);
		
		responseCode = HttpStatus.OK;
    	
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    
    //////////
    // info
    
    @GetMapping("/fgctl/f15c/whoami")
    @RequestMapping(value = "/fgctl/f15c/whoami", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> whoami() {   	
    	String retval = "{\"message\": \"I am a F15CController\"}";
    	
		return new ResponseEntity<String>(retval, HttpStatus.OK);
    }
	
    @GetMapping("/fgctl/f15c/telemetry")
    @RequestMapping(value = "/fgctl/f15c/telemetry", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTelemetry(@RequestParam("name") String name) {
    	
    	LOGGER.info("Getting telemetry for f15c aircraft {}", name);
    	
    	//get telemetry from our aircraft
    	
    	String message = "";
    	String telemJSON = null;
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		telemJSON = new JSONObject(f15cService.getTelemetry(name)).toString();
    		responseCode = HttpStatus.OK;
    		message = "None";
    	}
    	catch (Exception e) {
    		LOGGER.error("Exception parsing aircraft telemetry", e);
    		
    		message = "Exception: " + e.getLocalizedMessage();
    	}
    	finally {
        	if(telemJSON == null) {
        		telemJSON = "{}";
        	}
    	}
    	
    	responseJSON.put("response", responseCode);
    	responseJSON.put("message", message);
    	responseJSON.put("telemetry", telemJSON);
    	
    	//TODO: return responseJSON instead
        return new ResponseEntity<String>(telemJSON, responseCode);
    }
    
    ////////////
    //simulator management
    
    @GetMapping("/fgctl/f15c/resetSimulator")
    @RequestMapping(value = "/fgctl/f15c/resetSimulator", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetSimulator(@RequestParam("name") String name) {  
    	
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		f15cService.resetSimulator(name);
    		responseCode = HttpStatus.OK;
    	} finally {}
    	
    	return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    @GetMapping("/fgctl/f15c/terminateSimulator")
    @RequestMapping(value = "/fgctl/f15c/terminateSimulator", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> terminateSimulator(@RequestParam("name") String name) {  
    	
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		f15cService.terminateSimulator(name);
    		responseCode = HttpStatus.OK;
    	} finally {}
    	
    	return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
}
