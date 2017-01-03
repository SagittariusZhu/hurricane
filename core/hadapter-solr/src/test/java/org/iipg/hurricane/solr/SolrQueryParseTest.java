package org.iipg.hurricane.solr;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.qparser.SimpleQueryParser;
import org.iipg.hurricane.search.Query;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SolrQueryParseTest {

	@Test
	public void run() {
		String qStr = "+content:(-会计 +李克强) +senddate:[2013-11-01 TO 2015-01-01]";
		SimpleQueryParser parser = new SimpleQueryParser("text");
		try {
			Query q = parser.parse(qStr);
			System.out.println(q);
			System.out.println(SolrUtil.getSolrSql(new SchemaParser("email"), q));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.iipg.hurricane.qparser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
