package org.iipg.hurricane.client.metadata;

import java.util.Map;

public class HReportCriteria extends HCriteria {

	private String groupByField = "";
	private String havingClause = "";
	
	public void addCountField(String field) {
		// TODO Auto-generated method stub
		
	}

	public void addMaxField(String field) {
		// TODO Auto-generated method stub
		
	}

	public void setGroupBy(String field) { this.groupByField = field; }
	public void setHavingClause(String clause) { this.havingClause = clause; }
	
	public String getGroupBy() { return this.groupByField; }
	public String getHavingClause() { return this.havingClause; }

	public void addHavingEqualTo(String field, int value) {
		// TODO Auto-generated method stub
		
	}

	public void addHavingGreaterOrEqualThen(String field, int value) {
		// TODO Auto-generated method stub
		
	}

	public void addHavingGreaterThen(String field, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	public void addHavingLessOrEqualThen(String field, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void addHavingLessThen(String field, Object value) {
		// TODO Auto-generated method stub
		
	}
}
