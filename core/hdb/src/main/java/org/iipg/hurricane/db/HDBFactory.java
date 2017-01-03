package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class HDBFactory {

	private static Map<String, HDBBroker> brokers = new HashMap<String, HDBBroker>();
	
	public static synchronized HDBBroker getHDBBroker(String schemaName) throws ParserConfigurationException, SAXException, IOException {
		
		HDBBroker broker = null;
		
		if (brokers.containsKey(schemaName)) {
			broker = brokers.get(schemaName);
			broker.reloadSchema(schemaName);
			return broker;
		}
		
		if (schemaName.startsWith("__sys_")) {
			broker = new HDBSysBroker(schemaName);
		} else { 
			broker = new HDBNormalBroker(schemaName);
		}
		if (broker != null) {
			brokers.put(schemaName, broker);
		}
		
		return broker;
	}
}
