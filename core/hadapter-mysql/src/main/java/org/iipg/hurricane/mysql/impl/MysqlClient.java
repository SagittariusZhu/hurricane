package org.iipg.hurricane.mysql.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.mysql.ConnectionPoolMysql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlClient {
	
	private static Logger LOG = LoggerFactory.getLogger(MysqlClient.class);
	
	private static ConnectionPoolMysql pool = new ConnectionPoolMysql();
	static {
		pool.init();
	}
	
	public static HDBResultSet fetchData(String countSql, String sql) throws HQueryException {
		
		HDBResultSet result = new HDBResultSet();
	    int rowCount = 0;
	    
		try {
			long start = System.currentTimeMillis();
			ResultSet resultSet = MysqlClient.executeSQL(countSql);
			if (resultSet.next()) {
				int totalRowCount = resultSet.getInt(1);;
				result.setTotalCount(totalRowCount);
			}
			
			resultSet = MysqlClient.executeSQL(sql);
			ResultSetMetaData rsmd = resultSet.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
       
			while(resultSet.next()){
				HDBRecord record = new HDBRecord();
				for(int i=1;i<=numberOfColumns;i++){
					String column=rsmd.getColumnName(i);
					record.put(column, resultSet.getObject(column));
				}
				result.addItem(record);
				rowCount ++;
			}
			long end = System.currentTimeMillis();
			result.setCount(rowCount);
			result.setConsumeTime((int) (end - start));
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}

		return result;
	}

	public static boolean hasTable(String name) throws SQLException {
		Connection conn = pool.getConnection();
		if(conn == null || conn.isClosed()) {
			LOG.warn("Get connection from pool error!");
			throw new SQLException("Get connection from pool error!");
		}
		boolean result = false;
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet set = meta.getTables (null, null, name, null);
			if (set.next()) {
				result = true;
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		} finally {
			pool.return2Pool(conn);
		}
		return result;
	}
	
	public static int executeUpdateSQL(String sql) throws SQLException {
		LOG.debug("Execute update SQL: " + sql);
		Connection conn = pool.getConnection();
		if(conn == null || conn.isClosed()) {
			LOG.warn("Get connection from pool error!");
			throw new SQLException("Get connection from pool error!");
		}
		try {
			Statement stmt = conn.createStatement();
			int ret = stmt.executeUpdate(sql);
			LOG.debug("Execute successed.");
			return ret;
		} finally {
			pool.return2Pool(conn);
		}
	}
	
	public static ResultSet executeSQL(String sql) throws SQLException {
		LOG.debug("Execute SQL: " + sql);
		Connection conn = pool.getConnection();
		if(conn == null || conn.isClosed()) {
			LOG.warn("Get connection from pool error!");
			throw new SQLException("Get connection from pool error!");
		}
		try {
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(sql);
			LOG.debug("Execute successed.");
			return resultSet;
		} finally {
			pool.return2Pool(conn);
		}
	}
}
