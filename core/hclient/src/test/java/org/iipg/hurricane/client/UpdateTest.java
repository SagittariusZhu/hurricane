package org.iipg.hurricane.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.iipg.hurricane.client.metadata.HCriteria;
import org.iipg.hurricane.client.metadata.HDocument;
import org.iipg.hurricane.client.metadata.HDocumentList;
import org.iipg.hurricane.client.metadata.HQuery;
import org.iipg.hurricane.client.metadata.HQueryFactory;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UpdateTest {
	private static final String SCHEMA_NAME = "mytest";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private HConnection conn = null;

    @Before
    public void init() {
        try {
        	conn = HConnFactory.getInstanceByConf("config.client");
        } catch (HConnException e) {
        	System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        fail(conn);
    }
    
    @After
    public void destory() {
    	if (conn != null) {
    		conn.close();
    		conn = null;
    	}
        System.runFinalization();
        System.gc();
    }
    
	@Test
	public void addDocs() {
	    HDocumentList docs = new HDocumentList();
	    
	    HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    
//	    doc.setField("messageid", "abc");
//	    doc.setField("mstr", "58同城简历");
//	    doc.setField("mint", 12);
	    
		doc.setField("messageid", UUID.randomUUID().toString());
		doc.addField("fromaddr","58同城简历 <mailservice@58.com>");
		doc.setField("toaddr","wwang369@163.com");
		doc.setField("senddate", new Date(System.currentTimeMillis()));
//		//doc.setField("mint", 12);
		doc.addField("subject","会计/会计师-（现职位）会计/会计师-58.com");
		doc.setField("content","应聘职位：会计/会计师。 请通过简历中的电话或邮件联系我。（请勿直接回复邮件）");
		doc.setField("content_entity","会计,会计师");
		doc.setField("attachnames","简历.doc,照片.jpeg");
//		doc.setField("attach","我是一个对生活充满希望、积极向上的青年,对待朋友能以诚相待," +
//				"对工作态度执着、细心、责任心强、勤奋好学、认真负责、吃苦耐劳、勇于迎接新挑战,能够快速进入团队角色、配合团队完成目标");
//		doc.setField("reader","勤奋好学的人");
//		doc.setField("readtime", new Date(System.currentTimeMillis()));
		//doc.setField("readtime", "abc");
		
//	    doc.setField("mstr", "我是一个对生活充满希望、积极向上的青年");
//	    doc.setField("mint", 24);
	    
//		doc.addField("file", "C:/runtime/exports/IMG_7977.JPG");
//		
//		//二进制
		byte[] buf = "58同城简历 <mailservice@58.com> wwang369@163.com 2011-11-16T04:43:48Z 会计/会计师-（现职位）会计/会计师-58.com应聘职位：会计/会计师。 请通过简历中的电话或邮件联系我。（请勿直接回复邮件）".getBytes();	
		doc.setBField("emaildata", buf);
		buf = "shard是对完整文档集索引".getBytes();	
		doc.addBField("attachmentdata", buf);
		buf = "Core: 也就是Solr Core,一个Solr中包含一个或者多个Solr Core".getBytes();	
		doc.addBField("attachmentdata", buf);
	    
	    docs.add(doc);
	    
	    try {
	        //add docs
	        UpdateResponse response = conn.add(docs);
	        fail(response);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	public void update() {
		HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    
//	    doc.setField("messageid", "646f468f-d131-4b71-ab5c-7b806598841c");
//	    doc.setField("subject", "对生活充满希望abc");
//	    doc.setField("content", "责任心强、勤奋好学、认真负责");
//	    doc.addField("toaddr", "abc@gmail.com");
//	    doc.setField("reader", "nannan123");
//		doc.setField("readtime", new Date(System.currentTimeMillis()));
//		doc.setField("mint", 12);
		
		doc.setField("id", 2000000);
	    doc.setField("title", "58同城简历");
	    doc.setField("files", 1);
	    doc.setField("subcats", 25);

	    try {
	    	UpdateResponse response = conn.update(doc);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	public void updateByQuery() {
		String qStr = "会计";
    	HCriteria crit = new HCriteria();
    	crit.setQStr(qStr);
    	HQuery query = HQueryFactory.newQuery(SCHEMA_NAME, crit);
    	
		HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);	    
	    doc.setField("reader", "nannan123");
		doc.setField("readtime", new Date(System.currentTimeMillis()));
		doc.setField("mint", 12);
	    try {
	    	UpdateResponse response = conn.update(doc, query);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Test
	public void deleteByQuery() {
		String qStr = "*:*";
    	HCriteria crit = new HCriteria();
    	crit.setQStr(qStr);
    	HQuery query = HQueryFactory.newQuery(SCHEMA_NAME, crit);
    	
	    try {
	    	UpdateResponse response = conn.delete(query);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
    private final void fail(Object o) {
        Assert.assertNotNull(o);
    }
}
