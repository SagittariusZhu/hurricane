package org.iipg.hurricane.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.iipg.hurricane.client.metadata.HCriteria;
import org.iipg.hurricane.client.metadata.HQuery;
import org.iipg.hurricane.client.metadata.HReportCriteria;
import org.iipg.hurricane.client.metadata.HReportQuery;
import org.iipg.hurricane.client.util.HMWUtil;
import org.iipg.hurricane.model.HMWQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class HQueryUtil {
	
	private static Logger LOG = LoggerFactory.getLogger(HQueryUtil.class);

	public static HCriteria parseSelectedFields(HCriteria crit, String fl) {
		String[] fields = fl.split(",");
		for (String item : fields) {
			crit.addSelectField(item);
		}
		return crit;
	}
	

	public static HCriteria parseHighlightFields(HCriteria crit, String hfl) {
		String[] fields = hfl.split(",");
		for (String item : fields) {
			crit.addHighlightField(item);
		}
		return crit;
	}
	
//	public static Query parseQuery(String q) throws ParseException {
//		SimpleQueryParser parser = new SimpleQueryParser("text");
//		Query query = parser.parse(q);
//		return query;
//	}
//	
//	public static HCriteria parseQuery(HCriteria crit, String q) {		
//		try {
//			Query query = parseQuery(q);
//			System.out.println(query);			
//			JSONObject qObj = query.getJSONObject();
//			parseQuery(crit, qObj);
//		} catch (Exception e) {
//			LOG.warn(e.getMessage());
//			throw new ParseQueryException(e.getMessage());
//		}
////		String[] arr = q.split("\\+");
////		for (String item : arr) {
////			String[] pair = item.split(":");
////			if (pair.length != 2) continue;
////			crit.addLike(pair[0], pair[1]);
////		}
//		return crit;
//	}

	private static void parseQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		if (qObj.has("clauses")) {
			parseClauses(crit, (JSONArray) qObj.get("clauses"));
		} else if (qObj.has("AND")) {
			parseQuery(crit, qObj.getJSONObject("AND"));
		} else if (qObj.has("OR")) {
			parseQuery(crit, qObj.getJSONObject("OR"));			
		} else if (qObj.has("NOT")) {
			parseQuery(crit, qObj.getJSONObject("NOT"));			
		} else {
			String type = qObj.getString("type");
			if ("TermQuery".equals(type)) {
				parseTermQuery(crit, qObj);
			} else if ("PrefixQuery".equals(type)) {
				parsePrefixQuery(crit, qObj);
			} else if ("WildcardQuery".equals(type)) {
				parseWildcardQuery(crit, qObj);				
			} else if ("TermRangeQuery".equals(type)) {
				parseTermRangeQuery(crit, qObj);
			} else if ("MatchAllDocsQuery".equals(type)) {
				parseMatchAllDocsQuery(crit, qObj);
			} else {
				LOG.warn("Unknow query type : " + type);
				throw new JSONException("Unknow query type : " + type);
			}
		}
	}
	
	private static void parseClauses(HCriteria crit, JSONArray list) throws JSONException {
		for (int i=0; i < list.length(); i++) {
			JSONObject qObj = list.getJSONObject(i);
			parseQuery(crit, qObj);
		}
	}
	
	private static void parseTermQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		crit.addLike(fieldName, value);
	}
	
	private static void parsePrefixQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		crit.addLike(fieldName, value + "*");
	}
	
	private static void parseWildcardQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		crit.addLike(fieldName, value);
	}
	
	private static void parseTermRangeQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		String fieldName = qObj.getString("name");
		boolean startInclusive = qObj.getBoolean("startInclusive");
		boolean endInclusive = qObj.getBoolean("endInclusive");
		String start = null;
		String end = null;
		if (qObj.has("start")) {
			start = qObj.getString("start");			
		}
		if (qObj.has("end")) {
			end = qObj.getString("end");
		}
		if (start != null && end == null) {
			if (startInclusive) {
				crit.addGreaterOrEqualThan(fieldName, start);
			} else {
				crit.addGreaterThan(fieldName, start);
			}
		}
		if (start == null && end != null) {
			if (endInclusive) {
				crit.addLessOrEqualThan(fieldName, end);
			} else {
				crit.addLessThan(fieldName, end);
			}
		}
		if (start != null && end != null) {
			crit.addBetween(fieldName, start, end);
		}
	}
	
	private static void parseMatchAllDocsQuery(HCriteria crit, JSONObject qObj) throws JSONException {
		crit.addLike("*", "*");
	}

//	public static HQuery buildHQuery(String schemaName, String q, String fl, 
//			String sort, int start, int rows, String hfl) throws IOException {
//		HQuery hQuery = new HQuery();
//		hQuery.setSchema(schemaName);
//		HCriteria crit = new HCriteria();
//		if (fl != null && fl.length() > 0)
//			HQueryUtil.parseSelectedFields(crit, fl);
//		else {
//			SchemaParser parser;
//			try {
//				parser = new SchemaParser(schemaName);
//				//String defaultFieldName = parser.getUniqueKey().getName();
//				//crit.addSelectField(defaultFieldName);
//				for (Field field : parser.getFields()) {
//					crit.addSelectField(field.getName());
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new IOException(e.getMessage());
//			}
//		}
//		List<Map<String, String>> orderFields = new ArrayList();
//		if (sort != null && sort.length() > 0) {
//			String[] pairs = sort.split(",");
//			for (String pair : pairs) {
//				String[] arr = pair.split(" ");
//				String orderFlag = "ASC";
//				if (arr.length > 1) {
//					orderFlag = arr[1];
//				}
//				Map<String, String> of = new HashMap<String, String>();
//				of.put("field", arr[0]);
//				of.put("op", orderFlag);
//				orderFields.add(of);
//			}
//		}
//		if (hfl != null && hfl.length() > 0) {
//			HQueryUtil.parseHighlightFields(crit, hfl);
//		}
//		HQueryUtil.parseQuery(crit, q);
//		hQuery.setQStr(q);
//		hQuery.setCriteria(crit);
//		hQuery.setOrderFields(orderFields.toArray(new Map[0]));
//		hQuery.setRowStart(start);
//		hQuery.setRowCount(rows);
//		return hQuery;		
//	}

	public static HReportQuery buildHReportQuery(String schemaName, String fl, int numTerms) {
		HReportQuery hQuery = new HReportQuery();
		hQuery.setSchema(schemaName);
		HReportCriteria crit = new HReportCriteria();
		crit.setGroupBy(fl);
		crit.setHavingClause("@NUMTERMS >= " + numTerms);
		hQuery.setCriteria(crit);
		return hQuery;		
	}
	
//	public static HQuery buildHQuery(HMWQuery q) {
//		HQuery hQuery = HMWUtil.toHQuery(q);
//		if (q.qStr.length() > 0) {
//			HCriteria crit = new HCriteria();
//			HQueryUtil.parseQuery(crit, q.qStr);
//			hQuery.setWhereClause((Map<String, String>[]) crit.getWhereClause().toArray(new Map[0]));
//		}
//		return hQuery;
//	}
}
