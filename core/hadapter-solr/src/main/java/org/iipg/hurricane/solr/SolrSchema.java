package org.iipg.hurricane.solr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.iipg.hurricane.db.schema.ext.HblobTyper;
import org.iipg.hurricane.util.FileUtil;
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

public class SolrSchema {

	public static final String DEFAULT_SCHEMA_FILE = "schema.xml";
	
	private static Logger LOG = LoggerFactory.getLogger(SolrSchema.class);
	
	private HurricaneConfiguration conf = null;
	private String templatePath = "";
	private Document document = null;
	private XPath xpath;
	
	public SolrSchema(HurricaneConfiguration conf, String defaultSchemaFile) throws ParserConfigurationException, SAXException, IOException {
		this.conf = conf;
		this.templatePath = conf.get("solr.template.path") + File.separator + "conf";
		String schemaFile = this.templatePath + File.separator + defaultSchemaFile;
		load(schemaFile);
	}
	
	/*
	<field name="WZHAI" type="text" stored="true" language="cn" multiValued="false" />
	<field name="GJC" type="string" indexed="true" stored="true"  multiValued="true" />

	<field name="BWBS" type="string" indexed="true" stored="true" multiValued="false"/>
	<field name="XLMC" type="string" indexed="true" stored="true" multiValued="false"/>
	*/
	
	public boolean existField(String fieldName) {
		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "field" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				if (name.equalsIgnoreCase(fieldName))
					return true;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public boolean addField(Field item) {
		if (document == null) return false;
		if (existField(item.getName())) return false;
		
		if (HTyper.isExtType(item.getType())) {
			try {
				HTyper typer = HTyper.getTyper(item);
				Field[] list = typer.getFields();
				for (Field f : list) {
					addField(f);
				}
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Element newChild = document.createElement("field");
		
		newChild.setAttribute("name", item.getName());
		if ("text".equals(item.getType())) {
			newChild.setAttribute("type", item.getType() + "_" + item.getLanguage());
		} else {
			newChild.setAttribute("type", item.getType());			
		}
		newChild.setAttribute("indexed", item.isIndexed() + "");
		newChild.setAttribute("stored", item.isStored() + "");
		newChild.setAttribute("multiValued", item.isMultiValued() + "");
		newChild.setAttribute("required", item.isRequired() + "");

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields" }))
				.toString();

		Node node;
		try {
			node = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			node.appendChild(newChild);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// unique field
		// <uniqueKey>BWBS</uniqueKey>
		if (item.isUnique()) {
			Element uniqueNode = document.createElement("uniqueKey");
			Text nameValue = document.createTextNode(item.getName());
			uniqueNode.appendChild(nameValue);
			document.getDocumentElement().appendChild(uniqueNode);
		}
		
		// copy Field
		// <copyField source="YYJDZ" dest="text"/>
		if (item.isCombine()) {
			Element combineNode = document.createElement("copyField");
			combineNode.setAttribute("source", item.getName());
			combineNode.setAttribute("dest", "text");
			document.getDocumentElement().appendChild(combineNode);
		}
		return true;
	}

	public void persist(String target) throws Exception {
		File targetFile = new File(target);
//		if (targetFile.exists())
//			targetFile.delete();
		FileUtil.writeXmlFile(targetFile, document);
	}

	private void load(String schemaFile) throws ParserConfigurationException, SAXException, IOException {
		InputStream stream = new FileInputStream(schemaFile);
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

	public void setName(String name) {
		Element rootNode = document.getDocumentElement();
		rootNode.setAttribute("name", name);
	}

	public void setDesc(String desc) {
		Element rootNode = document.getDocumentElement();
		rootNode.setAttribute("desc", desc);
	}
}
