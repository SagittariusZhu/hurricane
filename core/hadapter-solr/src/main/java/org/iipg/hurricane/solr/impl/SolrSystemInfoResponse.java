package org.iipg.hurricane.solr.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.iipg.hurricane.util.JSONUtil;
import org.json.JSONObject;

public class SolrSystemInfoResponse extends SolrResponseBase {
	
	public boolean isCloudMode() {
		return getResponse().get("mode").equals("solrcloud");
	}
	
	public String getVersion() {
		SimpleOrderedMap luceneInfo = (SimpleOrderedMap) getResponse().get("lucene");
		return (String) luceneInfo.get("solr-spec-version");
	}
	
	public JSONObject getMemory() {
		SimpleOrderedMap memInfo = (SimpleOrderedMap) ((SimpleOrderedMap) ((SimpleOrderedMap) getResponse().get("jvm")).get("memory")).get("raw");
		Map p = new HashMap();
		for (int k = 0; k < memInfo.size(); k++) {  
             p.put(memInfo.getName(k), memInfo.getVal(k));
		}
		return  JSONUtil.toJSONObject(p);
	}

}
