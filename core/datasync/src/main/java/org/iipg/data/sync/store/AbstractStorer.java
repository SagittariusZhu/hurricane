package org.iipg.data.sync.store;

import java.util.Map;

import org.iipg.data.sync.conf.WorkerProp;
import org.json.JSONArray;

public abstract class AbstractStorer {
	
	public abstract int store(String tbName, Map<String, String> metads, JSONArray values);
	
}
