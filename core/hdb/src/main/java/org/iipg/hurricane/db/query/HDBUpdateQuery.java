package org.iipg.hurricane.db.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.SchemaParser;

public abstract class HDBUpdateQuery {

	protected Map<String,Object> updateFields = new HashMap<String,Object>();
	protected Map<String,String> sortFields = new HashMap<String,String>();
	protected List<HDBCondition> whereClauses = new ArrayList<HDBCondition>();
	
	protected int rowCount = 10;
	protected int rowStart = 0;
	protected String schemaName;
	
	protected SchemaParser schema;
	
	public HDBUpdateQuery() {}
	
	public HDBUpdateQuery(SchemaParser schemaParser, HDBBaseObject object){
		setSchemaName(schemaParser.getName());
		setSchema(schemaParser);
		parse((HDBRecord) object);
	}
	
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

	// update Fields
	public Map<String, Object> getUpdateFields() {
		return this.updateFields;
	}
	public void addUpdateField(String name, Object value) {
		this.updateFields.put(name, value);
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


	public void parse(HDBRecord doc) {
		String uuidField = getSchema().getUniqueKey().getName();
		Object uuid = doc.get(uuidField);
		if (uuid != null)
			addWhereClause(new HDBEqualCondition(uuidField, uuid));
		
		Map map = doc.getInformation();
		for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ){
			String column = (String) iter.next();
			if (uuidField.equals(column)) continue;
			Object value = map.get(column);
			addUpdateField(column, value);
		}
	}
	
	// rewrite method
	public abstract String getUpdateSql();
}
