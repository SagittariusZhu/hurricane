package org.iipg.hurricane.jmx.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pRequest;
import org.jolokia.client.request.J4pResponse;
import org.jolokia.client.request.J4pResponseExtractor;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class JMXClient extends JMXClientBuilderFactory {
	
	private String serverURL;
	
    // Http client used for connecting the j4p Agent
    private HttpClient httpClient;

    // Extractor used for creating J4pResponses
    private JMXResponseExtractor responseExtractor;
    
	public JMXClient(String serverURL) {
		this (serverURL, null);		
	}
	
    /**
     * Constructor for a given agent URl and a given HttpClient
     *
     * @param pJ4pServerUrl the agent URL for how to contact the server.
     * @param pHttpClient HTTP client to use for the connecting to the agent
     */
    public JMXClient(String serverURL, HttpClient pHttpClient) {
        this(serverURL,pHttpClient,null);
    }

    /**
     * Constructor using a given Agent URL, HttpClient and a proxy target config. If the HttpClient is null,
     * a default client is used. If no target config is given, a plain request is performed
     *
     * @param pJ4pServerUrl the agent URL for how to contact the server.
     * @param pHttpClient HTTP client to use for the connecting to the agent
     * @param pTargetConfig optional target
     */
    public JMXClient(String serverURL, HttpClient pHttpClient,JMXTargetConfig pTargetConfig) {
        this(serverURL,pHttpClient,pTargetConfig, ValidatingResponseExtractor.DEFAULT);
    }


    /**
     * Constructor using a given Agent URL, HttpClient and a proxy target config. If the HttpClient is null,
     * a default client is used. If no target config is given, a plain request is performed
     *
     * @param pJ4pServerUrl the agent URL for how to contact the server.
     * @param pHttpClient HTTP client to use for the connecting to the agent
     * @param pTargetConfig optional target
     * @param pExtractor response extractor to use
     */
    public JMXClient(String pServerURL, HttpClient pHttpClient,JMXTargetConfig pTargetConfig,JMXResponseExtractor pExtractor) {
    	serverURL = pServerURL;
        responseExtractor = pExtractor;
        // Using the default as defined in the client builder
        if (pHttpClient != null) {
            httpClient = pHttpClient;
        } else {
            JMXClientBuilder builder = new JMXClientBuilder();
            httpClient = builder.createHttpClient();
        }
    }

    /**
     * Execute a single J4pRequest returning a single response.
     * The HTTP Method used is determined automatically.
     *
     * @param pRequest request to execute
     * @param <RESP> response type
     * @param <REQ> request type
     * @return the response as returned by the server
     */
    public <RESP extends JMXResponse<REQ>, REQ extends JMXRequest> RESP execute(REQ pRequest)
            throws JMXException {
        // type spec is required to keep OpenJDK 1.6 happy (other JVM dont have a problem
        // with infering the type is missing here)
    	JMXResponseExtractor pExtractor = pRequest.getResponseExtractor();
        return this.execute(pRequest, pExtractor);
    }
    
    /**
     * Execute a single J4pRequest returning a single response.
     * The HTTP Method used is determined automatically.
     *
     * @param pRequest request to execute
     * @param <RESP> response type
     * @param <REQ> request type
     * @return the response as returned by the server
     */
    public <RESP extends JMXResponse<REQ>, REQ extends JMXRequest> RESP execute(REQ pRequest, JMXResponseExtractor pExtractor)
            throws JMXException {
        try {
        	JMXRequestHandler requestHandler = pRequest.getRequestHandler(serverURL);
            HttpResponse response = httpClient.execute(requestHandler.getHttpRequest(pRequest, null, null));
            JSONAware jsonResponse = extractJsonResponse(pRequest,response);
            if (! (jsonResponse instanceof JSONObject)) {
                throw new JMXException("Invalid JSON answer for a single request (expected a map but got a " + jsonResponse.getClass() + ")");
            }
            return pExtractor.extract(pRequest, (JSONObject) jsonResponse);
        }
        catch (IOException e) {
            throw new JMXException("IOException", e);
        } catch (URISyntaxException e) {
            throw new JMXException("URISyntaxException", e);
        }
    }

    private <REQ extends JMXRequest> JSONAware extractJsonResponse(REQ pRequest, HttpResponse pResponse) throws JMXException {
        try {
        	JMXRequestHandler requestHandler = pRequest.getRequestHandler(serverURL);
            return requestHandler.extractJsonResponse(pResponse);
        } catch (IOException e) {
            throw new JMXException("IO-Error while reading the response: " + e,e);
        } catch (ParseException e) {
            // It's a parse exception. Now, check whether the HTTResponse is
            // an error and prepare the proper J4pException
            StatusLine statusLine = pResponse.getStatusLine();
            if (HttpStatus.SC_OK != statusLine.getStatusCode()) {
                throw new JMXRemoteException(pRequest,statusLine.getReasonPhrase(), null, statusLine.getStatusCode(),null, null);
            }
            throw new JMXException("Could not parse answer: " + e,e);
        }
    }
}
