package org.iipg.hurricane.solr;

import org.iipg.hurricane.db.query.HDBReportQuery;

public class SolrReportQuery extends HDBReportQuery {

	@Override
	public String getSql() {
		return "*:*";
	}

	@Override
	public String getCountSql() {
		// TODO Auto-generated method stub
		return null;
	}

}
