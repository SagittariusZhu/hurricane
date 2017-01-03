package org.iipg.hurricane.server.inner;

import java.util.HashMap;
import java.util.Map;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.util.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeltaRunThread {

	private static Logger LOG = LoggerFactory.getLogger(DeltaRunThread.class);
	
	private HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	
	private String coreName = "";
	private String dihName = "";
	private String importCmd = "";
	private int interval = 0;
	private boolean interrupt = false;

	public DeltaRunThread(String coreName) {
		this.coreName = coreName;
	}
	public void setDihName(String dihName) { this.dihName = dihName; }
	public void setImportCmd(String importCmd) { this.importCmd = importCmd; }
	public void setInterval(int interval) { this.interval = interval; }

	public String getCoreName() { return this.coreName; }
	public String getDihName() { return this.dihName; }
	public String getImportCmd() { return this.importCmd; }
	public int getInterval() { return this.interval; }

	public void start() {
		Thread thread = new Thread(){
			public void run() {
				Map params = new HashMap();
				params.put("command", importCmd);
				params.put("clean", false);
				params.put("commit", true);
				params.put("wt", "json");
				params.put("indent", true);
				params.put("verbose", false);
				params.put("optimize", false);
				params.put("debug", false);
				while (!interrupt) {
					HandlerUtil.setStatus(conf, coreName, dihName, 
							"delta import with command=" + importCmd);

					HandlerUtil.runHandler(coreName, "DIH", dihName, params);
					try {
						Thread.sleep(interval * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}
	
	public boolean isBusy() {
		return !this.interrupt;
	}
	
	public void stop() {
		this.interrupt = true;
	}

}
