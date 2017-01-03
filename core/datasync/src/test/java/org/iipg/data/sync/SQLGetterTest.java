package org.iipg.data.sync;

import java.io.IOException;
import java.util.List;

import org.iipg.data.sync.SQLGetter;
import org.iipg.data.sync.conf.SyncConfiguration;
import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.data.sync.store.SolrStorer;
import org.json.JSONArray;
import org.junit.Test;

public class SQLGetterTest {

	@Test
	public void run() {
		SyncConfiguration conf = new SyncConfiguration("sync-news2.xml");

		List<WorkerProp> props = conf.getWorkerProps();

		WorkerProp prop = props.get(0);
		
		SQLGetter getter = new SQLGetter(prop.getSourceDS());
		
		JSONArray values = getter.get(prop.getQStr() + " LIMIT 0, 10");
		
		System.out.println("Total :" + values.length());
		
		// store data to remote server
		
		SolrStorer storer = new SolrStorer(prop.getDestDS());
		
		int count = storer.store(prop.getTarget(), prop.getMetaMap(), values);
		
		System.out.println("add " + count + " docs to solr.");

	}
}
