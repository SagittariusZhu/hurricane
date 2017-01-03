package org.iipg.hurricane.db.query;

public abstract class HDBReportQuery extends HDBQuery {
	
	private String groupField = null;
	private String havingClause = null;
	
	public void setGroupField(String groupField) { this.groupField = groupField; }
	public void setHavingClause(String clause) { this.havingClause = clause; }
	
	public String getGroupField() { return this.groupField; }
	public String getHavingClause() { return this.havingClause; }
	
	public int size() {
		int size = 0;
		size += getGroupField() != null ? 1 : 0;
	
		return size;
	}

}
