package org.iipg.hurricane.service.cmd;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.util.log.Log;
import org.iipg.hurricane.ParameterErrorException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.util.HandlerUtil;
import org.iipg.hurricane.server.inner.DeltaRunThread;
import org.iipg.hurricane.server.inner.DeltaThreadManager;
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
import org.xml.sax.SAXException;

public class DihCmd extends HCommand {

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

	private static Logger log = LoggerFactory.getLogger(DihCmd.class);
	
	public DihCmd(String schemaName) {
		super(schemaName);
	}
	
	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		String command = ConvertionUtil.getSimpleStringWithNull(req.getParameter("command"));
		if (LIST_CMD.equals(command)) {
			listHandlers();
		} else if (CREATE_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String dihType = ConvertionUtil.getSimpleStringWithNull(req.getParameter("type"));
			String content = ConvertionUtil.getSimpleStringWithNull(req.getParameter("stream.body"));
			createHandler(dihName, dihType, content);
		} else if (START_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String runMode = ConvertionUtil.getSimpleStringWithNull(req.getParameter("mode"));
			if (DELTA_RUNMODE.equals(runMode)) {
				int interval = ConvertionUtil.getSimpleIntegerWithNull(req.getParameter("interval"));
				deltaRun(dihName, interval);
			} else if (FULL_RUNMODE.equalsIgnoreCase(runMode)) {
				boolean clean = ConvertionUtil.getSimpleBooleanWithNull(req.getParameter("clean"), true);
				fullRun(dihName, clean);
			}
		} else if (REMOVE_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			removeHandler(dihName);
		} else if (STOP_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			stopRun(dihName);
		} else if (STATUS_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			getStatus(dihName);
		} else {
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				response.put("failure", "Unknown command " + command);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void getStatus(String dihName) {
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBResponse resp = broker.listHandlers("DIH");
			List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
			JSONArray list = new JSONArray();
			for (int i=0; i<hDocs.size(); i++) {
				HDBRecord doc = hDocs.get(i);
				Map props = doc.getInformation();
				JSONObject item = JSONUtil.toJSONObject(props);
				if (dihName.equals((String) item.get("name"))) {
					JSONObject statusObj = getStatus(broker, dihName);
					if (DeltaThreadManager.find(schemaName + "-" + dihName) != null) {
						statusObj.put("deltaStatus", "running");
					}
					item.put("status", statusObj);
					list.put(item);				
				}
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

	private JSONObject getStatus(HDBBroker broker, String dihName) throws Exception {
		HDBResponse resp = broker.getHandlerStatus("DIH", dihName);
		List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
		HDBRecord doc = hDocs.get(0);
		Map props = doc.getInformation();
		JSONObject item = JSONUtil.toJSONObject(props);
		String status = HandlerUtil.getStatus(conf, schemaName, dihName);
		if (status != null) {
			item.put("lastUpdateStatus", status);
		}
		String deltaStatus = HandlerUtil.getDeltaStatus(conf, schemaName, dihName);
		if (deltaStatus != null) {
			item.put("deltaImportStatus", deltaStatus);			
		}
		String dihType = HandlerUtil.getDihType(conf, schemaName, dihName);
		if (dihType != null) {
			item.put("dihType", dihType);
		}
		return item;
	}
	
	private void listHandlers() {
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBResponse resp = broker.listHandlers("DIH");
			List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
			JSONArray list = new JSONArray();
			for (int i=0; i<hDocs.size(); i++) {
				HDBRecord doc = hDocs.get(i);
				Map props = doc.getInformation();
				JSONObject item = JSONUtil.toJSONObject(props);
				if (DeltaThreadManager.find(schemaName + "-" + item.get("name")) != null) {
					item.put("deltaStatus", "running");
				}				
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
	
	private void createHandler(String dihName, String dihType, String content) {
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HandlerUtil.addHandler(conf, schemaName, dihName, dihType, content);
			List<HDBDiHandler> handlers = HandlerUtil.getHandlers(conf, schemaName);
			HDBResponse resp = broker.createHandlers("DIH", handlers);
			success = false;
			if (resp.getCount() >= 1) {
				handlers = HandlerUtil.getHandlers(conf, schemaName);
				for (HDBDiHandler handler : handlers) {
					if (handler.getDihName().equalsIgnoreCase(dihName) && handler.getDihType().equalsIgnoreCase(dihType)) {
						success = true;
						break;
					}
				}
			}
			if (success)
				response.put("create", "success");
			else {
				response.put("create", "failed");
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				success = false;
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
	
	private void removeHandler(String dihName) {
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HandlerUtil.removeHandler(conf, schemaName, dihName);
			List<HDBDiHandler> handlers = HandlerUtil.getHandlers(conf, schemaName);
			HDBResponse resp = broker.createHandlers("DIH", handlers);
			response.put("remove", "success");
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

	private void fullRun(String dihName, boolean clean) {
		HandlerUtil.setStatus(conf, schemaName, dihName, 
				"full import with clean=" + clean);
		Map params = new HashMap();
		params.put("command", "full-import");
		params.put("clean", clean);
		params.put("commit", true);
		params.put("wt", "json");
		params.put("indent", true);
		params.put("verbose", false);
		params.put("optimize", false);
		params.put("debug", false);
		runHandler("DIH", dihName, params);
	}

	private void deltaRun(String dihName, int interval) {
		try {
			DeltaRunThread deltaThread = new DeltaRunThread(schemaName);
			deltaThread.setDihName(dihName);
			String dihType = HandlerUtil.getDihType(conf, schemaName, dihName);
			if ("mysql".equals(dihType))
				deltaThread.setImportCmd(DELTA_IMPORT_CMD);
			else
				deltaThread.setImportCmd(FULL_IMPORT_CMD);
			if (interval <= 0) {
				String msg = "Interval for delta import must be large then 0";
				throw new ParameterErrorException(msg);
			}
			deltaThread.setInterval(interval);
			HandlerUtil.setDeltaStartStatus(conf, schemaName, dihName, interval);
			DeltaThreadManager.add(schemaName + "-" + dihName, deltaThread);
			deltaThread.start();
			response.put("run", "success");
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

	private void stopRun(String dihName) {
		DeltaRunThread deltaThread = DeltaThreadManager.find(schemaName + "-" + dihName);
		if (deltaThread != null && deltaThread.isBusy()) {
			deltaThread.stop();
			DeltaThreadManager.remove(schemaName + "-" + dihName);
			HandlerUtil.setDeltaStartStatus(conf, schemaName, dihName, -1);
		}
		try {
			response.put("stop", "success");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		success = true;
	}
	
	private void runHandler(String handlerType, String dihName, Map params) {
		HDBBroker broker = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBResponse resp = broker.startHandler(handlerType, dihName, params);
			if (resp.getCount() == 1) {
				response.put("run", "success");
				success = true;
			} else {
				response.put("run", "failed");
				success = false;
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
