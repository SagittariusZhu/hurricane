package org.iipg.hurricane.client.impl;

import java.io.IOException;
import java.util.Map;

import org.iipg.hurricane.client.HFtpClient;

public class FtpClientRunner {
	
	public static String FTP_ROOT = "/bigfile";

	private ICEConnection conn = null;
	private String hostname = "";
	private int port = 0;
	private String username = "";
	private String password = "";
	private boolean interrupt = false;

	public void start(ICEConnection conn) {
		this.conn = conn;
		Map props = conn.getConfiguration();
		this.hostname = (String) props.get("ftp.server.ip");
		this.port = Integer.parseInt((String) props.get("ftp.server.port"));
		this.username = (String) props.get("ftp.username");
		this.password = (String) props.get("ftp.password");
		Thread thread = new Thread(){
			public void run() {
				init();
			}
		};
		thread.start();
	}
	
	private void init() {
		while (!interrupt) {
			Map item = (Map) conn.QUEUE.poll();
			if (item != null) {
				HFtpClient client = new HFtpClient();
				client.setConnection(this.conn);
				
				try {
					client.connect(hostname, port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				String src = (String) item.get("src");
				String dest = (String) item.get("dest");
				String taskID = (String) item.get("taskID");
				client.setTaskID(taskID);
				if (taskID.startsWith("upload")) {
					upload(src, dest, client);
				} else {
					download(src, dest, client);
				}
				try {
					client.disconnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void upload(String src, String dest, HFtpClient client) {
		try {
			if (client.login(username, password)) {
				client.changeWorkingDirectory(FTP_ROOT);
				client.upload(src, dest);
				client.logout();
			} else {
				System.out.println("Login failed!");
			}
		} catch (IOException e) {    
			e.printStackTrace();    
		} 	
	}
	
	private void download(String src, String dest, HFtpClient client) {
		try {
			if (client.login(username, password)) {
				client.changeWorkingDirectory(FTP_ROOT);
				client.download(src, dest);
				client.logout();
			} else {
				System.out.println("Login failed!");
			}
		} catch (IOException e) {    
			e.printStackTrace();    
		} 	
	}

	public void close() {
		this.interrupt = true;
	}
}
