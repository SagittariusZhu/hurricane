package org.iipg.hurricane.search;

public class Term {

	private String field = "";
	private String text = "";
	
	public Term(String field, String queryText) {
		this.field = field;
		this.text = queryText;
	}

	public String getField() { return this.field; }
	public String getText() { return this.text; }

}
