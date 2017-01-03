package org.iipg.hurricane.db.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.HControllerFactory;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HCoreUtil {
	
	private static Logger LOG  = LoggerFactory.getLogger(HCoreUtil.class);
	
	public static boolean create(SchemaParser parser, boolean forceOnError) throws HurricaneException {
		HurricaneConfiguration conf = parser.getConf();
		
//		String schemaPath = conf.get("sys.schema.path");
//		if (schemaPath == null || schemaPath.length() == 0) {
//			LOG.error("sys.schema.path is not set, please check hurricane-site.xml file");
//			throw new HurricaneException("sys.schema.path is not set");
//		}
//		String name = parser.getName();
//		File schemaFile = new File(schemaPath + File.separator + name + ".xml");
//		if (schemaFile.exists() && !forceOnError) {
//			throw new HurricaneException("Schema " + name + " is already exist!");
//		}
		
		if (parser.exists() && !forceOnError) {
			throw new HurricaneException("Schema " + parser.getName() + " is already exist!");
		}
		
		try {
			Map<String, ?> props = conf.getProps();
			for (String key : props.keySet()) {
				if (key.startsWith("controller.")) {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					controller.createSchema(parser);
				}
			}			
			//FileUtil.writeXmlFile(schemaFile, parser.getDocument());
			parser.persistent(true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}

	public static boolean refresh(SchemaParser parser) throws HurricaneException {
		HurricaneConfiguration conf = parser.getConf();
		
//		String schemaPath = conf.get("sys.schema.path");
//		if (schemaPath == null || schemaPath.length() == 0) {
//			LOG.error("sys.schema.path is not set, please check hurricane-site.xml file");
//			throw new HurricaneException("sys.schema.path is not set");
//		}
//		String name = parser.getName();
//		File schemaFile = new File(schemaPath + File.separator + name + ".xml");
//		if (!schemaFile.exists()) {
//			throw new HurricaneException("Schema " + name + " is not exist!");
//		}
		
		if (!parser.exists()) {
			throw new HurricaneException("Schema " + parser.getName() + " is not exist!");
		}
		
		try {
			Map<String, ?> props = conf.getProps();
			for (String key : props.keySet()) {
				if (key.startsWith("controller.")) {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					controller.refreshSchema(parser);
				}
			}			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}
	
	public static boolean update(SchemaParser parser) throws HurricaneException {
		HurricaneConfiguration conf = parser.getConf();
		
//		String schemaPath = conf.get("sys.schema.path");
//		if (schemaPath == null || schemaPath.length() == 0) {
//			LOG.error("sys.schema.path is not set, please check hurricane-site.xml file");
//			throw new HurricaneException("sys.schema.path is not set");
//		}
//		String name = parser.getName();
//		File schemaFile = new File(schemaPath + File.separator + name + ".xml");
//		if (!schemaFile.exists()) {
//			throw new HurricaneException("Schema " + name + " is not exist!");
//		}
		
		if (!parser.exists()) {
			throw new HurricaneException("Schema " + parser.getName() + " is not exist!");
		}
		
		try {
			Map<String, ?> props = conf.getProps();
			for (String key : props.keySet()) {
				if (key.startsWith("controller.")) {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					controller.createSchema(parser);
				}
			}			
			//FileUtil.writeXmlFile(schemaFile, parser.getDocument());
			parser.persistent(true);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}

	public static boolean delete(String coreName, boolean fullDelete) throws HurricaneException {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
		
//		String schemaPath = conf.get("sys.schema.path");
//		if (schemaPath == null || schemaPath.length() == 0) {
//			LOG.error("sys.schema.path is not set, please check hurricane-site.xml file");
//			throw new HurricaneException("sys.schema.path is not set");
//		}
//		File schemaFile = new File(schemaPath + File.separator + coreName + ".xml");
//		if (schemaFile.exists()) {
//			FileUtil.removeFile(schemaFile);
//		}
		
		SchemaParser parser = null;
		try {
			parser = new SchemaParser(coreName);
		} catch (Exception ignore) {}
		
		try {
			Map<String, ?> props = conf.getProps();
			for (String key : props.keySet()) {
				if (key.startsWith("controller.")) {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					controller.deleteSchema(coreName, fullDelete);
				}
			}			
			if (parser != null)
				parser.drop();
			else
				SchemaParser.drop(coreName);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}

	public static boolean copySchema(String srcName, String destName) {
		SchemaParser parser = null;
		try {
			parser = new SchemaParser(srcName);
			SchemaParser newParser = parser.duplicate(destName);
			
			boolean ret = create(newParser, true);
			return ret;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			throw new HurricaneException(e);
		}
	}

	public static JSONObject getProps(String coreName) {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
		JSONObject propObj = new JSONObject();
		
		try {
			SchemaParser parser = new SchemaParser(coreName);
			propObj.put("name", parser.getName());
			propObj.put("desc", parser.getDesc());
			List<Field> list = parser.getFields();
			propObj.put("fields", list.size());
			
			HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));
			JSONObject data = controller.getSchemaStatus(coreName);
			if (data != null) {
				JSONObject shards = data.getJSONObject("shards");
				propObj.put("shards", shards.keySet().size());
				propObj.put("maxShardsPerNode", data.get("maxShardsPerNode"));
				propObj.put("replicationFactor", data.get("replicationFactor"));
				boolean health = true;
				for (Iterator<String> it = shards.keys(); it.hasNext(); ) {
					String key = it.next();
					if (!shards.getJSONObject(key).getString("state").equals("active")) {
						health = false;
						break;
					}
				}
				propObj.put("health", health);
				propObj.put("data", data);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			throw new HurricaneException(e);
		}

		return propObj;
	}

}
