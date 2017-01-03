package org.iipg.hurricane.service.cmd;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.util.HCoreUtil;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeCmd extends HCommand {

	private static Logger log = LoggerFactory.getLogger(SubscribeCmd.class);
	
	public SubscribeCmd(String name) {
		super(name);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		String qStr = ConvertionUtil.getSimpleStringWithNull(req.getParameter("q"));
		String tb = ConvertionUtil.getSimpleStringWithNull(req.getParameter("tb"));
		int rows = 10;
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			
			HMWQuery q = new HMWQuery();
			q.qStr = qStr;
			q.schema = schemaName;
			q.rowStart = 0;
			q.rowCount = rows;
			HQuery hQuery = HQuery.parse(q);
			
			if (HCoreUtil.copySchema(getName(), tb)) {
				int ret = broker.subscribe(hQuery, tb);
				response.put("numAffected", ret);
				success = true;
			} else {
				throw new HurricaneException("Create new schema " + tb + " failed!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				response.put("failure", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
