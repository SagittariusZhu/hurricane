package org.iipg.hurricane.server;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDBNormalBroker;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HDocument;
import org.iipg.hurricane.pool.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class StoreServer {

	public static String DEFAULT_SCHEMA_NAME = "email";
	private static Logger log = LoggerFactory.getLogger(StoreServer.class);
	
	public static void start(String[] args) {

		Thread thread = new Thread(){
			public void run() {
				HDBBroker broker = null;
				while (true) {
					HDocument doc = (HDocument) Global.QUEUE.poll();
					if (doc != null) {
						String schemaName = doc.getSchemaName();
						if (schemaName == null || schemaName.length() == 0) {
							schemaName = DEFAULT_SCHEMA_NAME;
						}
						try {
							broker = HDBFactory.getHDBBroker(schemaName);
							log.debug(schemaName + " : Loader " + broker);
							broker.store(doc);							
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		thread.start();
	}
}
