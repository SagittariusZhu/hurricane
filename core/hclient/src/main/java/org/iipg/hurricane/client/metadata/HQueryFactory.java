package org.iipg.hurricane.client.metadata;


public class HQueryFactory {

	public static HQuery newQuery(String schema, HCriteria crit) {
		HQuery query = new HQuery();
		query.setSchema(schema);
		query.setQStr(crit.getQStr());
		query.setCriteria(crit);
		return query;
	}

	public static HReportQuery newReportQuery(String schema, HReportCriteria crit) {
		HReportQuery query = new HReportQuery();
		query.setSchema(schema);
		query.setQStr(crit.getQStr());
		query.setCriteria(crit);
		return query;
	}

}
