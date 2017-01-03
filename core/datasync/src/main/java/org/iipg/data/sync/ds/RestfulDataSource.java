package org.iipg.data.sync.ds;

import org.dom4j.Element;

public class RestfulDataSource extends DataSource {

	private String url = "";
	private String dbName = "";
	
	@Override
	public void parse(Element dsEle) {
		super.parse(dsEle);		
		this.url = dsEle.attributeValue("url");
		this.dbName = dsEle.attributeValue("db");
	}
	
	public String getUrl() { return this.url; }
	public String getDbName() { return this.dbName; }
	
}
