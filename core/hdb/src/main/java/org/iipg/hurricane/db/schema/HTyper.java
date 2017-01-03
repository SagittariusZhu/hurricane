package org.iipg.hurricane.db.schema;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.ext.*;

public abstract class HTyper {

	private static Map<String, Object> extTypes = new HashMap<String, Object>();
	
	static {
		extTypes.put("hblob", HblobTyper.class);
		extTypes.put("hfile", HFileTyper.class);
		extTypes.put("hipaddr", HIPAddrTyper.class);
	}
	
	private Field oriField = null;
	private Object values = null;
	private Map<String, byte[]> binaryMap = new HashMap<String, byte[]>();

	public HTyper(Field ori) {
		this.oriField = ori;
	}
	
	public Field getOriField() {
		return this.oriField;
	}
	
	public static boolean isExtType(String type) {
		return extTypes.containsKey(type);
	}

	public static HTyper getTyper(Field field) throws Exception {
		Class clasz = (Class) extTypes.get(field.getType());
		Constructor c = clasz.getConstructor(new Class[] {Field.class});
		HTyper typer = (HTyper) c.newInstance(field);
		return typer;
	}
	
	public void setValues(Object fieldValue) {
		this.values = fieldValue;
	}

	public Object getValue(String name) {
		if (getOriField().isMultiValued()) {
			List ret = new ArrayList();
			if (values instanceof List) {
				for (Object item : (List) values) {
					if (item instanceof Map) {
						Object itemValue = ((Map) item).get(name);
						ret.add(itemValue);
					}
				}
			}
			return ret;
		} else {
			if (values instanceof Map) {
				return ((Map) values).get(name);
			}
		}
		return null;
	}

	public boolean hasBinary() {
		return (this.binaryMap.size() > 0);
	}

	public Map<String, byte[]> getBinary() {
		return this.binaryMap;
	}

	protected Field createField(String name, String type, String flag) {
		Field field = new Field();
		field.setName(name);
		field.setType(type);
		field.setFlag(flag);
		return field;
	}
	
	protected String mergeFlag(String flagSrc, String flagTarget) {
		StringBuffer ret = new StringBuffer(flagSrc);
		int idx = -1;
		while ( (idx = ret.indexOf("?")) >= 0) {
			ret.replace(idx, idx+1, flagTarget.substring(idx, idx+1));
		}
		return ret.toString();
	}
	
	protected void setBinary(String uuid, byte[] buf) {
		this.binaryMap.put(uuid, buf);
	}
	
	// abstract methods
	
	public abstract Field getDefaultField();
	
	public abstract Field[] getFields();

	public abstract Object run(Object value);

	public abstract void convert(HDBRecord record);

	public abstract String getQueryValue(String value);

}
