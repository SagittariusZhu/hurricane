package org.iipg.hurricane.solr.impl;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;

public class SolrSystemInfoRequest extends SolrRequest<SolrSystemInfoResponse> {

	public SolrSystemInfoRequest() {
		super(METHOD.GET, "/admin/info/system");
	}

	@Override
	protected SolrSystemInfoResponse createResponse(SolrClient client) {
		return new SolrSystemInfoResponse();
	}

	@Override
	public Collection<ContentStream> getContentStreams() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SolrParams getParams() {
		ModifiableSolrParams params = new ModifiableSolrParams();
	    return params;
	}

}
