package org.iipg.data.sync.ds;

import org.dom4j.Element;

public class MySQLDataSource extends DataSource {

	public static final String DEFAULT_DEIVER = "com.mysql.jdbc.Driver";
	
	private String url = "";
	private String username = "";
	private String password = "";
	private String driver = DEFAULT_DEIVER;
	
	@Override
	public void parse(Element dsEle) {
		super.parse(dsEle);
		this.url = dsEle.attributeValue("url");
		this.username = dsEle.attributeValue("user");
		this.password = dsEle.attributeValue("password");
	}
	
	public void setUrl(String url) { this.url = url; }
	public void setUser(String username) { this.username = username; }
	public void setPassword(String password) { this.password = password; }
	public void setDriver(String driver) { this.driver = driver; }
	
	public String getUrl() { return this.url; }
	public String getUser() { return this.username; }
	public String getPassword() { return this.password; }
	public String getDriver() { return this.driver; }

	public String getConnStr() {
		String connStr = String
				.format("%s?user=%s&password=%s&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false",
						url, username, password);
		return connStr;
	}

	public int getInitialPoolSize() {
		return 1;
	}

	public int getMinPoolSize() {
		return 3;
	}

	public int getMaxPoolSize() {
		return 10;
	}

	public int getTimeoutCheckInterval() {
		return 5;
	}

	public int getInactiveConnectionTimeout() {
		return 10;
	}

}
