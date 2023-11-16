package org.jason.fgcontrol.view.mjpeg;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/**
 * A basic handler for viewing the camera stream.
 * 
 * @author jason
 *
 */
public class CameraViewHandler implements HttpHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(CameraViewHandler.class);

	//even though the img src is just the web endpoint, it seems to use the correct port that the server is listening on 
	private final static byte[] HTTP_BODY = 
		("<!DOCTYPE html><html><body><img src=\"" + 
		MJPEGStreamer.STREAM_HTTP_ENDPOINT + 
		"\"></body></html>").getBytes();
	
	private final static int HTTP_BODY_LEN = HTTP_BODY.length;

	//TODO: needed?
//	private final static byte[] HTTP_BODY_STREAM_IN_USE = 
//		"<!DOCTYPE html><html><body><b>Stream is currently in use</b></body></html>".getBytes();
//	
//	private final static int HTTP_BODY_STREAM_IN_USE_LEN = HTTP_BODY_STREAM_IN_USE.length;
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
        
        OutputStream os = null;
        
        try {
        	if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Writing cameraView output stream");
        	}
        	
        	//if stream view is not currently in use
	        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, HTTP_BODY_LEN);
	        os = httpExchange.getResponseBody();
	        os.write(HTTP_BODY);
	        
	        
	        os.flush();
        }
        catch(Exception e) {
        	LOGGER.warn("Exception occurred writing cameraView body - Continuing.", e);
        }
        finally {
        	
        	LOGGER.info("Closing cameraView output stream");
        	
        	if(os != null) {
        		os.close();
        	}
        }
		
	}

}
