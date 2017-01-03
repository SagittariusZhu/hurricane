package org.iipg.hurricane.service.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.ParameterErrorException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.model.HMWConnException;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWQuery;
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

public class SelectCmd extends HCommand {
	
	private static Logger log = LoggerFactory.getLogger(SelectCmd.class);
	
	public SelectCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		
		String qStr = ConvertionUtil.getSimpleStringWithNull(req.getParameter("q"));
		String fl = ConvertionUtil.getSimpleStringWithNull(req.getParameter("fl"));
		String hfl = ConvertionUtil.getSimpleStringWithNull(req.getParameter("hl.fl"));
		String hmode = ConvertionUtil.getSimpleStringWithNull(req.getParameter("hl.mode"));
		String sort = ConvertionUtil.getSimpleStringWithNull(req.getParameter("sort"));
		int start = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("start"));
		int rows = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("rows"));
		if (rows == 0) rows = 10;
		
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HMWQuery q = new HMWQuery();
			q.qStr = qStr;
			q.schema = schemaName;
			q.selectFields = fl;
			q.highlightFields = hfl;
			q.highlightMode = "";
			q.orderByStr = sort;
			q.rowStart = 0;
			q.rowCount = rows;
			
			if (hmode != null && hmode.length() > 0) {
				if (!"abs".equalsIgnoreCase(hmode) && !"full".equalsIgnoreCase(hmode)) {
					throw new ParameterErrorException("hl.mode only support full/abs!");
				}
				q.highlightMode = hmode;
			}

			HQuery hQuery = HQuery.parse(q);
			HDBResponse resp = broker.query(hQuery);
			
			List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
			JSONArray list = new JSONArray();
			for (int i=0; i<hDocs.size(); i++) {
				HDBRecord doc = hDocs.get(i);
				Map props = doc.getInformation();
				JSONObject item = JSONUtil.toJSONObject(props);
				list.put(item);				
			}
			response.put("docs", list);
			response.put("start", start);
			response.put("numFound", resp.getTotalCount());
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

	private Map<String, String>[] buildOrderClause(String sort) {
		List<Map<String, String>> orderFields = new ArrayList();
		if (sort != null && sort.length() > 0) {
			String[] pairs = sort.split(",");
			for (String pair : pairs) {
				String[] arr = pair.split(" ");
				String orderFlag = "ASC";
				if (arr.length > 1) {
					orderFlag = arr[1];
				}
				Map<String, String> of = new HashMap<String, String>();
				of.put("field", arr[0]);
				of.put("op", orderFlag);
				orderFields.add(of);
			}
		}
		return orderFields.toArray(new Map[0]);
	}
}
