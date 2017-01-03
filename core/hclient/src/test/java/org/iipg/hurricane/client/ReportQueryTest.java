package org.iipg.hurricane.client;

import java.util.Iterator;
import java.util.Map;

import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.util.JSONUtil;
import org.iipg.hurricane.client.metadata.*;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReportQueryTest {
	
	private HConnection conn = null;

    @Before
    public void init() {
        try {
        	conn = HConnFactory.getInstanceByConf("config.client");
        } catch (HConnException e) {
        	System.out.println("Init connection failed! " + e.getMessage());
            //e.printStackTrace();
        }
        Assert.assertNotNull(conn);
    }
    
	@Test public void test(){
		HReportCriteria crit = new HReportCriteria();	
		crit.setGroupBy("BMJ");
		crit.setHavingClause("@NUMTERMS >= 1");
		crit.setRowCount(-1);

		HReportQuery q = HQueryFactory.newReportQuery("email666j", crit);
		QueryResponse results = null;
		try {
			results = conn.reportQuery(q);
			System.out.println("Total count: " + results.getTotalCount());	
			HDocumentList hDocs = results.getResults();
			if (hDocs != null) {
			for (int i=0; i<hDocs.size(); i++) {
				HDocument doc = (HDocument) hDocs.get(i);
				Map props = doc.getProps();
				System.out.println("---------------------");
				printMap(props);
			}
			}
		} catch (HConnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void printMap(Map props) {
		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
			String key = (String) it.next();
			System.out.println(key + " : " + props.get(key));
		}
	}
}
