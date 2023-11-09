package org.jason.fgcontrol.connection.rest.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import org.jason.fgcontrol.connection.rest.CommonHeaders;
import org.jason.fgcontrol.connection.rest.RESTClient;

public class RESTClientPollingTest {
	
	public static void main(String[] args) {
		
		LinkedHashMap<String, String> clientHeaders = new LinkedHashMap<String, String>();
		clientHeaders.put(CommonHeaders.CONNECTION, CommonHeaders.CONNECTION_CLOSE);
		clientHeaders.put(CommonHeaders.HTTP_ACCEPT, CommonHeaders.CONTENT_TYPE_JPG);
		
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("type", "jpg");
		
		RESTClient restClient = new RESTClient(clientHeaders);

		String outputDir = "/home/jason/";
		
		//flightgear running on localhost with httpd enabled on port 5222
		String simStreamURI = URI.create("http://localhost:5222/screenshot").toString();
		
		//dump the image data from the response to disk
		try {
			int max = 20;
			for( int i =0; i < max; i++) {
			
				System.out.println("GET request for simulator view");
				
				byte[] result = restClient.makeGETRequestAndGetBody(simStreamURI, params);
				
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));
	
				System.out.println("Writing to disk: " + i);
				ImageIO.write(img, "jpg", new File(outputDir + "/oneRead_" + i +".jpg"));
				System.out.println("Done");
				
				//probably don't need to sleep, the io should be enough of a delay
				Thread.sleep(200);
			}
		
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
