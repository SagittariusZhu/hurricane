package org.iipg.hurricane.client;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.iipg.hurricane.client.metadata.HCriteria;
import org.iipg.hurricane.client.metadata.HDocument;
import org.iipg.hurricane.client.metadata.HDocumentList;
import org.iipg.hurricane.client.metadata.HQuery;
import org.iipg.hurricane.client.metadata.HQueryFactory;
import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.iipg.hurricane.client.util.AssistantTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryTest {
	
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
    	if (conn != null) {
    		conn.close();
    		conn = null;
    	}
        System.runFinalization();
        System.gc();
    }
    
    @Test
    public void queryByStr() {
    	String qStr = "*:*";
    	//String qStr = "mint:[10 TO *]";
    	HCriteria crit = new HCriteria();
    	crit.addSelectField("fromaddr");
    	crit.addSelectField("subject");
    	crit.addSelectField("reader");
    	crit.addSelectField("readtime");
//    	crit.addSelectField("mstr");
    	crit.addSelectField("mint");
    	crit.setQStr(qStr);
    	crit.setRowCount(5);

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
	public void getHBlob() throws UnsupportedEncodingException {
		String uuid = "b837b204-42e1-4216-aed3-0baf2b03ed84";
		String itemID = "274:71";

		QueryResponse resp = null;
		try {
			resp = conn.getBinary(SCHEMA_NAME, uuid, itemID);		
			HDocumentList list = resp.getResults();
			if (list.size() > 0) {
				HDocument doc = list.get(0);
				byte[] buf = doc.getBinary();

				System.out.println("Blob: " + new String(buf, "utf-8"));
			} else {
				System.out.println("Not found item.");
			}
			
//	        File f = new File("C:/runtime/export/test.jpg");
//	        try {
//				FileOutputStream out = new FileOutputStream(f);
//				out.write(buf);
//				out.close();
//				System.out.println("Write to file: " + f.getAbsolutePath());
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} catch (HConnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

	}
    
    @Test
    public void updateByQuery() {
    	String qStr = "+content:(会计 李克强) +senddate:[2013-01-01 TO 2015-01-01]";
    	HCriteria crit = new HCriteria();
    	crit.addSelectField("fromaddr");
    	crit.addSelectField("subject");
    	crit.addSelectField("reader");
    	crit.addSelectField("readtime");
    	crit.setQStr(qStr);
    	crit.setRowCount(5);
    	HQuery query = HQueryFactory.newQuery(SCHEMA_NAME, crit);
    	
		HDocument doc = new HDocument();
	    doc.setSchema(SCHEMA_NAME);
	    doc.setField("reader", "zhu");
		doc.setField("readtime", new Date(System.currentTimeMillis()));

	    try {
	    	UpdateResponse response = conn.update(doc, query);
	    	System.out.println("Affected " + response.getRows() + " rows.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
}
