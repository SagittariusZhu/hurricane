package org.iipg.data.sync.ds;

import org.iipg.data.sync.ds.*;

public class DataSourceFactory {

	public static DataSource newInstance(String type) {
		if (type.equalsIgnoreCase("mysql"))
			return new MySQLDataSource();
		if (type.equalsIgnoreCase("solr"))
			return new SolrDataSource();
		if (type.equalsIgnoreCase("restful"))
			return new RestfulDataSource();
		return null;
	}

}
