package org.iipg.hurricane.client.response;

import org.iipg.hurricane.client.metadata.*;

public class QueryResponse {

	private HDocumentList docs = null;
	private int total = 0;
	
	public QueryResponse() {}
	
	public QueryResponse(int total) {
		this.total = total;
	}
	
	public void setTotalCount(int total) {
		this.total = total;
	}
	
	public int getTotalCount() {
		return this.total;
	}
	
	public HDocumentList getResults() {
		return this.docs;
	}

	public char[] getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setResults(HDocumentList docs) {
		this.docs = docs;		
	}

}
