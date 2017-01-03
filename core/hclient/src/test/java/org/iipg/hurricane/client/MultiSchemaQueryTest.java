package org.iipg.hurricane.client;

import java.util.Collection;

import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.metadata.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiSchemaQueryTest {
	
	private HConnection conn = null;

    @Before
    public void init() {
        try {
        	conn = HConnFactory.getInstanceByConf("config.client");
        } catch (HConnException e) {
        	System.out.println("Init connection failed! " + e.getMessage());
            //e.printStackTrace();
        }
        fail(conn);
    }
    
	@Test public void test(){
		HCriteria crit = new HCriteria();
		
		crit.addSelectField("reader");
		crit.addSelectField("readtime");
		crit.addSelectField("tel.name");
		
		crit.addGreaterOrEqualThan("readtime", "2014-06-12 15:43:00");
		crit.addEqualToField("reader", "tel.reader");
		
		crit.addOrderByDescending("readtime");
		
		HQuery q = HQueryFactory.newQuery("email", crit);
		QueryResponse resp = null;
		try {
			resp = conn.query(q);
			System.out.println(resp.toString());	
		} catch (HConnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
    private final void fail(Object o) {
        Assert.assertNotNull(o);
    }
}
