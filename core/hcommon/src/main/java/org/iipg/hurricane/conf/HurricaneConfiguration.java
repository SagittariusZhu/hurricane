package org.iipg.hurricane.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.util.ZkOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HurricaneConfiguration {

	private static final String CONF_NAME = "hurricane-site.xml";

	public static final Logger LOG = LoggerFactory.getLogger(HurricaneConfiguration.class);
	
	private Map<String, String> props = new HashMap<String, String>();
	
	private ZkOperator zkOperator = null;

	public HurricaneConfiguration(String fileName){
		load(fileName);
		zkOperator = new ZkOperator(get("zk.host.endpoints"));
	}

	public HurricaneConfiguration(){
		this(CONF_NAME);
	}

	private void load(String fileName) {
		URL url = null;
        ClassLoader cl = this.getClass().getClassLoader();  
        if (cl == null) {  
        	url = ClassLoader.getSystemResource(fileName);  
        } else {  
        	url = cl.getResource(fileName);
        }
        
        if (url == null) {
        	LOG.warn("cannot find configuration file: " + fileName);
        	return;
        } else {
        	LOG.info("Use configuration : " + url.getFile());
        }
        
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(url.getFile());
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList nodeList = (NodeList) root.getChildNodes();
			if(nodeList != null){
				for(int i=0; i<nodeList.getLength(); i++){
					Node node = nodeList.item(i);
					
					String propName = "";
					String propValue = "";
					for(Node prop = node.getFirstChild(); prop != null; prop = prop.getNextSibling()){
						if(prop.getNodeType() == Node.ELEMENT_NODE){
							if(prop.getNodeName().equals("name")){
								if (prop.getFirstChild() != null)
									propName = prop.getFirstChild().getNodeValue();
							}
							if(prop.getNodeName().equals("value")){
								if (prop.getFirstChild() != null)
									propValue = prop.getFirstChild().getNodeValue();
							}
						}
					}   
					if (propName != null && propName.length() > 0) {
						props.put(propName, propValue);
					}
				}        
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	} 

	public boolean isCloud() {
		if (props.containsKey("use.cloud")) {
			return Boolean.valueOf((props.get("use.cloud")));
		}
		return true;
	}

	public void setCloud(boolean isCloud) {
		props.put("use.cloud", String.valueOf(isCloud));
	}

	public String get(String propName) {
		return props.get(propName);
	}	
	
	public boolean exist(String propName) {
		return props.containsKey(propName);
	}
	
	public Map getProps() {
		return this.props;
	}

	public ZkOperator getZkOperator() {
		return this.zkOperator;
	}
}
