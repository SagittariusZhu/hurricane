/**
 * 
 */
package org.iipg.hurricane.solr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.iipg.hurricane.FieldTypeException;
import org.iipg.hurricane.db.DefaultSignatureGenerator;
import org.iipg.hurricane.db.HSignatureGenerator;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.schema.ext.HblobTyper;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.iipg.hurricane.util.TimeTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dr.Zhu
 * 
 */
public class ExDocumentBuilder {

	static final Logger LOG = LoggerFactory.getLogger(ExDocumentBuilder.class);
	
	private SchemaParser schema;
	private SolrInputDocument document = new SolrInputDocument();
	private String signature = null;
	private String simhashValue = null;

	private HSignatureGenerator signatureGenerator;

	public ExDocumentBuilder(SchemaParser schema) {
		this.schema = schema;
	}

	public ExDocumentBuilder(SchemaParser schema, HDBRecord record) throws Exception {
		this.schema = schema;
		if (record == null)
			throw new Exception("Record is null!");
		parse(record);
	}

	public SchemaParser getSchema() {
		return this.schema;
	}
	
	public SolrInputDocument getDocument() {
		return document;
	}

	public String getSignature() {
		return signature;
	}

	public String getSimhashValue() {
		return simhashValue;
	}

	// public String getRaw_md5() {
	// return raw_md5;
	// }

	public void parse(HDBRecord record) {

		document.clear();

		Map info = record.getInformation();

		ArrayList<Object> dedupList = new ArrayList<Object>();
		ArrayList<Object> signatureList = new ArrayList<Object>();

		for (Iterator it = info.keySet().iterator(); it.hasNext(); ) {
			String fieldName = (String) it.next();
			Field field = schema.getField(fieldName);		
			Object fieldValue = info.get(fieldName);
			
			if (HTyper.isExtType(field.getType())) {
				try {
					HTyper typer = HTyper.getTyper(field);
					Field[] fields = typer.getFields();
					typer.setValues(fieldValue);
					for (Field item : fields) {
						Object value = typer.getValue(item.getName());
						addFieldToDoc(item, value, dedupList, signatureList);					
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else 
				addFieldToDoc(field, fieldValue, dedupList, signatureList);
		}

		this.signature = computeSignature(signatureList);
		if (signature != null && !document.containsKey(HConstant.FIELD_NAME_SIGNATURE)) {
			document.addField(HConstant.FIELD_NAME_SIGNATURE, signature);
		}
		
		this.simhashValue = computeSimHash(dedupList);
	}

	private void addFieldToDoc(Field field, Object fieldValue, 
			ArrayList<Object> dedupList, ArrayList<Object> signatureList) {
		String fieldName = field.getName();
		
		//support update field operator, only for 4.0 upper
		if (fieldValue instanceof Map) {
			document.addField(fieldName, fieldValue);
			return;
		}
		
		if (field.isMultiValued()){
			Object[] values = null;
			if (fieldValue instanceof List) {
				values = (Object[]) ((List) fieldValue).toArray(new Object[0]);
			} else {
				values = new Object[1];
				values[0] = fieldValue;
			}
			for (Object value : values) {
				document.addField(fieldName, correctValue(field.getType(), value));
			}
		} else {
			document.setField(fieldName, correctValue(field.getType(), fieldValue));
		}
	
		if (field.isDedup()) {
			dedupList.add(fieldValue);				
		} else if (field.isSignature()) {
			signatureList.add(fieldValue);				
		}
	}
	
	private Object correctValue(String type, Object value) {
		if ("date".equals(type)) {
			Date cValue = null;
			if (value instanceof Date) {
				cValue = (Date) value;
			} else {
				try {
					cValue = TimeTool.parse((String) value);
				} catch (ParseException e) {
					String msg = "Expect correct DATE format, not " + value;
					LOG.warn(msg);
					throw new FieldTypeException(msg);
				}
			}
			return cValue;
		} else if ("int".equals(type)) {
			int cValue = 0;
			if (value instanceof Integer) {
				cValue = (Integer) value;
			} else {
				try {
					cValue = Integer.parseInt((String) value);
				} catch (Exception e) {
					String msg = "Expect correct INT format, not " + value;
					LOG.warn(msg);
					throw new FieldTypeException(msg);
				}
			}
			return cValue;
		}
		return value;
	}
	
	private String computeSignature(List<Object> inputList) {
		if (this.signatureGenerator == null) {
			this.signatureGenerator = new DefaultSignatureGenerator();
			LOG.warn("get signatureGenerator is null, use default signatureGenerator!");
		}

		String signature = null;
		String inputStr = "";
		for (Object o : inputList) {
			if (o != null) {
				inputStr += o.toString();
			}
		}

		signature = this.signatureGenerator.generateSignature(inputStr);

		return signature;
	}

	private String computeSimHash(List<Object> inputList) {
		String simhash = null;
		String inputStr = "";
		for (Object o : inputList) {
			if (o != null) {
				inputStr += o.toString();
			}
		}
		if (inputStr.trim().length() > 0) {
			//simhash = SimHashUtil.getSimHash(inputStr);
		}
		return simhash;
	}
}
