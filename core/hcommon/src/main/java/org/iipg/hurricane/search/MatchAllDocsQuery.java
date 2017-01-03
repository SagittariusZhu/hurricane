package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public class MatchAllDocsQuery extends Query {
	
	public static final String TYPE = "MatchAllDocsQuery";
	
	@Override
	public JSONObject getJSONObject() throws JSONException {
		JSONObject prop = new JSONObject();
		prop.put("type", TYPE);
		return prop;
	}

}
