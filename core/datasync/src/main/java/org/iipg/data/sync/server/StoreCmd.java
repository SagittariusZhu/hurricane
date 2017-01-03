package org.iipg.data.sync.server;

import java.io.File;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.iipg.data.sync.conf.DBConfiguration;
import org.iipg.data.sync.store.SQLStorer;
import org.iipg.web.cmd.HCommand;
import org.iipg.web.cmd.ResponseStatus;
import org.iipg.web.cmd.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StoreCmd extends HCommand {

	public StoreCmd(String cmdName) {
		super(cmdName);
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

		String contentStr = ServletUtil.getContentText(req);
		JSONObject contentObj = new JSONObject(contentStr);
		String tbName = contentObj.getString("tbname");
		JSONObject metads = contentObj.getJSONObject("metads");
		JSONArray records = contentObj.getJSONArray("records");
		JSONObject inputMap = new JSONObject();
		inputMap.put("tbname", tbName);
		inputMap.put("count", records.length());

		try {
			String fullPath = ServletUtil.getHome();
			DBConfiguration conf = new DBConfiguration(fullPath + File.separator + this.getName() + ".properties");
			RestfulRouter router = new RestfulRouter(conf);
			router.store(tbName, metads, records);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			response.put("header", headerMap);
			response.put("input", inputMap);
			response.put("failure", this.getName() + " is not recognized!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
