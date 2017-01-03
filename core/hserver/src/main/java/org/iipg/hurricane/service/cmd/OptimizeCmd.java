package org.iipg.hurricane.service.cmd;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizeCmd extends HCommand {
	private static Logger log = LoggerFactory.getLogger(OptimizeCmd.class);
	
	public OptimizeCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			boolean ret = broker.optimize();
			response.put("success", ret);
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
