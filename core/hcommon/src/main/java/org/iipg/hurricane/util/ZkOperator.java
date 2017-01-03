/**
 * ZooKeeperOperator.java
 * 版权所有(C) 2013 
 * 创建:cuiran 2013-01-16 15:03:40
 */
package org.iipg.hurricane.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.iipg.hurricane.HurricaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * @author cuiran
 * @version TODO
 */
public class ZkOperator implements Watcher {

	private static Logger log = LoggerFactory.getLogger(ZkOperator.class);
	private static final int SESSION_TIME = 2000;   

	protected static ZooKeeper zookeeper;
	protected static CountDownLatch countDownLatch = null;
	
	private String hosts = "";
	
	public ZkOperator() {}
	
	public ZkOperator(String hosts) {
		this.hosts = hosts;
	}
	
	public void setHosts(String hosts) { this.hosts = hosts; }
	public String getHosts() { return this.hosts; }

	/* (non-Javadoc)
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		if(event.getState() == KeeperState.SyncConnected){
			if (countDownLatch != null)
				countDownLatch.countDown();
		} else if (event.getState() == KeeperState.Expired) {
            System.out.println("[SUC-CORE] session expired. now rebuilding");

            //session expired, may be never happending.
            //close old client and rebuild new client
            close();

            getZooKeeper(this.hosts);
        }
	}

	public void close() {   
		log.info("watch ZK Client close.");
	    if (zookeeper != null) {
	        try {
	            zookeeper.close();
	        } catch (InterruptedException e) {}
            zookeeper = null;
	    }
	}  

	public static ZooKeeper getZooKeeper(String hosts) {
		if (zookeeper == null) {
			synchronized (ZkOperator.class) {
				if (zookeeper == null) {
					countDownLatch = new CountDownLatch(1);
					long startTime = System.currentTimeMillis();
					try {
						zookeeper = new ZooKeeper(hosts, SESSION_TIME, new ZkOperator());
						countDownLatch.await(2, TimeUnit.SECONDS);
					} catch (Exception e) {
						e.printStackTrace();
						throw new HurricaneException(e.getClass().getName() + ":" + e.getMessage());
					} finally {
						log.info("Build ZK Client use " + (System.currentTimeMillis() - startTime) + " ms.");
						countDownLatch = null;
					}
				}
			}
		}
		return zookeeper;
	}
	
	public static String getVersion() {
		String version = "";
		try {
			Manifest mf = FileUtil.findManifest(ZooKeeper.class);
			Attributes mainAttributes = mf.getMainAttributes();
			version = mainAttributes.getValue("Implementation-Title") + " "
					+ mainAttributes.getValue("Implementation-Version");
			return version;
		} catch (Exception ignore) {
			log.info("not found Zookeeper version information.");
		}
		return version;
	}
	
	//----------------------------------------
	public boolean create(String path, byte[] data) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			String[] arr = path.split("\\/");
			StringBuffer buf = new StringBuffer();
			for (int i=0; i<arr.length-1; i++) {
				String item = arr[i];
				if (item.length() > 0) {
					buf.append("/").append(item);
					if (!isExist(buf.toString())) {
						zk.create(buf.toString(), null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				}
			}
			/**
			 * PERSISTENT: The znode will not be deleted upon the client's disconnect.
			 */ 
			zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			return true;
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}

	public boolean update(String path, byte[] data) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			if (isExist(path))
				zk.setData(path, data, -1);
			else
				create(path, data);
			return true;
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}

	public boolean isExist(String path) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			if (zk.exists(path, false) != null)
				return true;
			return false;
		} catch (Exception e) {
			close();
			throw new HurricaneException(e.getClass().getName() + ":" + e.getMessage());
		}
	}
	
	public boolean remove(String path) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			if (hasChild(path)) {
				return false;
			}
			zk.delete(path, -1);
			return true;
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}

	public boolean hasChild(String path) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			Stat stat = zk.exists(path, false);
			return (stat.getNumChildren() > 0);
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}

	public List<String> getChild(String path) throws HurricaneException {   
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			List<String> list = zk.getChildren(path, false);
			return list;
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}

	public byte[] getData(String path) throws HurricaneException {
		ZooKeeper zk = getZooKeeper(this.hosts);
		try {
			return zk.getData(path, false, null);   
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}
	}  

	public static void main(String[] args) {
		try {   
			ZkOperator zkoperator = new ZkOperator();   
			zkoperator.setHosts("sr1,sr2,sr3");

			byte[] data = new byte[]{'a','b','c','d'};   

			zkoperator.create("/hurricane/schema",null);   
			//		            System.out.println(Arrays.toString(zkoperator.getData("/root")));   
			//		               
			//		            zkoperator.create("/root/child1",data);   
			//		            System.out.println(Arrays.toString(zkoperator.getData("/root/child1")));   
			//		               
			//		            zkoperator.create("/root/child2",data);   
			//		            System.out.println(Arrays.toString(zkoperator.getData("/root/child2")));   

			String zktest="ZooKeeper的Java API测试";
			Thread.sleep(30*1000);
			if (!zkoperator.isExist("/hurricane/schema/child3"))
				zkoperator.create("/hurricane/schema/child3", zktest.getBytes("utf-8"));
			else
				zkoperator.update("/hurricane/schema/child3", zktest.getBytes("utf-8"));

			//System.out.println("获取设置的信息：");
			//System.out.println(new String(zkoperator.getData("/hurricane/child3/adfa"), "utf-8"));

			System.out.println("节点孩子信息:");   
			List<String> children = zkoperator.getChild("/hurricane");
			for (String child : children) {
				System.out.println(child);
				if (zkoperator.remove("/hurricane/" + child)) {
					System.out.println("remove OK!");
				} else {
					System.out.println("remove failed!");					
				}
			}

			zkoperator.close();   


		} catch (Exception e) {   
			e.printStackTrace();   
		}   

	}
}
