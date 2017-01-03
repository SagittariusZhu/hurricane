package org.iipg.hurricane.conf;

public class HurricaneConfigurationFactory {
	
	private static HurricaneConfiguration conf = null;
	
	public static synchronized HurricaneConfiguration getInstance() {
		if (conf == null) {
			conf = new HurricaneConfiguration();
		}
		return conf;
	}

}
