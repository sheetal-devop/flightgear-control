package org.jason.fgcontrol.gateway.app.poc;

import java.util.HashMap;

import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloWorld {

	private final static Logger LOGGER = LoggerFactory.getLogger(HelloWorld.class);
	
	private static final HashMap<String, String> telem = new HashMap<> () {
		private static final long serialVersionUID = -6171034434162613119L;
		{
			put("hello", "world");
		}
	};
	
    @GetMapping("/helloworld/telem")
    @RequestMapping(value = "/helloworld/telem", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTelemetry() {
    	String telemJSON = null;
    	HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
    	
    	try {
    		telemJSON = new JSONObject(telem).toString();
    		responseCode = HttpStatus.OK;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
        	if(telemJSON == null) {
        		telemJSON = "{}";
        	}
    	}
    	
        return new ResponseEntity<String>(telemJSON, responseCode);
    }
    
    @GetMapping("/helloworld/hello")
    public String hello() {
        return "Hello World!";
    }
    
    /**
     * Shuts down the aircraft. Does not affect container.
     * 
     * @return
     */
    @GetMapping("/helloworld/shutdown") 
    @RequestMapping(value = "/helloworld/shutdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String shutdown()
    {    	
    	return "{\"Shutdown\": \"true\"}";
    }
    
    /**
     * 
     * 
     * @throws Exception
     */
    @PreDestroy
    public void onDestroy() throws Exception {
    	LOGGER.info("Destroying HelloWorld Container");
        
        shutdown();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(HelloWorld.class, args);
    }
}