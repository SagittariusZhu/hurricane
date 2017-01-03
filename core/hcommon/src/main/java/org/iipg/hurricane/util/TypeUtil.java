package org.iipg.hurricane.util;

public class TypeUtil {
	
	public static boolean isType(Object value, String type) {
		if ("int".equalsIgnoreCase(type)) {
			try {
				Integer.parseInt("" + value);
				return true;
			} catch (Exception ignore) {
				return false;
			}
		}
		return true;
	}

}
