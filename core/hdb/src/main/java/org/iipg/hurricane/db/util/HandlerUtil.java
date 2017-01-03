package org.iipg.hurricane.db.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerUtil {
	
	private static Logger LOG = LoggerFactory.getLogger(HandlerUtil.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void createHandler(HurricaneConfiguration conf, String schemaName, 
			String dihName, String dihType) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("dihType", dihType);
			byte[] data = obj.toString().getBytes("utf-8");
			ZkOperator zkOperator = conf.getZkOperator();
			zkOperator.create("/hurricane/dih/" + schemaName + "/" + dihName + ".properties", data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addHandler(HurricaneConfiguration conf,
			String schemaName, String dihName, String dihType, String content) throws HurricaneException, UnsupportedEncodingException {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		String configPath = "/hurricane/dih/" + schemaName + "/" + dihName + ".config";
		ZkOperator zkOperator = conf.getZkOperator();
		if (zkOperator.isExist(path) && zkOperator.isExist(configPath)) {
			String msg = "Handler " + dihName + " already exist, update it!";
			LOG.info(msg);
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("dihType", dihType);
			if (zkOperator.isExist(path))
				zkOperator.update(path, obj.toString().getBytes("utf-8"));
			else
				zkOperator.create(path, obj.toString().getBytes("utf-8"));
			
			if (zkOperator.isExist(configPath))
				zkOperator.update(configPath, content.getBytes("utf-8"));
			else
				zkOperator.create(configPath, content.getBytes("utf-8"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void removeHandler(HurricaneConfiguration conf,
			String schemaName, String dihName) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		String configPath = "/hurricane/dih/" + schemaName + "/" + dihName + ".config";
		ZkOperator zkOperator = conf.getZkOperator();
		if (zkOperator.isExist(path)) {
			zkOperator.remove(path);
		}
		if (zkOperator.isExist(configPath)) {
			zkOperator.remove(configPath);
		}
	}
	
	public static void runHandler(String schemaName, String handlerType, String dihName, Map params) {
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBResponse resp = broker.startHandler(handlerType, dihName, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getStatus(HurricaneConfiguration conf, String schemaName, String dihName) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		ZkOperator zkOperator = conf.getZkOperator();
		byte[] data = zkOperator.getData(path);
		try {
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			if (obj.has("status")) {
				return obj.getString("status");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDeltaStatus(HurricaneConfiguration conf,
			String schemaName, String dihName) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		ZkOperator zkOperator = conf.getZkOperator();
		byte[] data = zkOperator.getData(path);
		try {
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			if (obj.has("delta")) {
				return obj.getString("delta");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDihType(HurricaneConfiguration conf,
			String schemaName, String dihName) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		ZkOperator zkOperator = conf.getZkOperator();
		byte[] data = zkOperator.getData(path);
		try {
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			if (obj.has("dihType")) {
				return obj.getString("dihType");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void setStatus(HurricaneConfiguration conf,
			String schemaName, String dihName, String content) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		ZkOperator zkOperator = conf.getZkOperator();
		byte[] data = zkOperator.getData(path);
		try {
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			obj.put("status", "[" + sdf.format(new Date()) + "] " + content);
			zkOperator.update(path, obj.toString().getBytes("utf-8"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HurricaneException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setDeltaStartStatus(HurricaneConfiguration conf,
			String schemaName, String dihName, int interval) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		ZkOperator zkOperator = conf.getZkOperator();
		byte[] data = zkOperator.getData(path);
		try {
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			if (interval >= 0)
				obj.put("delta", "[" + sdf.format(new Date()) + "] start delta import with interval=" + interval + "(s)");
			else
				obj.put("delta", "[" + sdf.format(new Date()) + "] stop delta import.");
			zkOperator.update(path, obj.toString().getBytes("utf-8"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<HDBDiHandler> getHandlers(HurricaneConfiguration conf,
			String schemaName) {
		List<HDBDiHandler> handlers = new ArrayList<HDBDiHandler>();
		ZkOperator zkOperator = conf.getZkOperator();
		String path = "/hurricane/dih/" + schemaName;
		if (!zkOperator.isExist(path)) {
			return handlers;
		}
		
		List<String> files = zkOperator.getChild(path);
		for (String item : files) {
			if (item.endsWith(".properties")) {
				String dihName = item.substring(0, item.length() - 11);
				HDBDiHandler handler = buildHiHandler(zkOperator, schemaName, dihName);
				if (handler != null)
					handlers.add(handler);
			}
		}
		return handlers;
	}

	private static HDBDiHandler buildHiHandler(ZkOperator zkOperator, String schemaName, String dihName) {
		String path = "/hurricane/dih/" + schemaName + "/" + dihName + ".properties";
		String configPath = "/hurricane/dih/" + schemaName + "/" + dihName + ".config";
		if (!zkOperator.isExist(path) || !zkOperator.isExist(configPath)) {
			return null;
		}
		
		String dihType = "";
		try {
			byte[] data = zkOperator.getData(path);
			JSONObject obj = new JSONObject(new String(data, "utf-8"));
			if (obj.has("dihType")) {
				dihType = obj.getString("dihType");
			} else {
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HDBDiHandler handler = new HDBDiHandler(dihName, dihType);
		byte[] content = zkOperator.getData(configPath);
		handler.setDihContent(content);
		
		return handler;
	}

}
