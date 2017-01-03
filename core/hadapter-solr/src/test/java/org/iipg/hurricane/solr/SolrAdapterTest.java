package org.iipg.hurricane.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.*;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SolrAdapterTest {

	private static final String SCHEMA_NAME = "mytest";
	public static final Log LOG = LogFactory.getLog(SolrAdapterTest.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private HurricaneConfiguration conf;
	private HAdapter adapter;
	private SchemaParser schema;
	
	@Before
	public void init() {
		conf = new HurricaneConfiguration();
		try {
			adapter = new SolrAdapter(conf, SCHEMA_NAME);
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
	public void destory() {
		if (adapter != null)
			adapter.close();
	}
	
	@Ignore
	@Test
	public void store() throws ParseException {
		HDBRecord record = new HDBRecord();
		record.put("messageid", "2");
		record.put("fromaddr", "58同城简历 <mailservice@58.com>");
		record.put("toaddr","wwang369@163.com");
		record.put("senddate", sdf.parse("2010-11-16 18:00:23"));
		record.put("subject","会计/会计师-（现职位）会计/会计师-58.com");
		record.put("content","应聘职位：会计/会计师。 请通过简历中的电话或邮件联系我。（请勿直接回复邮件）");
		record.put("attachnames","简历.doc,照片.jpeg");
		record.put("attach","我是一个对生活充满希望、积极向上的青年,对待朋友能以诚相待,对工作态度执着、细心、责任心强、勤奋好学、认真负责、吃苦耐劳、勇于迎接新挑战,能够快速进入团队角色、配合团队完成目标");

		try {
			assertEquals(1, adapter.store(record));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void updateField() {
		HDBRecord record = new HDBRecord();
		record.put("messageid", "646f468f-d131-4b71-ab5c-7b806598841c");
		Map<String, String> setOper = new HashMap<String, String>();
		setOper.put("set", "iPod &amp; iPod Mini USB 2.0 Cable");
		record.put("subject", setOper);
		try {
			adapter.store(record);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void query() {
		
		HDBQuery q = new SolrQuery();
		
		q.setSchemaName(SCHEMA_NAME);
		q.setSchema(schema);
		
		q.addSelectField("fromaddr");
		q.addSelectField("toaddr");
		q.addSelectField("senddate");
		q.addSelectField("subject");
		q.addSelectField("content");
		
		HDBCondition cond = null;
		
//		cond = new HDBEqualCondition("senddate", "2011-11-16 04:43:48");
//		q.addWhereClause(cond);
		
//		cond = new HDBGreatCondition("senddate", "2009-11-24");
//		q.addWhereClause(cond);
		
//		cond = new HDBGreatOrEqualCondition("senddate", "2014-01-01 10:01:01");
//		q.addWhereClause(cond);
		
//		cond = new HDBLessCondition("senddate", "2014-01-01 10:01:01");
//		q.addWhereClause(cond);
		
//		cond = new HDBLessOrEqualCondition("senddate", "2014-07-05 10:01:01");
//		q.addWhereClause(cond);
		
//		cond = new HDBSpanCondition("subject", "a", "zzzzzz", false, false);
//		q.addWhereClause(cond);

//		cond = new HDBSpanCondition("senddate", "2014-01-01", "2014-10-01", false, false);
//		q.addWhereClause(cond);
		
		cond = new HDBLikeCondition("subject", "会计");
		q.addWhereClause(cond);

//		List<String> r = new ArrayList<String>();
//		r.add("zhuhy");
//		r.add("edward");
//		cond = new HDBInCondition("reader", r);
//		q.addWhereClause(cond);
		
		LOG.info(q.getSql());
		
		HDBResultSet rset = adapter.query(q);
		
		assertNotNull(rset);

		LOG.info("Got " + rset.getCount() + " row(s).");
		
	}
}
