package org.iipg.hurricane.client.metadata;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.client.util.StringUtil;

public class HDocument {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String schema = "";
	/**
	 * properties for file
	 */
	private Map<String, Object> props = new HashMap<String, Object>();
	
	private byte[] blob = null;
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return this.schema;
	}
	

	public Map<String, Object> getProps() {
		return this.props;
	}
	
	public void setProps(Map<String, Object> dict) {
		this.props = dict;		
	}

	public Map<String, String> getFormatProps() {
		Map<String, String> retProps = build();
		return retProps;
	}

	public void setFormatProps(Map<String, String> dict) {
		this.props = parse(dict);		
	}

	public void setBinary(byte[] blob) {
		this.blob = blob;		
	}	
	
	public byte[] getBinary() {
		return this.blob;
	}
	
	public void setField(String fieldName, Object fieldValue) {
		this.props.put(fieldName, fieldValue);
	}
	
	public void addField(String fieldName, Object value) {
		List list = null;
		if (props.containsKey(fieldName)) {
			list = (List) props.get(fieldName);
		} else {
			list = new ArrayList();
		}
		list.add(value);
		props.put(fieldName, list);
	}
	
	// return field value
	public Object getValue(String fieldName) {
		return props.get(fieldName);
	}
	
	// Only for multiValue field
	public int getSize(String fieldName) {
		Object value = props.get(fieldName);
		if (value instanceof List) {
			return ((List) value).size();
		}
		return 0;
	}

	// inner function
	private Map<String, String> build() {
		Map<String, String> retProps = new HashMap<String, String>();
		for (Iterator<String> it = props.keySet().iterator(); it.hasNext(); ) {
			String key = it.next(); 
			Object fieldValue = props.get(key);
			String className = StringUtil.getClassName(fieldValue);
			retProps.put(key, "[" + className + "]" + StringUtil.toString(fieldValue));
		}
		return retProps;
	}
	
	private Map<String, Object> parse(Map<String, String> inProps) {
		Map<String, Object> retProps = new HashMap<String, Object>();
		for (Iterator<String> it= inProps.keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			String value = inProps.get(key);
			int pos = value.indexOf("]");
			if (!value.startsWith("[") || pos < 1) {
				System.err.println(key + " has error value : " + value);
				continue;
			}
			String className = value.substring(1, pos);
			String realValue = value.substring(pos + 1);
			retProps.put(key, StringUtil.toObject(realValue, className));
		}
		return retProps;
	}	
	
	/*
	 * Binary Helper Methods
	 */
	public void setBField(String fieldName, byte[] buf) {
		String uri = setBinaryToBuf(buf);
		setField(fieldName, uri);
	}
	
	public void addBField(String fieldName, byte[] buf) {
		String uri = setBinaryToBuf(buf);
		addField(fieldName, uri);
	}
	
	public byte[] getBField(String fieldName) {
		return getBField(fieldName, 0);
	}
	
	public byte[] getBField(String fieldName, int index) {
		String uri = null; 
		Object value = props.get(fieldName);
		if (value instanceof List) {
			List list = (List) value;
			uri = (String) list.get(index);
		} else {
			uri = (String) value;
		}
		return getBinaryFromBuf(uri);
	}
	
	// inner support method
	private String setBinaryToBuf(byte[] buf) {
		int start = 0;
		int length = buf.length;
		if (this.blob == null) {
			this.blob = buf;
		} else {
			start = this.blob.length;
			byte[] newBuff = (byte[]) Array.newInstance(byte.class, start + length);
			System.arraycopy(this.blob, 0, newBuff, 0, start);
			System.arraycopy(buf, 0, newBuff, start, length);
			this.blob = newBuff;
		}
		return String.format("%d:%d", start, length);
	}
	
	private byte[] getBinaryFromBuf(String uri) {
		String[] arr = uri.split(":");
		int start = Integer.parseInt(arr[0]);
		int length = Integer.parseInt(arr[1]);
		byte[] newBuff = (byte[]) Array.newInstance(byte.class, length);
		System.arraycopy(this.blob, start, newBuff, 0, length);
		return newBuff;
	}

}
