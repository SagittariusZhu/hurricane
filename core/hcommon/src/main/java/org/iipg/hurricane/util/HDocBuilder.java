package org.iipg.hurricane.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HDocBuilder {

	public static Map<String, String> build(Map<String, Object> props) {
		Map<String, String> retProps = new HashMap<String, String>();
		for (Iterator<String> it = props.keySet().iterator(); it.hasNext(); ) {
			String key = it.next(); 
			Object fieldValue = props.get(key);
			String className = StringUtil.getClassName(fieldValue);
			retProps.put(key, "[" + className + "]" + StringUtil.toString(fieldValue));
		}
		return retProps;
	}
	
	public static Map<String, Object> parse(Map<String, String> inProps) {
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
}
