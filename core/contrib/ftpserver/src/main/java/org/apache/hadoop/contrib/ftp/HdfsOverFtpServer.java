package org.apache.hadoop.contrib.ftp;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Start-up class of FTP server
 */
public class HdfsOverFtpServer {

	private static Logger log = LoggerFactory.getLogger(HdfsOverFtpServer.class);

	private static int port = 0;
	private static int sslPort = 0;
	private static String passivePorts = null;
	private static String sslPassivePorts = null;
	private static String hdfsUri = null;
	
	private static String workMode = "normal";
	private static int workPort = 0;

	private static Properties props = new Properties();

	public static void start(String[] args) throws Exception {
		try {
			loadConfig();

			if (port != 0) {
				workPort = port;
				startServer();
				return;
			}

			if (sslPort != 0) {
				workPort = sslPort;
				startSSLServer();
				return;
			}
			
		} catch (Exception e) {
			workMode = e.getMessage();
			throw e;
		}
	}

	/**
	 * Load configuration
	 *
	 * @throws IOException
	 */
	private static void loadConfig() throws IOException {
		props.load(new FileInputStream(loadResource("/hdfs-over-ftp.properties")));

		try {
			port = Integer.parseInt(props.getProperty("port"));
			log.info("port is set. ftp server will be started");
		} catch (Exception e) {
			log.info("port is not set. so ftp server will not be started");
		}

		try {
			sslPort = Integer.parseInt(props.getProperty("ssl-port"));
			log.info("ssl-port is set. ssl server will be started");
		} catch (Exception e) {
			log.info("ssl-port is not set. so ssl server will not be started");
		}

		if (port != 0) {
			passivePorts = props.getProperty("data-ports");
			if (passivePorts == null) {
				log.error("data-ports is not set");
				System.exit(1);
			}
		}

		if (sslPort != 0) {
			sslPassivePorts = props.getProperty("ssl-data-ports");
			if (sslPassivePorts == null) {
				log.error("ssl-data-ports is not set");
				System.exit(1);
			}
		}

		hdfsUri = props.getProperty("hdfs-uri");
		if (hdfsUri == null) {
			log.error("hdfs-uri is not set");
			System.exit(1);
		}

		String superuser = props.getProperty("superuser");
		if (superuser == null) {
			log.error("superuser is not set");
			System.exit(1);
		}
		HdfsOverFtpSystem.setSuperuser(superuser);
	}

	/**
	 * Starts FTP server
	 *
	 * @throws Exception
	 */
	public static void startServer() throws Exception {

		log.info(
				"Starting Hdfs-Over-Ftp server. port: " + port + " data-ports: " + passivePorts + " hdfs-uri: " + hdfsUri);

		HdfsOverFtpSystem.setHDFS_URI(hdfsUri);

		DataConnectionConfigurationFactory dataConFactory = new DataConnectionConfigurationFactory();
		dataConFactory.setPassivePorts(passivePorts);
		DataConnectionConfiguration dataCon = (new DataConnectionConfigurationFactory()).createDataConnectionConfiguration();
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setDataConnectionConfiguration(dataCon);
		listenerFactory.setPort(port);
		Listener listener = listenerFactory.createListener();
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.addListener("default", listener);

		HdfsUserManager userManager = new HdfsUserManager("hurricane");
		final File file = loadResource("/users.properties");
		userManager.setFile(file);
		serverFactory.setUserManager(userManager);

		serverFactory.setFileSystem(new HdfsFileSystemFactory());

		FtpServer server = serverFactory.createServer();
		server.start();
	}

	private static File loadResource(String resourceName) {
		final URL resource = HdfsOverFtpServer.class.getResource(resourceName);
		if (resource == null) {
			throw new RuntimeException("Resource not found: " + resourceName);
		}
		return new File(resource.getFile());
	}

	/**
	 * Starts SSL FTP server
	 *
	 * @throws Exception
	 */
	public static void startSSLServer() throws Exception {

		log.info(
				"Starting Hdfs-Over-Ftp SSL server. ssl-port: " + sslPort + " ssl-data-ports: " + sslPassivePorts + " hdfs-uri: " + hdfsUri);

		workMode = "TLS/SSL";

		HdfsOverFtpSystem.setHDFS_URI(hdfsUri);

		DataConnectionConfigurationFactory dataConFactory = new DataConnectionConfigurationFactory();
		dataConFactory.setPassivePorts(sslPassivePorts);
		DataConnectionConfiguration dataCon = (new DataConnectionConfigurationFactory()).createDataConnectionConfiguration();
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setDataConnectionConfiguration(dataCon);
		listenerFactory.setPort(sslPort);
		MySslConfiguration ssl = new MySslConfiguration();
		ssl.setKeystoreFile(loadResource("/ftp.jks"));
		ssl.setKeystoreType("JKS");
		ssl.setKeyPassword("333333");
		listenerFactory.setSslConfiguration(ssl);
		listenerFactory.setImplicitSsl(true);		
		Listener listener = listenerFactory.createListener();
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.addListener("default", listener);

		HdfsUserManager userManager = new HdfsUserManager("hurricane");
		final File file = loadResource("/users.properties");
		userManager.setFile(file);
		serverFactory.setUserManager(userManager);

		serverFactory.setFileSystem(new HdfsFileSystemFactory());

		FtpServer server = serverFactory.createServer();
		server.start();
	}

	public static String getVersion() {
		String version = "";
		try {
			Manifest mf = findManifest(FtpServer.class);
			Attributes mainAttributes = mf.getMainAttributes();
			version = mainAttributes.getValue("Implementation-Title") + " "
					+ mainAttributes.getValue("Implementation-Version");
			return version;
		} catch (Exception ignore) {
			log.info("not found ftpserver version information.");
		}
		return version;
	}
	
	public static String getWorkMode() {
		return workMode;
	}
	
	public static int getWorkPort() {
		return workPort;
	}
	
	public static Properties getProps() {
		return props;
	}
	
	private static Manifest findManifest(final Class<?> clazz) throws IOException,
	URISyntaxException{
		Class cls = clazz;
		URL codeLocation = cls.getProtectionDomain().getCodeSource().getLocation();
		if (codeLocation == null) {
			URL r = cls.getResource("");
			synchronized (r) {
				String s = r.toString();
				Pattern jar_re = Pattern.compile("jar:\\s?(.*)!/.*");
				Matcher m = jar_re.matcher(s);
				if (m.find()) { // the code is run from a jar file.
					s = m.group(1);
				} else {
					String p = cls.getPackage().getName().replace('.', '/');
					s = s.substring(0, s.lastIndexOf(p));
				}
				try {
					codeLocation = new URL(s);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		System.out.println("codeLocation: " + codeLocation);
		final JarFile jf = new JarFile(new File(codeLocation.toURI()));

		//final JarFile jf = new JarFile(new File("C:\\workspace\\hmw\\java\\trunk\\dist\\hu-server-1.0.0-SNAPSHOT.jar"));
		//final ZipEntry entry = jf.getEntry("META-INF/MANIFEST.MF");
		return jf.getManifest();
	}
	
	public static void main(String[] args) {
		try {
			HdfsOverFtpServer.start(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
