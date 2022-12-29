package org.jason.fgcontrol.view.mjpeg;


import java.io.IOException;
import java.net.InetSocketAddress;

import org.jason.fgcontrol.view.CameraViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MJPEGStreamer implements Runnable {

	private final static Logger LOGGER = LoggerFactory.getLogger(MJPEGStreamer.class);
	private int serverPort;
	
	private final static int HTTP_SERVER_BACKLOG = 0;
	
	private final static long RUN_SLEEP = 2 * 1000L;
	
	//TODO: proper singleton implementation
	//stream can only be viewed by a single viewer
	//closing a browser tab should allow a new browser tab to view the stream
	private StreamHandler streamHandler;
	
	
	private CameraViewHandler cameraViewHandler;
	
	private HttpServer server;
	
	
	private final static int SHUTDOWN_TIMEOUT = 3;	//in seconds
	
	public MJPEGStreamer(CameraViewer cameraViewer, int serverPort) throws IOException {

		this.serverPort = serverPort;
		
		streamHandler = new StreamHandler(cameraViewer);
		cameraViewHandler = new CameraViewHandler();
		
		setup();
		
		LOGGER.info("Initialized mjpegstreamer listening on port {}", serverPort);
	}
	
	protected void setup() throws IOException {
		
		
		server = HttpServer.create(new InetSocketAddress(serverPort), HTTP_SERVER_BACKLOG);
		
		//web endpoint that shows the camera stream
		server.createContext("/cameraView", cameraViewHandler);
		
		//the camera stream itself
		server.createContext("/stream", streamHandler);
		
		//TODO: figure out why vvvv
		//using newCachedThreadPool causes a 1 min wait before shutdown
		//server.setExecutor(Executors.newCachedThreadPool());
		server.setExecutor(null);
		
		//stalls on shutdown indefinitely
		//server.setExecutor(Executors.newFixedThreadPool(1));
		
		//stalls on shutdown indefinitely
		//server.setExecutor(Executors.newSingleThreadExecutor());
	}
	
	@Override
	public void run() {

		LOGGER.debug("MJPEGStreamer run() invoked");
		
		LOGGER.info("MJPEGStreamer server starting");
		
		server.start();
		
		LOGGER.info("MJPEGStreamer server started");
		
		while(streamHandler.isRunning()) {
			try {
				LOGGER.info("MJPEGStreamer server running. Sleeping");
				Thread.sleep(RUN_SLEEP);
			} catch (InterruptedException e) {
				LOGGER.warn("Sleep interrupted", e);
			}
		}
		
		LOGGER.debug("MJPEGStreamer run() exiting");
	}

	public void shutdown() {
		
		//streamhandler can be shutdown internally or externally
		if(streamHandler.isRunning()) {
			streamHandler.shutdown();
		}
		
		LOGGER.info("MJPEGStreamer server stopping");
		
		server.removeContext("/cameraView");
		server.removeContext("/stream");
		
		LOGGER.info("MJPEGStreamer contexts removed");
		
		if(server != null) {
			//timeout in seconds
			server.stop(SHUTDOWN_TIMEOUT);
		}
		
		LOGGER.info("MJPEGStreamer server stopped");
	}
}
