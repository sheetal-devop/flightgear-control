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
@SuppressWarnings("restriction")
public class CameraViewHandler implements HttpHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(CameraViewHandler.class);

	
	private final static byte[] HTTP_BODY = "<!DOCTYPE html><html><body><img src=\"./stream\"></body></html>".getBytes();
	private final static int HTTP_BODY_LEN = HTTP_BODY.length;
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
        
        OutputStream os = null;
        
        try {
        	if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Writing cameraView output stream");
        	}
        	
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
