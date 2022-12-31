package org.jason.fgcontrol.connection.rest.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import org.jason.fgcontrol.connection.rest.RESTClient;

public class RESTClientPollingTest {

	private final static String HTTP_ACCEPT_FIELD = "accept";
	private final static String CAMERA_VIEW_CONTENT_TYPE = "image/jpg";
	
	public static void main(String[] args) {
		
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put(HTTP_ACCEPT_FIELD, CAMERA_VIEW_CONTENT_TYPE);
		
		
		RESTClient restClient = new RESTClient(headers);

		String outputDir = "/home/jason/";
		
		//flightgear running on localhost with httpd enabled on port 5222
		String simStreamURI = URI.create("http://localhost:5222/screenshot?type=jpg").toString();
		
		//dump the image data from the response to disk
		try {
			int max = 20;
			for( int i =0; i < max; i++) {
			
				System.out.println("GET request for simulator view");
				
				byte[] result = restClient.makeGETRequestAndGetBody(simStreamURI);
				
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
