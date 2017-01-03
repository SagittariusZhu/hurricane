/**
 * 
 */
package org.iipg.hurricane.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.db.conn.ConnectionPool;
import org.iipg.hurricane.db.conn.DBConfiguration;
import org.iipg.hurricane.db.conn.PoolConfiguration;

/**
 * @author lixiaojing
 *
 */
public class ConnectionPoolMysql implements ConnectionPool {

	private static final Log LOG = LogFactory.getLog(ConnectionPoolMysql.class);

	private static PoolDataSource pds = null;

	static {
		// Create pool-enabled data source instance.
		pds = PoolDataSourceFactory.getPoolDataSource();
		// PoolDataSource and UCP configuration
		// set the connection properties on the data source and pool properties
		try {
			String url = String
			.format("jdbc:mysql://address=(protocol=tcp)(host=%s)(port=%s)(user=%s)(password=%s)/%s?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false",
					DBConfiguration.getHost(), DBConfiguration.getPort(), 
					DBConfiguration.getUsername() != null ? DBConfiguration.getUsername() : "",
							DBConfiguration.getPassword() != null ? DBConfiguration.getPassword() : "",
									DBConfiguration.getDbname());
			LOG.debug(url);
			pds.setURL(url);
			pds.setConnectionFactoryClassName(DBConfiguration
					.getDriverClassName());

			// 连接池的配置和数据库基本连接的配置分开，以备以后换数据库，也许ucp这么强大的连接就不能用了
			pds.setConnectionPoolName(PoolConfiguration.getPoolName());
			pds.setInitialPoolSize(PoolConfiguration.getInitialPoolSize());
			pds.setMinPoolSize(PoolConfiguration.getMinPoolSize());
			pds.setMaxPoolSize(PoolConfiguration.getMaxPoolSize());
			pds.setTimeoutCheckInterval(PoolConfiguration
					.getTimeoutCheckInterval());
			pds.setInactiveConnectionTimeout(PoolConfiguration
					.getInactiveConnectionTimeout());
		} catch (SQLException e) {
			LOG.warn(e.getSQLState());
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.jn.iipg.sw.db.IConnectionPool#init()
	 */
	@Override
	public boolean init() {
		return (pds != null);
	}
	
	/* (non-Javadoc)
	 * @see org.jn.iipg.sw.db.IConnectionPool#getConnection()
	 */
	@Override
	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = pds.getConnection();
			int avlConnCount = pds.getAvailableConnectionsCount();
			LOG.info("Available connections in UCP: " + avlConnCount);
		} catch (SQLException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
		}
		return conn;
	}

	/* (non-Javadoc)
	 * @see org.jn.iipg.sw.db.IConnectionPool#return2Pool(java.sql.Connection)
	 */
	@Override
	public void return2Pool(Connection conn) {
		try {
			if (conn != null) conn.close();
			conn = null;
		} catch (SQLException e) {
			LOG.warn(e.getSQLState());
		}
	}

	/* (non-Javadoc)
	 * @see org.jn.iipg.sw.db.IConnectionPool#close()
	 */
	@Override
	public void close() {
		pds = null;
	}
}
