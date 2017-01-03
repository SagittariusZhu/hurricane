package org.iipg.hurricane.solr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SolrConfig {
	public static final String DEFAULT_CONFIG_FILE = "solrconfig.xml";
	
	public static final String DATAIMPORT_HANDLER_NAME = "solr.DataImportHandler";
	
	private static Logger LOG = LoggerFactory.getLogger(SolrConfig.class);
	
	private HurricaneConfiguration conf = null;
	private ZkOperator zkOperator = null;
	private String templatePath = "";
	private Document document = null;
	private XPath xpath;
	
	public SolrConfig(HurricaneConfiguration conf) throws ParserConfigurationException, SAXException, IOException {
		this.templatePath = conf.get("solr.template.path") + File.separator + "conf";
		String configFile = this.templatePath + File.separator + DEFAULT_CONFIG_FILE;
		this.conf = conf;
		zkOperator = conf.getZkOperator();
		load(configFile);
	}
	
	public SolrConfig(HurricaneConfiguration conf, String configFile) throws ParserConfigurationException, SAXException, IOException {
		this.conf = conf;
		zkOperator = conf.getZkOperator();
		load(configFile);
	}
	
	public String[] getDataImportHandlers() {
		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "config", "requestHandler" }))
				.toString();

		NodeList nodes;
		List<String> handlers = new ArrayList<String>();
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("class");
				String handlerName = attr.getNodeValue();
				if (DATAIMPORT_HANDLER_NAME.equalsIgnoreCase(handlerName)) {
					attr = attrs.getNamedItem("name");
					String name = attr.getNodeValue();
					if (name.startsWith("/"))
						name = name.substring(1);
					handlers.add(name);
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return handlers.toArray(new String[0]);
	}
	
	public boolean enableDIH() {
		if (document == null) return false;

		String libDir = conf.get("sys.lib.path");
		Element libChild = document.createElement("lib");
		libChild.setAttribute("dir", libDir);
		libChild.setAttribute("regex", ".*\\.jar");
		document.getDocumentElement().appendChild(libChild);
		
		return true;
	}
	
	public boolean addDataImportHandler(String dihName, String dihType, String confName) {
		if (document == null) return false;
		//if (isDIHExist(name)) return false;

		String requestName = dihName;
		if (!dihName.startsWith("/")) {
			requestName = "/" + dihName;
		}

		String libDir = conf.get("sys.lib.path") + "/" + dihType;
		Element libChild = document.createElement("lib");
		libChild.setAttribute("dir", libDir);
		libChild.setAttribute("regex", ".*\\.jar");
		document.getDocumentElement().appendChild(libChild);

		Element newChild = document.createElement("requestHandler");
		newChild.setAttribute("name", requestName);
		newChild.setAttribute("class", DATAIMPORT_HANDLER_NAME);
		
		Element subItem = document.createElement("lst");
		subItem.setAttribute("name", "defaults");
		newChild.appendChild(subItem);
		
		Element configItem = document.createElement("str");
		configItem.setAttribute("name", "config");
		subItem.appendChild(configItem);
		
		Text nameValue = document.createTextNode(confName);
		configItem.appendChild(nameValue);

		document.getDocumentElement().appendChild(newChild);

		return true;
	}
	
	public void persist(String target) throws Exception {
		File targetFile = new File(target);
//		if (targetFile.exists())
//			targetFile.delete();
		FileUtil.writeXmlFile(targetFile, document);
	}
	
	private void load(String configFile) throws ParserConfigurationException, SAXException, IOException {
		InputStream stream = null;
		File f = new File(configFile);
		if (f.exists() && f.isFile())
			stream = new FileInputStream(f);
		else {
			if (!zkOperator.isExist(configFile)) {
				LOG.warn("Cannot find schema file: " + configFile);
				throw new IOException("Cannot find schema file: " + configFile);
			}
			byte[] buf = zkOperator.getData(configFile);
			stream = new ByteArrayInputStream(buf);
		}
		load(stream);
	}

	private void load(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
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
		XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
	}
	
	private String stepsToPath(String[] steps) {
		StringBuilder builder = new StringBuilder();
		for (String step : steps)
			builder.append("/").append(step);
		return builder.toString();
	}


}
