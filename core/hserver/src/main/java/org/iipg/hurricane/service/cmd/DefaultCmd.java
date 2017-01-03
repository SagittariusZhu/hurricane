package org.iipg.hurricane.service.cmd;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultCmd extends HCommand {

	public DefaultCmd(String cmdName) {
		super(cmdName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		header = ServletUtil.getResponseHeader(ResponseStatus.RS_NOT_IMPLEMENTED);
		try {
			response.put("failure", this.getName() + " is not recognized!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
