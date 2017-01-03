package org.iipg.hurricane.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.iipg.hurricane.service.HManageServlet;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;

public class ManageServer {

	public static final int DEFAULT_PORT = 12345;
	
	private HurricaneConfiguration conf = null;
	
	public void start(HurricaneConfiguration conf) {
		this.conf = conf;
		Thread thread = new Thread(){
			public void run() {
				initServer();
			}
		};
		thread.start();
	}
	
	private void initServer() {
		try {
			String jetty_home = System.getProperty("hurricane.home",".");
			System.setProperty("jetty.home", jetty_home);
			System.out.println("jetty.home : " + jetty_home);
			Resource webapps = Resource.newSystemResource("jetty-webapps.xml");  
			XmlConfiguration configuration = new XmlConfiguration(webapps.getInputStream());
			Map properies = configuration.getProperties();
			Server server = (Server)configuration.configure();
			
//	        ServletHandler handler = new ServletHandler();
//	        handler.addServletWithMapping(HManageServlet.class, "/hurricane/*");
//	        
//			((HandlerCollection) server.getHandler()).addHandler(handler);
			
			String manager_home = System.getProperty("manager.home",".");
			System.out.println("manager.home : " + manager_home);
	        WebAppContext context = new WebAppContext();  
	        context.setContextPath("/hmw");  
	        context.setDefaultsDescriptor(jetty_home + "/etc/webdefault.xml");
	        context.setTempDirectory(new File(jetty_home + File.separator + "temp"));
	        context.setResourceBase(manager_home);  
	        context.setParentLoaderPriority(true);  
	        context.setDescriptor(manager_home + "/WEB-INF/web.xml");

	        ((HandlerCollection) server.getHandler()).addHandler(context);
			
			server.start();  
			server.join();  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initServer2() {
		int port  = Integer.parseInt(conf.get("manager.server.port"));
		if (port <= 0)
			port = DEFAULT_PORT;
		Server server = new Server(port);

//		File warPath = new File("webapps");
//		if (warPath.isDirectory()) {
//			File[] warList = warPath.listFiles();
//			for (File warItem : warList) {
//				loadWar(warItem, server);
//			}
//		}
		
        HandlerCollection collection = new HandlerCollection();  

        //handler 1
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(HManageServlet.class, "/hurricane/*");
        collection.addHandler(handler);  
          
        //handler 2
        RequestLogHandler logHandler = new RequestLogHandler();  
        NCSARequestLog log = new NCSARequestLog();  
        log.setFilename("target/request.log");  
//      log.setAppend(true);  
        logHandler.setRequestLog(log);  
        collection.addHandler(logHandler);  
        
        //hander 3
		String jetty_home = System.getProperty("jetty.home","C:/workspace/hmw/java/trunk/dist/hmw");
        WebAppContext webapp = new WebAppContext();  
        webapp.setContextPath("/hmw");  
		//webapp.setDefaultsDescriptor(jetty_home+"/lib/webdefault.xml"); 
		webapp.setTempDirectory(new File(jetty_home+"/work/hmw"));
        //webapp.setAttribute("org.eclipse.jetty.webapp.basetempdir", jetty_home+"/webapps/hmw");
        HashLoginService dummyLoginService = new HashLoginService("TEST-SECURITY-REALM");  
        webapp.getSecurityHandler().setLoginService(dummyLoginService);  
        webapp.setWar(jetty_home+"/webapps/hmw.war");  
        server.setHandler(webapp);  
        collection.addHandler(webapp);
          
        webapp = new WebAppContext();  
        webapp.setContextPath("/test");  
		//webapp.setDefaultsDescriptor(jetty_home+"/lib/webdefault.xml");        
		webapp.setTempDirectory(new File(jetty_home+"/work/test"));
        //webapp.setAttribute("org.eclipse.jetty.webapp.basetempdir", jetty_home+"/webapps/hmw");
        webapp.getSecurityHandler().setLoginService(dummyLoginService);  
        webapp.setWar(jetty_home+"/webapps/test.war");  
        server.setHandler(webapp);  
        collection.addHandler(webapp);

        server.setHandler(collection);  

		try {
			server.start();
			System.out.println("Manage Server start!");
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadWar(String war, Server server) {
		File f = new File(war);
		try {
			loadWar(f, server);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadWar(File war, Server server) throws MalformedURLException {
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/test");
		//webapp.setDefaultsDescriptor("C:/workspace/hmw/java/trunk/dist/hmw/lib/webdefault.xml");
		final URL warUrl = war.toURI().toURL();  
	    final String warUrlString = warUrl.toExternalForm();  
	    
		webapp.setWar(warUrlString);

		//将war解压的目录
		//webapp.setTempDirectory(new File("C:/workspace/hmw/java/trunk/dist/hmw/webapps/test"));
		//webapp.setExtractWAR(true);
		
		//webapp.setTempDirectory("wabapps/");
		server.setHandler(webapp);
	}
	
	public static void main(String[] args) {
		(new ManageServer()).start(HurricaneConfigurationFactory.getInstance());
	}
}
