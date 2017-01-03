package org.iipg.hurricane.solr.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

public class SolrClientCache {
	private static final Log LOG = LogFactory.getLog(SolrClientCache.class);

	private static ConcurrentLinkedQueue<CloudSolrClient> cloudClientQueue = new ConcurrentLinkedQueue<CloudSolrClient>();

	public static CloudSolrClient getCloudSolrClient() {
		CloudSolrClient client = null;
		LOG.info("cloudClientQueue: " + cloudClientQueue.size());

		synchronized(cloudClientQueue) {
			if(!cloudClientQueue.isEmpty()) {
				client = cloudClientQueue.poll();
				LOG.info("Brower CloudSolrClient from cache.");
			} else {
				client = createCloudClient();
				LOG.info("Create new CloudSolrClient.");
			}
		}
		return client;
	}

	private static CloudSolrClient createCloudClient() {
		ResourceBundle bundle = null;
		CloudSolrClient client = null;
		try {
			bundle = ResourceBundle.getBundle("zk");			
			String zkHost = bundle.getString("zk.host.endpoints");		

			client = new CloudSolrClient(zkHost);

			client.setZkClientTimeout(Integer.valueOf(bundle.getString("zk.client.timeout")));
			client.setZkConnectTimeout(Integer.valueOf(bundle.getString("zk.connect.timeout")));

			client.connect();

			return client;
		} catch(MissingResourceException e){
			throw new IllegalArgumentException("[zk.properties] is not found!");
		}
	}

	public static void release(SolrClient client) {
		if (client instanceof CloudSolrClient) {
			synchronized(cloudClientQueue) {
				((CloudSolrClient) client).setDefaultCollection(null);
				cloudClientQueue.add((CloudSolrClient) client);
				LOG.info("Return CloudSolrClient to cache.");
			}
		}

	}

}
