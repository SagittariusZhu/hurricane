package org.iipg.data.sync.ds;

import org.dom4j.Element;

public abstract class DataSource {

	private String name;
	private String type;
	
	public void parse(Element dsEle) {
		this.name = dsEle.attributeValue("name");
		this.type = dsEle.attributeValue("type");
	}
	
	public String getName() { return this.name; }
	public String getType() { return this.type; }
}
