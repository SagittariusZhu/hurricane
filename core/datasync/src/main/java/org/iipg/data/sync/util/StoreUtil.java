package org.iipg.data.sync.util;

import java.io.IOException;

import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.data.sync.ds.DataSource;
import org.iipg.data.sync.store.*;
import org.json.JSONArray;


public class StoreUtil {

	public static int store(WorkerProp prop, JSONArray values) throws IOException {
		AbstractStorer storer = null;
		
		DataSource ds = prop.getDestDS();
		if (ds.getType().equals("mysql")) {
			storer = new SQLStorer(ds);
		} else if (ds.getType().equals("solr")) {
			storer = new SolrStorer(ds);
		} else if (ds.getType().equals("restful")) {
			storer = new RestfulStorer(ds);
		}

		int count = 0;
		if (ds != null) {
			count = storer.store(prop.getTarget(), prop.getMetaMap(), values);
			System.out.println("add " + count + " docs to " + prop.getDest());
		}
		
		return count;
	}

	public static int solrStore(WorkerProp prop, JSONArray values) throws IOException {
		SolrStorer storer = new SolrStorer(prop.getDestDS());
		int count = storer.store(prop.getTarget(), prop.getMetaMap(), values);
		System.out.println("add " + count + " docs to solr.");
		return count;
	}
}
