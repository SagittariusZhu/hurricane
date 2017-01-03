package org.iipg.hurricane.util;

import org.iipg.hurricane.service.ResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class ServletUtil {

	public static JSONObject getResponseHeader(ResponseStatus value) {
		JSONObject header = new JSONObject();
		try {
			header.put("status", value.status);
			header.put("message", value.message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return header;
	}
}
