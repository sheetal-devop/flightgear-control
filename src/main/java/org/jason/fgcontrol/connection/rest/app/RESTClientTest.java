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

public class RESTClientTest {

	//for the flightgear simulator screenshot endpoint 
	
	public static void main(String[] args) {
		
		LinkedHashMap<String, String> clientHeaders = new LinkedHashMap<String, String>();
		clientHeaders.put(CommonHeaders.CONNECTION, CommonHeaders.CONNECTION_CLOSE);
		clientHeaders.put(CommonHeaders.HTTP_ACCEPT, CommonHeaders.CONTENT_TYPE_JPG);
		
		RESTClient restClient = new RESTClient(clientHeaders);

		String outputDir = "/home/jason/";
		
		//flightgear running on localhost with httpd enabled on port 5222
		//"http://localhost:5222/screenshot?type=jpg"
		String simStreamURI = URI.create("http://localhost:5222/screenshot").toString();
		
		//just print out the status code from the get request

		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("type", "jpg");
		
		System.out.println("REST get stream uri statuscode: " + restClient.makeGETRequestAndGetStatusCode(simStreamURI, params));

		
		//dump the image data from the response to disk
		System.out.println("GET request for simulator view");
		byte[] body = restClient.makeGETRequestAndGetBody(simStreamURI, params);
			
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(body));
			
			System.out.println("Writing to disk");
			ImageIO.write(img, "jpg", new File(outputDir + "/oneRead.jpg"));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done");
	}

}
