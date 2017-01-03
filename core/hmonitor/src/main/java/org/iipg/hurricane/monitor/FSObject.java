package org.iipg.hurricane.monitor;

import java.io.File;

import org.json.JSONObject;

public class FSObject {
	
	private static long GB = 1024*1024*1024;
	
	public static JSONObject getSpace() {
		JSONObject info = new JSONObject();
		File[] roots = File.listRoots();  
        for (File _file : roots) {
        	JSONObject space = new JSONObject();
            space.put("Free Space", _file.getFreeSpace() * 1.0 / GB);  
            space.put("Usable space", _file.getUsableSpace() * 1.0 / GB);  
            space.put("Total space", _file.getTotalSpace() * 1.0 / GB);  
            info.put(_file.getPath(), space);  
        }
        return info;
	}

}
