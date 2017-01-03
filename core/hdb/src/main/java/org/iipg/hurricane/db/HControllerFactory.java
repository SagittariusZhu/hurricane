package org.iipg.hurricane.db;

import java.lang.reflect.Constructor;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;

public class HControllerFactory {

	private static HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();

	public static HController getInstance(String className) throws Exception {
		Class controllerC = Class.forName(className);
		Constructor c = controllerC.getConstructor(new Class[]{HurricaneConfiguration.class});
		HController controller = (HController) c.newInstance(new Object[]{conf});
		return controller;
	}

}
