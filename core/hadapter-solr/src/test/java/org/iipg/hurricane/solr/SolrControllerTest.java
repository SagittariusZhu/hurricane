package org.iipg.hurricane.solr;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HandlerUtil;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.iipg.hurricane.util.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SolrControllerTest {

	private HurricaneConfiguration conf = null;
	private SolrController controller = null;
	
	@Before
	public void init() {
		conf = new HurricaneConfiguration();
		controller = new SolrController(conf);
	}
	
	@Test
	public void addDih() {
		List<HDBDiHandler> handlers = HandlerUtil.getHandlers(conf, "mytest");
		boolean retValue = controller.createHandlers("mytest", handlers);
		assertTrue(retValue);
	}
	
	@Test
	public void reload() {
		boolean retValue = controller.reloadSchema("mytest");
		assertTrue(retValue);
	}
	
	@Test
	public void run() {
		String schemaFile = "email2";
		SchemaParser parser = null;
		try {
			parser = new SchemaParser(schemaFile);
			controller.createSchema(parser);
		} catch (ParserConfigurationException e) {
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
	
	@Test
	public void delete() {
		String coreName = "email2";
		controller.deleteSchema(coreName, false);
	}
	
	@Test
	public void listDIHs() {
		List<HDBDiHandler> handlers = new ArrayList<HDBDiHandler>();
		HDBDiHandler handler = new HDBDiHandler("dbimport", "hsqldb");
		byte[] content = FileUtil.read(new File("C:/runtime/export/db-data-config.xml"));
		handler.setDihContent(content);
		handlers.add(handler);
		
		handler = new HDBDiHandler("rssimport", "rss");
		content = FileUtil.read(new File("C:/runtime/export/rss-data-config.xml"));
		handler.setDihContent(content);
		handlers.add(handler);
		
		controller.addDataImportHandler("email", handlers);
		//controller.listDataImportHandlers();
	}
	
	@Test
	public void fullImport() {
		String url = "http://sr1:8080/solr/email";
		SolrClient client = new HttpSolrClient(url);
        SolrQuery query;
        try {
            query = new SolrQuery();
            //query.setParam(CommonParams.QT,"/dataimport");
            query.setRequestHandler("/rssimport");
            query.setParam("command", "status");
            QueryRequest request = new QueryRequest(query);
            QueryResponse response = request.process(client);
            client.commit();
            int status = response.getStatus();
            System.out.println("status: " + status);
            System.out.println(response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
}
