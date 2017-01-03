package org.iipg.hurricane.solr;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBMultiClause;
import org.iipg.hurricane.db.query.HDBMultiCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBSpanCondition;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.json.JSONException;

public class SolrQuery extends HDBQuery {

	public static final Log LOG = LogFactory.getLog(SolrQuery.class);
	
	private static SimpleDateFormat sdfNormal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdfSolr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
	private static String defaultTZ = "UTC";
	
	private String timeZoneID = TimeZone.getDefault().getID();
	
	@Override
	public String getSql() {
		String retVal = "";
		if (whereQuery != null) {
			try {
				retVal = SolrUtil.getSolrSql(schema, whereQuery);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new HQueryException(e.getMessage());
			}
		} else {
			retVal = getSqlOld();
		}
		LOG.info(retVal);
		return retVal;
	}
		
	public String getSqlOld() {

		StringBuffer solrQL = new StringBuffer();
		
		for (Object clause : whereClauses) {
			HDBCondition cond = (HDBCondition) clause;
			solrQL.append(buildQueryClause(cond));				
		}
		
		// solr not support sort

		if(solrQL.length() == 0)
			solrQL.append("*:*");

		LOG.debug(solrQL);

		return solrQL.toString();
	}
	
	public String buildQueryClause(HDBCondition cond) {
		String fieldName = cond.getFieldName();
		
		//support match all docs.
		if ("*".equals(fieldName)) {
			return "*:*";
		}
		
		Field field = schema.getField(fieldName);
		String type = field.getType();
		String clause = "+" + cond.getFieldName() + ":";
		
		try {
			Class<?> clz = Class.forName("org.iipg.hurricane.solr.QueryClauseBuilder");
			Object o = clz.newInstance();
			String[] arr = cond.getConditionType().split("\\.");
			String methodName = "to" + SolrUtil.firstUpper(type) + "ClauseFor" + arr[arr.length - 1];
			try {
				Method m = clz.getMethod(methodName, HDBCondition.class);
				clause += m.invoke(o, cond);
			} catch (NoSuchMethodException nse) {
				clause += cond.getFieldValue();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clause += " ";
		
		return clause;
	}
	
	public static String parseConditions(HDBCondition condition){
		String sql = "";
		if(condition.getConditionType().equals(HDBMultiClause.ConditionType.SPAN)){
			HDBSpanCondition span = (HDBSpanCondition)condition;
//			sql = span.getFieldName() + ":" + span.getLowerFlag() + span.getLowerValue() 
//					+ " TO " + span.getUpperValue() + span.getUpperFlag();
			sql = span.toString();
			return sql;
		}else if(condition.getConditionType().equals(HDBMultiClause.ConditionType.MULTI)){
			HDBMultiCondition multi = (HDBMultiCondition)condition;
			for(int index=0; index<multi.getClauseCount(); index++){
				sql = sql + multi.getHConditionRelation(index) + "(" +
						parseConditions(multi.getHCondition(index)) + ") ";
			}
			return sql;
		}
		return null;
	}

	@Override
	public String getCountSql() {
		// TODO Auto-generated method stub
		return null;
	}
}
