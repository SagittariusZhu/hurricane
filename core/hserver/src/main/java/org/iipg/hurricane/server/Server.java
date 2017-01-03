package org.iipg.hurricane.server;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.server.inner.HBrokerImpl;
import org.iipg.hurricane.util.ServerInfo;

import Ice.Application;

public class Server extends Application {

	@Override
	public int run(String[] args) {        
		//start ice server
		Ice.ObjectAdapter adapter = communicator().createObjectAdapter("DataReceiverAdapter");
		adapter.add(new HBrokerImpl(), communicator().stringToIdentity("DataReceiver"));
        adapter.activate();
        communicator().waitForShutdown();
        
		return 0;
	}
	
	/**
	 * Main entry
	 * @param args
	 */
	public static void main(String[] args) {
		
		ServerInfo.setStartTime(System.currentTimeMillis());
		
//        if (!ExtClassLoader.loadClasspath()) {
//        	return;
//        }
        
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
		
		//start jetty server
		(new ManageServer()).start(conf);
		
		//start store thread
		//StoreServer.start(args);
        
		Server app = new Server();
        int status = app.main("Server", args, "config.server");
        System.exit(status);
	}
}
