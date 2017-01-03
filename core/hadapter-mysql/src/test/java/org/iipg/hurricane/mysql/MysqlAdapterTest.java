package org.iipg.hurricane.mysql;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.*;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HDBUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class MysqlAdapterTest {
	
	private static final String SCHEMA_NAME = "email2";
	public static final Log LOG = LogFactory.getLog(MysqlAdapterTest.class);
	
	private HurricaneConfiguration conf;
	private HAdapter adapter;
	private SchemaParser schema;

	@Before
	public void init() {
		conf = new HurricaneConfiguration();
		try {
			adapter = new MysqlAdapter(conf, SCHEMA_NAME);
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
		try {
			schema = new SchemaParser(SCHEMA_NAME);
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
	public void destroy() {
		adapter.close();
	}
	
	@Test
	public void store() {
		HDBRecord record = new HDBRecord();
		record.put("messageid", "2");
		record.put("reader", "edward");
		record.put("readtime", new Date(System.currentTimeMillis()));
		try {
			assertEquals(1, adapter.store(record));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void query() {
		HDBQuery q = new MysqlQuery();
		
		q.setSchemaName(SCHEMA_NAME);
		q.setSchema(schema);
		
		q.addSelectField("messageid");
		q.addSelectField("reader");
		q.addSelectField("readtime");
		
		HDBCondition cond = null;
		
//		cond = new HDBEqualCondition("reader", "edward");
//		q.addWhereClause(cond);
//		
//		cond = new HDBGreatCondition("readtime", "2014-01-01");
//		q.addWhereClause(cond);
		
		cond = new HDBGreatOrEqualCondition("readtime", "2014-06-12 15:43:00");
		q.addWhereClause(cond);
		
//		cond = new HDBLessCondition("readtime", "2014-01-01 10:01:01");
//		q.addWhereClause(cond);
		
//		cond = new HDBLessOrEqualCondition("readtime", "2014-07-05 10:01:01");
//		q.addWhereClause(cond);
		
//		cond = new HDBSpanCondition("reader", "a", "zzzzzz", false, false);
//		q.addWhereClause(cond);
//
//		cond = new HDBSpanCondition("readtime", "2014-01-01", "2014-10-01", false, false);
//		q.addWhereClause(cond);
		
//		cond = new HDBLikeCondition("reader", "zhu%");
//		q.addWhereClause(cond);

//		List<String> r = new ArrayList<String>();
//		r.add("zhuhy");
//		r.add("edward");
//		cond = new HDBInCondition("reader", r);
//		q.addWhereClause(cond);
		
		LOG.info(q.getSql());
		
		HDBResultSet rset = adapter.query(q);
		
		assertNotNull(rset);

		LOG.info("Got " + rset.getCount() + " row(s).");
		HDBUtil.printResultSet(rset);
		
	}
}
