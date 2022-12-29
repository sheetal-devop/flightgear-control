package org.jason.fgcontrol.connection.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Basic REST client wrapper.
 * 
 * @author jason
 *
 */
public class RESTClient {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(RESTClient.class);

	//borrowed from CoreConnectionPNames
	private final static String CONNECTION_TIMEOUT = "http.connection.timeout";
	private final static String SO_TIMEOUT = "http.socket.timeout";
	
	//timeouts are longs in many places, io.restassured uses ints
	private final static int DEFAULT_CONNECTION_TIMEOUT = 1000;
	private final static int DEFAULT_SO_TIMEOUT = 1000;
	
	private RequestSpecification request;
	
	/**
	 * Build a client to make REST GETs with no headers and default timeouts
	 */
	public RESTClient() {
		this(new LinkedHashMap<String, String>(), DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT);
	}
	
	/**
	 * Build a client to make REST GETs with defined headers and default timeouts
	 * 
	 * @param headers
	 */
	public RESTClient(LinkedHashMap<String, String> headers) {
		this(headers, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT);
	}
	
	/**
	 * Build a client to make REST GETs with defined headers
	 * 
	 * @param headers
	 */
	public RESTClient(LinkedHashMap<String, String> headers, int connectionTimeout, int socketTimeout) {
		RestAssuredConfig config = RestAssured.config()
		        .httpClient(HttpClientConfig.httpClientConfig()
		                .setParam(CONNECTION_TIMEOUT, connectionTimeout)
		                .setParam(SO_TIMEOUT, socketTimeout));
		
		//TODO: allow list of header names
		
		if(headers != null && headers.size() > 0) {
			request = RestAssured.given().config(config).headers(headers);
		}
		else {
			request = RestAssured.given().config(config);
		}
	}
	
	public boolean makeGETRequest(URI uri) {
		
		boolean retval = false;
	
		try {
			retval = (makeGETRequestURIAndGetStatusCode(uri) == HttpURLConnection.HTTP_OK);
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Exception occurred making REST request", e);
		}
		
		return retval;
	}
	
	public int makeGETRequestURIAndGetStatusCode(URI uri) throws IOException, InterruptedException {
	
		return makeGETRequestURI(uri).getStatusCode();
	}
	
	public Response makeGETRequestURI(URI uri) throws IOException, InterruptedException {
		
		//synchronous request
		return request.get(uri);
	}
	
	public byte[] makeGETRequestURIAndGetBody(URI uri) throws IOException, InterruptedException {
		
		//synchronous request
		return request.get(uri).getBody().asByteArray();
	}
	
//	public Response makeGETRequestURI(URI uri, Response response) throws IOException, InterruptedException {
//		
//		//synchronous request
//		return RestAssured.given().config(config).get(uri);
//	}
}
