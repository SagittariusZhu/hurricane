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
public class DBConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(DBConfiguration.class);
	
    private static String host;
    private static String port;
    private static String dbname;
    private static String driverClassName;
    private static String username;
    private static String password;
    private static String dbType;

	static{
//		reload("/db.properties");
		reload("db");
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
            host = bundle.getString("host").trim();
            port = bundle.getString("port").trim();
            dbname = bundle.getString("dbname").trim();
            driverClassName = bundle.getString("driverClassName").trim();
            username = bundle.getString("username").trim();
            password = bundle.getString("password").trim();
            dbType = bundle.getString("dbtype").trim();
		    flag = true;
		}catch(java.util.MissingResourceException e){
	        throw new IllegalArgumentException(     
	                "[db.properties] is not found!");
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

	public static String getHost() {
		return host;
	}

	public static String getPort() {
		return port;
	}

	public static String getDbname() {
		return dbname;
	}

	public static String getDriverClassName() {
		return driverClassName;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

	public static String getDbType() {
		return dbType;
	}

}
