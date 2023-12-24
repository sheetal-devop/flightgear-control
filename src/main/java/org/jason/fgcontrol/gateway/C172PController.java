package org.jason.fgcontrol.gateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.groovy.parser.antlr4.util.StringUtils;
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
public class C172PController {

	private final static Logger LOGGER = LoggerFactory.getLogger(C172PController.class);
	
	@Autowired
	C172PService c172pService;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.info("Constructing C172PController");
		
	}
	
    @PreDestroy
    private void onDestroy() {
    	LOGGER.info("Destroying C172PController");
    }
	
    ///////////////////////////////
    //endpoints
    
    /**
     * 
     * 
     * @param jsonConfigStr	A String representation of simulator properties
     * @return
     */
    /*
    @PostMapping("/fgctl/c172p/build")
    @RequestMapping(
    	value = "/fgctl/c172p/build", 
    	method = RequestMethod.POST, 
    	consumes = MediaType.APPLICATION_JSON_VALUE,
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> build(@RequestBody String jsonConfigStr) {
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	    	
    	JSONObject responseJSON = new JSONObject();
    	
    	String message = "None";
    	
    	C172PManager c172pConfig = null;
    	try {
	    	//Properties simProperties = new Properties();
	    	
	    	//TODO: safety check config input string
    		//length, 
	    	
    		LOGGER.info("RECEIVED: {}" + jsonConfigStr);
    		
			c172pConfig = new C172PManager(jsonConfigStr);
	    	
	    	responseJSON.append("message", message);
	    	
	    	String name = c172pConfig.getAircraftName();
	    	
	    	LOGGER.info("Building c172p aircraft {}", name);
	    	
	    	LOGGER.info("Using config:\n{}", c172pConfig.toString());
	    	
	    	c172pService.buildC172P(c172pConfig);
	    	
	    	responseCode = HttpStatus.OK;
	    	message = "C172P built";
	    	responseJSON.append("aircraftName", name);
		} catch (FlightGearSetupException e) {
			LOGGER.error("FlightGearSetupException building C172P", e);
		}
    	
    	responseJSON.put("response", responseCode);
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    */
    
    @GetMapping("/fgctl/c172p/build")
    @RequestMapping(
    	value = "/fgctl/c172p/build", 
    	method = RequestMethod.GET, 
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> build(@RequestParam("name") String name) {   	
    	
    	String message = "None";
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	JSONObject responseJSON = new JSONObject();
    	
    	if(name != null && !StringUtils.isEmpty(name)) {
    	
	    	try {
				c172pService.buildC172P(name);
				message = "C172P built with name: " + name;
				responseCode = HttpStatus.OK;
			} catch (FlightGearSetupException e) {
				LOGGER.error("FlightGearSetupException building C172P", e);
			}
    	}
    	else {
    		message = "Cannot build C172P with null or empty name";
    		LOGGER.error(message);
    	}
    	
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    @GetMapping("/fgctl/c172p/runFlightPlan")
    @RequestMapping(
    	value = "/fgctl/c172p/runFlightPlan", 
    	method = RequestMethod.GET, 
    	produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> runFlightPlan(@RequestParam("name") String name) {   	
    	
    	String message = "None";
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	JSONObject responseJSON = new JSONObject();
    	
    	//TODO: get result
    	c172pService.runRunwayPlan(name);
		
		responseCode = HttpStatus.OK;
    	
    	responseJSON.put("message", message);
    	
		return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
    
    @GetMapping("/fgctl/c172p/whoami")
    @RequestMapping(value = "/fgctl/c172p/whoami", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> whoami() {   	
    	String retval = "{\"message\": \"I am a C172PController\"}";
    	
		return new ResponseEntity<String>(retval, HttpStatus.OK);
    }
	
    @GetMapping("/fgctl/c172p/telemetry")
    @RequestMapping(value = "/fgctl/c172p/telemetry", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTelemetry(@RequestParam("name") String name) {
    	
    	LOGGER.info("Getting telemetry for c172p aircraft {}", name);
    	
    	//get telemetry from our aircraft
    	
    	String message = "";
    	String telemJSON = null;
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		//TODO: telemetry : { [actual telem] }
    		telemJSON = new JSONObject(c172pService.getTelemetry(name)).toString();
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
    
    @GetMapping("/fgctl/c172p/setCarbIce")
    @RequestMapping(value = "/fgctl/c172p/setCarbIce", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setCarbIce(@RequestParam("name") String name, @RequestParam("iceAmt") double iceAmt) {
    	
    	LOGGER.info("Getting telemetry for c172p aircraft {}", name);
    	
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	String responseBody = "{\"message\": \"None\"}";
    	try {
    		c172pService.setCarbIce(name, iceAmt);
    		responseCode = HttpStatus.OK;
    		
    	}
    	catch (Exception e) {
    		responseBody = "{\"message\": \"" + e.getLocalizedMessage() + "\"}";
    	}
    	
    	return new ResponseEntity<String>(responseBody, responseCode);
    }
    
    @GetMapping("/fgctl/c172p/resetSimulator")
    @RequestMapping(value = "/fgctl/c172p/resetSimulator", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetSimulator() {  
    	
    	JSONObject responseJSON = new JSONObject();
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	
    	
    	return new ResponseEntity<String>(responseJSON.toString(), responseCode);
    }
}
