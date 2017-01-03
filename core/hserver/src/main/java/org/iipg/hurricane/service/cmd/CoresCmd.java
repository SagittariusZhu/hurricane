package org.iipg.hurricane.service.cmd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.ParameterErrorException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HCoreUtil;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class CoresCmd extends HCommand {

	private static Logger log = LoggerFactory.getLogger(CoresCmd.class);

	private String action = "";
	private JSONArray cores = null;

	public CoresCmd(String name) {
		super(name);
	}

	@Override
	public JSONObject getOutputJSON() {
		JSONObject retObj = new JSONObject();
		try {
			if ("create".equalsIgnoreCase(action)) {
				if (success) {
					retObj.put("success", cores);
				} else {
					retObj.put("failure", cores);
				}
			} else {
				retObj.put("cores", cores);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return retObj;
	}

	@Override
	public void run(HttpServletRequest req) {

		action = ConvertionUtil.getSimpleStringWithNull(req.getParameter("action"));

		if (action.length() == 0 || "list".equalsIgnoreCase(action)) {
			listCores();
		} else if ("create".equalsIgnoreCase(action)) {
			String content = ConvertionUtil.getSimpleStringWithNull(req.getParameter("stream.body"));
			boolean forceOnError = ConvertionUtil.getSimpleBooleanWithNull(req.getParameter("forceOnError"), false);
			createCores(content, forceOnError);
		} else if ("property".equalsIgnoreCase(action)) {
			String coreName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			getCoreProperty(coreName);
		} else if ("desc".equalsIgnoreCase(action)) {
			String coreName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			descCore(coreName);
		} else if ("refresh".equalsIgnoreCase(action)) {
			String coreName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			refreshCore(coreName);
		} else if ("update".equalsIgnoreCase(action)) {
			String coreName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			String content = ConvertionUtil.getSimpleStringWithNull(req.getParameter("stream.body"));
			updateCores(coreName, content);
		} else if ("delete".equalsIgnoreCase(action)) {
			String coreName = ConvertionUtil.getSimpleStringWithNull(req.getParameter("name"));
			boolean fullDelete = ConvertionUtil.getSimpleBooleanWithNull(req.getParameter("full"), false);
			deleteCores(coreName, fullDelete);
		} else {
			listCores();
		}
	}

	private void listCores() {
		cores = new JSONArray();
		List<String> list = null;
		ZkOperator zkOperator = conf.getZkOperator();
		try {
			if (zkOperator.isExist(SchemaParser.SCHEMA_PATH))
				list = zkOperator.getChild(SchemaParser.SCHEMA_PATH);
			else
				list = new ArrayList<String>();
		} catch (Exception e) {
			list = new ArrayList<String>();			
		}
		for (String item : list) {
			String[] arr = item.split("\\.");
			try {
				SchemaParser parser = new SchemaParser(arr[0]);
				JSONObject obj = new JSONObject();
				obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
				obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
				cores.put(obj);
			} catch (Exception e) {
				e.printStackTrace();
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				try {
					JSONObject obj = new JSONObject();
					obj.put("name", arr[0]);
					obj.put("error", e.getMessage());
					cores.put(obj);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		success = true;
	}

	private void listCores_old() {
		cores = new JSONArray();
		String schemaPath = conf.get("sys.schema.path");
		if (schemaPath == null || schemaPath.length() == 0) {
			log.error("sys.schema.path is not set, please check hurricane-site.xml file");
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			return;
		}
		File path = new File(schemaPath);
		if (path.isDirectory()) {
			File[] list = path.listFiles();
			for (File item : list) {
				SchemaParser parser = null;
				try {
					parser = new SchemaParser(new FileInputStream(item));
					try {
						JSONObject obj = new JSONObject();
						obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
						obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
						cores.put(obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		success = true;
	}

	private void createCores(String content, boolean forceOnError) {
		cores = new JSONArray();

		SchemaParser parser = null;
		try {
			parser = new SchemaParser(new ByteArrayInputStream(content.getBytes("UTF-8")));
			success = HCoreUtil.create(parser, forceOnError);
			try {
				JSONObject obj = new JSONObject();
				obj.put("create", success);				
				obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
				obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
				cores.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				try {
					JSONObject obj = new JSONObject();
					obj.put("", e.getMessage());
					cores.put(obj);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void getCoreProperty(String coreName) {
		HDBBroker broker = null;
		cores = new JSONArray();
		try {
			JSONObject coreObj = new JSONObject();
			
			JSONObject obj = HCoreUtil.getProps(coreName);
			coreObj.put("core", obj);
			
			JSONObject indexObj = new JSONObject();
			broker = HDBFactory.getHDBBroker(coreName);
			HMWQuery q = new HMWQuery();
			q.qStr = "*:*";
			q.schema = coreName;
			q.rowStart = 0;
			q.rowCount = 1;
			HQuery hQuery = HQuery.parse(q);
			HDBResponse resp = broker.query(hQuery);
			List<HDBRecord> hDocs = (List<HDBRecord>) resp.getData();
			indexObj.put("numDocs", resp.getTotalCount());

			coreObj.put("index", indexObj);
			
			cores.put(coreObj);
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		success = true;
	}

	private void descCore(String coreName) {
		cores = new JSONArray();

		SchemaParser parser = null;
		try {
			parser = new SchemaParser(coreName);
			JSONObject obj = new JSONObject();
			obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
			obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
			JSONArray fields = new JSONArray();
			List<Field> list = parser.getFields();
			for (Field field : list) {
				JSONObject item = new JSONObject();
				item.put("name", field.getName());
				item.put("desc", field.getDesc());
				item.put("type", field.getType());
				item.put("mode", field.getMode());
				if ("text".equalsIgnoreCase(field.getType())) {
					item.put("language", field.getLanguage());
				}
				item.put("flag", field.getFlag());
				fields.put(item);
			}
			obj.put("fields", fields);
			cores.put(obj);
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		}

		success = true;
	}

	private void refreshCore(String coreName) {
		cores = new JSONArray();

		SchemaParser parser = null;
		try {
			parser = new SchemaParser(coreName);
			success = HCoreUtil.refresh(parser);
			try {
				JSONObject obj = new JSONObject();
				obj.put("refresh", success);				
				obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
				obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
				cores.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				try {
					JSONObject obj = new JSONObject();
					obj.put("", e.getMessage());
					cores.put(obj);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		success = true;
	}

	private void updateCores(String coreName, String content) {
		cores = new JSONArray();

		SchemaParser parser = null;
		try {
			parser = new SchemaParser(coreName);
			parser.mergeFields(content);
			success = HCoreUtil.update(parser);
			try {
				JSONObject obj = new JSONObject();
				obj.put("update", success);				
				obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
				obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
				cores.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				try {
					JSONObject obj = new JSONObject();
					obj.put("", e.getMessage());
					cores.put(obj);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void deleteCores(String coreName, boolean fullDelete) {
		cores = new JSONArray();

		SchemaParser parser = null;
		try {
			parser = new SchemaParser(coreName);
		} catch (Exception ignore) {}
		try {
			success = HCoreUtil.delete(coreName, fullDelete);
			try {
				JSONObject obj = new JSONObject();
				obj.put("delete", success);
				if (parser != null && coreName.equals(parser.getName())) {
					obj.put("name", ConvertionUtil.getSimpleStringWithNull(parser.getName()));
					obj.put("desc", ConvertionUtil.getSimpleStringWithNull(parser.getDesc()));
				} else {
					obj.put("name", coreName);					
				}
				cores.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
				header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				try {
					JSONObject obj = new JSONObject();
					obj.put("", e.getMessage());
					cores.put(obj);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				JSONObject obj = new JSONObject();
				obj.put("", e.getMessage());
				cores.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
