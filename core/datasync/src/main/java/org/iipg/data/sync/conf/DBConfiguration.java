/**
 * 
 */
package org.iipg.data.sync.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.lucene.util.packed.PackedLongValues.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(DBConfiguration.class);

	private Properties props = new Properties();

	private String name;

	private String host;
	private String port;
	private String dbname;
	private String driverClassName;
	private String username;
	private String password;
	private String dbType;

	private String poolName;
	private int initialPoolSize;
	private int minPoolSize;
	private int maxPoolSize;
	private int timeoutCheckInterval;
	private int inactiveConnectionTimeout;

	public DBConfiguration(String name) {
		this.name = name;
		reload(name);
	}


	public String getName() { return this.name;	}

	private boolean isAbsolutePath(String path) {
		if (path.startsWith("/") || path.indexOf(":") > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 重载配置文件
	 *
	 * @return 重载成功返回true，否则为false
	 */
	private boolean reload(String filePath) {
		boolean flag = false;

		if (isAbsolutePath(filePath)) {
			try {
				InputStream in = new FileInputStream(filePath);
				return reload(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		try{
			ResourceBundle bundle = ResourceBundle.getBundle(filePath); 
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				props.setProperty(key, bundle.getString(key));
			}

			host = bundle.getString("host").trim();
			port = bundle.getString("port").trim();
			dbname = bundle.getString("dbname").trim();
			driverClassName = bundle.getString("driverClassName").trim();
			username = bundle.getString("username").trim();
			password = bundle.getString("password").trim();
			dbType = bundle.getString("dbtype").trim();

			poolName = bundle.getString("poolName").trim();
			initialPoolSize = Integer.parseInt(bundle.getString("initialPoolSize").trim());
			minPoolSize = Integer.parseInt(bundle.getString("minPoolSize").trim());
			maxPoolSize = Integer.parseInt(bundle.getString("maxPoolSize").trim());
			timeoutCheckInterval = Integer.parseInt(bundle.getString("timeoutCheckInterval").trim());
			inactiveConnectionTimeout = Integer.parseInt(bundle.getString("inactiveConnectionTimeout").trim());
			return true;
		}catch(java.util.MissingResourceException e){ }

		if (!flag) {
			URL url = null;
			String fileName = filePath;
			ClassLoader cl = this.getClass().getClassLoader();  
			if (cl == null) {  
				url = ClassLoader.getSystemResource(fileName);  
			} else {  
				url = cl.getResource(fileName);
			}

			if (url == null) {
				LOG.warn("cannot find configuration file: " + fileName);
				return false;
			} else {
				LOG.info("Use configuration : " + url.getFile());
			}

			try {
				InputStream in = new FileInputStream(url.getFile());
				return reload(in);
			} catch (IOException e) {
			}
		}

		if (!flag) {
			LOG.error("找不系统配置文件 %s，请检查！", filePath);
		}

		return flag;
	}

	private boolean reload(InputStream in) {
		if (in != null) {
			try {
				props.load(in);

				host = props.getProperty("host").trim();
				port = props.getProperty("port").trim();
				dbname = props.getProperty("dbname").trim();
				driverClassName = props.getProperty("driverClassName").trim();
				username = props.getProperty("username").trim();
				password = props.getProperty("password").trim();
				dbType = props.getProperty("dbtype").trim();

				poolName = props.getProperty("poolName").trim();
				initialPoolSize = Integer.parseInt(props.getProperty("initialPoolSize").trim());
				minPoolSize = Integer.parseInt(props.getProperty("minPoolSize").trim());
				maxPoolSize = Integer.parseInt(props.getProperty("maxPoolSize").trim());
				timeoutCheckInterval = Integer.parseInt(props.getProperty("timeoutCheckInterval").trim());
				inactiveConnectionTimeout = Integer.parseInt(props.getProperty("inactiveConnectionTimeout").trim());
				return true;
			} catch (IOException e) {
			}
		}
		return false;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getDbname() {
		return dbname;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDbType() {
		return dbType;
	}

	public String getPoolName() {
		return poolName;
	}

	public int getInitialPoolSize() {
		return initialPoolSize;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public int getTimeoutCheckInterval() {
		return timeoutCheckInterval;
	}

	public int getInactiveConnectionTimeout() {
		return inactiveConnectionTimeout;
	}


	public boolean getBoolean(String fieldName) {
		try {
			if (props.containsKey(fieldName)) {
				return Boolean.parseBoolean(props.getProperty(fieldName).trim());
			}
		} catch (Exception e) {}
		return false;
	}


	public String getString(String fieldName) {
		if (props.containsKey(fieldName)) {
			return props.getProperty(fieldName).trim();
		}
		return "";
	}


	public int getInt(String fieldName) {
		try {
			if (props.containsKey(fieldName)) {
				return Integer.parseInt(props.getProperty(fieldName).trim());
			}
		} catch (Exception e) {}
		return 0;
	}

}
