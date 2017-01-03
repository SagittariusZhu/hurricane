/**
 * 
 */
package org.iipg.hurricane.db.conn;

import java.sql.Connection;

/**
 * @author lixiaojing
 *
 */
public interface ConnectionPool {
	public boolean init();
	public Connection getConnection();
	public void return2Pool(Connection conn);
	public void close();
}
