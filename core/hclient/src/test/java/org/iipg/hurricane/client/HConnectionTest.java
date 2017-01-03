package org.iipg.hurricane.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.response.StatusResponse;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.iipg.hurricane.client.util.AssistantTool;
import org.iipg.hurricane.client.metadata.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

import com.google.common.net.InetAddresses;

public class HConnectionTest {
	private static final String SCHEMA_NAME = "mytest";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private HConnection conn = null;

    @Before
    public void init() {
        try {
        	conn = HConnFactory.getInstanceByConf("config.client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @After
    public void destory() {
    	conn.close();
        conn = null;
        System.runFinalization();
        System.gc();
    }


    @Test
    public void query() throws ParseException {
        HCriteria crit = new HCriteria();
		crit.addSelectField("messageid");
		crit.addSelectField("fromaddr");
		crit.addSelectField("toaddr");
		crit.addSelectField("senddate");
		crit.addSelectField("subject");
		crit.addSelectField("reader");
		crit.addSelectField("readtime");
		crit.addSelectField("attachmentdata");

//		crit.addLike("fromaddr", "mailservice@58.com");
//		crit.addLike("content", "会计");

//		crit.addGreaterOrEqualThan("readtime", sdf.parse("2014-07-15 15:43:00"));	
		crit.addOrderByDescending("readtime");

		crit.setQStr("+reader:(user007 zhu) +readtime:[2013-01-01 TO 2015-01-01]");
		crit.setRowCount(1);
		
        HQuery query = HQueryFactory.newQuery(SCHEMA_NAME, crit);
        try {
            QueryResponse response = conn.query(query);
            System.out.println("Total: " + response.getTotalCount());
            HDocumentList list = response.getResults();
            AssistantTool.printResultSet(list);
        } catch (HConnException e) {
            e.printStackTrace();
        } 
    }
    
    @Test
	public void getConfiguration() {
		Map props = conn.getConfiguration();
		for (Object key : props.keySet()) {
			System.out.println(key + " : " + props.get(key));
		}
    }
    
    @Test
	public void getSchemaMetadata() {
		SchemaMetadata schema = conn.getSchemaMetadata(SCHEMA_NAME);
		System.out.println(schema.getName() + " (" + schema.getDesc() + ")");
		Collection<FieldMetadata> list = schema.getFields();
		for (FieldMetadata fmd : list) {
			System.out.println(fmd.getName() + "(" + fmd.getType() + "): " + fmd.getFlag());
		}
    }

	@Test
	public void addDocs() {
	    HDocumentList docs = new HDocumentList();
	    
	    HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    
		doc.setField("messageid", UUID.randomUUID().toString());
		doc.setField("fromaddr","58同城简历 <mailservice@58.com>");
		doc.addField("toaddr","wwang369@163.com");
		doc.setField("senddate", new Date(System.currentTimeMillis()));
		doc.setField("subject","会计/会计师-（现职位）会计/会计师-58.com");
		doc.setField("content","应聘职位：会计/会计师。 请通过简历中的电话或邮件联系我。（请勿直接回复邮件）");
		doc.setField("content_entity","会计,会计师");
		doc.setField("attachnames","简历.doc,照片.jpeg");
		doc.setField("attach","我是一个对生活充满希望、积极向上的青年,对待朋友能以诚相待," +
				"对工作态度执着、细心、责任心强、勤奋好学、认真负责、吃苦耐劳、勇于迎接新挑战,能够快速进入团队角色、配合团队完成目标");
		doc.setField("reader","zhu");
		doc.setField("readtime", new Date(System.currentTimeMillis()));
		
		doc.addField("file", "C:/runtime/exports/IMG_7977.JPG");
		
		//二进制
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
    public void delete() {
		HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    doc.setField("messageid", "7ff3c130-7950-4653-a43c-49cd959eba1a");
        
	    try {
	    	UpdateResponse response = conn.delete(doc);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
    @Test
    public void deleteByQuery() {
    	HCriteria crit = new HCriteria();

    	crit.addLike("content", "会计");
    	//crit.addEqualTo("messageid", "ebb0422d-20d1-4e3e-947d-3a79b35f39b7");

        HQuery query = HQueryFactory.newQuery(SCHEMA_NAME, crit);
        
	    try {
	    	UpdateResponse response = conn.delete(query);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
	@Test
	public void update() {
		HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    
	    doc.setField("messageid", "7ff3c130-7950-4653-a43c-49cd959eba1a");
	    doc.setField("reader", "zhu");
		doc.setField("readtime", new Date(System.currentTimeMillis()));

	    try {
	    	UpdateResponse response = conn.update(doc);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    
    @Test
    public void upload() {
    	String srcFile = "C:/runtime/export/照片_7976.JPG";
    	String destFile = conn.getSerialID(true);
    	System.out.println("Remote file: " + destFile);
    	String taskID = conn.upload(srcFile, destFile);
    	while (true) {
    		StatusResponse resp = conn.getStatus(taskID);
			System.out.println("Process: " + resp.getProcess());    		
    		if (resp.getProcess() >= 100) {
    			break;
    		}
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	System.out.println("Upload Done. Dest is " + destFile);
    }
    
    @Test
    public void download() {
    	String srcFile = "20141023/bbc7a903-5216-42a9-afad-af4545be867a";
    	String destFile = "C:/runtime/export/照片_7976.JPG";
    	System.out.println("Remote file: " + srcFile);
    	String taskID = conn.download(srcFile, destFile);
    	while (true) {
    		StatusResponse resp = conn.getStatus(taskID);
			System.out.println("Process: " + resp.getProcess());    		
    		if (resp.getProcess() >= 100) {
    			break;
    		}
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	System.out.println("Download Done.");
    }
    
    
    @Test
    public void ipaddrConvert() {
			int id1 = InetAddresses.coerceToInteger(InetAddresses.forString("192.168.1.4"));			
			int idLower = InetAddresses.coerceToInteger(InetAddresses.forString("192.168.0.0"));
			int idHigh = InetAddresses.coerceToInteger(InetAddresses.forString("192.168.255.255"));
			System.out.println("\nLower:  " + idLower 
							 + "\nhigher: " + idHigh 
							 + "\nid:     " + id1);
			int id2 = -1062721516;
			String ip1 = InetAddresses.fromInteger(id2).getHostAddress();
			System.out.println("IP: " + ip1);
    }
    
    @Test
    public void hfileTest() {
    	
    }
    
    private final void fail(Object o) {
        Assert.assertNotNull(o);
    }
}
