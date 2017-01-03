package org.iipg.web.cmd;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultAdminCmd extends HAdminCommand {
	
	private String cmdName = null;
	
	public DefaultAdminCmd(String cmdName) {
		this.cmdName = cmdName;
	}

	@Override
	public void run(HttpServletRequest req) throws IIPGCmdException {
		response = new JSONObject();
		header = ServletUtil.getResponseHeader(ResponseStatus.RS_NOT_IMPLEMENTED);
		try {
			response.put("failure", "ADMIN Command " + this.cmdName + " is not recognized!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
