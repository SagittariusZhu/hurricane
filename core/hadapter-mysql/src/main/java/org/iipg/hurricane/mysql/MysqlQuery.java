package org.iipg.hurricane.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.mysql.impl.SqlBuilder;
import org.json.JSONException;

public class MysqlQuery extends HDBQuery {
	
	private static final Log LOG = LogFactory.getLog(MysqlQuery.class);
	
	@Override
	public String getSql() {

		StringBuffer sqlStr = new StringBuffer("select ");

		for (Object clause : selectFields) {
			String name = (String) clause;
			sqlStr.append(name).append(",");
		}
		int pos = sqlStr.lastIndexOf(",");
		sqlStr.replace(pos, pos + 1, "");

		sqlStr.append(" from ").append(this.schemaName);
		
		try {
			String where = getWhereStr();
			if (where.length() > 0)
				sqlStr.append(" where ")
				.append(where);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer orderby = new StringBuffer();
		for (String fieldName : sortFields.keySet()) {
			String op = (String) sortFields.get(fieldName);
			orderby.append(fieldName)
			.append(" ")
			.append(op)
			.append(",");
		}
		pos = orderby.lastIndexOf(",");
		if (pos > 0)
			orderby.replace(pos, pos + 1, "");
		if (orderby.length() > 0) {
			sqlStr.append(" order by ")
				  .append(orderby);
		}

		sqlStr.append(" limit ")
			  .append(rowStart)
			  .append(",")
			  .append(rowCount);

		LOG.debug(sqlStr);

		return sqlStr.toString();
	}

	@Override
	public String getCountSql() {
		StringBuffer sqlStr = new StringBuffer("select count(*) from ").append(this.schemaName);
			
		try {
			String where = getWhereStr();
			if (where.length() > 0)
				sqlStr.append(" where ")
				.append(where);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sqlStr.toString();
	}
	
	private String getWhereStr() throws JSONException {
		if (whereQuery == null) {
			StringBuffer where = new StringBuffer();
			for (Object clause : whereClauses) {
				HDBCondition cond = (HDBCondition) clause;			
				where.append(buildWhereClause(cond))
				.append(" and ");
			}
			int pos = where.lastIndexOf("and");
			if (pos > 0)
				where.replace(pos, pos + 4, "");
			return where.toString();
		} else {
			return SqlBuilder.getMysqlSql(whereQuery);		
		}
	}
	
	private String buildWhereClause(HDBCondition cond) {
		Field field = schema.getField(cond.getFieldName());
		String type = field.getType();
		
		return cond.getSQL92(type);
	}




}
