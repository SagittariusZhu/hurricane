package org.iipg.hurricane.service.cmd;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.HControllerFactory;
import org.iipg.hurricane.service.HCommand;
import org.json.JSONObject;

public class PropertiesCmd extends HCommand {
	
	private String[] systemPropNames = {
			"user.timezone"
	};

	public PropertiesCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		JSONObject respObj = createOutputJSON();
		
		JSONObject propsObj = new JSONObject();
		Map<String, ?> props = sortByKey(conf.getProps());
		for (Map.Entry<String, ?> entry : props.entrySet()) {
			propsObj.put(entry.getKey(), entry.getValue());
		}
		
		/* ftp property will be moved to zookeeper
		Properties ftpProps = HdfsOverFtpServer.getProps();
		for (Entry entry : ftpProps.entrySet()) {
			propsObj.put("ftp." + entry.getKey(), entry.getValue());
		}*/
		
		for (String key : props.keySet()) {
			if (key.startsWith("controller.")) {
				try {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					Map<String, ?> subProps = controller.getProps();
					if (subProps != null) {
						for (Map.Entry<String, ?> entry : subProps.entrySet()) {
							propsObj.put(entry.getKey(), entry.getValue());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for (String key : systemPropNames) {
			propsObj.put(key, System.getProperty(key));
		}
		
		respObj.put("system.properties", propsObj);
		
		success = true;
	}
	
	 /** 
     * 使用 Map按key进行排序 
     * @param map 
     * @return 
     */  
    public static Map<String, String> sortByKey(Map<String, String> map) {  
        if (map == null || map.isEmpty()) {  
            return null;  
        }  
        Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String str1, String str2) {  
                return str1.compareTo(str2);  
            }
        });  
        sortMap.putAll(map);  
        return sortMap;  
    }
}
