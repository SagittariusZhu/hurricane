package org.iipg.hurricane.service.cmd;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HReportQuery;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.model.HMWReportQuery;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HukeCmd extends HCommand {

	private static Logger log = LoggerFactory.getLogger(HukeCmd.class);
	
	public HukeCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		String fl = ConvertionUtil.getSimpleStringWithNull(req.getParameter("fl"));
		int numTerms = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("numTerms"));
		String qStr = ConvertionUtil.getSimpleStringWithNull(req.getParameter("q"));
		int start = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("start"));
		int rows = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("rows"));
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HMWReportQuery q = new HMWReportQuery();
			q.schema = schemaName;
			q.groupByField = fl;
			q.qStr = qStr;
			q.rowStart = start;
			q.rowCount = rows;
			
			HReportQuery query = HReportQuery.parse(q);
			HDBResponse resp = broker.reportQuery(query);
			
			List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
			JSONArray list = new JSONArray();
			for (int i=0; i<hDocs.size(); i++) {
				HDBRecord doc = hDocs.get(i);
				Map props = doc.getInformation();
				JSONObject item = JSONUtil.toJSONObject(props);
				list.put(item);				
			}
			response.put("docs", list);
			response.put("numTerms", resp.getTotalCount());
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
