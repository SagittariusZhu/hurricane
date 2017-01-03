package org.iipg.data.sync.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.iipg.data.sync.conf.DBConfiguration;
import org.iipg.data.sync.ds.MySQLDataSource;
import org.iipg.data.sync.store.AbstractStorer;
import org.iipg.data.sync.store.SQLStorer;
import org.json.JSONArray;
import org.json.JSONObject;

public class RestfulRouter {

	private AbstractStorer storer = null;
	
	public RestfulRouter(DBConfiguration conf) {
		if (conf.getDbType().equals("mysql")) {
			
			MySQLDataSource ds = new MySQLDataSource();
			
			ds.setUrl(buildUrl(conf));
			ds.setUser(conf.getUsername());
			ds.setPassword(conf.getPassword());
			ds.setDriver(conf.getDriverClassName());
			
			storer = new SQLStorer(ds);
		}
	}

	private String buildUrl(DBConfiguration conf) {
		String url = String
				.format("jdbc:mysql://%s:%s/%s",
						conf.getHost(), conf.getPort(), conf.getDbname());
		return url;
	}

	public void store(String tbName, JSONObject metads, JSONArray records) {
		if (storer != null) {
			Map<String, String> colCache = new HashMap<String, String>();
			for (Iterator<String> it = metads.keys(); it.hasNext(); ) {
				String key = it.next();
				colCache.put(key, metads.getString(key));
			}
			storer.store(tbName, colCache, records);
		}
	}

}
