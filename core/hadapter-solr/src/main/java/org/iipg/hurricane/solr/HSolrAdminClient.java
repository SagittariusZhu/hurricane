package org.iipg.hurricane.solr;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.solr.impl.SolrSystemInfoRequest;
import org.iipg.hurricane.solr.impl.SolrSystemInfoResponse;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.iipg.hurricane.util.JSONUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HSolrAdminClient {
	
	static final Logger LOG = LoggerFactory.getLogger(HSolrAdminClient.class);
	
	private CloudSolrClient cloudSolrClient = null;
	private HttpSolrClient httpSolrClient = null;

	public HSolrAdminClient() {
		cloudSolrClient = SolrUtil.getCloudSolrClient(null);
		
		httpSolrClient = SolrUtil.getHttpSolrAdminClient(
				SolrConfiguration.getString(SolrConfiguration.SOLR_HTTPSERVER_URL));
	}

	public HSolrAdminClient(String url) {
		httpSolrClient = SolrUtil.getHttpSolrAdminClient(url);
	}
	
	/*
	 *  admin/collections?
	 * 	action=CREATE
	 * &name=email
	 * &replicationFactor=2
	 * &collection.configName=email
	 * &numShards=3
	 * &maxShardsPerNode=2
	 */
	public boolean createCore(String coreName, String confName,	
			int factor, int shards, int maxShardsPerNode) throws Exception {
		CollectionAdminRequest.Create req = new CollectionAdminRequest.Create();
		try {
			req.setCollectionName(coreName);
			req.setConfigName(confName);
			
			//req.setAction(CollectionAction.CREATE);
			//req.setName(coreName);
			//req.setConfigName(confName);
			req.setReplicationFactor(factor);
			req.setNumShards(shards);
			req.setMaxShardsPerNode(maxShardsPerNode);
			CollectionAdminResponse res = req.process(cloudSolrClient);
			if (res.getResponse().findRecursive("error", "failure") != null)
				return false;
			return true;
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return false;		
	}
	
	/**
	 * admin/collections?action=DELETE&name=collection1
	 * @return
	 */
	public boolean deleteCore(String coreName) throws Exception {
		CollectionAdminRequest.Delete req = new CollectionAdminRequest.Delete(); 
		try {
			req.setCollectionName(coreName);
			CollectionAdminResponse res = req.process(cloudSolrClient);
			if (res.getResponse().findRecursive("error", "failure") != null)
				return false;
			return true;
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return false;
	}
	
	/**
	 * admin/collections?action=RELOAD&name=email2
	 * @param coreName
	 * @return
	 */
	public boolean reloadCore(String coreName) throws Exception {
		CollectionAdminRequest.Reload req = new CollectionAdminRequest.Reload(); 
		try {
			req.setCollectionName(coreName);
			SolrResponse res = req.process(cloudSolrClient);
			if (res.getResponse().findRecursive("error", "failure") != null)
				return false;
			return true;
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return false;
	}
	
	public JSONObject getSystemInfo() {
		SolrSystemInfoRequest req = new SolrSystemInfoRequest();
		try {
			SolrSystemInfoResponse res = req.process(httpSolrClient);
			JSONObject obj =  new JSONObject();
			obj.put("isCloud", res.isCloudMode());
			obj.put("version", res.getVersion());
			obj.put("memory", res.getMemory());
			return obj;
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject();
	}
	
	public JSONObject getIndexStatus(String coreName) {
		JSONObject ret = new JSONObject();
		try {
			NamedList<Object> list = CoreAdminRequest
			        .getStatus(coreName, httpSolrClient).getCoreStatus()
			        .get(coreName);
			for (int i=0; i<list.size(); i++) {
				Object val = list.getVal(i);
				if (val instanceof NamedList)
					ret.put(list.getName(i), toJSONObject((NamedList) val));
				else
					ret.put(list.getName(i), val);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	private JSONObject toJSONObject(NamedList list) {
		JSONObject ret = new JSONObject();
		for (int i=0; i<list.size(); i++) {
			Object val = list.getVal(i);
			if (val instanceof NamedList)
				ret.put(list.getName(i), toJSONObject((NamedList) val));
			else
				ret.put(list.getName(i), val);
		}
		return ret;
	}

	public boolean optimize(String coreName) {
		boolean ret = false;
		if (cloudSolrClient != null) {
			try {
				cloudSolrClient.optimize(coreName);
				cloudSolrClient.commit(coreName);
				ret = true;
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				httpSolrClient.optimize(coreName);
				httpSolrClient.commit(coreName);
				ret = true;
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public void close() {
		if (cloudSolrClient != null) {
			SolrUtil.releaseClient(cloudSolrClient);
			cloudSolrClient = null;
		}
		
		if (httpSolrClient != null) {
			try {
				httpSolrClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpSolrClient = null;
		}
	}
}
