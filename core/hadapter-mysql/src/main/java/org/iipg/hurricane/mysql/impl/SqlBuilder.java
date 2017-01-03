package org.iipg.hurricane.mysql.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.iipg.hurricane.search.Query;
import org.iipg.hurricane.util.DateTool;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlBuilder {
	private static Logger LOG = LoggerFactory.getLogger(SqlBuilder.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String getMysqlSql(Query q) throws JSONException {
		StringBuffer buf = new StringBuffer();
		JSONObject qObj = q.getJSONObject();
	
		buf.append(parseQuery(qObj));
		LOG.debug(buf.toString());
		return buf.toString();
	}
	
	private static String parseQuery(JSONObject qObj) throws JSONException {
		String ret = "";
		if (qObj.has("clauses")) {
			ret = parseClauses((JSONArray) qObj.get("clauses"));
		} else if (qObj.has("AND")) {
			ret = parseQuery(qObj.getJSONObject("AND"));
		} else if (qObj.has("OR")) {
			ret = parseQuery(qObj.getJSONObject("OR"));			
		} else if (qObj.has("NOT")) {
			ret = " NOT (" + parseQuery(qObj.getJSONObject("NOT")) + ")";			
		} else {
			String type = qObj.getString("type");
			if ("TermQuery".equals(type)) {
				ret = parseTermQuery(qObj);
			} else if ("PrefixQuery".equals(type)) {
				ret = parsePrefixQuery(qObj);	
			} else if ("WildcardQuery".equals(type)) {
				ret = parseWildcardQuery(qObj);				
			} else if ("TermRangeQuery".equals(type)) {
				ret = parseTermRangeQuery(qObj);
			} else if ("MatchAllDocsQuery".equals(type)) {
				ret = parseMatchAllDocsQuery(qObj);
			} else {
				throw new JSONException("Unknow query type : " + type);
			}
		}
		return ret;
	}
	
	private static String parseClauses(JSONArray list) throws JSONException {
		StringBuffer buf = new StringBuffer();

		if (list.length() < 1) return "";
		
		JSONObject qObj = list.getJSONObject(0);
		String oper = "";
		if (qObj.has("AND")) {
			oper = "AND";
		} else if (qObj.has("OR")) {
			oper = "OR";
		} else if (qObj.has("NOT")) {
			oper = "NOT";
		}
		
		if (list.length() == 1) {
			if ("NOT".equals(oper))
				buf.append(oper + " " + parseQuery(qObj));
			else
				buf.append(parseQuery(qObj));
			return buf.toString();
		}
		
		buf.append("(").append(parseQuery(qObj));
		for (int i=1; i < list.length(); i++) {
			qObj = list.getJSONObject(i);
			buf.append(" " + oper + " " + parseQuery(qObj));
		}
		buf.append(")");
		return buf.toString();
	}
	
	private static String parseTermQuery(JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		if (value.indexOf("*") >= 0 || value.indexOf("?") >= 0)
			buf.append(fieldName + " like " + value);
		else
			buf.append(fieldName + "=" + valueToString(value));
		return buf.toString();
	}
	
	private static String parsePrefixQuery(JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		buf.append(fieldName + " like '" + value + "%'");
		return buf.toString();
	}
	
	private static String parseWildcardQuery(JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name");
		String value = qObj.getString("value");
		value = value.replaceAll("[?]", "_")
					 .replaceAll("\\*", "%");
		buf.append(fieldName + " like '" + value + "'");
		return buf.toString();
	}
	
	private static String parseTermRangeQuery(JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
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
				buf.append(fieldName + ">=" + valueToString(start));
			} else {
				buf.append(fieldName + ">" + valueToString(start));
			}
		}
		if (start == null && end != null) {
			if (endInclusive) {
				buf.append(fieldName + "<=" + valueToString(end));
			} else {
				buf.append(fieldName + "<" + valueToString(end));
			}
		}
		if (start != null && end != null) {
			buf.append(fieldName + " BETWEEN ");
			if (startInclusive) {
				buf.append(valueToString(start) + " AND ");
			} else {
				buf.append(valueToString(start) + " AND ");				
			}
			if (endInclusive) {
				buf.append(valueToString(end));
			} else {
				buf.append(valueToString(end));
			}
		}
		return buf.toString();
	}
	
	private static String parseMatchAllDocsQuery(JSONObject qObj) throws JSONException {
		return "*:*";
	}

	public static String valueToString(Object value) {
		String retValue = "";
		if (value instanceof String) {

			//try Date
			try {
				Date newValue = DateTool.string2Date((String) value);
				retValue = "'" + sdf.format(newValue) + "'";
				return retValue;
			} catch (ParseException ignore) {}

			//try Integer
			try {
				Integer.parseInt((String) value);
				return (String) value;
			} catch (Exception ignore) {}
			
			//try float
			try {
				Float.parseFloat((String) value);
				return (String) value;
			} catch (Exception ignore) {}

			retValue =  "'" + value + "'";
			
			return retValue;
		} else if (value instanceof Date) {
			retValue = "'" + sdf.format((Date) value) + "'";
			return retValue; 
		} else {
			retValue = "" + value;
		}

		return retValue;
	}
	
	public static  String valueToString(String fieldType, Object value) {
		String retValue = "";
		if ("string".equalsIgnoreCase(fieldType)) {
			retValue = "'" + value + "'";
		} else if ("date".equalsIgnoreCase(fieldType)) {
			if (value instanceof Date) {
				retValue = "'" + sdf.format((Date)value) + "'";
			} else if (value instanceof String){
				try {
					Date newValue = DateTool.string2Date((String) value);
					retValue = "'" + sdf.format(newValue) + "'";
				} catch (ParseException e) {
					retValue = "'" + value + "'";
				}
			} else {
				retValue = "'" + value + "'";
			}
		} else {
			retValue = "" + value;
		}

		return retValue;
	}	
}
