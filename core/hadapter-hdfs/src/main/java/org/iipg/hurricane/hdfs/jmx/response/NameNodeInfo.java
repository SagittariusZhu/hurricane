package org.iipg.hurricane.hdfs.jmx.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.hdfs.jmx.HadoopJMXResponse;
import org.iipg.hurricane.util.JSONUtil;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NameNodeInfo {

	private Map<String, Object> vals;

	public NameNodeInfo(HadoopJMXResponse resp) {
		vals = new HashMap<String, Object>();
		
		org.json.simple.JSONObject valueObj = resp.getValue();
		if (valueObj.containsKey("LiveNodes")) {
			JSONParser parser = new JSONParser();
			try {
				vals.put("liveNodes", parser.parse((String) valueObj.get("LiveNodes")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		vals.put("version", valueObj.get("SoftwareVersion"));
		vals.put("used", valueObj.get("Used"));
		vals.put("max", valueObj.get("Total"));		
	}

	public String getVersion() {
		return (String) vals.get("version");
	}

	public long getCapacity() {
		return (long) vals.get("max");
	}

	public long getUsed() {
		return (long) vals.get("used");
	}
	
	public List<String> getLiveNodes() {
		Map nodeProps = JSONUtil.toMap((String) vals.get("liveNodes"));
		List<String> nodes = new ArrayList<String>();
		return nodes;
	}
	
	public JSONObject asJSONObject() {
		return JSONUtil.toJSONObject(vals);
	}

}
