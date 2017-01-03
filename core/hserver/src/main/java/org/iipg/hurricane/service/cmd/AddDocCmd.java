package org.iipg.hurricane.service.cmd;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HDocument;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddDocCmd extends HCommand {
	private static Logger log = LoggerFactory.getLogger(AddDocCmd.class);
	
	public AddDocCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		String data = ConvertionUtil.getSimpleStringWithNull(req.getParameter("data"));
		boolean autoUniqueKey = ConvertionUtil.getSimpleBooleanWithNull(req.getParameter("autoUniqueKey"), false);
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			Map<String, Object> dataMap = JSONUtil.toMap(data);
			HDocument doc = HDocument.parse(dataMap, null, schemaName);
			if (autoUniqueKey) {
				doc.setUniqueKey(UUID.randomUUID().toString());
			}
			int ret = broker.store(doc);
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
