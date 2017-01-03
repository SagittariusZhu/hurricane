package org.iipg.hurricane.solr.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.search.Query;
import org.iipg.hurricane.solr.SolrUpdateQuery;
import org.iipg.hurricane.util.TimeTool;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SolrUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdfSolr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

	public static SolrQuery toSolrQuery(HDBQuery hQuery) {
		SolrQuery query = new SolrQuery(hQuery.getSql());
		query.setStart(hQuery.getRowStart());
		query.setRows(hQuery.getRowCount());
		List<String> fields = hQuery.getSelectFields();
		query.setFields(fields.toArray(new String[0]));
		List<String> highlightFields = hQuery.getHighlightFields();
		if (highlightFields != null && highlightFields.size() > 0) {
			query.setHighlight(true);
			if ("abs".equalsIgnoreCase(hQuery.getHighlightMode())) {
				query.setHighlightFragsize(50);
				query.setHighlightSnippets(3);
			} else {
				query.setHighlightFragsize(0);
			}
			for (String f : highlightFields)
				query.addHighlightField(f);
		}
		Iterator<String> sortFields = hQuery.getSortFields().keySet().iterator(); 
		while (sortFields.hasNext()){
			String key = sortFields.next();
			SolrQuery.ORDER order = SolrQuery.ORDER.desc;
			if ( hQuery.getSortFields().get(key).equalsIgnoreCase("asc") ){
				order = SolrQuery.ORDER.asc;
			}
			query.addOrUpdateSort(key, order);
		}
		return query;
	}
	
	public static Date correctDate(Date fieldValue, boolean store) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(fieldValue);
		if (store)
			cal.add(Calendar.HOUR, 8);
		else
			cal.add(Calendar.HOUR, -8);
		return cal.getTime();
	}
	
	public static String firstUpper(String str) {
		StringBuffer sb = new StringBuffer(str);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	public static String getSolrSql(SchemaParser schema, Query q) throws JSONException {
		StringBuffer buf = new StringBuffer();
		JSONObject qObj = q.getJSONObject();
	
		buf.append(parseQuery(schema, qObj));
		
		return buf.toString();
	}
	
	private static String parseQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		String ret = "";
		if (qObj.has("clauses")) {
			ret = parseClauses(schema, (JSONArray) qObj.get("clauses"));
		} else if (qObj.has("AND")) {
			ret = " +" + parseQuery(schema, qObj.getJSONObject("AND"));
		} else if (qObj.has("OR")) {
			ret = parseQuery(schema, qObj.getJSONObject("OR"));			
		} else if (qObj.has("NOT")) {
			ret = " -" + parseQuery(schema, qObj.getJSONObject("NOT"));			
		} else {
			String type = qObj.getString("type");
			if ("TermQuery".equals(type)) {
				ret = parseTermQuery(schema, qObj);
			} else if ("PrefixQuery".equals(type)) {
				ret = parsePrefixQuery(schema, qObj);	
			} else if ("WildcardQuery".equals(type)) {
				ret = parseWildcardQuery(schema, qObj);				
			} else if ("TermRangeQuery".equals(type)) {
				ret = parseTermRangeQuery(schema, qObj);
			} else if ("MatchAllDocsQuery".equals(type)) {
				ret = parseMatchAllDocsQuery(schema, qObj);
			} else {
				throw new JSONException("Unknow query type : " + type);
			}
		}
		return ret;
	}
	
	private static String parseClauses(SchemaParser schema, JSONArray list) throws JSONException {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i=0; i < list.length(); i++) {
			JSONObject qObj = list.getJSONObject(i);
			buf.append(parseQuery(schema, qObj) + " ");
		}
		buf.append(")");
		return buf.toString();
	}
	
	private static String parseTermQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name").replace(".", "_");
		Field field = schema.getField(fieldName);
		String value = getSolrValue(field, qObj.getString("value"));
		buf.append(fieldName + ":\"" + value + "\"");
		return buf.toString();
	}
	
	private static String parsePrefixQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name").replace(".", "_");
		Field field = schema.getField(fieldName);
		String value = getSolrValue(field, qObj.getString("value"));
		buf.append(fieldName + ":\"" + value + "\"");
		return buf.toString();
	}
	
	private static String parseWildcardQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name").replace(".", "_");
		Field field = schema.getField(fieldName);
		String value = getSolrValue(field, qObj.getString("value"));
		buf.append(fieldName + ":" + value + "");
		return buf.toString();
	}
	
	private static String parseTermRangeQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		StringBuffer buf = new StringBuffer();
		String fieldName = qObj.getString("name").replace(".", "_");
		Field field = schema.getField(fieldName);
		boolean startInclusive = qObj.getBoolean("startInclusive");
		boolean endInclusive = qObj.getBoolean("endInclusive");
		String start = null;
		String end = null;
		if (qObj.has("start")) {
			start = getSolrValue(field, qObj.getString("start"));			
		}
		if (qObj.has("end")) {
			end = getSolrValue(field, qObj.getString("end"));
		}
		if (start != null && end == null) {
			if (startInclusive) {
				buf.append(fieldName + ":[" + start + " TO *]");
			} else {
				buf.append(fieldName + ":{" + start + " TO *]");
			}
		}
		if (start == null && end != null) {
			if (endInclusive) {
				buf.append(fieldName + ":[* TO " + end + "]");
			} else {
				buf.append(fieldName + ":[* TO " + end + "}");
			}
		}
		if (start != null && end != null) {
			buf.append(fieldName + ":");
			if (startInclusive) {
				buf.append("[" + start + " TO ");
			} else {
				buf.append("{" + start + " TO ");				
			}
			if (endInclusive) {
				buf.append(end + "]");
			} else {
				buf.append(end + "}");
			}
		}
		return buf.toString();
	}
	
	private static String parseMatchAllDocsQuery(SchemaParser schema, JSONObject qObj) throws JSONException {
		return "*:*";
	}
	
	
	private static String getSolrValue(Field field, String value) {
		String solrValue = value;
		
		if (HTyper.isExtType(field.getType())) {
			try {
                HTyper typer = HTyper.getTyper(field);
                solrValue = typer.getQueryValue(value);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		} else if ("date".equalsIgnoreCase(field.getType())) {
			try {
				Date d = TimeTool.parse(value);
				solrValue = sdfSolr.format(d);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("string".equalsIgnoreCase(field.getType())) {
			solrValue = value.replaceAll("\\:", "\\\\:");
		}
		return solrValue;
	}
	
	//-----------------------------------------------------------------
	
	public static HDBRecord buildUpdateRecord(SolrUpdateQuery q) {
		HDBRecord record = new HDBRecord();
		List<HDBCondition> conds = q.getWhereClauses();
		for (HDBCondition cond : conds) {
			record.put(cond.getFieldName(), cond.getFieldValue());
		}
		Map<String, Object> updateFields = q.getUpdateFields();
		for (Iterator<String> it = updateFields.keySet().iterator(); it.hasNext(); ) {
			String fieldName = it.next();
			Map<String, String> setOper = q.getUpdateOper(fieldName);
			record.put(fieldName, setOper);
		}
		return record;
	}

	public static CloudSolrClient getCloudSolrClient(String dbName) {
		CloudSolrClient cloudSolrClient = SolrClientCache.getCloudSolrClient();
		if (cloudSolrClient != null)
			cloudSolrClient.setDefaultCollection(dbName);
		
		return cloudSolrClient;
	}
	
	public static HttpSolrClient getHttpSolrAdminClient(String url) {
		String realUrl = url;
		if (!url.startsWith("http://"))
			realUrl = "http://" + url + "/solr";
		
		HttpSolrClient client = new HttpSolrClient(realUrl);
		return client;
	}

	public static HttpSolrClient getHttpSolrClient(String url, String dbName) {
		if (dbName != null) {
			url += "/" + dbName;
		}
		HttpSolrClient client = new HttpSolrClient(url);
		return client;
	}

	public static void releaseClient(SolrClient solrClient) {
		SolrClientCache.release(solrClient);		
	}
}
