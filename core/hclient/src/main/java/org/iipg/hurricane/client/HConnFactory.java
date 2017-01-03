package org.iipg.hurricane.client;

import org.iipg.hurricane.client.impl.ICEConnection;

public class HConnFactory {

	public static HConnection getInstance(String serverUrl) throws HConnException {
		HConnection conn = new ICEConnection(serverUrl, "DataReceiver");
		return conn;
	}
	
	public static HConnection getInstanceByConf(String configName) throws HConnException {
		HConnection conn = new ICEConnection(configName);
		return conn;
	}
}
