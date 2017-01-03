package org.iipg.hurricane.service.cmd;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.iipg.data.sync.SyncTask;
import org.iipg.data.sync.util.DSyncUtil;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.server.inner.DeltaThreadManager;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.iipg.hurricane.util.XmlUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSyncCmd extends HCommand {

	public static final String LIST_CMD = "list";
	public static final String CREATE_CMD = "create";
	public static final String REMOVE_CMD = "remove";
	public static final String CONFIG_CMD = "config";

	public static final String GETCONFIG_CMD = "getconfig";

	public static final String TEST_CMD = "test";

	public static final String START_CMD = "start";
	public static final String STOP_CMD = "stop";
	public static final String STATUS_CMD = "status";

	public static final String FULL_RUNMODE = "full";
	public static final String DELTA_RUNMODE = "delta";

	public static final String FULL_IMPORT_CMD = "full-import";
	public static final String DELTA_IMPORT_CMD = "delta-import";

	private static Logger log = LoggerFactory.getLogger(DSyncCmd.class);

	public DSyncCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		response = new JSONObject();
		String command = ConvertionUtil.getSimpleStringWithNull(req.getParameter("command"));
		if (LIST_CMD.equals(command)) {
			listHandlers();
		} else if (CREATE_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String content = ConvertionUtil.getSimpleStringWithNull(req.getParameter("stream.body"));
			createHandler(taskName, content);
		} else if (REMOVE_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			removeHandler(taskName);
		} else if (CONFIG_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String params = ConvertionUtil.getSimpleStringWithNull(req.getParameter("params"));
			configHandler(taskName, params);			
		} else if (GETCONFIG_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			getConfig(taskName);			
		} else if (TEST_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			testHandler(taskName);			
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
		} else if (STOP_CMD.equalsIgnoreCase(command)) {
			String dihName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			stopRun(dihName);
		} else if (STATUS_CMD.equalsIgnoreCase(command)) {
			String taskName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			getStatus(taskName);
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

	private void createHandler(String taskName, String content) {
		String fullPath = "/hurricane/configs/" + getName() + "/sync/" + taskName + ".xml";
		ZkOperator zkOper = new ZkOperator();

		try {   
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (!zkOper.isExist(fullPath))
				zkOper.create(fullPath, content.getBytes("utf-8"));
			else
				zkOper.update(fullPath, content.getBytes("utf-8"));
			success = true;
			response.put("create", "success");
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				response.put("failure", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}  finally {
			zkOper.close();
		}
	}

	private void listHandlers() {
		String fullPath = "/hurricane/configs/" + getName() + "/sync";
		ZkOperator zkOper = new ZkOperator();

		try {
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			JSONArray list = new JSONArray();

			if (zkOper.isExist(fullPath)) {
				List<String> tasks = zkOper.getChild(fullPath);
				Collections.sort(tasks);
				for (int i=0; i<tasks.size(); i++) {
					String taskName = tasks.get(i);
					if (taskName.endsWith(".xml")) {
						taskName = taskName.substring(0, taskName.length() - 4);
						JSONObject item = new JSONObject();
						item.put("name", taskName);
						list.put(item);				
					}
				}
			}
			response.put("docs", list);
			response.put("numTerms", list.length());
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
		}  finally {
			zkOper.close();
		}
	}

	private void removeHandler(String taskName) {
		String fullPath = "/hurricane/configs/" + getName() + "/sync/" + taskName + ".xml";
		ZkOperator zkOper = new ZkOperator();

		try {   
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (zkOper.isExist(fullPath))
				zkOper.remove(fullPath);
			success = true;
			response.put("remove", "success");
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				response.put("failure", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}  finally {
			zkOper.close();
		}
	}

	private void configHandler(String taskName, String params) {
		SyncTask task = new SyncTask(getName(), taskName);
		task.setCrons(params);
		
		DSyncUtil.updateSyncTask(task);
		success = true;
		response.put("config", "success");

		/*
		String fullPath = "/hurricane/configs/" + getName() + "/sync/" + taskName + "_params.json";
		ZkOperator zkOper = new ZkOperator();

		try {   
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (!zkOper.isExist(fullPath))
				zkOper.create(fullPath, params.getBytes("utf-8"));
			else
				zkOper.update(fullPath, params.getBytes("utf-8"));
			success = true;
			response.put("config", "success");
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				response.put("failure", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}  finally {
			zkOper.close();
		}*/
	}
	
	private void getConfig(String taskName) {
		SyncTask task = DSyncUtil.getSyncTask(getName(), taskName);
		JSONObject obj = new JSONObject();
		
		obj.put("crons", task.getCrons());
		
		this.setHeader(KEY_CONTENT_TYPE, "application/json;charset=utf-8");
		this.setRawOutput(obj.toString());				
		success = true;
	}

	private void testHandler(String taskName) {
		String fullPath = "/hurricane/configs/" + getName() + "/sync/" + taskName + ".xml";
		ZkOperator zkOper = new ZkOperator();

		try {
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (zkOper.isExist(fullPath)) {
				String content = new String(zkOper.getData(fullPath), "utf-8");
				success = DSyncUtil.runTest(content);
				if (success)
					response.put("msg", "Run " + taskName + " is OK!");
				else
					response.put("msg", "Run " + taskName + " is failed!");
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
		}  finally {
			zkOper.close();
		}
	}

	private void getStatus(String taskName) {
		String fullPath = "/hurricane/configs/" + getName() + "/sync/" + taskName + "_status.json";
		ZkOperator zkOper = new ZkOperator();

		try {
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (zkOper.isExist(fullPath)) {
				String content = new String(zkOper.getData(fullPath), "utf-8");
				JSONObject status = new JSONObject(content);
				response.put("status", status);
			} else {
				response.put("status", new JSONObject());
			}
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
		}  finally {
			zkOper.close();
		}
	}

	private void stopRun(String dihName) {
		// TODO Auto-generated method stub

	}

	private void fullRun(String dihName, boolean clean) {
		// TODO Auto-generated method stub

	}

	private void deltaRun(String dihName, int interval) {
		// TODO Auto-generated method stub

	}



}
