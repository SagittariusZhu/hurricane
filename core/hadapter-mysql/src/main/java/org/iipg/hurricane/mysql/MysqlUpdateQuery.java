package org.iipg.hurricane.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.mysql.impl.SqlBuilder;

public class MysqlUpdateQuery extends HDBUpdateQuery {
	
	private static final Log LOG = LogFactory.getLog(MysqlUpdateQuery.class);
	
	public MysqlUpdateQuery(SchemaParser schemaParser, HDBBaseObject object) {
		super(schemaParser, object);
	}
	
	public MysqlUpdateQuery() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getUpdateSql() {
		int pos = 0;
		StringBuffer sqlStr = new StringBuffer("update ");

		sqlStr.append(this.schemaName);
		
		StringBuffer setClause = new StringBuffer();
		for (String fieldName : updateFields.keySet()) {
			Field field = schema.getField(fieldName);
			Object value = updateFields.get(fieldName);
			setClause.append(fieldName)
					 .append("=")
					 .append(SqlBuilder.valueToString(field.getType(), value))
					 .append(",");
		}
		pos = setClause.lastIndexOf(",");
		if (pos > 0)
			setClause.replace(pos, pos + 1, "");
		if (setClause.length() > 0) {
			sqlStr.append(" set ")
				  .append(setClause);
		}
		
		StringBuffer where = new StringBuffer();
		for (Object clause : whereClauses) {
			HDBCondition cond = (HDBCondition) clause;			
			where.append(buildWhereClause(cond))
				 .append(" and ");
		}
		pos = where.lastIndexOf("and");
		if (pos > 0)
			where.replace(pos, pos + 4, "");
		if (where.length() > 0)
			sqlStr.append(" where ")
				  .append(where);

		LOG.debug(sqlStr);

		return sqlStr.toString();
	}
	
	private String buildWhereClause(HDBCondition cond) {
		Field field = schema.getField(cond.getFieldName());
		String type = field.getType();
		
		return cond.getSQL92(type);
	}
}
