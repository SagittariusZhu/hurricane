package org.iipg.hurricane.monitor;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.web.cmd.DefaultCmdServer;

public class HMonitorServer {

	public static void main(String[] args) {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
		
		
		DefaultCmdServer server = new DefaultCmdServer();
		server.setProperty("cmdPrefix", "org.iipg.hurricane.monitor.cmd");
		if (conf.exist("monitor.port"))
			server.setProperty("serverPort", Integer.parseInt(conf.get("monitor.port")));
		if (conf.exist("monitor.context"))
			server.setProperty("appName", conf.get("monitor.context"));
		server.start();
	}
}
