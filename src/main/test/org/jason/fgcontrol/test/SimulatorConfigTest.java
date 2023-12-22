package org.jason.fgcontrol.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimulatorConfigTest {

	private final String testPropertiesFile1 = "src/main/test/resources/simulatorConfigs/c172p_alpha_flight_van_tour.properties";
	//private final String testPropertiesFile2 = "src/main/test/resources/simulatorConfigs/c172p_alpha_runway.properties";
	
    @Test
    public void testDefault() {
    	
    	SimulatorConfig config = new SimulatorConfig();
    	
    	Assert.assertEquals(config.getTelemetryOutputHost(), SimulatorConfig.DEFAULT_SOCKETS_TELEM_HOST);
    	Assert.assertEquals(config.getCameraViewerHost(), SimulatorConfig.DEFAULT_CAMERA_VIEW_HOST);
    	Assert.assertEquals(config.getTelnetHost(), SimulatorConfig.DEFAULT_TELNET_HOST);
    	Assert.assertEquals(config.getControlInputHost(), SimulatorConfig.DEFAULT_SOCKETS_INPUT_HOST);
    
    	Assert.assertEquals(config.getSshdPort(), SimulatorConfig.DEFAULT_SSHD_PORT);
    	
    	
    	Assert.assertNull(config.getFlightPlanName());
    	
    	
    }
    
    @Test
    public void testLoadFromValidProperties() {
		Properties simProperties = new Properties();
		

		
		try {
			simProperties.load(new FileInputStream(testPropertiesFile1));
		} catch (FileNotFoundException e) {
			Assert.fail("FileNotFoundException loading properties file", e);
		} catch (IOException e) {
			Assert.fail("IOException loading properties file", e);
		}
		
		SimulatorConfig simConfig = new SimulatorConfig(simProperties);		
		
		Assert.assertEquals(simConfig.getTelemetryOutputPort(), 5000);
	
    }
    
    @Test
    public void testLoadInvalidProperty() {
    	
    	Properties simProperties = new Properties();
    	
		String badConfigDirective = "BadConfigDirective";
		
		try {
			simProperties.load(new FileInputStream(testPropertiesFile1));
		} catch (FileNotFoundException e) {
			Assert.fail("FileNotFoundException loading properties file", e);
		} catch (IOException e) {
			Assert.fail("IOException loading properties file", e);
		}
		
		simProperties.setProperty(badConfigDirective, "bad value");
		
		Assert.assertTrue(simProperties.containsKey(badConfigDirective));
		
		SimulatorConfig simConfig = new SimulatorConfig(simProperties);
		
		//bad value doesn't appear in json
		Assert.assertFalse( new JSONObject(simConfig.toJSON()).has(badConfigDirective));
    }
    
    @Test 
    public void testLoadFromJSON() {
		Properties simProperties = new Properties();
		
		try {
			simProperties.load(new FileInputStream(testPropertiesFile1));
		} catch (FileNotFoundException e) {
			Assert.fail("FileNotFoundException loading properties file", e);
		} catch (IOException e) {
			Assert.fail("IOException loading properties file", e);
		}
		
		SimulatorConfig simConfig1 = new SimulatorConfig(simProperties);	
		
		String configJson1 = simConfig1.toJSON();
		
		Assert.assertNotNull(configJson1);
		Assert.assertFalse(StringUtils.isEmpty(configJson1));
		
		SimulatorConfig simConfig2 = new SimulatorConfig(configJson1);
		
		Assert.assertEquals(simConfig2.getTelemetryOutputPort(), 5000);
    }
    
    @Test 
    public void testLoadFromJSONWithInvalidDirective() {
		Properties simProperties = new Properties();
		
		String badConfigDirective = "BadConfigDirective";
		
		try {
			simProperties.load(new FileInputStream(testPropertiesFile1));
		} catch (FileNotFoundException e) {
			Assert.fail("FileNotFoundException loading properties file", e);
		} catch (IOException e) {
			Assert.fail("IOException loading properties file", e);
		}
		
		SimulatorConfig simConfig1 = new SimulatorConfig(simProperties);	
		
		JSONObject configJson = new JSONObject(simConfig1.toJSON());
		
		configJson.append(badConfigDirective, "badValue");
		
		SimulatorConfig simConfig2 = new SimulatorConfig(configJson.toString());
		
		//good value appears in config
		Assert.assertEquals(simConfig2.getTelemetryOutputPort(), 5000);
		
		//bad value doesn't appear in json
		Assert.assertFalse( new JSONObject(simConfig2.toJSON()).has(badConfigDirective));
    }
}
