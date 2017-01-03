package org.iipg.hurricane.hdfs.jmx;

import org.iipg.hurricane.jmx.client.JMXRemoteException;
import org.iipg.hurricane.jmx.client.JMXRequest;
import org.iipg.hurricane.jmx.client.JMXResponse;
import org.iipg.hurricane.jmx.client.JMXResponseExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HadoopJMXResponseExtractor implements JMXResponseExtractor {

	@Override
	public <RESP extends JMXResponse<REQ>, REQ extends JMXRequest> RESP extract(
			REQ request, JSONObject jsonResp) throws JMXRemoteException {
		JSONObject extractObj = new JSONObject();
		
		JSONObject beanObj = (JSONObject) ((JSONArray) jsonResp.get("beans")).get(0);
		extractObj.put("value", beanObj);
		
		return request.createResponse(extractObj);
	}

}
