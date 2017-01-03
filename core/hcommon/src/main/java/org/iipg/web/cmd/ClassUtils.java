package org.iipg.web.cmd;

import java.lang.reflect.Constructor;

public class ClassUtils {

	public static Object newInstance(Class cmdClass) {
		return newInstance(cmdClass, null, null);
	}

	public static Object newInstance(Class cmdClass, Class[] parameterTypes,
			Object[] parameters) {
		try {
			Constructor con = cmdClass.getConstructor(parameterTypes);
			Object instance = con.newInstance(parameters);
			return instance;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
