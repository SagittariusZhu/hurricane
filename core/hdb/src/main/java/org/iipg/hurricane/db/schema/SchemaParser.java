package org.iipg.hurricane.db.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.util.ZkOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SchemaParser {
	
	public static final String SCHEMA_PATH = "/hurricane/schema";
	
	private static Logger LOG = LoggerFactory.getLogger(SchemaParser.class);

	private static HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	private ZkOperator zkOperator = conf.getZkOperator();
	private Document document = null;
	private XPath xpath;

	public static boolean drop(String coreName) {
		ZkOperator zkOperator = conf.getZkOperator();
		String zkNode = SCHEMA_PATH + "/" + coreName + ".xml";
		return zkOperator.remove(zkNode);		
	}
	
	public SchemaParser duplicate(String destName) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		DOMSource doms = new DOMSource( document );
		StreamResult result = new StreamResult( swapStream );
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		Properties properties = transformer.getOutputProperties();
		properties.setProperty( OutputKeys.ENCODING, "utf-8" );
		properties.setProperty( OutputKeys.METHOD, "xml" );
		properties.setProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperties( properties );
		transformer.transform( doms, result );
		
		ByteArrayInputStream bis = new ByteArrayInputStream(swapStream.toByteArray());
		SchemaParser newParser = new SchemaParser(bis);
		String expression = stepsToPath(new String[] { "schema" });
		try {
			Element nd = (Element) xpath.evaluate(expression, newParser.document,
					XPathConstants.NODE);
			if (nd != null) {
				nd.setAttribute("name", destName);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newParser;
	}

	public SchemaParser(String name) throws IOException, ParserConfigurationException, SAXException {
		String zkNode = SCHEMA_PATH + "/" + name + ".xml";
		if (!zkOperator.isExist(zkNode)) {
			LOG.warn("Cannot find schema file: " + zkNode);
			throw new IOException("Cannot find schema file: " + zkNode);
		}
		byte[] buf = zkOperator.getData(zkNode);
		InputStream stream = new ByteArrayInputStream(buf);
		parse(stream);
	}
	
//	public SchemaParser(String name) throws ParserConfigurationException, SAXException, IOException {
//		String fname = name + ".xml";
//		InputStream stream = ClassLoader.getSystemResourceAsStream(fname);
//		if (stream == null) {
//			stream = this.getClass().getResourceAsStream(fname);
//		}
//		if (stream == null) {
//			LOG.warn("Cannot find schema file: " + fname);
//			throw new IOException("Cannot find schema file: " + fname);
//		}
//		parse(stream);
//	}

	public SchemaParser(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		parse(stream);
	}
	
	private void parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
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

	public Schema getSchema() {
		Schema schema = new Schema();
		return null;

	}
	
	public HurricaneConfiguration getConf() {
		return this.conf;
	}
	
	public Document getDocument() {
		return this.document;
	}

	public String getName() {
		if (document == null) return null;
		
		String expression = stepsToPath(new String[] { "schema", "@name" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = nd.getNodeValue();
		return name;
	}

	public String getTable() {
		if (document == null) return null;
		
		String expression = stepsToPath(new String[] { "schema", "@table" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String table = nd.getNodeValue();
		return table;
	}

	public String getDesc() {
		if (document == null) return null;
		
		String expression = stepsToPath(new String[] { "schema", "@desc" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			if (nd != null) {
				String desc = nd.getNodeValue();
				return desc;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int getFactor() {
		if (document == null) return 0;
		
		String expression = stepsToPath(new String[] { "schema", "@factor" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			if (nd != null) {
				String factor = nd.getNodeValue();
				return Integer.parseInt(factor);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
	
	public int getShards() {
		if (document == null) return 0;
		
		String expression = stepsToPath(new String[] { "schema", "@shards" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			if (nd != null) {
				String shards = nd.getNodeValue();
				return Integer.parseInt(shards);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
	
	public int getMaxShardsPerNode() {
		if (document == null) return 0;
		
		String expression = stepsToPath(new String[] { "schema", "@maxShardsPerNode" });
		Node nd = null;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			if (nd != null) {
				String maxShardsPerNode = nd.getNodeValue();
				return Integer.parseInt(maxShardsPerNode);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public String getVersion() {
		if (document == null) return null;
		
		String expression = stepsToPath(new String[] { "schema", "@version" });
		Node nd;
		try {
			nd = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			String version = nd.getNodeValue();
			return version;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * @return 获取唯一键值
	 */
	public Field getUniqueKey() {
		if (document == null) return null;
		
		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "field" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Field field = new Field();

				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();

				String value;
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				field.setName(name);
				attr = attrs.getNamedItem("type");
				String type = attr.getNodeValue();
				field.setType(type);
				attr = attrs.getNamedItem("indexed");
				if (attr != null) {
					value = attr.getNodeValue();
					boolean indexed = Boolean.parseBoolean(value);
					field.setIndexed(indexed);
				}

				attr = attrs.getNamedItem("stored");
				if (attr != null) {
					value = attr.getNodeValue();
					boolean stored = Boolean.parseBoolean(value);
					field.setStored(stored);
				}
				attr = attrs.getNamedItem("multiValued");
				if (attr != null) {
					value = attr.getNodeValue();
					boolean multiValued = Boolean.parseBoolean(value);
					field.setMultiValued(multiValued);
				}
				attr = attrs.getNamedItem("required");
				if (attr != null) {
					value = attr.getNodeValue();
					boolean required = Boolean.parseBoolean(value);
					field.setRequired(required);
				}
				attr = attrs.getNamedItem("language");
				if (attr != null) {
					String language = attr.getNodeValue();
					field.setLanguage(language);
				}

				attr = attrs.getNamedItem("mode");
				if (attr != null) {
					String mode = attr.getNodeValue();
					field.setMode(mode);
				}

				attr = attrs.getNamedItem("unique");
				if (attr != null) {
					value = attr.getNodeValue();
					boolean unique = Boolean.parseBoolean(value);
					field.setUnique(unique);
					if (unique)
						return field;
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, Type> getTypes() {
		if (document == null) return null;
		
		Map<String, Type> types = new HashMap<String, Type>();

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "types", "fieldType" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++) {
				Type type = new Type();
				Node node = nodes.item(i);

				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				type.setName(name);
				types.put(name, type);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return types;
	}

	/**
	 * @param fieldname
	 *            字段名称
	 * @return 获取指定名称的字段
	 */

	public Field getField(String fieldName) {
		
		if (fieldName == null || fieldName.length() == 0) return null;
		

		if ("text".equals(fieldName)) {
			return fixedTextField();
		}
		
		String fName = fieldName;
		Node node = getFieldNode(fName);
		
		if (node == null) {
			// try extend field type
			if (fieldName.indexOf("_") >= 0) {
				String[] arr = fieldName.split("_");
				fName = arr[0];
			}

			node = getFieldNode(fName);
		}
		
		if (node == null) return null;
		
		Field field = new Field();
		fillField(field, node);
		
		return field;
	}

	/**
	 * @return 获取所有字段
	 */
	public List<Field> getFields() {
		if (document == null) return null;
		
		List<Field> fields = new ArrayList<Field>();

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "field" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Field field = new Field();
				Node node = nodes.item(i);			
				fillField(field, node);	
				if (field.getName().length() > 0)
					fields.add(field);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fields;
	}
	
	public String[] getFieldNames() {
		List<String> names = new ArrayList<String>();
		for (Field field : getFields()) {
			names.add(field.getName());
		}
		return names.toArray(new String[0]);
	}
	
	public Node getFieldsNode() {
		if (document == null) return null;
		
		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields" }))
				.toString();

		Node node;
		try {
			node = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			return node;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public Field getDynamicField(String fieldname) {
		if (document == null) return null;
		
		List<Field> fields = new ArrayList<Field>();

		String expression = new StringBuilder()
				.append(stepsToPath(new String[] { "schema", "fields",
						"dynamicField" })).toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Field field = new Field();

				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();

				String value;
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				// 取schema中动态字段的后缀
				String suffix = name.split("_")[1];

				if (!fieldname.endsWith(suffix))
					continue;
				
				fillField(field, node);
				
				return field;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public List<Field> getDynamicFields() {
		if (document == null) return null;
		
		List<Field> fields = new ArrayList<Field>();

		String expression = new StringBuilder()
				.append(stepsToPath(new String[] { "schema", "fields",
						"dynamicField" })).toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Field field = new Field();
				Node node = nodes.item(i);
				fillField(field, node);
				fields.add(field);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fields;
	}
	
	private Node getFieldNode(String fieldName) {
		if (document == null) return null;
		
		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "field" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				if (!name.equalsIgnoreCase(fieldName))
					continue;
				
				return node;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	private void refreshFieldNode(Field field) {
		String fieldName = field.getName();
		Node node = getFieldNode(fieldName);
		if (node == null) return;
		
		Node newAttr = document.createAttribute("desc");
		newAttr.setNodeValue(field.getDesc());
		node.getAttributes().setNamedItem(newAttr);
		return;
	}
	
	private void fillField(Field field, Node node) {
		NamedNodeMap attrs = node.getAttributes();
		String value;
		
		Node attr = attrs.getNamedItem("name");
		String name = attr.getNodeValue();
		field.setName(name);

		attr = attrs.getNamedItem("desc");
		if (attr != null) {
			String desc = attr.getNodeValue();
			field.setDesc(desc);
		}
		
		attr = attrs.getNamedItem("type");
		if (attr != null) {
			String type = attr.getNodeValue();
			field.setType(type);
		}
		
		attr = attrs.getNamedItem("indexed");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean indexed = Boolean.parseBoolean(value);
			field.setIndexed(indexed);
		}

		attr = attrs.getNamedItem("stored");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean stored = Boolean.parseBoolean(value);
			field.setStored(stored);
		}
		attr = attrs.getNamedItem("multiValued");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean multiValued = Boolean.parseBoolean(value);
			field.setMultiValued(multiValued);
		}
		attr = attrs.getNamedItem("required");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean required = Boolean.parseBoolean(value);
			field.setRequired(required);
		}
		attr = attrs.getNamedItem("language");
		if (attr != null) {
			String language = attr.getNodeValue();
			field.setLanguage(language);
		}

		attr = attrs.getNamedItem("mode");
		if (attr != null) {
			String mode = attr.getNodeValue();
			field.setMode(mode);
		}

		attr = attrs.getNamedItem("unique");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean unique = Boolean.parseBoolean(value);
			field.setUnique(unique);
		}
		attr = attrs.getNamedItem("log");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean log = Boolean.parseBoolean(value);
			field.setLog(log);
		}
		attr = attrs.getNamedItem("mask");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean mask = Boolean.parseBoolean(value);
			field.setMask(mask);
		}
		attr = attrs.getNamedItem("combine");
		if (attr != null) {
			value = attr.getNodeValue();
			boolean combine = Boolean.parseBoolean(value);
			field.setCombine(combine);
		}	
		attr = attrs.getNamedItem("length");
		if (attr != null) {
			value = attr.getNodeValue();
			int length = Integer.parseInt(value);
			field.setLength(length);
		}	
	}

	private Field fixedTextField() {
		Field field = new Field();
		
		field.setName("text");
		field.setDesc("Full Text");
		field.setType("text");
		field.setIndexed(true);
		field.setStored(false);
		field.setMultiValued(false);
		field.setRequired(true);
		field.setLanguage("cn");
		field.setMode("ro");
		field.setUnique(false);
		field.setLog(false);
		field.setMask(false);
		field.setCombine(false);
		
		return field;
	}
	
	public Relation getRelation(String relationame) {
		if (document == null) return null;
		
		List<Field> fields = new ArrayList<Field>();

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "relation" }))
				.toString();

		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Relation relation = new Relation();

				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();

				String value = null;
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();

				if (!name.equalsIgnoreCase(relationame))
					continue;
				relation.setName(relationame);
				attr = attrs.getNamedItem("type");
				String type = attr.getNodeValue();
				relation.setType(type);

				attr = attrs.getNamedItem("from");
				if (attr != null) {
					value = attr.getNodeValue();
					String from = attr.getNodeValue();
					relation.setFrom(from);
				}

				attr = attrs.getNamedItem("to");
				if (attr != null) {
					String to = attr.getNodeValue();
					relation.setTo(to);
				}

				attr = attrs.getNamedItem("field");
				if (attr != null) {
					String field = attr.getNodeValue();
					relation.setField(field);
				}
				return relation;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public List<Relation> getRelations() {
		if (document == null) return null;
		
		List<Relation> relations = new ArrayList<Relation>();

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "schema", "fields", "relation" }))
				.toString();
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Relation relation = new Relation();

				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("name");
				if (attr != null) {
					String name = attr.getNodeValue();
					relation.setName(name);
				}
				attr = attrs.getNamedItem("type");
				if (attr != null) {
					String type = attr.getNodeValue();
					relation.setType(type);
				}
				attr = attrs.getNamedItem("from");
				if (attr != null) {
					String from = attr.getNodeValue();
					relation.setFrom(from);
				}
				attr = attrs.getNamedItem("to");
				if (attr != null) {
					String to = attr.getNodeValue();
					relation.setTo(to);
				}

				attr = attrs.getNamedItem("field");
				if (attr != null) {
					String field = attr.getNodeValue();
					relation.setField(field);
				}
				relations.add(relation);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return relations;
	}

	/**
	 * 
	 * @param content
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void mergeFields(String content) throws ParserConfigurationException, SAXException, IOException {
		InputSource is = new InputSource(new ByteArrayInputStream(content.getBytes("UTF-8")));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setXIncludeAware(true);
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		db = dbf.newDocumentBuilder();
		Document updateDocument = db.parse(is);

		String expression = new StringBuilder().append(
				stepsToPath(new String[] { "fields", "field" }))
				.toString();
		
		Node parentNode = getFieldsNode();
		
		if (parentNode == null) {
			LOG.warn("No valid fields node!");
			return;
		}
		
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, updateDocument,
					XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node attr = attrs.getNamedItem("name");
				String name = attr.getNodeValue();
				Field currField = getField(name);
				if (currField != null) {
					attr = attrs.getNamedItem("desc");
					if (attr == null) {
						throw new ParserConfigurationException("Field " + name + " already exist, only support DESC update.");						
					}
					String desc = attr.getNodeValue();
					currField.setDesc(desc);
					refreshFieldNode(currField);
				} else 
					parentNode.appendChild(document.importNode(node, true));
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean persistent(boolean force) throws TransformerException {
		String zkNode = getSchemaPath();
		if (!force && zkOperator.isExist(zkNode)) {
			return false;
		}
		
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		DOMSource doms = new DOMSource( document );
		StreamResult result = new StreamResult( swapStream );
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		Properties properties = transformer.getOutputProperties();
		properties.setProperty( OutputKeys.ENCODING, "utf-8" );
		properties.setProperty( OutputKeys.METHOD, "xml" );
		properties.setProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperties( properties );
		transformer.transform( doms, result );

		zkOperator.update(zkNode, swapStream.toByteArray());
		return true;
	}

	public boolean exists() {
		String zkNode = getSchemaPath();
		return zkOperator.isExist(zkNode);
	}
	
	public boolean drop() {
		String zkNode = getSchemaPath();
		return zkOperator.remove(zkNode);		
	}
	
	private String getSchemaPath() {
		return SCHEMA_PATH + "/" + getName() + ".xml";
	}
	
	public static void main(String[] args) {
		SchemaParser test;
		try {
			InputStream is = new FileInputStream(new File("C:/workspace/hmw/java/trunk/dist/hmw/schema/email.xml"));
			test = new SchemaParser(is);

			System.out.println(test.getName());
			System.out.println(test.getVersion());
			System.out.println(test.getDesc());

			System.out.println(test.getUniqueKey().getName());
			
			String fieldContext = "<fields><field name=\"fromaddr\" desc=\"发件人\" /></fields>";
			test.mergeFields(fieldContext);
			
			Field currField = test.getField("fromaddr");
			System.out.println(currField.getName());
			System.out.println(currField.getDesc());
			
//			System.out.println(test.getFields().toString());

//			System.out.println(test.getDynamicField("test_str").getName());
//			System.out.println(test.getDynamicFields().toString());
//
//			System.out.println(test.getRelation("send_receive").getType());
//			System.out.println(test.getRelations().toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
