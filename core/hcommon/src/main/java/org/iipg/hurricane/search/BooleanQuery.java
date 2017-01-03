package org.iipg.hurricane.search;

import java.util.ArrayList;
import java.util.List;

import org.iipg.hurricane.search.BooleanClause.Occur;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BooleanQuery extends Query {

	private List<Clause> clauseList = new ArrayList<Clause>();
	
	public BooleanQuery(boolean disableCoord) {
		// TODO Auto-generated constructor stub
	}

	public List<Clause> clauses() {
		return this.clauseList;
	}

	public void setMinimumNumberShouldMatch(int i) {
		// TODO Auto-generated method stub
		
	}

	public void add(BooleanClause clause) {
		if (clauseList.size() == 0) {
			clause.setProhibited(true);
		}
		clauseList.add(clause);		
	}
	

	public void add(Query newQuery, Occur operator) {
		BooleanClause clause = new BooleanClause(newQuery, operator);
		add(clause);		
	}

	@Override
	public JSONObject getJSONObject() throws JSONException {
		JSONObject ret = null;
		if (clauseList.size() == 1) {
			ret = clauseList.get(0).getJSONObject();
		} else {
			ret = new JSONObject();
			JSONArray list = new JSONArray();
			for (Clause clause : clauseList) {
				list.put(clause.getJSONObject());
			}
			ret.put("clauses", list);
		}
		return ret;
	}

}
