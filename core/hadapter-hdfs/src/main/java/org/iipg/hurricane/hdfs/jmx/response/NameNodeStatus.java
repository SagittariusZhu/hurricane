package org.iipg.hurricane.hdfs.jmx.response;

import java.util.HashMap;
import java.util.Map;

import org.iipg.hurricane.hdfs.jmx.HadoopJMXResponse;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NameNodeStatus {
	private Map<String, Object> vals;

	public NameNodeStatus(HadoopJMXResponse resp) {
		vals = new HashMap<String, Object>();
		
		org.json.simple.JSONObject valueObj = resp.getValue();
		vals.put("state", valueObj.get("State"));
	}
	
	public String getStatus() {
		return (String) vals.get("state");
	}
}
