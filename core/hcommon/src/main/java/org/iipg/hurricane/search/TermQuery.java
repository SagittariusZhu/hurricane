package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public class TermQuery extends Query {
	
	public static final String TYPE = "TermQuery";
	
	private Term term = null;
	
	public TermQuery(Term term) {
		this.term = term;
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
