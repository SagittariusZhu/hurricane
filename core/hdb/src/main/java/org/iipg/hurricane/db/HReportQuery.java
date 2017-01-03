package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.ParseFieldException;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.model.HMWReportQuery;
import org.iipg.hurricane.qparser.SimpleQueryParser;
import org.iipg.hurricane.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class HReportQuery {
	private static final Logger LOG = LoggerFactory.getLogger(HReportQuery.class);
	
	private Map<String, HDBReportQuery> query = null;

	public HReportQuery() {
		query = new HashMap<String, HDBReportQuery>();
	}
	
	public HDBReportQuery getSolrQuery() {
		HDBReportQuery roQuery = query.get("roQuery");
		return roQuery;
	}
	public void setSolrQuery(HDBReportQuery roQuery) {
		query.put("roQuery", roQuery);
	}
	
	public static HReportQuery parse(HMWReportQuery q) throws IOException, ParserConfigurationException, SAXException {
		String schemaName = q.schema;
		SchemaParser schema = new SchemaParser(schemaName);
		
		HDBReportQuery roQuery = HAdapterFactory.createSolrAdapter(schemaName).createReportQuery();
		
		roQuery.setSchema(schema);
		
		roQuery.setSchemaName(schema.getName());
		
		if (q.qStr.length() > 0) {
			SimpleQueryParser parser = new SimpleQueryParser("text");
			try {
				Query query = parser.parse(q.qStr);
				roQuery.setWhereClauses2(query);
			} catch (Exception ignore) {}
//			HCriteria crit = new HCriteria();
//			HQueryUtil.parseQuery(crit, q.qStr);
//			hQuery.setWhereClause((Map<String, String>[]) crit.getWhereClause().toArray(new Map[0]));
		}

		String groupByField = q.groupByField;
		if (groupByField != null) {
			Field field = schema.getField(groupByField);
			if (field == null) {
				field = schema.getDynamicField(groupByField);
			}
			if (field == null) {
				String msg = "没有定义的字段:" + groupByField;
				LOG.warn(msg);
				throw new ParseFieldException(msg);
			}
			String mode = field.getMode();
			if (mode == null) {
				roQuery.setGroupField(groupByField);
			} else if (mode.equalsIgnoreCase("ro")) {
				roQuery.setGroupField(groupByField);
			} else {
				String msg = "未知存储方式:" + mode;
				LOG.warn(msg);
				throw new ParseFieldException(msg);
			}
		}
		
		String havingClause = q.havingClause;
		if (havingClause != null) {
			roQuery.setHavingClause(havingClause);
		}
		
		int start = q.rowStart;
		int rowCount = q.rowCount;
		
		roQuery.setRowStart(start);
		roQuery.setRowCount(rowCount);
	
		HReportQuery query = new HReportQuery();
		query.setSolrQuery(roQuery);
				
		return query;
	}
}
