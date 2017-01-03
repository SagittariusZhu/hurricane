package org.iipg.data.sync.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.iipg.data.sync.ds.DataSource;
import org.iipg.data.sync.ds.DataSourceFactory;

public class SyncConfiguration {

	private Map<String, DataSource> dsCache = new HashMap<String, DataSource>();
	private List<WorkerProp> taskCache = new ArrayList<WorkerProp>();

	public SyncConfiguration(InputStream in) {
		if (in != null) {
			try {
				load(in);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public SyncConfiguration(String filePath) {
		InputStream in = null;

		// first: absolute path
		if (isAbsolutePath(filePath)) {
			try {
				in = new FileInputStream(filePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		// second: classpath search
		if (in == null) {
			URL url = null;
			ClassLoader cl = this.getClass().getClassLoader();  
			if (cl == null) {  
				url = ClassLoader.getSystemResource(filePath);  
			} else {  
				url = cl.getResource(filePath);
			}

			if (url != null) {
				try {
					in = new FileInputStream(url.getFile());
				} catch (IOException e) {
				}
			}
		}

		if (in != null) {
			try {
				load(in);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isAbsolutePath(String path) {
		if (path.startsWith("/") || path.indexOf(":") > 0) {
			return true;
		}
		return false;
	}

	public void load(InputStream in) throws DocumentException {

		SAXReader reader = new SAXReader();  
		Document document = reader.read(in);

		Element node = document.getRootElement();  

		// get root element
		Element configEle = node;
		if (!configEle.getName().equals("dataConfig")) return;

		// get all dataSource elements
		List<Element> dsEles = configEle.elements("dataSource");
		parseDataSource(dsEles);

		Element tasksEle = configEle.element("tasks");
		List<Element> taskEles = tasksEle.elements("entity");
		parseTasks(taskEles);
	}

	private void parseDataSource(List<Element> dsEles) {
		for (int i=0; i<dsEles.size(); i++) {
			Element dsEle = dsEles.get(i);

			String name = dsEle.attributeValue("name");
			String type = dsEle.attributeValue("type");

			DataSource ds = DataSourceFactory.newInstance(type);
			if (ds == null) {
				System.out.println("No suitable DataSource for type " + type);
				continue;
			}

			ds.parse(dsEle);

			dsCache.put(name, ds);
		}
	}

	private void parseTasks(List<Element> taskEles) {
		for (int i=0; i<taskEles.size(); i++) {
			Element taskEle = taskEles.get(i);

			WorkerProp task = new WorkerProp(this);
			task.parse(taskEle);

			taskCache.add(task);
		}
	}

	public List<WorkerProp> getWorkerProps() { return this.taskCache; }

	public DataSource getDataSource(String dsName) {
		if (dsCache.containsKey(dsName))
			return dsCache.get(dsName);
		return null;
	}
}
