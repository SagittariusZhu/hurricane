/**
 * @author James
 * @date : 2013年12月20日 下午7:23:57 
 * @version:
 */
package org.iipg.hurricane.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author James
 *
 */
public class JSONUtil {
	public static List toList(String str)
	{
		JSONArray jArr;
		try
		{
			jArr = new JSONArray(str);
		} catch (JSONException e) {
			return new ArrayList();
		}
		List list = toList(jArr);
		return list;
	}

	public static Map toMap(String str) {
		JSONObject jObj;
		try {
			jObj = new JSONObject(str);
		} catch (JSONException e) {
			return new HashMap();
		}
		Map map = toMap(jObj);
		return map;
	}

	private static List toList(JSONArray jArr) {
		List list = new ArrayList();
		for (int i = 0; i < jArr.length(); i++) {
			Object o;
			try {
				o = jArr.get(i);
			} catch (JSONException e) {
				continue;
			}
			if ((o instanceof JSONArray))
				list.add(toList((JSONArray)o));
			else if ((o instanceof JSONObject))
				list.add(toMap((JSONObject)o));
			else {
				list.add(o);
			}
		}
		return list;
	}

	private static Map toMap(JSONObject jObj) {
		Map map = new HashMap();
		for (Iterator it = jObj.keys(); it.hasNext();) {
			String key = (String) it.next();
			Object o;
			try {
				o = jObj.get(key);
			} catch (JSONException e) {
				continue;
			}

			if ((o instanceof JSONArray))
				map.put(key, toList((JSONArray) o));
			else if ((o instanceof JSONObject))
				map.put(key, toMap((JSONObject) o));
			else {
				map.put(key, o);
			}
		}
		return map;
	}
	
	public static JSONObject toJSONObject(Map item) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		for (Iterator it = item.keySet().iterator(); it.hasNext(); ) {
			String key = (String) it.next();
			jsonObject.put(key, item.get(key));
		}
		return jsonObject;
	}
	
	public static JSONObject toJSONObject(Object item) throws JSONException {
		if (item instanceof Map) {
			return toJSONObject((Map) item);
		} else if (item instanceof String) {
			return toJSONObject(toMap((String) item));
		}
		return null;
	}
}
