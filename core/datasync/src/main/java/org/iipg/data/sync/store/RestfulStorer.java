package org.iipg.data.sync.store;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.data.sync.ds.DataSource;
import org.iipg.data.sync.ds.RestfulDataSource;
import org.iipg.data.sync.ds.SolrDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

public class RestfulStorer extends AbstractStorer {
	
	private RestfulDataSource ds = null;
	
	public RestfulStorer(DataSource ds) {
		this.ds = (RestfulDataSource) ds;
	}

	@Override
	public int store(String tbName, Map<String, String> metads, JSONArray values) {
		String storeUrl = ds.getUrl() + "/" + ds.getDbName() + "/store?wt=json";
		
		JSONObject contentObj = new JSONObject();
		contentObj.put("tbname", tbName);
		contentObj.put("metads", buildMetaDataSet(metads));
		contentObj.put("records", values);

		StringEntity entity = new StringEntity(contentObj.toString(),
									ContentType.create("plain/text", Consts.UTF_8));
		//entity.setChunked(true);
		HttpPost httppost = new HttpPost(storeUrl);
		httppost.setEntity(entity);

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return values.length();
	}

	private JSONObject buildMetaDataSet(Map<String, String> metaMap) {
		JSONObject metaObj = new JSONObject();
		
		for (Iterator<String> it = metaMap.keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			
			metaObj.put(key, metaMap.get(key));
		}
		
		return metaObj;
	}

}
