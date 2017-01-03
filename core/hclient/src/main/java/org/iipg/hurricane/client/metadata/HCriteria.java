package org.iipg.hurricane.client.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.client.util.StringUtil;

public class HCriteria {
	
	private int start = 0;
	private int count = 10;
	private List<String> selectFields = new ArrayList<String>();
	private List<String> highlightFields = new ArrayList<String>();
	private List<Map<String, String>> whereClause = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> orderFields = new ArrayList<Map<String, String>>();
	private String qStr = "";
	
	public HCriteria(String query) {
		this.qStr = query;
	}

	public HCriteria() {
		// TODO Auto-generated constructor stub
	}

	public List getSelectFields() {
		return selectFields;
	}
	
	public List getHighlightFields() {
		return highlightFields;
	}
	
	public List getWhereClause() {
		return whereClause;
	}
	
	public List getOrderFields() {
		return orderFields;
	}
	
	public void addSelectField(String fieldName) {
		if (!selectFields.contains(fieldName))
			selectFields.add(fieldName);
	}
	
	public void addHighlightField(String fieldName) {
		if (!highlightFields.contains(fieldName))
			highlightFields.add(fieldName);
	}
	
	public void addEqualTo(String fieldName, Object value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", StringUtil.toString(value));
		w1.put("op", "eq");
		whereClause.add(w1);		
	}
	
	public void addLike(String fieldName, String value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", value);
		w1.put("op", "like");
		whereClause.add(w1);
	}
	
	public void addGreaterOrEqualThan(String fieldName, Object value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", StringUtil.toString(value));
		w1.put("op", "ge");
		whereClause.add(w1);
	}
	
	public void addGreaterThan(String fieldName, Object value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", StringUtil.toString(value));
		w1.put("op", "gt");
		whereClause.add(w1);
	}
	
	public void addLessOrEqualThan(String fieldName, Object value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", StringUtil.toString(value));
		w1.put("op", "le");
		whereClause.add(w1);
	}
	
	public void addLessThan(String fieldName, Object value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("value", StringUtil.toString(value));
		w1.put("op", "lt");
		whereClause.add(w1);
	}
	
	public void addBetween(String fieldName, Object value1, Object value2) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("lvalue", StringUtil.toString(value1));
		w1.put("uvalue", StringUtil.toString(value2));
		w1.put("op", "between");
		whereClause.add(w1);
	}
	
	public void addIn(String fieldName, List value) {
		Map<String, String> w1 = new HashMap<String, String>();
		w1.put("field", fieldName);
		w1.put("values", StringUtil.toString(value));
		w1.put("op", "in");
		whereClause.add(w1);
	}
	
	public void addOrCriteria(HCriteria crit) {
		
	}
	
	public void addOrderByAscending(String fieldName) {
		Map<String, String> order1 = new HashMap<String, String>();
		order1.put("field", fieldName);
		order1.put("op","asc");
		orderFields.add(order1);
	}
	
	public void addOrderByDescending(String fieldName) {
		Map<String, String> order1 = new HashMap<String, String>();
		order1.put("field", fieldName);
		order1.put("op","desc");
		orderFields.add(order1);
	}
	
	// multi schema API
	public void addEqualToField(String field1, String field2) {
		// TODO Auto-generated method stub
		
	}
	
	public void addGreaterOrEqualThanField(String field1, String field2) {
		
	}

	public void addGreaterThanField(String field1, String field2) {
		
	}

	public void addLessOrEqualThanField(String field1, String field2) {
		
	}
	
	public void addLessThanField(String field1, String field2) {
		
	}

	public int getRowStart() {
		return this.start;
	}
	public void setRowStart(int start) {
		this.start = start;
	}
	
	public int getRowCount() {
		return this.count;
	}
	public void setRowCount(int count) {
		this.count = count;
	}

	public void setQStr(String qStr) {
		this.qStr = qStr;		
	}
	
	public String getQStr() {
		return this.qStr;
	}

}
