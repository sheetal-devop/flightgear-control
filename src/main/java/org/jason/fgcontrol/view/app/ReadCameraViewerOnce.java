package org.jason.fgcontrol.view.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.jason.fgcontrol.view.CameraViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read a frame from the CameraViewer and write to disk
 * 
 * @author jason
 *
 */
public class ReadCameraViewerOnce {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ReadCameraViewerOnce.class);

	
	public static void main(String[] args) {
		
		if(args.length != 3) {
			System.err.println("Usage: ReadCameraViewerOnce host port outputDir");
			System.exit(1);
		}
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String outputDir = args[2];
		
		try {
			CameraViewer cameraViewer = new CameraViewer(host, port);
			
			byte[] result = cameraViewer.readCurrentView();
			
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));
			
			ImageIO.write(img, "jpg", new File(outputDir + "/oneRead.jpg"));
			
			LOGGER.info("Success");
			
		} catch (URISyntaxException e) {
			LOGGER.error("URISyntaxException thrown", e);
		} catch (IOException e) {	
			LOGGER.error("IOException thrown", e);
		}
	}
}
