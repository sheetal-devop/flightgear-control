package org.jason.fgcontrol.gateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApp {

	private final static Logger LOGGER = LoggerFactory.getLogger(GatewayApp.class);
	
    ///////////////////////////////
    
	@PostConstruct
	private void postConstruct() {
    	LOGGER.info("Constructing Gateway Application");
	}
	
    @PreDestroy
    private void onDestroy(){
    	LOGGER.info("Destroying Gateway Application");
    }

    //////////////////////////
    //main

	public static void main(String[] args) {
		LOGGER.info("Launching Gateway Application");

		SpringApplication.run(GatewayApp.class, args);
	}
}
