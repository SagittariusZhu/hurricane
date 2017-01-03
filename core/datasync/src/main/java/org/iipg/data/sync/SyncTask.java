package org.iipg.data.sync;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

public class SyncTask {

	private String tbName;
	
	private String taskName;
	
	private String crons = "";
	
	private JSONObject status = new JSONObject();
	
	public SyncTask(String tbName, String taskName) {
		this.tbName = tbName;
		this.taskName = taskName;
	}
	
	public void setCrons(String crons) {
		this.crons = crons;
	}
	
	public void setStatus(JSONObject obj) {
		this.status = obj;
	}
	
	public String getTableName() { return this.tbName; }
	public String getName() { return this.taskName; }
	public String getCrons() { return this.crons; }
	public JSONObject getStatus() { return this.status; }
	
	public static Map<String, Map<String, SyncTask>> parse(String content) {
		
		Map<String, Map<String, SyncTask>> allItems = new HashMap();
		
		JSONObject allObjs = new JSONObject(content);
		
		for (Iterator it = allObjs.keys(); it.hasNext(); ) {
			String tbName = (String) it.next();
			
			JSONObject subObjs = allObjs.getJSONObject(tbName);
			
			allItems.put(tbName, parseSub(tbName, subObjs));
		}
		
		return allItems;
	}
	
	private static Map<String, SyncTask> parseSub(String tbName, JSONObject objs) {
		
		Map<String, SyncTask> items = new HashMap();
		
		for (Iterator it = objs.keys(); it.hasNext(); ) {
			
			String taskName = (String) it.next();
			
			JSONObject obj = objs.getJSONObject(taskName);
			
			SyncTask task = new SyncTask(tbName, taskName);
			if (obj.has("crons"))
				task.setCrons(obj.getString("crons"));
			
			if (obj.has("status"))
				task.setStatus(obj.getJSONObject("status"));
			
			items.put(taskName, task);
		}
		
		return items;
	}
	
	public static String format(Map<String, Map<String, SyncTask>> allItems) {
		
		JSONObject allObjs = new JSONObject();
		
		for (Iterator it = allItems.keySet().iterator(); it.hasNext(); ) {
			
			String tbName = (String) it.next();
			
			Map<String, SyncTask> subItems = allItems.get(tbName);
			
			allObjs.put(tbName, formatSub(subItems));
		}
		
		return allObjs.toString();
	}
	
	private static JSONObject formatSub(Map<String, SyncTask> subItems) {
		JSONObject subObjs = new JSONObject();
		
		for (Iterator it = subItems.keySet().iterator(); it.hasNext(); ) {
			
			String taskName = (String) it.next();
			
			SyncTask task = subItems.get(taskName);
			
			JSONObject obj = new JSONObject();
			obj.put("crons", task.getCrons());
			obj.put("status", task.getStatus());
			
			subObjs.put(taskName, obj);
		}
		
		return subObjs;
	}
}
