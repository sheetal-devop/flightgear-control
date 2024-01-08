package org.jason.fgcontrol.view.mjpeg;


import java.io.IOException;
import java.net.InetSocketAddress;

import org.jason.fgcontrol.view.CameraViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * MJPEG Streaming from an external REST endpoint- in our case, the FlightGear simulator's http server's /screenshot endpoint.
 * Enforce a single viewer. Discarded views can be re-opened.
 * 
 * @author jason
 *
 */
public class MJPEGStreamer {

	private final static Logger LOGGER = LoggerFactory.getLogger(MJPEGStreamer.class);
	private int serverPort;
	
	public final static String STREAM_HTTP_ENDPOINT = "/stream";
	public final static String CAMERA_VIEW_HTTP_ENDPOINT = "/cameraView";
	public final static String CAMERA_VIEW_HTTP_ENDPOINT_PROTO = "http";
	
	private final static int HTTP_SERVER_BACKLOG = 0;
	
	private StreamHandler streamHandler;
	private CameraViewHandler cameraViewHandler;
	
	private HttpServer server;
	
	private final static int SHUTDOWN_TIMEOUT = 3;	//in seconds
	
	/**
	 * Create an mjpeg streamer for our camera view.
	 * 
	 * @param cameraViewer
	 * @param serverPort
	 * @throws IOException
	 */
	public MJPEGStreamer(CameraViewer cameraViewer, int serverPort) throws IOException {

		this.serverPort = serverPort;
		
		streamHandler = new StreamHandler(cameraViewer);
		cameraViewHandler = new CameraViewHandler();
		
		setup();
		
		LOGGER.info("Initialized MJPEGStreamer listening on port {}", serverPort);
	}
	
	protected void setup() throws IOException {
		
		
		server = HttpServer.create(new InetSocketAddress(serverPort), HTTP_SERVER_BACKLOG);
		
		//web endpoint that shows the camera stream
		server.createContext(CAMERA_VIEW_HTTP_ENDPOINT, cameraViewHandler);
		
		//the camera stream itself
		server.createContext(STREAM_HTTP_ENDPOINT, streamHandler);
		
		//TODO: figure out why vvvv
		//using newCachedThreadPool causes a 1 min wait before shutdown 
		//  ==> likely from test program sleep time plus default timeouts
		//keep at null for the near future
		//server.setExecutor(Executors.newCachedThreadPool());
		server.setExecutor(null);
		
		//stalls on shutdown indefinitely
		//server.setExecutor(Executors.newFixedThreadPool(1));
		
		//stalls on shutdown indefinitely
		//server.setExecutor(Executors.newSingleThreadExecutor());
	}
	
	/**
	 * Start up the underlying server. Invoker needs to manage in thread.
	 */
	public void start() {
		
		LOGGER.debug("MJPEGStreamer server starting");
		
		server.start();
		
		LOGGER.debug("MJPEGStreamer server started");
	}

	public void shutdown() {
		
		//streamhandler can be shutdown internally or externally
		if(streamHandler.isRunning()) {
			streamHandler.shutdown();
		}
		
		LOGGER.info("MJPEGStreamer server stopping");
		
		//TODO: seeing java.lang.IllegalArgumentException occasionally on double shutdowns of FlightGearAircraft
		server.removeContext(CAMERA_VIEW_HTTP_ENDPOINT);
		server.removeContext(STREAM_HTTP_ENDPOINT);
		
		LOGGER.debug("MJPEGStreamer contexts removed");
		
		if(server != null) {
			//timeout in seconds
			server.stop(SHUTDOWN_TIMEOUT);
		}
		
		LOGGER.debug("MJPEGStreamer server stopped");
	}
}
