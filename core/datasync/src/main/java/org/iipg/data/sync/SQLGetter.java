package org.iipg.data.sync;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.iipg.data.sync.conn.ConnManager;
import org.iipg.data.sync.conn.MySQLConnPool;
import org.iipg.data.sync.ds.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;

public class SQLGetter {

	private Connection conn = null;
	private MySQLConnPool pool = null;
	private DataSource ds = null;
	
	public SQLGetter(DataSource dataSource) {
		this.ds = dataSource;
		pool = ConnManager.getDBConnPool(ds);
	}

	public JSONArray get(String qStr) {
		JSONArray results = new JSONArray();

		conn = pool.getConnection();
		
		if (conn == null) {
			throw new NullPointerException("Cannot get DB connection.");
		}

		try {
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(qStr);

			ResultSetMetaData rsmd = rs.getMetaData();
			int numCols = rsmd.getColumnCount(); 

			while(rs.next()) {
				JSONObject curObj = new JSONObject();

				for (int i = 1; i <= numCols; i++) {
					JSONObject curCol = new JSONObject();
					String colName = rsmd.getColumnName(i);
					int colType = rsmd.getColumnType(i);
					Object colData = rs.getObject(i);
					curCol.put("type", colType);
					switch (colType) {
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						if (colData != null)
							colData = new Date(((Timestamp) colData).getTime());
						break;
					}
					if (colData != null)
						curCol.put("value", colData);
					else
						curCol.put("value", "");

					curObj.put(colName, curCol);
				}

				results.put(curObj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.return2Pool(conn);
		}

		return results;
	}
}
