package org.iipg.hurricane.db.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.search.Query;

public abstract class HDBQuery {

	protected List<String> selectFields = new ArrayList<String>();
	protected List<String> highlightFields = new ArrayList<String>();
	protected Map<String,String> sortFields = new HashMap<String,String>();
	protected List<HDBCondition> whereClauses = new ArrayList<HDBCondition>();
	protected Query whereQuery = null;
	
	protected int rowCount = 10;
	protected int rowStart = 0;
	protected String schemaName;
	protected String hmode = "abs";
	
	protected SchemaParser schema;
	
	public void setRowStart(int start) {
		this.rowStart = start;
		
	}
	public int getRowStart() {
		return this.rowStart;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	public int getRowCount() {
		return this.rowCount;
	}

	// Select Fields
	public List<String> getSelectFields() {
		return this.selectFields;
	}
	public void addSelectField(String name) {
		selectFields.add(name);
		
	}
	
	public List<String> getHighlightFields() {
		return this.highlightFields;
	}
	public void addHighlightField(String fieldName) {
		highlightFields.add(fieldName);
	}
	
	public void setHighlightMode(String highlightMode) {
		this.hmode = highlightMode;
	}
	public String getHighlightMode() {
		return this.hmode;
	}
	
	// Sort Field	
	public Map<String, String> getSortFields() {
		return sortFields;
	}
	public void addSortField(String field, String order){
		if (order.equalsIgnoreCase("asc")){
			this.sortFields.put(field, "asc");
		}else{
			this.sortFields.put(field, "desc");
		}
	}
	
	// Where Field
	public List<HDBCondition> getWhereClauses() {
		return this.whereClauses;
	}
	public void addWhereClause(HDBCondition condition) {
		whereClauses.add(condition);		
	}

	public Query getWhereClauses2() {
		return this.whereQuery;
	}
	public void setWhereClauses2(Query condition) {
		this.whereQuery = condition;
	}
	
	
	// Schema Name
	public void setSchemaName(String objectName) {
		this.schemaName = objectName;		
	}
	public String getSchemaName() {
		return this.schemaName;
	}

	// Schema
	public void setSchema(SchemaParser schema) {
		this.schema = schema;
	}
	public SchemaParser getSchema() {
		return this.schema;
	}

	// rewrite method
	public abstract String getSql();
	public abstract String getCountSql();

}
