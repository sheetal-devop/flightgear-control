package org.jason.fgcontrol.view;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;

import org.jason.fgcontrol.connection.rest.RESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;

/**
 * Manage screenshot retrieval from the simulator.
 * 
 * Supports direct retrieval from the simulator screen endpoint for most use cases, and buffering many screengrabs at once.
 * 
 * @author jason
 *
 */
public class CameraViewer {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CameraViewer.class);
	
    private final static int CAM_VIEW_DEFAULT_PORT = 9999;
    
    //http only
    private final static String CAM_VIEW_PROTO = "http";
    
    private final static String CAM_VIEW_DEFAULT_HOST = "localhost";
	private final static String CAM_VIEW_RESOURCE = "screenshot?type=jpg";
	
	private final static String HTTP_ACCEPT_FIELD = "accept";
	private final static String CAMERA_VIEW_CONTENT_TYPE = "image/jpg";
	
	//buffer size of screen captures
	//at 30fps a buffer of 1024 is 34.13 seconds of mostly-continuous video, with the REST call round trip and overhead. 
	//not as good as just looking at the sim screen but should still be suitable for most applications 
	//at 800x600 simulator resolution this can be/become a sizeable memory impact 
	private final static int MAX_BUFFER_SIZE = 1024;
	
	private String host;
	private int port;
	
	private String cameraViewUri;

	private RESTClient restClient;

	private byte[] responseBody;

	private Queue<byte[]> cameraViewBuffer;

	//ints in ms
	//TODO: tune these
	private final static int CONNECTION_TIMEOUT = 500;
	private final static int SO_TIMEOUT = 500;
	
	
	public CameraViewer() throws URISyntaxException {
		this(CAM_VIEW_DEFAULT_HOST, CAM_VIEW_DEFAULT_PORT, CONNECTION_TIMEOUT, SO_TIMEOUT);
	}
	
	public CameraViewer(String host, int port) throws URISyntaxException {
		this(host, port, CONNECTION_TIMEOUT, SO_TIMEOUT);
	}
	
	public CameraViewer(String host, int port, int connectiontimeout, int socketTimeout) throws URISyntaxException {
		this.host = host;
		this.port = port;
		
		//uri never changes once its loaded from config
		cameraViewUri = URI.create(CAM_VIEW_PROTO + "://" + this.host + ":" + this.port + "/" +  CAM_VIEW_RESOURCE).toString();
		
		//autodiscarding queue with a fixed size
		cameraViewBuffer = EvictingQueue.create(MAX_BUFFER_SIZE);
		
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put(HTTP_ACCEPT_FIELD, CAMERA_VIEW_CONTENT_TYPE);
		
		restClient = new RESTClient(headers, connectiontimeout, socketTimeout);
		
		LOGGER.info("Initialized CameraViewer with url: {}", cameraViewUri);
	}

	/**
	 * Write the results of a camera view update to the local camera view buffer
	 */
	public synchronized void update() {
		
		try {
			cameraViewBuffer.add(readCurrentView());
		}
		catch (NullPointerException e) {
			//TODO: rethrow if we see enough of these in a row?
			LOGGER.error("NPE - Camera view update likely received a null response from the simulator REST endpoint. Continuing.", e);
		} 
	}
	
	/**
	 * Return a single camera view from the end of the queue.
	 * 
	 * @return A byte array of the camera view data
	 */
	public synchronized byte[] readBuffer() {
		return cameraViewBuffer.poll();
	}
	
	/**
	 * Read from the currently set simulator view with a REST request to the simulator screenshot endpoint.
	 * Intended to be used both internally and externally.
	 * 
	 * @return	A byte array of the camera view data
	 * 
	 */
	public synchronized byte[] readCurrentView() {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Retrieving current camera view for uri: {}", cameraViewUri);
		}
		
		try {
			//TODO: look at hard limiting the size of the returned byte array. 
			//what if it's comically large? maybe this can be enforced when building the client?
			
			responseBody = null;
			responseBody = restClient.makeGETRequestAndGetBody(cameraViewUri);
			
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Received {} bytes from camera view", responseBody.length);
			}
		}
		catch (IOException e) {
			LOGGER.error("IOException occurred updating Camera View. Continuing.", e);
		}
		catch (InterruptedException e) {
			
			//swallow exceptions to not impact more important simulator functionality
			//technically okay if camera view fails sporadically
		
			LOGGER.error("InterruptedException occurred updating Camera View. Likely while sending REST request. Continuing.", e);
		}
		catch (Exception e) {
			LOGGER.error("Exception occurred updating Camera View. Continuing.", e);
		}
		
		return responseBody;
	}
	
	/**
	 * Return a set amount of camera views from the end of the queue.
	 * 
	 * @param returnBufferSize The number of camera views to return from the buffer. 
	 *                         Bounded by the buffer size and the number of
	 *                         elements.
	 * 
	 * @return A list of byte arrays of the camera view data
	 */
	public synchronized List<byte[]> readBuffer(int returnBufferSize) {

		if (returnBufferSize > MAX_BUFFER_SIZE) {
			LOGGER.warn("Adjusting readBuffer request to max size");
			returnBufferSize = MAX_BUFFER_SIZE;
		} else {
			int queueSize = cameraViewBuffer.size();
			if (returnBufferSize > queueSize) {
				LOGGER.warn("Adjusting readBuffer request to queueSize");
				returnBufferSize = queueSize;
			}
		}

		// TODO: sloppy and slow but serviceable for now
		List<byte[]> results = new ArrayList<>(returnBufferSize);

		// retrieve latest 'size' byte arrays in buffer
		int addedCount = 0;
		while (!cameraViewBuffer.isEmpty() && addedCount <= returnBufferSize) {
			results.add(readBuffer());
			addedCount++;
		}

		// return list without any nulls
		// return
		// results.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());

		// return list. EvictingQueue does not permit nulls.
		return results;
	}
}
