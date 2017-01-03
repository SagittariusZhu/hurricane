package org.iipg.data.sync.store;

import java.io.IOException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.data.sync.ds.DataSource;
import org.iipg.data.sync.ds.SolrDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Configuration file
 * 
 *   solr.cloud.enable: true/false
 *   
 *   zk.host.endpoints: sr1:2181,sr2:2181,sr3:2181
 *   zk.client.timeout: 2000
 *   zk.connect.timeout: 2000
 *   
 *   solr.url: http://sr1:8080/solr
 *   
 * @author zhu
 *
 */

public class SolrStorer extends AbstractStorer {

	private SolrClient solrClient = null;
	private SolrDataSource ds = null;

	public SolrStorer(DataSource ds) {
		this.ds = (SolrDataSource) ds;
	}

	@Override
	public int store(String tbName, Map<String, String> metads, JSONArray values) {
		int retVal = 0;
		
		if (ds.isCloud())
			solrClient = getCloudSolrClient(tbName);
		else
			solrClient = getHttpSolrClient(tbName);

		if (solrClient != null) {

			try {
				Collection<SolrInputDocument> docs = buildDocs(metads, values);
				UpdateResponse resp = solrClient.add(docs);
				solrClient.commit();

				System.out.println("----------------------------------------");
				System.out.println("solr response status: " + resp.getStatus());
				
				if (resp.getStatus() == 0)
					retVal = docs.size();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					solrClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	
	private static SimpleDateFormat solrSDF = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private Collection<SolrInputDocument> buildDocs(Map<String, String> metads, JSONArray values) {
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		for (int i=0; i<values.length(); i++) {
			SolrInputDocument doc = new SolrInputDocument();
			JSONObject rowObj = values.getJSONObject(i);
			
			for (Iterator<String> it = rowObj.keys(); it.hasNext();) {
				String colName = it.next();
				JSONObject value = rowObj.getJSONObject(colName);
				
				String realName = colName;
				if (metads.containsKey(colName))
					realName = metads.get(colName);
				
				Object realValue = null;
				int colType = value.getInt("type");
				switch (colType) {
				case Types.TIMESTAMP:
					try {
						realValue = solrSDF.format((Date) value.get("value"));
					} catch (Exception e) {
						//e.printStackTrace();
						realValue = solrSDF.format(new Date(0));
					}
					break;
				default:
					realValue = value.get("value");
				}
				doc.setField(realName, realValue);
			}
			
			docs.add(doc);
		}
		return docs;
	}

	private CloudSolrClient getCloudSolrClient(String dbName) {
		CloudSolrClient client = null;
		String zkHost = ds.getZkHost();		

		client = new CloudSolrClient(zkHost);

		client.setZkClientTimeout(ds.getZkTimeout());
		client.setZkConnectTimeout(ds.getZkTimeout());

		client.connect();

		if (client != null)
			client.setDefaultCollection(dbName);

		return client;
	}

	private HttpSolrClient getHttpSolrClient(String dbName) {
		String url = ds.getUrl();
		if (dbName != null) {
			url += "/" + dbName;
		}
		HttpSolrClient client = new HttpSolrClient(url);
		return client;
	}
}