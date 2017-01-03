package org.iipg.hurricane.hdfs.jmx;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.iipg.hurricane.jmx.client.JMXQueryParameter;
import org.iipg.hurricane.jmx.client.JMXRequest;
import org.iipg.hurricane.jmx.client.JMXRequestHandler;
import org.iipg.hurricane.jmx.client.JMXTargetConfig;
import org.json.simple.JSONObject;

public class HadoopJMXRequestHandler extends JMXRequestHandler {

	public HadoopJMXRequestHandler(String pServerUrl,
			JMXTargetConfig pTargetConfig) {
		super(pServerUrl, pTargetConfig);
		// TODO Auto-generated constructor stub
	}

    /**
     * Get the HttpRequest for executing the given single request
     *
     * @param pRequest request to convert
     * @param pPreferredMethod HTTP method preferred
     * @param pProcessingOptions optional map of processiong options
     * @return the request used with HttpClient to obtain the result.
     */
	@Override
	public HttpUriRequest getHttpRequest(JMXRequest pRequest, String pPreferredMethod,
			Map<JMXQueryParameter, String> pProcessingOptions) throws UnsupportedEncodingException, URISyntaxException {
		String method = pPreferredMethod;
		if (method == null) {
			method = pRequest.getPreferredHttpMethod();
		}
		if (method == null) {
			method = doUseProxy(pRequest) ? HttpPost.METHOD_NAME : HttpGet.METHOD_NAME;
		}

		// GET request
		if (method.equals(HttpGet.METHOD_NAME)) {
			if (doUseProxy(pRequest)) {
				throw new IllegalArgumentException("Proxy mode can only be used with POST requests");
			}
			List<String> parts = pRequest.getRequestParts();
			// If parts == null the request decides, that POST *must* be used
			if (parts != null) {
				String queryParams = "qry=" + escape(parts.get(0));
				return new HttpGet(createRequestURI(getServerUrl().getPath(), queryParams));
			}
		}

		// We are using a post method as fallback
		JSONObject requestContent = getJsonRequestContent(pRequest);
		String queryParams = prepareQueryParameters(pProcessingOptions);
		HttpPost postReq = new HttpPost(createRequestURI(getServerUrl().getPath(),queryParams));
		postReq.setEntity(new StringEntity(requestContent.toJSONString(),"utf-8"));
		return postReq;
	}

}
