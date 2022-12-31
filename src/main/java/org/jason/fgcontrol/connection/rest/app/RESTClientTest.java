package org.jason.fgcontrol.connection.rest.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.jason.fgcontrol.connection.rest.RESTClient;

import io.restassured.response.Response;

public class RESTClientTest {

	public static void main(String[] args) {
		RESTClient restClient = new RESTClient();

		String outputDir = "/home/jason/";
		
		//flightgear running on localhost with httpd enabled
		String simStreamURI = URI.create("http://localhost:5222/screenshot?type=jpg").toString();
		
		//just print out the status code from the get request
		try {
			System.out.println("REST get stream uri statuscode: " + restClient.makeGETRequestAndGetStatusCode(simStreamURI));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		//dump the image data from the response to disk
		try {
			System.out.println("GET request for simulator view");
			Response result = restClient.makeGETRequest(simStreamURI);
			
			
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(result.getBody().asByteArray()));

			System.out.println("Writing to disk");
			ImageIO.write(img, "jpg", new File(outputDir + "/oneRead.jpg"));
			System.out.println("Done");
		
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
