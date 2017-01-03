package org.iipg.hurricane.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;

public class HAdapterFactory {

	private static HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	
	public static HAdapter createFSAdapter(String schemaName) {
		String adapterName = conf.get("adapter.fs");
		if (adapterName != null && adapterName.length() > 0) {
			try {
				return createAdapter(adapterName, schemaName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static HAdapter createSolrAdapter(String schemaName) {
		String adapterName = conf.get("adapter.ro");
		if (adapterName != null && adapterName.length() > 0) {
			try {
				return createAdapter(adapterName, schemaName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static HAdapter createAdapter(String className, String schemaName) 
			throws ClassNotFoundException, 
				   SecurityException, NoSuchMethodException,
				   IllegalArgumentException, InstantiationException,
				   IllegalAccessException, InvocationTargetException {
		Class adapterC = Class.forName(className);
		Constructor c = adapterC.getConstructor(new Class[]{HurricaneConfiguration.class, String.class});
		HAdapter adapter = (HAdapter) c.newInstance(new Object[]{conf, schemaName});
		return adapter;
	}
}
