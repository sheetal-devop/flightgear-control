package org.jason.fgcontrol.connection.rest;

import java.net.HttpURLConnection;
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
	private final static String CONNECTION_TIMEOUT_PARAM = "http.connection.timeout";
	private final static String SO_TIMEOUT_PARAM = "http.socket.timeout";
	
	//timeouts are longs in many places, io.restassured uses ints
	private final static int DEFAULT_CONNECTION_TIMEOUT = 1000;
	private final static int DEFAULT_SO_TIMEOUT = 1000;
	
	private RestAssuredConfig config;

	private LinkedHashMap<String, String> headers;

	/**
	 * Build a client to make REST GETs and default timeouts
	 * 
	 * @param headers
	 */
	public RESTClient() {
		this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT, null);
	}
	
	public RESTClient(int connectionTimeout, int socketTimeout) {
		this(connectionTimeout, socketTimeout,  null);
	}
	
	public RESTClient(LinkedHashMap<String, String> headers) {
		this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SO_TIMEOUT, headers);
	}
	
	/**
	 * Build a client to make REST GETs with defined headers
	 * 
	 * @param connectionTimeout
	 * @param socketTimeout
	 */
	public RESTClient(int connectionTimeout, int socketTimeout, LinkedHashMap<String, String> headers) {
		
		config = RestAssured.config()
//				.connectionConfig(
//					//complains of chunking errors - for sending images its likely multipart messages
//					ConnectionConfig.connectionConfig().closeIdleConnectionsAfterEachResponse()
//				)
				.httpClient(
			       	HttpClientConfig.httpClientConfig()
			       		//no specification of reuseHttpClientInstance seems to work
			       		//.reuseHttpClientInstance()	//complains of unreleased connections
			       		//.dontReuseHttpClientInstance()	//complains of chunking errors - for sending images its likely multipart messages
			       		.setParam(CONNECTION_TIMEOUT_PARAM, connectionTimeout)
			       		.setParam(SO_TIMEOUT_PARAM, socketTimeout)
			    );
	
		this.headers = headers;
	}
	
	private RequestSpecification buildRequest() {
		//TODO: find a way to store the RequestSpecification and make further requests with parameters.
		//parameters endlessly tacked on to the end, eventually causing failures, unless the RequestSpecification 
		//is rebuilt:
		//GET /screenshot?accept=image%2Fjpg?accept=image%2Fjpg?accept=image%2Fjpg?accept=image%2Fjpg
		if(headers != null && !headers.isEmpty()) {
			//use of headerconfig doesn't seem to work well with the null switch
			return RestAssured.given().config(config).headers(headers);
		} else {
			return RestAssured.given().config(config);
		}
	}
	
	private boolean hasValidParams(LinkedHashMap<String, String> params) {
		return params != null && params.size() > 0;
	}
	
	///////////////////
	public boolean makeGETRequestAndGetStatusCodeWasSuccessful(String uri) {
		return makeGETRequestAndGetStatusCodeWasSuccessful(uri);
	}
	
	public boolean makeGETRequestAndGetStatusCodeWasSuccessful(String uri, LinkedHashMap<String, String> params) {
		return makeGETRequestAndGetStatusCode(uri, params) == HttpURLConnection.HTTP_OK;
	}
	
	///////////////////
	public int makeGETRequestAndGetStatusCode(String uri, LinkedHashMap<String, String> params) {
		if(hasValidParams(params)) {
			return makeGETRequest(uri, params).getStatusCode();
		} else {
			return makeGETRequest(uri).getStatusCode();
		}
	}
	
	public int makeGETRequestAndGetStatusCode(String uri) {
		return makeGETRequest(uri).getStatusCode();
	}
	
	///////////////////
	public Response makeGETRequest(String uri) {
		return makeGETRequest(uri, new LinkedHashMap<String, String>());
	}
	
	public Response makeGETRequest(String uri, LinkedHashMap<String, String> params) {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Making request to uri: {}, with params {}", uri, params);
		}
		
		Response response;
		//synchronous request
		if(hasValidParams(params)) {
			response = buildRequest().baseUri(uri).params(params).get();
		} else {
			response = buildRequest().baseUri(uri).get();
		}
		
		return response;
	}
	
	///////////////////
	public byte[] makeGETRequestAndGetBody(String uri) {
		return makeGETRequestAndGetBody(uri, new LinkedHashMap<String, String>());
	}

	public byte[] makeGETRequestAndGetBody(String uri, LinkedHashMap<String, String> params) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Making request to uri: {}", uri);
		}
		
		//synchronous request	
		if(params == null || params.size() == 0) {
			return makeGETRequest(uri).getBody().asByteArray();
		}
		else {
			return makeGETRequest(uri, params).getBody().asByteArray();
		}
	}
}
