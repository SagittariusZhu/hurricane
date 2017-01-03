package org.iipg.hurricane.client;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HFtpClientTest {

	private String hostname = "113.11.214.56";
	private int port = 2222;
	private String username = "hurricane";
	private String password = "333";
	
	private HFtpClient client = null;
	
	@Before
	public void init() {
		client = new HFtpClient();
		try {
			client.connect(hostname, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void close() {
		try {
			client.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void upload() {
		String fileName = "C:/runtime/exports/照片_7976.JPG";
		String targetFile = "照片_7976.jpg";
		try {
			if (client.login(username, password)) {
				client.changeWorkingDirectory("/bigfile");
				client.upload(fileName, targetFile);
				client.logout();
			} else {
				System.out.println("Login failed!");
			}
		} catch (IOException e) {    
			System.out.println("连接FTP出错：" + e.getMessage());    
		} 	
//        try {    
//            myFtp.connect("192.168.21.181", 21, "nid", "123");    
////          myFtp.ftpClient.makeDirectory(new String("电视剧".getBytes("GBK"),"iso-8859-1"));    
////          myFtp.ftpClient.changeWorkingDirectory(new String("电视剧".getBytes("GBK"),"iso-8859-1"));    
////          myFtp.ftpClient.makeDirectory(new String("走西口".getBytes("GBK"),"iso-8859-1"));    
////          System.out.println(myFtp.upload("http://www.5a520.cn /yw.flv", "/yw.flv",5));    
////          System.out.println(myFtp.upload("http://www.5a520.cn /走西口24.mp4","/央视走西口/新浪网/走西口24.mp4"));    
//            System.out.println(myFtp.download("/央视走西口/新浪网/走西口24.mp4", "E:\\走西口242.mp4"));    
//            myFtp.disconnect();    
//        } catch (IOException e) {    
//            System.out.println("连接FTP出错："+e.getMessage());    
//        } 		
	}
	
	@Test
	public void download() {
		String srcFile = "照片_7976.jpg";
		String fileName = "C:/runtime/export/照片_7976.JPG";
		try {
			if (client.login(username, password)) {
				client.changeWorkingDirectory("/bigfile");
				client.download(srcFile, fileName);
				client.logout();
			} else {
				System.out.println("Login failed!");
			}
		} catch (IOException e) {    
			System.out.println("连接FTP出错：" + e.getMessage());    
		} 
	}

}
