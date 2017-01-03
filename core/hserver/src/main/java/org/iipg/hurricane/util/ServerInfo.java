package org.iipg.hurricane.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import com.sun.management.OperatingSystemMXBean;

import org.eclipse.jetty.util.log.Log;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerInfo {

	private static Logger LOG = LoggerFactory.getLogger(ServerInfo.class);
	
	public static final String SERVER_NAME = "hmw-spec";
	public static final String SERVER_VERSION = "version";
	public static final String COMPILER_TIMESTAMP = "timestamp";
	
	private static long startTime = 0;
	private static String version = "";
	
	private static HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	
	public static String getInfo() {
		return String.format("%s %s", SERVER_NAME, getVersion());
	}
	
	public static String getName() {
		return SERVER_NAME;
	}
	
	public static String getVersion() {
		try {
			Manifest mf = FileUtil.findManifest(ServerInfo.class);
			Attributes mainAttributes = mf.getMainAttributes();
			version = mainAttributes.getValue(SERVER_VERSION) + " (build on " 
					+ mainAttributes.getValue(COMPILER_TIMESTAMP) + ")";
			return version;
		} catch (Exception ignore) {
			LOG.info("use debeg mode");
		}
		return "debug-mode";
	}
	
	public static void main(String[] args) {
		String info = getInfo();
		System.out.println(info);
	}

	public static void setStartTime(long time) { startTime = time; }
	public static long getStartTime() { return startTime; }

	public static JSONObject getProperties() {
		JSONObject props = new JSONObject();
		Properties sp = System.getProperties();
		for (Iterator it = sp.keySet().iterator(); it.hasNext(); ) {
			String key = (String) it.next();
			if (key.indexOf("hurricane") >= 0 ||
					key.indexOf("jetty") >= 0) {
				try {
					props.put(key, sp.get(key));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return props;
	}
	
	public static String getJVMVersion() {
		//Oracle Corporation Java HotSpot(TM) 64-Bit Server VM (1.7.0_71 24.71-b01)
		return System.getProperty("java.vm.vendor") + " Java HotSpot(TM) " +
				System.getProperty("sun.arch.data.model") + "-Bit Server VM (" +
				System.getProperty("java.version") + " " +
				System.getProperty("java.vm.version") + ")";
	}
	
	public static JSONObject getRuntimeInfo() throws JSONException {
		int kb = 1024;
		JSONObject props = new JSONObject();	
		
		OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		props.put("totalMemory", osmb.getTotalPhysicalMemorySize() / kb); 
		props.put("freeMemory", osmb.getFreePhysicalMemorySize() / kb);
		
		return props;
	}
	
	public static JSONObject getJVMInfo() throws JSONException {
		int kb = 1024;
		JSONObject props = new JSONObject();		

		MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean(); 
		MemoryUsage usage = memorymbean.getHeapMemoryUsage(); 
		
		props.put("max", usage.getMax() / kb); 
		props.put("capacity", usage.getCommitted() / kb);
		props.put("used", usage.getUsed() / kb); 
		//props.put("heapUsage", memorymbean.getHeapMemoryUsage()); 
		//props.put("nonHeapUsage", memorymbean.getNonHeapMemoryUsage()); 

		return props;
	}
	
	public static JSONObject getFtpServerInfo() {
		JSONObject info = new JSONObject();
		//TODO: get ftp info from zookeeper
		info.put("version", "not implemented");
		return info;
	}
	
	public static JSONObject getZooKeeperInfo() {
		JSONObject info = new JSONObject();
		ZkOperator zkOperator = conf.getZkOperator();
		info.put("version", zkOperator.getVersion());
		info.put("liveNodes", zkOperator.getHosts());
		return info;
	}
}
