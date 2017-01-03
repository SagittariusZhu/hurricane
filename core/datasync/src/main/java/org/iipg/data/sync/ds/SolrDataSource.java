package org.iipg.data.sync.ds;

import org.dom4j.Element;

public class SolrDataSource extends DataSource {

	private String url = "";
	private boolean useCloud = false;
	private String zkhost = "";
	private int zktimeout = 2000;
	
	@Override
	public void parse(Element dsEle) {
		super.parse(dsEle);		
		String fullUrl = dsEle.attributeValue("url");
		if (fullUrl.startsWith("cloud")) {
			parseCloudUrl(fullUrl);
			this.zktimeout = Integer.parseInt(dsEle.attributeValue("zktimeout")); 
			this.useCloud = true;
		} else {
			this.url = fullUrl;
			this.useCloud = false;
		}
	}
	
	private void parseCloudUrl(String fullUrl) {
		this.zkhost = fullUrl.substring("cloud://".length());
	}

	public boolean isCloud() { return this.useCloud; }
	public String getUrl() { return this.url; }
	public String getZkHost() { return this.zkhost; }
	public int getZkTimeout() { return this.zktimeout; }
	
}
