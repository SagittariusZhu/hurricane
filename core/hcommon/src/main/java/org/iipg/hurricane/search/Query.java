package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Query {

	public enum Operator { AND, OR }
	
	//public void setRewriteMethod(RewriteMethod multiTermRewriteMethod) {}

	public abstract JSONObject getJSONObject() throws JSONException;

	public String toString() {
		try {
			JSONObject ret = getJSONObject();
			return ret.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
