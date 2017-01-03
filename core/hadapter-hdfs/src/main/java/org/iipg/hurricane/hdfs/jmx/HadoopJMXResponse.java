package org.iipg.hurricane.hdfs.jmx;

import java.util.Map;

import org.iipg.hurricane.jmx.client.JMXResponse;
import org.json.simple.JSONObject;

public class HadoopJMXResponse extends JMXResponse<HadoopJMXRequest> {
	
	public HadoopJMXResponse(HadoopJMXRequest pRequest, JSONObject pJsonResponse) {
		super(pRequest, pJsonResponse);
	}

}
