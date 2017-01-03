package org.iipg.hurricane.search;

import org.json.JSONException;
import org.json.JSONObject;

public class TermRangeQuery extends Query {
	
	public static final String TYPE = "TermRangeQuery";
	
	private String field = "";
	private String start = "";
	private String end = "";
	private boolean startInclusive = false;
	private boolean endInclusive = false;

	public TermRangeQuery(String field, String start, String end,
			boolean startInclusive, boolean endInclusive) {
		this.field = field;
		this.start = start;
		this.end = end;
		this.startInclusive = startInclusive;
		this.endInclusive = endInclusive;
	}
	
	public String getField() { return this.field; }
	public String getStart() { return this.start; }
	public String getEnd() { return this.end; }
	public boolean getStartInclusive() { return this.startInclusive; }
	public boolean getEndInclusive() { return this.endInclusive; }

	@Override
	public JSONObject getJSONObject() throws JSONException {
		JSONObject prop = new JSONObject();
		prop.put("type", TYPE);
		prop.put("name", field);
		prop.put("start", start);
		prop.put("end", end);
		prop.put("startInclusive", startInclusive);
		prop.put("endInclusive", endInclusive);
		return prop;
	}
}
