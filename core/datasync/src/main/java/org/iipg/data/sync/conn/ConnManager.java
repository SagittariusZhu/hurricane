package org.iipg.data.sync.conn;

import java.util.HashMap;
import java.util.Map;

import org.iipg.data.sync.ds.DataSource;
import org.iipg.data.sync.ds.MySQLDataSource;

public class ConnManager {

	private static Map dbPools = new HashMap();
	
	public static synchronized MySQLConnPool getDBConnPool(DataSource ds) {
		if (!dbPools.containsKey(ds.getName())) {
			MySQLConnPool pool = new MySQLConnPool((MySQLDataSource) ds);
			dbPools.put(ds.getName(), pool);
		}
			
		return (MySQLConnPool) dbPools.get(ds.getName());
	}
	
}
