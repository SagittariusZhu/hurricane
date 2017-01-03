package org.iipg.hurricane.hdfs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HDFSAdapterTest {
	private static final String SCHEMA_NAME = "email";
	public static final Log LOG = LogFactory.getLog(HDFSAdapterTest.class);
	
	private HurricaneConfiguration conf;
	private HAdapter adapter;
	
	@Before
	public void init() {
		conf = new HurricaneConfiguration();
		try {
			adapter = new HDFSAdapter(conf, SCHEMA_NAME);
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
		assertNotNull(adapter);
	}
	
	@After
	public void destory() {
		if (adapter != null)
			adapter.close();
	}
	
	@Test
	public void store() {
		HDBBlob record = new HDBBlob();
		byte[] buf = "58同城简历 <mailservice@58.com> wwang369@163.com 2011-11-16T04:43:48Z 会计/会计师-（现职位）会计/会计师-58.com应聘职位：会计/会计师。 请通过简历中的电话或邮件联系我。（请勿直接回复邮件）".getBytes();	
		record.setBlob(buf);
		try {
			assertEquals(1, adapter.store(record));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void query() {
		//String uuid = "edbaad7d-12d1-4cdb-af9f-923688da0ed1";
		String uuid = "edbaad7d-12d1-4cdb-af9f-923688da0ed2";
		try {
			HDBBlob record = (HDBBlob) adapter.queryByID(uuid, null);
			assertNotNull(record.getBlob());
			System.out.println(new String(record.getBlob(), "utf-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
