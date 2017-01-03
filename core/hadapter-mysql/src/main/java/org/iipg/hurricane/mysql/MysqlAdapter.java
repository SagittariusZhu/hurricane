package org.iipg.hurricane.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.HInsertException;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.StoreException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.HDBEqualCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.util.HDBUtil;
import org.iipg.hurricane.mysql.impl.MysqlClient;
import org.iipg.hurricane.mysql.impl.SqlBuilder;
import org.iipg.hurricane.search.Query;
import org.xml.sax.SAXException;

public class MysqlAdapter extends HAdapter {
	public static final Log LOG = LogFactory.getLog(MysqlAdapter.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public MysqlAdapter(HurricaneConfiguration conf, String dbName) throws ParserConfigurationException, SAXException, IOException {
		super(conf, dbName);
	}
	
	@Override
	public HDBResultSet query(HDBQuery con) throws HQueryException {
		LOG.debug("select from  " + getDbName() + ":" + con.getWhereClauses2());
		
		try {
			if (!MysqlClient.hasTable(getDbName())) {
				String msg = "Table " + getDbName() + " is not exist!";
				LOG.info(msg);
				return null;
			} else {
				LOG.info("Check table " + getDbName() + " is OK!");
			}
		} catch (SQLException e) {
			LOG.info(e.getMessage());
			throw new HQueryException(e.getMessage());
		}
		String countSql = con.getCountSql();
		String sql = con.getSql();
		
		return MysqlClient.fetchData(countSql, sql);
	}
		
	@Override
	public HDBBaseObject queryByID(Object uuid, List<String> fields) throws Exception {
		Field uuidField = this.getSchemaParser().getUniqueKey();
		
		HDBQuery q = new MysqlQuery();
		q.setSchema(getSchemaParser());
		q.setSchemaName(getDbName());
		
		for (String field : fields) {
			q.addSelectField(field);
		}
		q.addWhereClause(new HDBEqualCondition(uuidField.getName(), uuid));
		q.setWhereClauses2(null);
		HDBResultSet rset = query(q);
		if (rset != null && rset.getCount() > 0)
			return rset.getItem(0);
		return null;
	}
	
	@Override
	public int store(HDBBaseObject object) throws Exception {
		LOG.debug("insert into " + getDbName() + " value ( " + object + " )");
		
		if (!(object instanceof HDBRecord)) {
			LOG.warn("store invalid type object!");
			return 0;
		}
		
		HDBRecord record = (HDBRecord) object;
		
		try {		
			StringBuffer columns = new StringBuffer();
			StringBuffer values = new StringBuffer();
			
			Map map = record.getInformation();
			if (map.size() <= 1) return 0;
			
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ){
				String column = (String) iter.next();
				Object value = map.get(column);
				
				columns.append(column).append(",");
				values.append(SqlBuilder.valueToString(value)).append(",");
			}
			int len = columns.lastIndexOf(",");
			columns.replace(len, len + 1, "");
			len = values.lastIndexOf(",");
			values.replace(len, len + 1, "");
			String statment = "insert into " + getDbName() + " (" + columns + ") values (" + values +")";
			LOG.debug(statment);	
			MysqlClient.executeUpdateSQL(statment);
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			throw new StoreException(e.getMessage());
		}
		return 1;
	}
	
	@Override
	public int store(List<HDBBaseObject> list) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int deleteByID(String uuid) throws Exception {
		// TODO Auto-generated method stub
		LOG.debug("delete from " + getDbName());
		Field uuidField = this.getSchemaParser().getUniqueKey();
		String sql = "DELETE FROM " + getDbName() + " WHERE " + uuidField.getName() 
						+ "='" + uuid + "'";
		int retSize = MysqlClient.executeUpdateSQL(sql);
		return retSize;
	}
	
