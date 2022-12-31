package org.jason.fgcontrol.view.mjpeg.app;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jason.fgcontrol.view.CameraViewer;
import org.jason.fgcontrol.view.mjpeg.MJPEGStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MJPEGStreamerApp {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(MJPEGStreamerApp.class);

	
	public static void main(String[] args) throws URISyntaxException, IOException {
		
		//httpd host and port set for the simulator
		String simHost = "localhost";
		
		//f15c beta - port the simulator has open
		int simPort = 5222;
		
		//streaming - port to use to view the stream managed by the app
		//http://localhost:9999/cameraView
		int streamerPort = 9999;
		
		CameraViewer cameraViewer = new CameraViewer(simHost, simPort);
		
		MJPEGStreamer mjpegStreamer = new MJPEGStreamer(cameraViewer, streamerPort);
		
		Thread streamerThread = new Thread() {
			@Override
			public void run() {
				
				LOGGER.info("Running mjpeg streamer thread");
				
				mjpegStreamer.start();
				
				LOGGER.info("Mjpeg streamer thread exiting");
			}
		};

		LOGGER.info("Mjpeg streamer thread starting");
		
		streamerThread.start();
		
		LOGGER.info("Mjpeg streamer thread started");
		
		int i = 0;
		while(i < 60) {
			try {
				LOGGER.info("Runtime sleep");
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
		
		LOGGER.info("Mjpeg streamer shutting down");
		
		mjpegStreamer.shutdown();

		LOGGER.info("Mjpeg streamer shut down complete");
	}
}
