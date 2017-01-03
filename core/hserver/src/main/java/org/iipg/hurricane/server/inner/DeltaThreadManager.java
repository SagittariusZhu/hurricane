package org.iipg.hurricane.server.inner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeltaThreadManager {

	private static Map<String, DeltaRunThread> threads = new ConcurrentHashMap();
	
	public static void add(String fullName, DeltaRunThread deltaThread) {
		threads.put(fullName, deltaThread);
	}
	
	public static boolean contains(String fullName) {
		return threads.containsKey(fullName);
	}

	public static DeltaRunThread getThread(String fullName) {
		return threads.get(fullName);
	}

	public static DeltaRunThread find(String fullName) {
		if (threads.containsKey(fullName))
			return threads.get(fullName);
		return null;
	}

	public static void remove(String fullName) {
		if (threads.containsKey(fullName))
			threads.remove(fullName);		
	}
}
