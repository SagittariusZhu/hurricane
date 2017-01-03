/**
 * 
 */
package org.iipg.hurricane.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.metadata.*;
import org.iipg.hurricane.db.query.*;
import org.iipg.hurricane.db.schema.*;
import org.iipg.hurricane.solr.impl.SolrDataImportRequest;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhn
 *
 */
public class HSolrClient {
	
	static final Logger LOG = LoggerFactory.getLogger(HSolrClient.class);
	
	protected SolrClient solrClient = null;
	
	protected boolean autoCommit = false;
	
	protected String solrServerUrl = null;
	
	private String dbName = "";
	
	public HSolrClient(HurricaneConfiguration conf, String dbName) {
		if (conf.isCloud())
			solrClient = SolrUtil.getCloudSolrClient(dbName);
		else
			solrClient = SolrUtil.getHttpSolrClient(
					SolrConfiguration.getString(SolrConfiguration.SOLR_HTTPSERVER_URL),
					dbName);
		this.dbName = dbName;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public String getSolrServerUrl() {
		return solrServerUrl;
	}

	public void setSolrServerUrl(String solrServerUrl) {
		this.solrServerUrl = solrServerUrl;
	}
	
	public void close(){
		SolrUtil.releaseClient(solrClient);
	}

	public void add(SolrInputDocument doc) throws SolrServerException, IOException{
		UpdateResponse resp = solrClient.add(doc);
		if (!isAutoCommit()){
			solrClient.commit();
		}
		if (resp.getStatus() != 0) {
			LOG.warn(resp.toString());
			throw new SolrServerException(resp.toString());
		}
	}
	
	public void add(Collection<SolrInputDocument> docs) throws SolrServerException, IOException{
		UpdateResponse resp = solrClient.add(docs);
		if (!isAutoCommit()){
			solrClient.commit();
		}
		if (resp.getStatus() != 0) {
			LOG.warn(resp.toString());
			throw new SolrServerException(resp.toString());
		}
	}
	

	public SolrDocumentList queryRawCollection(HDBQuery hQuery) {
		try {
			SolrQuery query = SolrUtil.toSolrQuery(hQuery);
			QueryResponse response = solrClient.query(query);
			//response.get
			SolrDocumentList docs = response.getResults();
			return docs;
		} catch (SolrServerException e) {
			LOG.warn(e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public HDBResultSet query(HDBQuery hQuery, Field uniqueField){
		try {
			HDBResultSet result = new HDBResultSet();
			SolrQuery query = SolrUtil.toSolrQuery(hQuery);
			QueryResponse response = solrClient.query(query);
			//response.get
			SolrDocumentList docs = response.getResults();
			Map<String,Map<String,List<String>>> hl = response.getHighlighting();
			
			result.setConsumeTime(response.getQTime());
			result.setTotalCount(docs.getNumFound());
			result.setCount(docs.size());
			List<String> fields = hQuery.getSelectFields();
			for (SolrDocument doc : docs) {
				HDBRecord record = new HDBRecord();
				if (fields != null){
					Object key = doc.getFieldValue(uniqueField.getName());
					for(String field : fields){
						if (hl != null) {
							Map<String,List<String>> hset = hl.get(key);
							if (hset.containsKey(field)) {
								List<String> v = hset.get(field);
								if ("abs".equalsIgnoreCase(hQuery.getHighlightMode())) {
									StringBuffer r = new StringBuffer();
									for (String vstr : v) {
										r.append(vstr).append("...");
									}
									record.put(field, r.toString());
								} else {
									record.put(field, v.get(0));
								}
								continue;
							}
						}
						record.put(field, doc.getFieldValue(field));
					}
				}
				record.put(HConstant.FIELD_NAME_SIGNATURE, doc.getFieldValue(HConstant.FIELD_NAME_SIGNATURE));
				result.addItem(record);
			}
			return result;
		} catch (SolrServerException e) {
			LOG.warn(e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public HDBResultSet facetQuery(HDBReportQuery hquery){
		SolrQuery query = new SolrQuery(hquery.getSql());
		query.setStart(0);
		query.setRows(0);

		query.setIncludeScore(true);
		query.setFacet(true);
		
		query.addFacetField(hquery.getGroupField());

		query.setFacetLimit(hquery.getRowCount());
		query.set(FacetParams.FACET_OFFSET,hquery.getRowStart());
		
		QueryResponse response;
		try {
			HDBResultSet result = new HDBResultSet();
			response = solrClient.query(query);

			FacetField facetField = response.getFacetField(hquery.getGroupField());
			List<Count> returnList = facetField.getValues();
			result.setConsumeTime(response.getQTime());
			result.setTotalCount(facetField.getValueCount());
			result.setCount(returnList.size());

			for (Count count : returnList) {
				if (count.getCount() > 0) {
					HDBRecord record = new HDBRecord();
					record.put("value", count.getName());
					record.put("count", (int) count.getCount());
					//record.put("focus", getRecordBySignature(count.getName(),hquery.getSelectFields()));
					result.addItem(record);
				}
			}
			return result;
		} catch (SolrServerException e) {
			LOG.warn(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private HashMap<String,Object> getRecordBySignature(String signature,Collection<String> fields){
		HashMap<String,Object> result = new HashMap<String,Object>();
		
		SolrQuery query = new SolrQuery(HConstant.FIELD_NAME_SIGNATURE + ":" + signature);
		query.setStart(0);
		query.setRows(1);

		QueryResponse response;
		try {
			response = solrClient.query(query);
			SolrDocumentList docs = response.getResults();
			if(docs != null){
				SolrDocument doc = docs.get(0);
				for(String field : fields){
					result.put(field, doc.getFieldValue(field));
				}
			}
		} catch (SolrServerException e) {
			LOG.warn(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return result;
	}
	
	public void deleteByQuery(String query) throws SolrServerException, IOException{
		solrClient.deleteByQuery(query);
		if (!isAutoCommit()){
			solrClient.commit(true, true);
		}
	}
	
	public void deleteById(String id) throws SolrServerException, IOException{
		solrClient.deleteById(id);
		if (!isAutoCommit()){
			solrClient.commit(true, true);
		}
	}
	
	public void deleteByIds(List<String>ids) throws SolrServerException, IOException{
		solrClient.deleteById(ids);
		if (!isAutoCommit()){
			solrClient.commit(true, true);
		}
	}
	
	public int update(){
		int num = 0;
		return num;
	}
	
	public void optimize() throws SolrServerException, IOException{
		long startTime = System.currentTimeMillis();
		solrClient.optimize(true, true);
		solrClient.commit();
		long endTime = System.currentTimeMillis();
		LOG.info("Core " + dbName + " optimized used " + (endTime - startTime)/1000 + " seconds.");
	}

	public boolean startDataImport(String dihName, Map params) {
        SolrQuery query;
        try {
            query = new SolrQuery();
            String dihHandler = dihName;
            if (!dihName.startsWith("/"))
            	dihHandler = "/" + dihName;
            query.setRequestHandler(dihHandler);
            for (Iterator it = params.keySet().iterator(); it.hasNext(); ) {
            	String key = (String) it.next();
            	query.setParam(key, "" + params.get(key));
            }
            QueryRequest request = new QueryRequest(query, METHOD.POST);
            QueryResponse response = request.process(solrClient);
            solrClient.commit();
            int status = response.getStatus();
            return (status == 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
	}

	public HDBResultSet getDataImportStatus(String dihName) {
        SolrQuery query;
        try {
            query = new SolrQuery();
            String dihHandler = dihName;
            if (!dihName.startsWith("/"))
            	dihHandler = "/" + dihName;
            query.setRequestHandler(dihHandler);
            //command=status&indent=true&wt=json
           	query.setParam("command", "status");
           	query.setParam("indent", true);
           	query.setParam("wt", "json");
            QueryRequest request = new QueryRequest(query);
            QueryResponse response = request.process(solrClient);
            solrClient.commit();
            int status = response.getStatus();
            if (status == 0) {
            	NamedList list = response.getResponse();
            	String mode = (String) list.get("status");
            	Map statusMessages = (Map) list.get("statusMessages");
            	HDBResultSet rset = new HDBResultSet();
            	HDBRecord item = new HDBRecord();
            	item.put("status", mode);
            	item.put("messages", statusMessages);
            	rset.addItem(item);
            	rset.setConsumeTime(response.getQTime());
            	rset.setTotalCount(1);
            	rset.setCount(1);
            	return rset;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
	}

}
