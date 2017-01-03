package org.iipg.hurricane.service.cmd;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
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

public class DSourceCmd extends HCommand {
	
	public static final String LIST_CMD = "list";
	public static final String CREATE_CMD = "create";
	public static final String REMOVE_CMD = "remove";
	public static final String START_CMD = "start";
	public static final String STOP_CMD = "stop";
	public static final String STATUS_CMD = "status";
	
	public static final String FULL_RUNMODE = "full";
	public static final String DELTA_RUNMODE = "delta";

	public static final String FULL_IMPORT_CMD = "full-import";
	public static final String DELTA_IMPORT_CMD = "delta-import";
	
	private static Logger log = LoggerFactory.getLogger(DSourceCmd.class);
	
	public DSourceCmd(String schemaName) {
		super(schemaName);
	}
	
	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		String command = ConvertionUtil.getSimpleStringWithNull(req.getParameter("command"));
		if (LIST_CMD.equals(command)) {
			listSources();
		} else if (CREATE_CMD.equals(command)) {
			String sourceName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String content = ConvertionUtil.getSimpleStringWithNull(req.getParameter("stream.body"));
			createSource(sourceName, content);
//		} else if (START_CMD.equals(command)) {
//			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
//			String runMode = ConvertionUtil.getSimpleStringWithNull(req.getParameter("mode"));
//			if (DELTA_RUNMODE.equals(runMode)) {
//				int interval = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("interval"));
//				deltaRun(dihName, interval);
//			} else if (FULL_RUNMODE.equals(runMode)) {
//				boolean clean = ConvertionUtil.getSimpleBooleanWithNull(req.getParameter("clean"), true);
//				fullRun(dihName, clean);
//			}
//		} else if (STOP_CMD.equals(command)) {
//			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
//			stopRun(dihName);
		} else if (REMOVE_CMD.equals(command)) {
			String sourceName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			removeSource(sourceName);
		} else if (STATUS_CMD.equals(command)) {
			getStatus();
		}
	}

	private void createSource(String sourceName, String content) {
		// TODO Auto-generated method stub
		
	}

	private void removeSource(String sourceName) {
		// TODO Auto-generated method stub
		
	}

	private void getStatus() {
		// TODO Auto-generated method stub
		
	}

	private void listSources() {
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBResponse resp = broker.listHandlers("source");
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
