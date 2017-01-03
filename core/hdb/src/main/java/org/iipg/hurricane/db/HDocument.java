package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.ParseFieldException;
import org.iipg.hurricane.db.metadata.*;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.db.schema.Relation;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.util.HDocBuilder;
import org.iipg.hurricane.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class HDocument {

	private static final Logger LOG = LoggerFactory.getLogger(HDocument.class);

	private Map<String, HDBBaseObject> record = null;
	private String schemaName = "";
	private SchemaParser parser = null;

	public HDocument() {
		record = new HashMap<String, HDBBaseObject>();
	}

	public String getSchemaName() {
		return this.schemaName;
	}
	public SchemaParser getSchema() {
		return this.parser;
	}
	public void setSchemaName(String schemaName) throws IOException, ParserConfigurationException, SAXException {
		this.schemaName = schemaName;
		parser = new SchemaParser(schemaName);
	}

	//Solr record
	public HDBRecord getSolrObject() {
		HDBRecord ro = (HDBRecord) record.get("ro");
		return ro;
	}
	public void setROObject(HDBRecord ro) {
		record.put("ro", ro);
	}

	//Raw byte array
	public HDBBlob getBlobObject() {
		HDBBlob blob = (HDBBlob) record.get("blob");
		return blob;
	}
	public void setBlobObject(HDBBlob blob) {
		record.put("blob", blob);
	}

	public void setUniqueKey(Object uniqueValue) {
		String uniqueKey = parser.getUniqueKey().getName();
		HDBRecord ro = getSolrObject();
		if (ro != null) {
			ro.put(uniqueKey, uniqueValue);
		}
	}
	
	public static HDocument parse(HMWDocument record) throws IOException, ParserConfigurationException, SAXException {
		Map pro = HDocBuilder.parse(record.dict);

		HDBBlob blob = new HDBBlob();
		if (record.blob != null) {
			blob.setBlob(record.blob);
		}

		return parse(pro, blob, record.schema);
	}
	
	public static HDocument parse(Map<String, Object> dataMap, HDBBlob blob, String schemaName) throws IOException, ParserConfigurationException, SAXException {
		HDocument doc = new HDocument();
		doc.setSchemaName(schemaName);

		Object uuid = null;

		HDBRecord ro = new HDBRecord();
		HDBRecord rw = new HDBRecord();
		HDBSRShip cr = null;
		HDBTShip er = null;

		Relation relation1 = doc.getSchema().getRelation("send_receive");
		if (relation1 != null)
			cr = new HDBSRShip();

		Relation relation2 = doc.getSchema().getRelation("together");
		if (relation2 != null)
			er = new HDBTShip();

		Iterator iter = dataMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();

			// inner field, ignore it
			if (key.equalsIgnoreCase("signature")) continue;

			//Skip System reverse keyword
			if (key.startsWith("__")) { 
				continue;
			}

			if (key.indexOf("\\.") >= 0) {
				String[] arr = key.split("\\.");
				key = arr[0];
			}

			Object value = dataMap.get(key);
			LOG.debug(key + ":" + value);

			Field field = null;
			field = doc.getSchema().getField(key);
			if (field == null) {
				field = doc.getSchema().getDynamicField(key);
			}
			if (field == null) {
				String msg = "Not defined field: " + key;
				LOG.warn(msg);
				throw new ParseFieldException(msg);
			}

			String type = field.getType();
			if (HTyper.isExtType(type)) {
				try {
					HTyper typer = HTyper.getTyper(field);
					ro.push(key, typer.run(value));
					if (typer.hasBinary()) {
						blob.addExt(key, typer.getBinary());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			} else {
				//TODO: check whether value type is matched fieldType
				if (!TypeUtil.isType(value, type)) {
					String msg = "Type match failed: " + type + " - " + value; 
					LOG.warn(msg);
					throw new ParseFieldException(msg);
				}
			}

			if(field.isUnique()) { 
				uuid = value;
				ro.push(key, value);
				rw.push(key, value);
				blob.setUuid(uuid);
				continue;
			}

			String mode = field.getMode();
			if (mode == null) {
				ro.push(key, value);
			} else if (mode.equalsIgnoreCase("ro")) {
				ro.push(key, value);
			} else {
				LOG.warn("未知存储方式:" + key);
				continue;
			}

			//			if (relation1 != null) {
			//				cr.setType(type);
			//				if (relation1.getFrom().equalsIgnoreCase(key))
			//					cr.setSnode((String) value);
			//				if (relation1.getTo().equalsIgnoreCase(key))
			//					cr.setEnode((String) value);
			//			}
			//			
			//			if (relation2 != null) {
			//				er.setType(type);
			//				if (relation2.getField().endsWith(key))
			//					er.setContent_entity((String) value);
			//			}

		}

		doc.setROObject(ro);
		doc.setBlobObject(blob);

		return doc;
	}
}
