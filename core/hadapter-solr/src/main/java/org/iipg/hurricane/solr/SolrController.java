package org.iipg.hurricane.solr;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HandlerUtil;
import org.iipg.hurricane.solr.util.ZkConnector;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.XmlUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SolrController extends HController {

	private static Logger LOG = LoggerFactory.getLogger(SolrController.class);

	public SolrController(HurricaneConfiguration conf) {
		super(conf);
	}

	@Override
	public boolean createSchema(SchemaParser schemaParser) throws HurricaneException {
		boolean needReload = false;

		String instanceDir = getConf().get("sys.temp.path") + File.separator + "data" + new Date().getTime() + File.separator + "conf" + File.separator;
		String templateDir = getConf().get("solr.template.path") + File.separator + "conf";
		try {
			FileUtil.clone(templateDir, instanceDir);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new HurricaneException(e1);
		}

		//create schema.xml
		SolrSchema schema;
		try {
			schema = new SolrSchema(getConf(), SolrSchema.DEFAULT_SCHEMA_FILE);
			List<Field> list = schemaParser.getFields();
			for (Field item : list) {
				if ("ro".equalsIgnoreCase(item.getMode()) || item.isUnique()) {
					schema.addField(item);
				}
			}
			schema.setName(schemaParser.getName());
			if (schemaParser.getDesc() != null) {
				schema.setDesc(schemaParser.getDesc());
			}
			schema.persist(instanceDir + File.separator + "schema.xml");
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}

		String confName = schemaParser.getName();
		String coreName = confName;

		// 1. upload confDir
		ZkConnector.upConfig(instanceDir, confName);

		// 2. link collection
		//ZkConnector.linkConfig(confName, confName);

		// 3. create solr core
		LOG.info("Create core " + coreName);
		HSolrAdminClient solrAdminClient = new HSolrAdminClient();
		int factor = schemaParser.getFactor();
		int shards = schemaParser.getShards();
		int maxShardsPerNode = schemaParser.getMaxShardsPerNode();
		try {
			boolean ret = solrAdminClient.createCore(coreName, confName, factor, shards, maxShardsPerNode);
			if (!ret)
				needReload = true;
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			needReload = true;
		}

		boolean status = true;
		
		// 4. anyway, reload the core
		if (needReload) {
			LOG.info("Reload core " + coreName);
			try {
				solrAdminClient.reloadCore(coreName);
			} catch (Exception e) {
				LOG.warn(e.getMessage());
				status = false;
			}
		}

		solrAdminClient.close();

		return status;
	}

	@Override
	public boolean deleteSchema(String coreName, boolean fullDelete) {
		HSolrAdminClient solrAdminClient = new HSolrAdminClient();
		LOG.info("Delete core " + coreName);
		try {
			solrAdminClient.deleteCore(coreName);
		} catch (Exception ignore) {}

		if (fullDelete) {
			ZkConnector.clearConfig(coreName);
		}

		solrAdminClient.close();

		return true;
	}

	@Override
	public boolean refreshSchema(SchemaParser schemaParser) {
		String instanceDir = getConf().get("sys.temp.path") + File.separator + "data" + new Date().getTime() + File.separator + "conf" + File.separator;
		String templateDir = getConf().get("solr.template.path") + File.separator + "conf";
		try {
			FileUtil.clone(templateDir, instanceDir);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new HurricaneException(e1);
		}

		//create schema.xml
		SolrSchema schema;
		try {
			schema = new SolrSchema(getConf(), SolrSchema.DEFAULT_SCHEMA_FILE);
			List<Field> list = schemaParser.getFields();
			for (Field item : list) {
				if ("ro".equalsIgnoreCase(item.getMode()) || item.isUnique()) {
					schema.addField(item);
				}
			}
			schema.setName(schemaParser.getName());
			if (schemaParser.getDesc() != null) {
				schema.setDesc(schemaParser.getDesc());
			}
			schema.persist(instanceDir + File.separator + "schema.xml");
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}

		String confName = schemaParser.getName();
		String coreName = confName;

		// 1. remove confDir
		ZkConnector.clearConfig(coreName);

		// 2. upload confDir
		ZkConnector.upConfig(instanceDir, confName);

		// 4. anyway, reload the core
		LOG.info("Reload core " + coreName);
		HSolrAdminClient solrAdminClient = new HSolrAdminClient();
		boolean status = true;
		try {
			solrAdminClient.reloadCore(coreName);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			status = false;
		}
		solrAdminClient.close();

		return status;
	}

	public boolean reloadSchema(String schemaName) {
		HSolrAdminClient solrAdminClient = new HSolrAdminClient();
		boolean status = true;
		try {
			solrAdminClient.reloadCore(schemaName);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			status = false;
		} finally {
			solrAdminClient.close();
		}

		return status;
	}

	@Override
	public boolean createHandler(String schemaName, String dihName,
			String dihType, String content) {
		try {
			List<HDBDiHandler> handlers = new ArrayList<HDBDiHandler>();
			HDBDiHandler handler = new HDBDiHandler(dihName, dihType);
			byte[] buf = content.getBytes("utf-8");
			handler.setDihContent(buf);
			handlers.add(handler);
			boolean retValue = addDataImportHandler(schemaName, handlers);
			return retValue;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	@Override
	public boolean createHandlers(String schemaName, List<HDBDiHandler> handlers) {
		boolean retValue = addDataImportHandler(schemaName, handlers);
		return retValue;
	}

	public boolean addDataImportHandler(String coreName, List<HDBDiHandler> diHandlers) {	
		String instanceDir = getConf().get("sys.temp.path") + File.separator + "data" + new Date().getTime() + File.separator + "conf" + File.separator;
		String templateDir = getConf().get("solr.template.path") + File.separator + "conf";
		try {
			FileUtil.clone(templateDir, instanceDir);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new HurricaneException(e1);
		}

		//create schema.xml
		try {
			SchemaParser schemaParser = new SchemaParser(coreName);
			SolrSchema schema = new SolrSchema(getConf(), SolrSchema.DEFAULT_SCHEMA_FILE);
			List<Field> list = schemaParser.getFields();
			for (Field item : list) {
				if ("ro".equalsIgnoreCase(item.getMode()) || item.isUnique()) {
					schema.addField(item);
				}
			}
			schema.setName(schemaParser.getName());
			if (schemaParser.getDesc() != null) {
				schema.setDesc(schemaParser.getDesc());
			}
			schema.persist(instanceDir + File.separator + "schema.xml");
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}

		//create conf file
		if (diHandlers != null) {
			try {
				SolrConfig config = new SolrConfig(getConf());
				config.enableDIH();
				for (HDBDiHandler handler : diHandlers) {
					String dihName = handler.getDihName();
					String dihType = handler.getDihType();
					String confName = "dih-" + dihName + "-" + dihType + ".xml";
					File f = new File(instanceDir + File.separator + confName);
					try {
						Document doc = XmlUtil.parse(handler.getDihContent());
						FileUtil.writeXmlFile(f, doc);
					} catch (Exception ignore) {
						HandlerUtil.removeHandler(getConf(), coreName, dihName);
						continue;
					}

					//create solrconfig.xml
					config.addDataImportHandler(dihName, dihType, confName);
				}
				config.persist(instanceDir + File.separator + "solrconfig.xml");
			} catch (Exception e) {
				e.printStackTrace();
				throw new HurricaneException(e);
			}
		}

		// 1. remove confDir
		LOG.info("Clear core " + coreName);
		ZkConnector.clearConfig(coreName);

		// 2. upload confDir
		LOG.info("Upload core " + coreName);
		ZkConnector.upConfig(instanceDir, coreName);

		// 3. anyway, reload the core
		LOG.info("Reload core " + coreName);
		HSolrAdminClient solrAdminClient = new HSolrAdminClient();
		boolean status = true;
		try {
			solrAdminClient.reloadCore(coreName);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			status = false;
		} finally {
			solrAdminClient.close();
		}

		return status;
	}

	@Override
	public HDBResultSet listHandlers(String coreName, String type) {
		try {
			long start = System.currentTimeMillis();
			String confFile = "/configs/" + coreName + "/solrconfig.xml";
			SolrConfig config = new SolrConfig(getConf(), confFile);
			HDBResultSet result = new HDBResultSet();
			int count = 0;
			for (String handler : config.getDataImportHandlers()) {
				LOG.info("handler " + count + " : " + handler);
				HDBRecord record = new HDBRecord();
				record.put("name", handler);
				result.addItem(record);
				count ++;
			}
			result.setConsumeTime((int)(System.currentTimeMillis() - start));
			result.setTotalCount(count);
			result.setCount(count);
			return result;

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		return "solr";
	}

	@Override
	public JSONObject getInfo() {
		String lives = "/live_nodes";
		long maxMemory = 0;
		long usedMemory = 0;
		boolean isCloud = false;
		String version = "";
		JSONObject info = new JSONObject();
		ZkOperator zkOperator = getConf().getZkOperator();
		try {
			if (zkOperator.isExist(lives)) {
				List<String> nodes = zkOperator.getChild(lives);
				JSONObject nodesObj = new JSONObject();
				for (String node : nodes) {
					String[] arr = node.split("_");
					HSolrAdminClient adminClient = new HSolrAdminClient(arr[0]);
					try {
						JSONObject obj = adminClient.getSystemInfo();
						nodesObj.put(arr[0], obj);
						maxMemory += obj.getJSONObject("memory").getLong("max");
						usedMemory += obj.getJSONObject("memory").getLong("used");
						if (version.length() == 0)
							version = obj.getString("version");
						isCloud = obj.getBoolean("isCloud");
					} finally { 
						adminClient.close();
					}
				}
				info.put("liveNodes", nodesObj);
				info.put("max", maxMemory);
				info.put("used", usedMemory);
				info.put("version", version);
				info.put("isCloud", isCloud);
			}
		} catch (Exception ignore) {}
		return info;
	}

	@Override
	public boolean startHandler(String schemaName, String dihName, Map params) {
		HSolrClient solrClient = new HSolrClient(getConf(), schemaName);
		boolean ret = solrClient.startDataImport(dihName, params);
		solrClient.close();
		return ret;
	}

	@Override
	public HDBResultSet getHandlerStatus(String schemaName, String dihName) {
		HSolrClient solrClient = new HSolrClient(getConf(), schemaName);
		HDBResultSet ret = solrClient.getDataImportStatus(dihName);
		solrClient.close();
		try {
			String content = ZkConnector.getData("/hurricane/dih/" + schemaName + "/" + dihName + ".config");
			((HDBRecord)ret.getItem(0)).put("content", content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public Map<String, ?> getProps() {
		return null;
	}

	@Override
	public JSONObject getSchemaStatus(String coreName) {
		JSONObject ret = null;
		try {
			String path = "/collections/" + coreName + "/state.json";
			if (ZkConnector.exist(path)) {
				String content = ZkConnector.getData("/collections/" + coreName + "/state.json");
				JSONObject stateObj = new JSONObject(content);
				ret = stateObj.getJSONObject(coreName);
			} else {
				String content = ZkConnector.getData("/clusterstate.json");
				JSONObject allStateObj = new JSONObject(content);
				if (allStateObj.has(coreName))
					ret = allStateObj.getJSONObject(coreName);
			}
			if (ret != null) {
				String realCoreName = ret.getJSONObject("shards")
										.getJSONObject("shard1")
										.getJSONObject("replicas")
										.getJSONObject("core_node1")
										.getString("core");
				ret.put("indexStatus", getIndexStatus(realCoreName));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ret;
	}

	private JSONObject getIndexStatus(String coreName) {	
		HSolrAdminClient solrAdminClient = null;
		JSONObject ret = null;
		try {
			solrAdminClient = new HSolrAdminClient();
			ret = solrAdminClient.getIndexStatus(coreName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (solrAdminClient != null)
				solrAdminClient.close();
		}
		
		return ret;
	}
	
	/*
	SolrCore createNewCore(CoreContainer cores, SolrCore core) {
		CoreDescriptor descriptor = core.getCoreDescriptor();
		String oldDir = descriptor.getDataDir();
		String newDir = "";
		File file = new File(oldDir).getParentFile();
		newDir = file.getAbsolutePath() + "/" + "data" + new Date().getTime() + "/";
		File f = new File(newDir);
		if (!f.isDirectory()) {
			f.mkdir();
		}
		SolrConfig solrConfig = null;
		SolrConfig conf = core.getSolrConfig();
		try {
			solrConfig = new SolrConfig(descriptor.getInstanceDir(),
					SolrConfig.DEFAULT_CONF_FILE, null);
			solrConfig.setDataDir(newDir);
		} catch (ParserConfigurationException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		CoreDescriptor newDescriptor = new CoreDescriptor(cores,
				core.getName(), solrConfig.getResourceLoader().getInstanceDir());
		newDescriptor.setDataDir(newDir);
		SolrCore newCore = new SolrCore(core.getName(), newDir, solrConfig,
				core.getSchema(), newDescriptor);
		return newCore;
	}
	 */

}
