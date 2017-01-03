package org.iipg.hurricane.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XmlUtil {

	public static Document parse(byte[] content) throws Exception {
		Document document = null;

		InputStream stream = new ByteArrayInputStream(content);  
		InputSource is = new InputSource(stream);
		/*
		 * File schema = new File(fname); InputSource is = null; try { is = new
		 * InputSource(new FileInputStream(schema)); } catch
		 * (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setXIncludeAware(true);
		dbf.setNamespaceAware(true);
		dbf.setFeature("http://xml.org/sax/features/namespaces", false);
		DocumentBuilder db;
		db = dbf.newDocumentBuilder();
		document = db.parse(is);

		return document;
	}

	public static String prettyPrint(String content) {
		try {
			SAXReader reader = new SAXReader();  
			org.dom4j.Document document = reader.read(new StringReader(content));  
			String requestXML = null;  
			XMLWriter writer = null;  
			if (document != null) {  
				try {  
					StringWriter stringWriter = new StringWriter();  
					OutputFormat format = new OutputFormat("    ", true);  
					writer = new XMLWriter(stringWriter, format);  
					writer.write(document);  
					writer.flush();  
					requestXML = stringWriter.getBuffer().toString();  
				} finally {  
					if (writer != null) {  
						try {  
							writer.close();  
						} catch (IOException e) {  
						}  
					}  
				}  
			}  
			return requestXML;
		} catch (Exception e) {}
		return content;
	}

}
