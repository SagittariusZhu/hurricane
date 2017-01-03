/**
 * 
 */
package org.iipg.data.sync.conn;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.data.sync.ds.MySQLDataSource;

public class MySQLConnPool {

	private static final Log LOG = LogFactory.getLog(MySQLConnPool.class);

	private PoolDataSource pds = null;

	public MySQLConnPool(MySQLDataSource ds) {
		// Create pool-enabled data source instance.
		pds = PoolDataSourceFactory.getPoolDataSource();
		// PoolDataSource and UCP configuration
		// set the connection properties on the data source and pool properties
		try {
			String url = ds.getConnStr();
			
			LOG.debug(url);
			pds.setURL(url);
			pds.setConnectionFactoryClassName(ds.getDriver());

			// pool configuration
			pds.setConnectionPoolName(ds.getName());
			pds.setInitialPoolSize(ds.getInitialPoolSize());
			pds.setMinPoolSize(ds.getMinPoolSize());
			pds.setMaxPoolSize(ds.getMaxPoolSize());
			pds.setTimeoutCheckInterval(ds.getTimeoutCheckInterval());
			pds.setInactiveConnectionTimeout(ds.getInactiveConnectionTimeout());
		} catch (SQLException e) {
			LOG.warn(e.getSQLState());
			e.printStackTrace();
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean ready() {
		return (pds != null);
	}

	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = pds.getConnection();
			int avlConnCount = pds.getAvailableConnectionsCount();
			LOG.info("Available connections in UCP: " + avlConnCount);
		} catch (SQLException e) {
			e.printStackTrace();
			//LOG.warn(e.getMessage());
		}
		return conn;
	}

	public void return2Pool(Connection conn) {
		try {
			if (conn != null) conn.close();
			conn = null;
		} catch (SQLException e) {
			LOG.warn(e.getSQLState());
		}
	}

	public void close() {
		pds = null;
	}
}
