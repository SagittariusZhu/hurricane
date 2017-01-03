package org.iipg.data.sync.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;

import org.iipg.data.sync.conn.ConnManager;
import org.iipg.data.sync.conn.MySQLConnPool;
import org.iipg.data.sync.ds.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLStorer extends AbstractStorer {
	
	private static final Logger LOG = LoggerFactory.getLogger(SQLStorer.class);

	private Connection conn = null;
	private MySQLConnPool pool = null;

	public SQLStorer(DataSource ds) {
		pool = ConnManager.getDBConnPool(ds);
	}

	@Override
	public int store(String tbName, Map<String, String> metads, JSONArray records) {
		conn = pool.getConnection();

		if (conn == null) {
			throw new NullPointerException("Cannot get DB connection.");
		}
		
		String insertSQL = buildPreparedSQL(tbName, metads, records.getJSONObject(0));
		PreparedStatement stmt = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(insertSQL);

			for (int i=0; i<records.length(); i++) {
				JSONObject rowObj = records.getJSONObject(i);
				int j = 1;
				for (Iterator<String> it = rowObj.keys(); it.hasNext();) {
					String colName = it.next();
					JSONObject value = rowObj.getJSONObject(colName);
					int colType = value.getInt("type");
					switch (colType) {
					case Types.INTEGER:
						stmt.setInt(j++, value.getInt("value"));
						break;
					case Types.VARCHAR:
						stmt.setString(j++, value.getString("value"));
						break;
					default:
						stmt.setObject(j++, value.get("value"));
						break;
					}
				}
				stmt.addBatch();
			}

			int[] counts = stmt.executeBatch();
			conn.commit();
			
			int number = 0;
			for (int i=0; i<counts.length; i++) {
				number += counts[i];
			}
			
			System.out.println("----------------------------------------");
			
			return number;
		} catch (SQLException e) {
			LOG.warn(e.getSQLState());
			//e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			pool.return2Pool(conn);
		}
		return 0;
	}

	private String buildPreparedSQL(String tbName, Map<String, String> metads, JSONObject rowObj) {
		StringBuffer sb = new StringBuffer("INSERT INTO ");
		sb.append(tbName)
		  .append("(");
		for (Iterator<String> it = rowObj.keys(); it.hasNext(); ) {
			String key = it.next();
			
			String realName = key;
			if (metads.containsKey(key))
				realName = metads.get(key);
			
			sb.append(realName).append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), ")");
		sb.append(" VALUES (");
		for (Iterator<String> it = rowObj.keys(); it.hasNext(); ) {
			it.next();
			sb.append("?,");
		}
		sb.replace(sb.length() - 1, sb.length(), ")");
		return sb.toString();
	}
}