package org.iipg.hurricane.service.cmd;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HDocument;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCmd extends HCommand {
	private static Logger log = LoggerFactory.getLogger(UpdateCmd.class);
	
	public UpdateCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		String data = ConvertionUtil.getSimpleStringWithNull(req.getParameter("data"));
		String qStr = ConvertionUtil.getSimpleStringWithNull(req.getParameter("q"));
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			Map<String, Object> dataMap = JSONUtil.toMap(data);
			HDocument doc = HDocument.parse(dataMap, null, schemaName);
			
			HMWQuery q = new HMWQuery();
			q.qStr = qStr;
			q.schema = schemaName;
			q.rowStart = 0;
			q.rowCount = 1000;
			HQuery hQuery = HQuery.parse(q);

			int ret = broker.updateByQuery(doc, hQuery);
			response.put("numAffected", ret);
			success = true;
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
