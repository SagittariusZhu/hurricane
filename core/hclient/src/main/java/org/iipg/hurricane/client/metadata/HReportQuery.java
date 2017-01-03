package org.iipg.hurricane.client.metadata;

public class HReportQuery extends HQuery {
	
	private HReportCriteria criteria = null;
	
	public String getGroupByField() {
		if (criteria != null)
			return criteria.getGroupBy();
		return null;
	}
	
	public String getHavingClause() {
		if (criteria != null)
			return criteria.getHavingClause();
		return null;
	}

	public void setCriteria(HReportCriteria crit) { 
		this.criteria = crit;
		setRowStart(crit.getRowStart());
		setRowCount(crit.getRowCount());
		setQStr(crit.getQStr());
	}

	public void setGroupByField(String groupByField) {
		if (criteria == null) 
			criteria = new HReportCriteria();
		criteria.setGroupBy(groupByField);
	}

	public void setHavingClause(String havingClause) {
		if (criteria == null) 
			criteria = new HReportCriteria();
		criteria.setHavingClause(havingClause);
	}
		
}
