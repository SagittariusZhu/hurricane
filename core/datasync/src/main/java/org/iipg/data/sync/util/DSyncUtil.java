package org.iipg.data.sync.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.iipg.data.sync.SQLGetter;
import org.iipg.data.sync.SyncTask;
import org.iipg.data.sync.conf.SyncConfiguration;
import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONArray;
import org.json.JSONObject;

public class DSyncUtil {

	public static final String DEFAULT_LIMITS = " LIMIT 0, 10";

	public static final String DATA_SYNC_CONFIG = "/hurricane/datasync.json";

	private static Properties props = FileUtil.readPropertiesFile("zk.properties");

	public static boolean runTest(String content) {

		boolean ret = false;

		InputStream in = null;

		try {
			in = new ByteArrayInputStream(content.getBytes("UTF-8"));

			SyncConfiguration conf = new SyncConfiguration(in);

			List<WorkerProp> props = conf.getWorkerProps();

			for (int i=0; i<props.size(); i++) {
				WorkerProp prop = props.get(i);
				SQLGetter getter = new SQLGetter(prop.getSourceDS());
				JSONArray values = getter.get(prop.getQStr() + DEFAULT_LIMITS);

				// for each record do
				for (int j=0; j<values.length(); j++) {
					JSONObject obj = values.getJSONObject(j);
					String info = String.format("No.%s: %s", j+1, obj);
					System.out.println(info);
				}

				// store data to remote server
				int saveCount = StoreUtil.store(prop, values);

				if (values.length() == saveCount) {
					System.out.println("store records successful!");
					ret = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	public static boolean removeSyncTask(SyncTask task) {
		return false;
	}

	public static boolean updateSyncTask(SyncTask task) {
		String tbName = task.getTableName();
		String taskName = task.getName();

		String zkServerAddress = props.getProperty("zk.host.endpoints");
		ZkOperator zk = new ZkOperator(zkServerAddress);
		if (zk != null) {
			try {
				if (zk.isExist(DATA_SYNC_CONFIG)) {
					String content = "";
					content = new String(zk.getData(DATA_SYNC_CONFIG), "utf-8");
					Map<String, Map<String, SyncTask>> allItems = SyncTask.parse(content);
					Map<String, SyncTask> subItems = null;
					if (allItems.containsKey(tbName)) {
						subItems = allItems.get(tbName);
					} else {
						subItems = new HashMap();
					}
					subItems.put(taskName, task);
					zk.update(DATA_SYNC_CONFIG, SyncTask.format(allItems).getBytes("utf-8"));
				} else {
					Map<String, Map<String, SyncTask>> allItems = new HashMap();
					Map<String, SyncTask> subItems = new HashMap();
					subItems.put(taskName, task);
					allItems.put(tbName, subItems);
					zk.create(DATA_SYNC_CONFIG, SyncTask.format(allItems).getBytes("utf-8"));
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (HurricaneException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				zk.close();
			}
		}
		return false;
	}

	public static SyncTask getSyncTask(String tbName, String taskName) {
		SyncTask ret = null;
		
		String zkServerAddress = props.getProperty("zk.host.endpoints");
		ZkOperator zk = new ZkOperator(zkServerAddress);
		
		if (zk != null) {
			try {
				String content = new String(zk.getData(DATA_SYNC_CONFIG), "utf-8");
				Map<String, Map<String, SyncTask>> allItems = SyncTask.parse(content);
				if (allItems.containsKey(tbName)) {
					Map<String, SyncTask> subItems = allItems.get(tbName);
					if (subItems.containsKey(taskName)) {
						ret = subItems.get(taskName);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				zk.close();
			}
		}
		return ret;
	}
}
