package org.iipg.hurricane.solr.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths; 
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkConfigManager;
import org.apache.zookeeper.KeeperException;
import org.iipg.hurricane.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkConnector {
	private static Logger LOG = LoggerFactory.getLogger(ZkConnector.class);
	
	private static final String SCHEMA_XML = "schema.xml";
	private static final String ZKHOST = "zk.host.endpoints";

	private static SolrZkClient zkClient = null;
	private static ZkConfigManager confManager = null;
	private static String zkServerAddress = "";
	
	
	static {
		Properties props = FileUtil.readPropertiesFile("zk.properties");
		zkServerAddress = props.getProperty(ZKHOST);

		zkClient = new SolrZkClient(zkServerAddress, 30000, 30000,
				new OnReconnect() {
			@Override
			public void command() {}
		});
		
		confManager = new ZkConfigManager(zkClient);
	}

	public static boolean upConfig(String confDir, String confName) {
		try {
			if(!ZkController.checkChrootPath(zkServerAddress, true)) {
				System.out.println("A chroot was specified in zkHost but the znode doesn't exist. ");
				return false;
			}
			
			String zkConfPath = "/configs/" + confName + "/" + SCHEMA_XML;
			if (zkClient.exists(zkConfPath, true)) {
				zkClient.makePath(zkConfPath, new File(confDir, SCHEMA_XML), false, true);
			} else {
				confManager.uploadConfigDir(Paths.get(confDir), confName);
			}
			return true;
			
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static boolean downloadConfig(String confDir, String confName) {
		try {
			confManager.downloadConfigDir(confName, Paths.get(confDir));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return false;
	}

	public static boolean linkConfig(String collection, String confName) {
		try {
			ZkController.linkConfSet(zkClient, collection, confName);
			return true;
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static boolean clearConfig(String confName) {
		try {
			deletePath("/configs/" + confName);
			return true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private static void deletePath(String path) throws KeeperException, InterruptedException {
		List<String> files = zkClient.getChildren(path, null, true);
		if (files == null || files.size() == 0) {
			zkClient.delete(path, -1, true);
			LOG.info("Delete Path: " + path);
		} else {
			for (String item : files) {
				deletePath(path + "/" + item);
			}
			zkClient.delete(path, -1, true);
			LOG.info("Delete Path: " + path);
		}
	}
	

	public static String getData(String path) throws Exception {
		byte[] raw = zkClient.getData(path, null, null, true);
		return new String(raw, "utf-8");
	}

	public static boolean exist(String path) throws Exception {
		return zkClient.exists(path, true);
	}
}