	@Override
	public int deleteByIDs(List<String> list) throws Exception {
		LOG.debug("delete from " + getDbName());
		Field uuidField = this.getSchemaParser().getUniqueKey();
		StringBuffer values = new StringBuffer();
		for (String item : list ){
			values.append(SqlBuilder.valueToString(item)).append(",");
		}
		int len = values.lastIndexOf(",");
		values.replace(len, len + 1, "");
		String sql = "DELETE FROM " + getDbName() + " WHERE " + uuidField.getName() 
						+ " IN (" + values.toString() + ")";
		int retSize = MysqlClient.executeUpdateSQL(sql);
		return retSize;
	}
	
	@Override
	public int delete(HDBQuery con) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isAutoCommit() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setAutoCommit(boolean autoCommit) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void close() {
		
	}

	@Override
	public void fillResultSet(HDBResultSet rset, List<String> fields) {
		if (rset == null || rset.getCount() == 0) return;
		Field uuidField = this.getSchemaParser().getUniqueKey();

		for (int i=0; i<rset.getCount(); i++) {
			HDBRecord record = (HDBRecord) rset.getItem(i);
			Object uuid = record.get(uuidField.getName());
			try {
				HDBRecord anotherRecord = (HDBRecord) queryByID(uuid, fields);
				if (anotherRecord == null) {
					LOG.warn("No record for " + uuid + " in Mysql DB.");
					continue;
				}
				for (String field : fields) {
					record.put(field, anotherRecord.get(field));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public HDBQuery createQuery() {
		return new MysqlQuery();
	}

	@Override
	public HDBUpdateQuery createUpdateQuery() {
		return new MysqlUpdateQuery();
	}
	
	@Override
	public int update(HDBBaseObject object) throws Exception {
		LOG.debug("update " + getDbName() + " value ( " + object + " )");
		
		HDBUpdateQuery q = new MysqlUpdateQuery(getSchemaParser(), object);
		
		int ret = update(q);
		if (ret == 0) {
			ret = store(object);
		}
		return ret;
	}

	@Override
	public int update(HDBUpdateQuery q) throws Exception {
		String sql = q.getUpdateSql();
		
		int rows = 0;
		
		try {
			rows = MysqlClient.executeUpdateSQL(sql);
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			return 0;
		}
		return rows;
	}

	@Override
	public HDBReportQuery createReportQuery() {
		return new MysqlReportQuery();
	}

	@Override
	public HDBResultSet reportQuery(HDBReportQuery roQuery)
			throws HQueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResultSet copyToByQuery(HAdapter adapter, HDBQuery hQuery)
			throws Exception {
		LOG.debug("Copy from  " + getDbName() + " to " + adapter.getDbName()
				+ ":" + hQuery.getWhereClauses2());
		long start = System.currentTimeMillis();
		
		String sql = "INSERT INTO " + adapter.getDbName()
						+ "  " + hQuery.getSql();
		
		int rows = 0;
		try {
			rows = MysqlClient.executeUpdateSQL(sql);
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			throw new HInsertException(e.getMessage());
		}
		
		HDBResultSet rset = query(hQuery);
		rset.setConsumeTime((int) (System.currentTimeMillis() - start));
		return rset;
	}

	@Override
	public int copyToByList(HAdapter adapter, List<String> list)
			throws Exception {

		Field uuidField = this.getSchemaParser().getUniqueKey();

		StringBuffer sql = new StringBuffer(); 
		sql.append("INSERT INTO " + adapter.getDbName())
		   .append(" SELECT * FROM " + getDbName())
		   .append(" WHERE " + uuidField.getName() + " IN (");
		for (String id : list) {
			if ("string".equalsIgnoreCase(uuidField.getType()))
				sql.append("'" + id + "',");
			else
				sql.append(id + ",");
		}
		int pos = sql.lastIndexOf(",");
		if (pos > 0)
			sql.replace(pos, pos + 1, ")");
		
		int rows = 0;
		try {
			rows = MysqlClient.executeUpdateSQL(sql.toString());
		} catch (SQLException e) {
			LOG.warn(e.getMessage());
			throw new HInsertException(e.getMessage());
		}
		
		return rows;
	}

	@Override
	public void optimize() {
		// TODO Auto-generated method stub
		
	}

}
