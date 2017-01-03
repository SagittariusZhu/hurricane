package org.iipg.web.cmd;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultCmd extends HCommand {

	private String cmdPara = null;
	
	public DefaultCmd(String cmdName, String cmdPara) {
		super(cmdName);
		this.cmdPara = cmdPara;
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		header = ServletUtil.getResponseHeader(ResponseStatus.RS_NOT_IMPLEMENTED);
		Enumeration<String> names = req.getHeaderNames();
		JSONObject headerMap = new JSONObject();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String value = req.getHeader(name);
			headerMap.put(name, value);
		}
		try {
			response.put("header", headerMap);
			response.put("failure", this.getName() + "(" + this.cmdPara + ") is not recognized!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
