package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public class WildcardQuery extends Query {

	public static final String TYPE = "WildcardQuery";
	
	private Term term = null;
	
	public WildcardQuery(Term t) {
		this.term = t;
	}

	public Term getTerm() { return this.term; }
	
	@Override
	public JSONObject getJSONObject() throws JSONException {
		JSONObject prop = new JSONObject();
		prop.put("type", TYPE);
		prop.put("name", term.getField());
		prop.put("value", term.getText());
		return prop;
	}

}
