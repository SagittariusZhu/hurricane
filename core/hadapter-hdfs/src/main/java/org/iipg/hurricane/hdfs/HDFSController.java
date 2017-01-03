package org.iipg.hurricane.hdfs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ha.HAServiceProtocol;
import org.apache.hadoop.ha.HAServiceProtocol.HAServiceState;
import org.apache.hadoop.hdfs.DFSUtil;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.hdfs.jmx.HadoopJMXRequest;
import org.iipg.hurricane.hdfs.jmx.HadoopJMXResponse;
import org.iipg.hurricane.hdfs.jmx.response.NameNodeInfo;
import org.iipg.hurricane.hdfs.jmx.response.NameNodeStatus;
import org.iipg.hurricane.jmx.client.JMXClient;
import org.iipg.hurricane.jmx.client.JMXException;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDFSController extends HController {
	
	private static Logger LOG = LoggerFactory.getLogger(HDFSController.class);

	public HDFSController(HurricaneConfiguration conf) {
		super(conf);
		String hadoop_home = System.getProperty("hurricane.home",".");
		System.setProperty("hadoop.home.dir", hadoop_home);
		LOG.info("hadoop.home : " + hadoop_home);
	}

	@Override
	public boolean createSchema(SchemaParser parser) {
		return true;
	}

	@Override
	public boolean deleteSchema(String schemaName, boolean fullDelete) {
		return HDFSSchema.clearSchema(schemaName);
	}

	@Override
	public boolean refreshSchema(SchemaParser parser) {
		return true;
	}

	@Override
	public String getName() {
		return "hdfs";
	}

	@Override
	public JSONObject getInfo() {
		try {
			JMXClient client = new JMXClient(getConf().get("hdfs.info.url"));
			HadoopJMXRequest req = new HadoopJMXRequest.NameNodeInfo();
			HadoopJMXResponse resp = client.execute(req);
			
			NameNodeInfo info = new NameNodeInfo(resp);
			
			String version = info.getVersion();
			long used = info.getUsed();
			long max = info.getCapacity();
			float usage = (float) (used * 100.0 / max);
			System.out.println("Memory usage: used: " + used + 
					" / max: " + max + " = " + usage + "%" +
					"\nVersion: " + version);
			
			JSONObject obj = info.asJSONObject();
			
			req = new HadoopJMXRequest.NameNodeStatus();
			resp = client.execute(req);
			
			NameNodeStatus status = new NameNodeStatus(resp);
			obj.put("nnStatus", status.getStatus());
			String name = HDFSSchema.getNameServices();
			if (name.startsWith("ha-hdfs"))
				name = name.substring(8);
			obj.put("nameservices", name);
			Collection<String> nnIds = DFSUtil.getNameNodeIds(HDFSSchema.getConf(), 
					DFSUtil.getNamenodeNameServiceId(HDFSSchema.getConf()));
			JSONObject nnObj = new JSONObject();
			
			for (String nnId : nnIds) {
				NNHAServiceTarget target = (NNHAServiceTarget) HDFSSchema.resolveTarget(nnId);
				HAServiceProtocol proto = target.getProxy(
						HDFSSchema.getConf(), HDFSSchema.getRpcTimeoutForChecks());
				HAServiceState state = proto.getServiceStatus().getState();
				nnObj.put(target.getAddress() + "", state.toString());
			}
			obj.put("nameNodes", nnObj);
			
			ZkOperator zkOperator = getConf().getZkOperator();
			try {
				String activeNN = "/hadoop-ha/" + name + "/ActiveBreadCrumb";
				String standbyNN = "/hadoop-ha/" + name + "/ActiveStandbyElectorLock";
				if (zkOperator.isExist(activeNN)) {
					byte[] data = zkOperator.getData(activeNN);
					
				}
			} catch (Exception ignore) {}
			return obj;
			
		} catch (JMXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return new JSONObject();
		//return HDFSSchema.getInfo();
	}

	@Override
	public HDBResultSet listHandlers(String coreName, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createHandler(String schemaName, String dihName,
			String dihType, String content) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startHandler(String schemaName, String dihName, Map params) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HDBResultSet getHandlerStatus(String schemaName, String dihName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createHandlers(String schemaName, List<HDBDiHandler> handlers) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private static String[] dispPropsName = {
		"dfs.blocksize",
		"dfs.datanode.address",
		"dfs.datanode.data.dir",
		"dfs.datanode.http.address",
		"dfs.datanode.ipc.address",
		"dfs.ha.automatic-failover.enabled",
		"dfs.journalnode.edits.dir",
		"dfs.journalnode.http-address",
		"dfs.journalnode.rpc-address",
		"dfs.namenode.http-address.hurricanecluster.nn1",
		"dfs.namenode.http-address.hurricanecluster.nn2",
		"dfs.ha.namenodes.hurricanecluster",
		"dfs.namenode.name.dir",
		"dfs.namenode.rpc-address.hurricanecluster.nn1",
		"dfs.namenode.rpc-address.hurricanecluster.nn2",
		"dfs.namenode.shared.edits.dir",
		"dfs.nameservices",
		"dfs.permissions.enabled",
		"fs.defaultFS",
		"ha.zookeeper.parent-znode",
		"ha.zookeeper.quorum",
		"hadoop.tmp.dir",
		"io.map.index.skip",
		"io.seqfile.compress.blocksize"
	};
	
	@Override
	public Map<String, ?> getProps() {
//		Map<String, Object> innerMap = new HashMap<String, Object>();
//		for (Iterator it = HDFSSchema.getConf().iterator(); it.hasNext(); ) {
//			Entry<String, ?> entry = (Entry<String, ?>) it.next();
//			String key = entry.getKey();
//			Object value = entry.getValue();
//			innerMap.put(key, value);
//		}
		Configuration conf = HDFSSchema.getConf();
		
		Map<String, Object> ret = new HashMap<String, Object>();
		for (String propName : dispPropsName) {
			String key = propName.trim();
			ret.put("hdfs." + key, conf.get(key));
		}
		return ret;
	}

	@Override
	public JSONObject getSchemaStatus(String coreName) {
		// TODO Auto-generated method stub
		return null;
	}

}
