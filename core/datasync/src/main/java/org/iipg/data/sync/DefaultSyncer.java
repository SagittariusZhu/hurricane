package org.iipg.data.sync;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;

import org.iipg.data.sync.conf.SyncConfiguration;
import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.ZkOperator;

public class DefaultSyncer implements WorkerConstants {

	public static void main(String[] args) {
		
		String tbName = args[0];
		
		String taskName = args[1];
		
		byte[] content = getContent(tbName, taskName);
		
		if (content == null) {
			System.out.println("Not found sync script for [" + tbName + ":" + taskName + "], exit now!");
			System.exit(1);
		}
		
		InputStream in = new ByteArrayInputStream(content);

		SyncConfiguration conf = new SyncConfiguration(in);

		List<WorkerProp> props = conf.getWorkerProps();

		final long start = System.currentTimeMillis();  
		CyclicBarrier barrier = new CyclicBarrier(props.size(), new Runnable(){  
			@Override  
			public void run() {  
				long end = System.currentTimeMillis();  
				System.out.println("total time : "+(end-start)+"ms");  
			}  
		});  

		for (int i=0; i<props.size(); i++) {
			WorkerProp prop = props.get(i);
			Worker worker = WorkerFactory.newInstance(prop);
			worker.setBarrier(barrier, i);
			
			if (worker != null)
				worker.start();
		}
	}
	
	private static byte[] getContent(String tbName, String taskName) {
		String fullPath = "/hurricane/configs/" + tbName + "/sync/" + taskName + ".xml";
		ZkOperator zkOper = new ZkOperator();
		byte[] content = null;
		
		try {   
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);
			if (zkOper.isExist(fullPath))
				content = zkOper.getData(fullPath);
		} catch (Exception e) {
			e.printStackTrace();
		}  finally {
			zkOper.close();
		}
		
		return content;
	}

}
