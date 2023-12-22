package org.jason.fgcontrol.aircraft.config.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.jason.fgcontrol.aircraft.config.SimulatorConfig;

/**
 * Util to read a properties file into a SimulatorConfig, then output its JSON form.
 * 
 */
public class PropertiesToJSON {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		//need a file
		if(args.length == 1) {
					
			Properties p = new Properties();
			
			p.load( new FileInputStream(args[0]) );
			
			System.out.println(new SimulatorConfig(p).toJSON());
			
		} else {
			System.out.println("Usage: java PropertiesToJSON propertiesFile.properties");
			System.exit(1);
		}

	}

}
