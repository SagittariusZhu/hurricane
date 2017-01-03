package org.iipg.hurricane.service;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HCommand implements HCommandConstants {

	protected HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	
	protected String schemaName = "";
	
	protected JSONObject response = null;
	protected JSONObject header = ServletUtil.getResponseHeader(ResponseStatus.RS_OK);;
	protected boolean success = false;
	protected String raw = "";
	
	public HCommand(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getName() {
		return this.schemaName;
	}

	public JSONObject getOutputJSON() {	
		return response;
	}
	
	public JSONObject createOutputJSON() {
		response = new JSONObject();
		return response;
	}
	
	public void setHeader(String key, Object value) {
		header.put(key, value);
	}
	
	public JSONObject getResponseHeader() {
		if (success) {
			header.put(KEY_STATUS, ResponseStatus.RS_OK.status);
		} else {
			header.put(KEY_STATUS, ResponseStatus.RS_INTERNAL_ERROR.status);
			header.put(KEY_CONTENT_TYPE, "text/plain;charset=utf-8");
		}
		return header;
	}
	
	public String getRawOutput() {
		return this.raw;
	}
	
	public void setRawOutput(String output) {
		this.raw = output;
	}

	public abstract void run(HttpServletRequest req);
	
}
