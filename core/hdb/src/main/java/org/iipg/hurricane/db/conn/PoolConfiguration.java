/**
 * 
 */
package org.iipg.hurricane.db.conn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lixiaojing
 *
 */
public class PoolConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(PoolConfiguration.class);
	
    private static String poolName;
    private static int initialPoolSize;
    private static int minPoolSize;
    private static int maxPoolSize;
    private static int timeoutCheckInterval;
    private static int inactiveConnectionTimeout;

	static{
//		reload("/pool.properties");
		reload("pool");
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
            poolName = bundle.getString("poolName").trim();
            initialPoolSize = Integer.parseInt(bundle.getString("initialPoolSize").trim());
            minPoolSize = Integer.parseInt(bundle.getString("minPoolSize").trim());
            maxPoolSize = Integer.parseInt(bundle.getString("maxPoolSize").trim());
            timeoutCheckInterval = Integer.parseInt(bundle.getString("timeoutCheckInterval").trim());
            inactiveConnectionTimeout = Integer.parseInt(bundle.getString("inactiveConnectionTimeout").trim());
		    flag = true;
		}catch(java.util.MissingResourceException e){
	        throw new IllegalArgumentException(     
	                "[pool.properties] is not found!");
		}

/*        boolean flag = false;
        Properties prop = new Properties();
        InputStream in = DBConfiguration.class.getResourceAsStream(filePath);
        try {
            prop.load(in);
            poolName = prop.getProperty("poolName").trim();
            initialPoolSize = Integer.parseInt(prop.getProperty("initialPoolSize").trim());
            minPoolSize = Integer.parseInt(prop.getProperty("minPoolSize").trim());
            maxPoolSize = Integer.parseInt(prop.getProperty("maxPoolSize").trim());
            timeoutCheckInterval = Integer.parseInt(prop.getProperty("timeoutCheckInterval").trim());
            inactiveConnectionTimeout = Integer.parseInt(prop.getProperty("inactiveConnectionTimeout").trim());
            flag = true;
			in.close();
        } catch (IOException e) {
            LOG.error("找不系统配置文件{}，请检查！",filePath);
            e.printStackTrace();
        }*/
        
        return flag;
    }

	public static String getPoolName() {
		return poolName;
	}

	public static int getInitialPoolSize() {
		return initialPoolSize;
	}

	public static int getMinPoolSize() {
		return minPoolSize;
	}

	public static int getMaxPoolSize() {
		return maxPoolSize;
	}

	public static int getTimeoutCheckInterval() {
		return timeoutCheckInterval;
	}

	public static int getInactiveConnectionTimeout() {
		return inactiveConnectionTimeout;
	}

}
