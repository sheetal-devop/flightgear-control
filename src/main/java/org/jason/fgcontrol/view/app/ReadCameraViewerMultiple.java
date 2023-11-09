package org.jason.fgcontrol.view.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jason.fgcontrol.view.CameraViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read several frames from the CameraViewer and write to disk
 * 
 * @author jason
 *
 */
public class ReadCameraViewerMultiple {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ReadCameraViewerMultiple.class);

	private static boolean QUIT_UPDATE_THREAD = false;
	
	public static void main(String[] args) {
		
		if(args.length != 3) {
			System.err.println("Usage: ReadCameraViewerMulti host port outputDir");
			System.exit(1);
		}
		
		int iterations = 5000;
		
		int resultSetSize = 50;
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String outputDir = args[2];
		
		try {
			CameraViewer cameraViewer = new CameraViewer(host, port);
			
			LOGGER.info("Starting update thread");

			//doesn't have to be threaded but flightgear-control will do this in a threaded manner  
			Thread t = new Thread() {
				@Override
				public void run() {
					int i =0;
					while ( i < iterations && !QUIT_UPDATE_THREAD) {
						cameraViewer.update();
						i++;
						
						try {
							Thread.sleep(2L);
						} catch (InterruptedException e) {
							LOGGER.error("Update thread sleep interrupted", e);
						}
					}
					
					LOGGER.info("Update thread exiting");
				}
			};
			t.start();
			
			//accumulate some feeds
			Thread.sleep(30 * 1000);
			
			LOGGER.info("Retrieving first result set");
			
			//query the first bunch
			List<byte[]> results1 = cameraViewer.readBuffer(resultSetSize);
			
			LOGGER.info("Retrieving second result set");
			
			
			Thread.sleep(30 * 1000);
			
			//query the second bunch
			List<byte[]> results2 = cameraViewer.readBuffer(resultSetSize);

			QUIT_UPDATE_THREAD = true;
			
			LOGGER.info("Writing results");
			
			//write the results to disk
			int counter = 0;
			for( byte[] screen : results1) {
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(screen));
			
				ImageIO.write(img, "jpg", new File(outputDir + "/reads_1_" + counter + ".jpg"));
			
				counter++;
			}
			
			counter = 0;
			for( byte[] screen : results2) {
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(screen));
			
				ImageIO.write(img, "jpg", new File(outputDir + "/reads_2_" + counter + ".jpg"));
			
				counter++;
			}

			LOGGER.info("Quitting update thread");
			
			
			
			LOGGER.info("Results written");
		} catch (URISyntaxException e) {
			LOGGER.error("URISyntaxException thrown", e);
		} catch (IOException e) {	
			LOGGER.error("IOException thrown", e);
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException thrown", e);

		}
	}
}
