package org.iipg.hurricane.client.metadata;

import java.util.Map;

public class HQuery {
	
	private String schema = "";
	
	private String[] selectFields = null;
	private String[] highlightFields = null;
	private Map<String, String>[] whereClause = null;
	private String qStr = "";
	private Map<String, String>[] orderFields = null;
	private int start = 0;
	private int count = 10;
	private String hmode = "abs";
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return this.schema;
	}

	public void setSelectFields(String[] fields) {
		this.selectFields = fields;
	}
	public String[] getSelectFields() {
		return this.selectFields;
	}

	public void setHighlightFields(String[] fields) {
		this.highlightFields = fields;
	}
	public String[] getHighlightFields() {
		return this.highlightFields;
	}
	
	public void setWhereClause(Map<String, String>[] whereClause) {
		this.whereClause = whereClause;
	}
	public Map<String, String>[] getWhereClause() {
		return this.whereClause;
	}
	
	public void setQStr(String qStr) {
		this.qStr = qStr;
	}
	public String getQStr() {
		return this.qStr;
	}
	
	public void setOrderFields(Map<String, String>[] orderFields) {
		this.orderFields = orderFields;
	}
	public Map<String, String>[] getOrderFields() {
		return this.orderFields;
	}

	public void setCriteria(HCriteria crit) {
		setSelectFields((String[]) crit.getSelectFields().toArray(new String[0]));
		setHighlightFields((String[]) crit.getHighlightFields().toArray(new String[0]));
		setWhereClause((Map<String, String>[]) crit.getWhereClause().toArray(new Map[0]));
		setOrderFields((Map<String, String>[]) crit.getOrderFields().toArray(new Map[0]));
		setRowStart(crit.getRowStart());
		setRowCount(crit.getRowCount());
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

	public void setHighlightMode(String hmode) {
		this.hmode = hmode;		
	}
	
	public String getHighlightMode() {
		return this.hmode;
	}

	public String getSelectFieldsStr() {
		StringBuffer buf = new StringBuffer();
		if (selectFields != null) {
			for (String field : selectFields) {
				buf.append(field).append(",");
			}
			int idx = buf.lastIndexOf(",");
			if (idx >= 0) buf.replace(idx, idx+1, "");
		}
		return buf.toString();
	}

	public String getOrderFieldsStr() {
		StringBuffer buf = new StringBuffer();
		if (orderFields != null) {
			for (Map<String, String> orderClause : orderFields) {
				buf.append(orderClause.get("field")).append(" ")
				   .append(orderClause.get("op")).append(",");
			}
			int idx = buf.lastIndexOf(",");
			if (idx >= 0) buf.replace(idx, idx+1, "");
		}
		return buf.toString();
	}

	public String getHighlightFieldsStr() {
		StringBuffer buf = new StringBuffer();
		if (highlightFields != null) {
			for (String field : highlightFields) {
				buf.append(field).append(",");
			}
			int idx = buf.lastIndexOf(",");
			if (idx >= 0) buf.replace(idx, idx+1, "");
		}
		return buf.toString();
	}

	public String getUUID() {
		for (Map<String, String> clause : whereClause) {
			if (clause.get("field").equals("uuid"))
				return clause.get("value");
		}
		return null;
	}

	public String getItem() {
		for (Map<String, String> clause : whereClause) {
			if (clause.get("field").equals("item"))
				return clause.get("value");
		}
		return null;
	}
}
