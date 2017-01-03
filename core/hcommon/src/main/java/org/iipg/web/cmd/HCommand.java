package org.iipg.web.cmd;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HCommand {
	
	protected String schemaName = "";
	
	protected String pureText = null;
	protected JSONObject response = null;
	protected JSONObject header = null;
	protected boolean success = false;
	
	public HCommand(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getName() {
		return this.schemaName;
	}

	public JSONObject getOutputJSON() {
		JSONObject retObj = new JSONObject();
		try {
			retObj.put("response", response);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return retObj;
	}
	
	public String getOutputText() {
		return this.pureText;
	}
	
	public void setOutputText(String text) {
		this.pureText = text;
	}
	
	public JSONObject getResponseHeader() {
		if (success)
			return ServletUtil.getResponseHeader(ResponseStatus.RS_OK);
		return header;
	}

	public abstract void run(HttpServletRequest req) throws IIPGCmdException;


	
}
