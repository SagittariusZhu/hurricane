package org.iipg.data.sync.server;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.iipg.web.cmd.CommandFactory;
import org.iipg.web.cmd.HManageServlet;

public class DefaultStoreServer {

		public static final int DEFAULT_PORT = 12345;
		public static final String DEFAULT_APPNAME = "dsync";
		public static final String DEFAULT_WEBAPP = "./webapps";
		
		private int port = DEFAULT_PORT;
		private String appName = DEFAULT_APPNAME;
		private String webApp = DEFAULT_WEBAPP;
		private String tempPath = System.getProperty("java.io.tmpdir");
		private String jettyHome = System.getProperty("jetty.home");
		private String webName = "";
		private boolean isSecurity = false;
		
		public void start() {
			Thread thread = new Thread(){
				public void run() {
					initServer();
				}
			};
			thread.start();
		}
		
		private void initServer() {
			Server server = new Server(port);

	        HandlerCollection collection = new HandlerCollection();  

	        //handler 1
	        ServletHandler handler = new ServletHandler();
	        handler.addServletWithMapping(HManageServlet.class, "/" + appName + "/*");
	        collection.addHandler(handler);

	        //handler webapp
	        if (webName.length() > 0) {
	        	WebAppContext context = new WebAppContext();  
	        	if (isSecurity) {
	        		HashLoginService realm = new HashLoginService("Test Realm", this.jettyHome + "/etc/realm.properties");
	        		SecurityHandler securityHandler = new ConstraintSecurityHandler();
	        		securityHandler.setLoginService(realm);  
	        		context.setSecurityHandler(securityHandler);
	        	}
	        	
	        	context.setContextPath("/" + webName);  
	        	context.setDefaultsDescriptor(webApp + "/etc/webdefault.xml");
	        	context.setTempDirectory(new File(tempPath));
	        	context.setResourceBase(webApp);  
	        	context.setParentLoaderPriority(true);  
	        	context.setDescriptor(webApp + "/WEB-INF/web.xml");
	        	collection.addHandler(context);
	        	System.out.println("Add WebAppContext " + webName);
	        }
	        
	        server.setHandler(collection);  

			try {
				server.start();
				System.out.println("IIPG Data Sync Server started" +
									"\n  context : /" + appName +
									"\n     port : " + port);
				server.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public static void main(String[] args) {
			DefaultStoreServer server = new DefaultStoreServer();
			server.setProperty("cmdPrefix", "org.iipg.data.sync.storer");
			server.start();
		}

		public void setProperty(String propName, Object value) {
			if (propName.equals("cmdPrefix")) {
				CommandFactory.setCmdPrefix((String) value);
			} else if (propName.equals("serverPort")) {
				this.port = (int) value;
			} else if (propName.equals("appName")) {
				this.appName = (String) value;
			} else if (propName.equals("webApp")) {
				this.webApp = (String) value;
				if (!(new File(this.webApp)).isAbsolute()) {
					try {
						File dir = new File (".");
						System.out.println ("Current dir : " + dir.getCanonicalPath());
						this.webApp = dir.getCanonicalPath() + File.separator + this.webApp;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (propName.equals("tempPath")) {
				this.tempPath = (String) value;
			} else if (propName.equals("webName")) {
				this.webName = (String) value;
			} else  if (propName.equals("jettyHome")) {
				this.jettyHome = (String) value;
				if (!(new File(this.jettyHome)).isAbsolute()) {
					try {
						File dir = new File (".");
						System.out.println ("Current dir : " + dir.getCanonicalPath());
						this.jettyHome = dir.getCanonicalPath() + File.separator + this.jettyHome;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
