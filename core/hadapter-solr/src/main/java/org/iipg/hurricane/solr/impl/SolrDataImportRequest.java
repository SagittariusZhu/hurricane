package org.iipg.hurricane.solr.impl;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;

public class SolrDataImportRequest extends SolrRequest {

	private SolrQuery query = null;
	
	public SolrDataImportRequest(SolrQuery query) {
		super(METHOD.POST, query.getRequestHandler());
		this.query = query;
	}

	@Override
	public Collection<ContentStream> getContentStreams() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SolrParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SolrResponse createResponse(SolrClient client) {
		// TODO Auto-generated method stub
		return null;
	}

}
