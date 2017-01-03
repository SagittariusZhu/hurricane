package org.iipg.hurricane.client.util;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;

public class StringUtil {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String toString(Object fieldValue) {
		if (fieldValue instanceof Integer) {
			int value = ((Integer) fieldValue).intValue();
			return String.format("%d", value);
		} else if (fieldValue instanceof String) {
			String s = (String) fieldValue;
			return s;
		} else if (fieldValue instanceof Double) {
			double d = ((Double) fieldValue).doubleValue();
			return String.format("%f", d);
		} else if (fieldValue instanceof Float) {
			float f = ((Float) fieldValue).floatValue();
			return String.format("%f", f);
		} else if (fieldValue instanceof Long) {
			long l = ((Long) fieldValue).longValue();
			return String.format("%ld", l);
		} else if (fieldValue instanceof Boolean) {
			boolean b = ((Boolean) fieldValue).booleanValue();
			return String.format("%b", b);
		} else if (fieldValue instanceof Date) {
			Date d = (Date) fieldValue;
			return sdf.format(d);
		} else if (fieldValue instanceof List) {
			List list = (List) fieldValue;
			JSONArray jsonList = new JSONArray();
			for (int i=0; i<list.size(); i++) {
				jsonList.put(toString(list.get(i)));
			}
			return jsonList.toString();
		}
		return String.valueOf(fieldValue);
	}

	public static Object toObject(String realValue, String className) {
		if (className.equals("list")) {
			List list = JSONUtil.toList(realValue);
			return list;
		} else if (className.equals("date")) {
			try {
				return sdf.parse(realValue);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (className.equals("int")) {
			return Integer.parseInt(realValue);
		} else if (className.equals("dbl")) {
			return Double.parseDouble(realValue);
		} else if (className.equals("float")) {
			return Float.parseFloat(realValue);
		} else if (className.equals("long")) {
			return Long.parseLong(realValue);
		} else if (className.equals("bool")) {
			return Boolean.parseBoolean(realValue);
		} else if (className.equals("unknown")) {
			return null;
		}
		return realValue;
	}

	public static String getClassName(Object fieldValue) {
		if (fieldValue instanceof Integer) {
			return "int";
		} else if (fieldValue instanceof String) {
			return "string";
		} else if (fieldValue instanceof Double) {
			return "dbl";
		} else if (fieldValue instanceof Float) {
			return "float";
		} else if (fieldValue instanceof Long) {
			return "long";
		} else if (fieldValue instanceof Boolean) {
			return "bool";
		} else if (fieldValue instanceof Date) {
			return "date";
		} else if (fieldValue instanceof List) {
			return "list";
		}
		return "unknown";
	}

}
