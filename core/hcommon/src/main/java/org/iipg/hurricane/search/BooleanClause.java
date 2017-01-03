package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public class BooleanClause extends Clause {
	
	private Occur occur;
	private Query q;
	private boolean prohibited = false;
	
	public BooleanClause(Query q, Occur occur) {
		this.q = q;
		this.occur = occur;
	}

	public void setOccur(Occur occur) {	this.occur = occur;	}
	public void setQuery(Query q) { this.q = q; }
	public void setProhibited(boolean prohibited) { this.prohibited = prohibited; }

	public Occur getOccur() { return this.occur; }
	public Query getQuery() { return this.q; }	
	public boolean isProhibited() {	return this.prohibited;	}

	
	@Override
	public JSONObject getJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		switch (occur) {
		case SHOULD:
			ret.put("OR", q.getJSONObject());
			break;
		case MUST:
			ret.put("AND", q.getJSONObject());
			break;
		case MUST_NOT:
			ret.put("NOT", q.getJSONObject());
			break;
		}
		return ret;
	}

	public enum Occur { SHOULD, MUST, MUST_NOT }

}
