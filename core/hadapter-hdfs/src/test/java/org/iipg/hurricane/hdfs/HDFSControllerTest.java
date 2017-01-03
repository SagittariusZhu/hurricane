package org.iipg.hurricane.hdfs;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HDFSControllerTest {
	private HDFSController controller = null;
	
	@Before
	public void init() {
		HurricaneConfiguration conf = new HurricaneConfiguration();
		controller = new HDFSController(conf);
	}
	
	@Test
	public void run() {
		String schemaFile = "wang002";
		SchemaParser parser = null;
		try {
			parser = new SchemaParser(schemaFile);
			controller.createSchema(parser);
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
	}
	
	@Test
	public void delete() {
		String coreName = "wang002";
		controller.deleteSchema(coreName, false);
	}

}
