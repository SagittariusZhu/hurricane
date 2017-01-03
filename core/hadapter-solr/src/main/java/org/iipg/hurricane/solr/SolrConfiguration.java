package org.iipg.hurricane.solr;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrConfiguration {
	
	private static final Logger LOG = LoggerFactory.getLogger(SolrConfiguration.class);
	
	public static final String SOLR_USE_CLOUD = "solr.use.cloud";
	public static final String SOLR_ZK_HOST = "solr.zk.host";

	public static final String SOLR_HTTPSERVER_URL = "solr.httpserver.url";
	public static final String SOLR_HTTPSERVER_SOTIMEOUT = "solr.httpserver.sotimeout";
	public static final String SOLR_HTTPSERVER_CONNECTIONTIMEOUT = "solr.httpserver.connectiontimeout";
	public static final String SOLR_HTTPSERVER_MAXCONNECTIONSPERHOST = "solr.httpserver.maxconnectionsperhost";
	public static final String SOLR_HTTPSERVER_MAXTOTALCONNECTION = "solr.httpserver.maxtotalconnections";
	public static final String SOLR_HTTPSERVER_FOLLOWREDIRECT = "solr.httpserver.followredirect";
	public static final String SOLR_HTTPSERVER_ALLOWCOMPRESSION = "solr.httpserver.allowcompression";
	public static final String SOLR_HTTPSERVER_MAXRETRIES = "solr.httpserver.maxretries";
			
	private static Properties props = new Properties();
	
	static{
		reload("solr");
	}
	
    /**
     * 重载配置文件
     *
     * @return 重载成功返回true，否则为false
     */
    private static boolean reload(String filePath) {
        boolean flag = false;
        
		try{
			ResourceBundle bundle = ResourceBundle.getBundle(filePath);
			Enumeration<String> en = bundle.getKeys();
			while (en.hasMoreElements()) {
				String key = en.nextElement();
				props.setProperty(key, bundle.getString(key).trim());
			}
		    flag = true;
		}catch(java.util.MissingResourceException e){
	        throw new IllegalArgumentException(     
	                "[solr.properties] is not found!");
		}

/*        Properties prop = new Properties();
        InputStream in = DBConfiguration.class.getResourceAsStream(filePath);
        try {
            prop.load(in);
            host = prop.getProperty("host").trim();
            port = prop.getProperty("port").trim();
            dbname = prop.getProperty("dbname").trim();
            driverClassName = prop.getProperty("driverClassName").trim();
            username = prop.getProperty("username").trim();
            password = prop.getProperty("password").trim();
            dbType = prop.getProperty("dbtype").trim();
            flag = true;
			in.close();
        } catch (IOException e) {
            LOG.error("找不系统配置文件{}，请检查！",filePath);
            e.printStackTrace();
        }*/
       
        return flag;
    }

	public static String getString(String key) {
		return props.getProperty(key);
	}
	
	public static int getInt(String key) {
		return Integer.parseInt(props.getProperty(key));
	}
	
	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(props.getProperty(key));
	}
	
}
