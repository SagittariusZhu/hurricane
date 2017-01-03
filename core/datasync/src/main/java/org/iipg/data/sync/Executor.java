package org.iipg.data.sync;

/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.iipg.data.sync.util.DSyncUtil;
import org.iipg.hurricane.util.FileUtil;

public class Executor
implements Watcher, Runnable, DataMonitor.DataMonitorListener {

	private String znode;

	private DataMonitor dm;

	private ZooKeeper zk;

	private String filename;

	private List<String> args = new ArrayList<String>();

	private Process child;

	public Executor(String hostPort) throws KeeperException, IOException {
		this.znode = DSyncUtil.DATA_SYNC_CONFIG;

		zk = new ZooKeeper(hostPort, 3000, this);
		dm = new DataMonitor(zk, znode, null, this);

		args.add("java");
		args.add("-classpath");
		args.add(System.getProperty("java.class.path"));
		args.add("org.iipg.data.sync.DefaultSyncer");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = FileUtil.readPropertiesFile("zk.properties");
		String zkServerAddress = props.getProperty("zk.host.endpoints");
		try {
			new Executor(zkServerAddress).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * We do process any events ourselves, we just need to forward them on.
	 *
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
	 */
	public void process(WatchedEvent event) {
		dm.process(event);
	}

	public void run() {
		try {
			synchronized (this) {
				while (!dm.dead) {
					wait();
				}
			}
		} catch (InterruptedException e) {
		}
	}

	public void closing(int rc) {
		synchronized (this) {
			notifyAll();
		}
	}

	static class StreamWriter extends Thread {
		OutputStream os;

		InputStream is;

		StreamWriter(InputStream is, OutputStream os) {
			this.is = is;
			this.os = os;
			start();
		}

		public void run() {
			byte b[] = new byte[80];
			int rc;
			try {
				while ((rc = is.read(b)) > 0) {
					os.write(b, 0, rc);
				}
			} catch (IOException e) {
			}

		}
	}

	public void exists(byte[] data) {
		if (data == null) {
			if (child != null) {
				System.out.println("Killing process");
				child.destroy();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
				}
			}
			child = null;
		} else {
			if (child != null) {
				System.out.println("Stopping child");
				child.destroy();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			try {
				String content = new String(data, "utf-8");
				Map<String, Map<String, SyncTask>> allItems = SyncTask.parse(content);
				int count = 0;
				int forkCount = 0;
				for (Iterator it = allItems.keySet().iterator(); it.hasNext(); ) {
					String tbName = (String) it.next();
					Map<String, SyncTask> subItems = allItems.get(tbName);
					for (Iterator subIt = subItems.keySet().iterator(); subIt.hasNext(); ) {
						String taskName = (String) subIt.next();
						SyncTask task = subItems.get(taskName);
						if (task.getCrons().equals("active")) {
							forkCount ++;
							forkChild(tbName, taskName);
						}
					}
					count += subItems.size();
				}
				System.out.println("Watch " + count + " tasks, forking " + forkCount + " tasks.");


			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void forkChild(String tbName, String taskName) throws IOException {
		List<String> currArgs = new ArrayList<String>();
		currArgs.addAll(args);
		currArgs.add(tbName);
		currArgs.add(taskName);
		String[] exec = currArgs.toArray(new String[0]);
		child = Runtime.getRuntime().exec(exec);
		new StreamWriter(child.getInputStream(), System.out);
		new StreamWriter(child.getErrorStream(), System.err);
	}
}