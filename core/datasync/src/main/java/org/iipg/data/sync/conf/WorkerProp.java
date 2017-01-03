package org.iipg.data.sync.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.iipg.data.sync.ds.DataSource;

public class WorkerProp {

	private SyncConfiguration conf = null;
	
	private String source = "";
	private String dest = "";
	private String target = "";
	private String qStr = "";
	private String deltaStr = "";
	private Map<String, String> colCache = new HashMap<String, String>();
	
	public WorkerProp(SyncConfiguration conf) {
		this.conf = conf;
	}

	public void parse(Element taskEle) {
		this.source = taskEle.attributeValue("source");
		this.dest = taskEle.attributeValue("dest");
		this.target = taskEle.attributeValue("target");
		this.qStr = taskEle.attributeValue("query");
		this.deltaStr = taskEle.attributeValue("deltaQuery");
		
		List<Element> fieldEles = taskEle.elements("field");
		for (int i=0; i<fieldEles.size(); i++) {
			Element fieldEle = fieldEles.get(i);
			String colName = fieldEle.attributeValue("column");
			String destName = fieldEle.attributeValue("name");
			colCache.put(colName, destName);
		}
	}

	public String getSource() { return this.source; }
	public String getDest() { return this.dest; }
	public String getTarget() { return this.target; }
	public String getQStr() { return this.qStr; }
	public String getDeltaStr() { return this.deltaStr; }
	
	public String getTargetFieldName(String fieldName) {
		if (colCache.containsKey(fieldName))
			return colCache.get(fieldName);
		return fieldName;
	}

	public DataSource getSourceDS() {
		return conf.getDataSource(this.source);
	}

	public DataSource getDestDS() {
		return conf.getDataSource(this.dest);
	}

	public Map<String, String> getMetaMap() {
		return colCache;
	}
}
