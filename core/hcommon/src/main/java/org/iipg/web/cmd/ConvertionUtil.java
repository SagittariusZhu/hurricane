package org.iipg.web.cmd;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertionUtil {

	public static String getSimpleStringWithNull(Object obj) {
		if (obj != null)
			return ("" + obj).trim();
		else
			return "";
	}
	
	public static int getSimpleIntegerWithNull(Object obj) {
		int ret = 0;
		try {
			ret = (int) getSimpleDoubleWithNull(obj);
			//ret = Integer.parseInt(getSimpleStringWithNull(obj));
		} catch (Exception ignore) {}
		return ret;
	}
	
	public static double getSimpleDoubleWithNull(Object obj) {
		double ret = 0;
		try {
			ret = Double.parseDouble(getSimpleStringWithNull(obj));
		} catch (Exception ignore) {}
		return ret;
	}
	
	public static boolean getSimpleBooleanWithNull(String obj, boolean defaultValue) {
		boolean ret = defaultValue;
		try {
			ret = Boolean.parseBoolean(getSimpleStringWithNull(obj));
		} catch (Exception ignore) {}
		return ret;
	}
	
	public static void main(String args[]) {
		double dd = 30.0;
		int ii = getSimpleIntegerWithNull(dd);
		System.out.println(ii);
		System.out.println(Double.parseDouble("30.0"));
		System.out.println(Integer.parseInt("30"));
	}

}
