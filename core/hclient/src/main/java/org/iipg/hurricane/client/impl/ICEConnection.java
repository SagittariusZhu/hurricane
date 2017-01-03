package org.iipg.hurricane.client.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.iipg.hurricane.client.HConnException;
import org.iipg.hurricane.client.HConnFactory;
import org.iipg.hurricane.client.HConnection;
import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.response.StatusResponse;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.iipg.hurricane.client.metadata.*;
import org.iipg.hurricane.model.HMWBlobHolder;
import org.iipg.hurricane.model.HMWConnException;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWDocumentSeqHolder;
import org.iipg.hurricane.model.HMWResponse;
import org.iipg.hurricane.service.HBrokerPrx;
import org.iipg.hurricane.service.HBrokerPrxHelper;
import org.iipg.hurricane.client.util.HMWUtil;
import org.iipg.hurricane.client.util.JSONUtil;
import org.json.JSONObject;

public class ICEConnection implements HConnection {

	public static Queue QUEUE = new ConcurrentLinkedQueue();
	public static Map<String, StatusResponse> ftpStatus = new HashMap<String, StatusResponse>();

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	private static Ice.Properties properties;

	private Ice.Communicator ic;
	private Ice.ObjectPrx obj = null;

	private FtpClientRunner ftpRunner = new FtpClientRunner();

	public void setCommunicator(Ice.Communicator ic) {
		this.ic = ic;
		this.obj = ic.propertyToProxy("DataReceiver.Proxy");
	}

	public ICEConnection(String serverUrl, String proxyName) throws HConnException {
		ic = getCommunicator(serverUrl, proxyName);  
        String[] arr = serverUrl.split(":");
        String url = String.format("%1$s -t -e 1.0:tcp -h %2$s -p %3$s", proxyName, arr[0], arr[1]);
        obj = ic.stringToProxy(url);
	}

	public ICEConnection(String configName) throws HConnException {
		ic = getCommunicator(configName);  
		obj = ic.propertyToProxy("DataReceiver.Proxy");
	}

	private void startFTPRunner() {
		if (ftpRunner == null) {
			ftpRunner = new FtpClientRunner();
		}
		ftpRunner.start(this);
	}

	private void stopFTPRunner() {
		if (ftpRunner != null) {
			ftpRunner.close();
			ftpRunner = null;
		}
	}

	/** 
	 * @param sConfig 
	 * @return Communicator 
	 * @throws HConnException 
	 */ 
	protected Ice.Communicator getCommunicator(String sConfig) throws HConnException 
	{ 
		Ice.Communicator communicator = null; 
		properties = Ice.Util.createProperties(); 
		//System.out.println("Starting load");      
		try {
			properties.load(sConfig);
		} catch (Ice.FileException e) {
			throw new HConnException("Config File " + sConfig + " load failed, please check it in classpath.");
		}
		//System.out.println("end load"); 
		Ice.InitializationData data = new Ice.InitializationData();
		data.properties = properties;
		communicator = Ice.Util.initialize(data); 
		if (communicator == null) { 
			System.out.println("ICE get Communicator by properties is  null!!!!!!"); 
		} 

		return communicator; 
	}
	
	/**
	 * 
	 * @param serverUrl
	 * @param proxyName
	 * @return
	 * @throws HConnException
	 */
	protected Ice.Communicator getCommunicator(String serverUrl, String proxyName) throws HConnException 
    {
		Ice.Communicator communicator = null; 
        Ice.Properties properties = Ice.Util.createProperties();
        properties.setProperty("Ice.Warn.Connections", "1");
        properties.setProperty("Ice.MessageSizeMax", "1000000000");
        Ice.InitializationData initData = new Ice.InitializationData();
        initData.properties = properties;
        communicator = Ice.Util.initialize(initData);
		if (communicator == null) { 
			System.out.println("ICE get Communicator by properties is  null!!!!!!"); 
		} 
        return communicator;
    }

	public void close() {
		stopFTPRunner();

		if (ic != null) {
			try {  
				ic.destroy();  
			} catch (Exception e) {  
				System.err.println(e.getMessage());  
			}  
		}  
	}

	@Override
	public UpdateResponse add(HDocument doc) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.add(HMWUtil.toHMWDocument(doc));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.reason);
		}
	}

	@Override
	public UpdateResponse add(HDocumentList docs) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		List<HMWDocument> hDocs = new ArrayList<HMWDocument>();
		for (int i=0; i<docs.size(); i++) {
			HMWDocument hDoc = HMWUtil.toHMWDocument(docs.get(i));
			hDocs.add(hDoc);
		}
		try {
			HMWResponse resp = broker.addBatch(hDocs.toArray(new HMWDocument[0]));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.reason);
		}
	}

	@Override
	public QueryResponse query(HQuery q) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.query(HMWUtil.toHMWQuery(q));
			return HMWUtil.createQueryResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.reason);
		}

	}

	@Override
	public QueryResponse reportQuery(HReportQuery q) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.reportQuery(HMWUtil.toHMWReportQuery(q));
			return HMWUtil.createQueryResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.reason);
		}
	}


	@Override
	public QueryResponse getBinary(String schemaName, Object uuid, String itemID) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.getBinary(HMWUtil.toSimpleHMWQuery(schemaName, uuid, itemID));
			return HMWUtil.createQueryResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.reason);
		}
	}


	@Override
	public UpdateResponse update(HDocument doc) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.update(HMWUtil.toHMWDocument(doc));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.getMessage());
		}
	}


	@Override
	public UpdateResponse update(HDocument doc, HQuery q) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.updateByQuery(HMWUtil.toHMWDocument(doc), HMWUtil.toHMWQuery(q));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.getMessage());
		}
	}


	@Override
	public UpdateResponse delete(HDocument doc) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.delete(HMWUtil.toHMWDocument(doc));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.getMessage());
		}
	}


	@Override
	public UpdateResponse delete(HQuery q) throws HConnException {
		HBrokerPrx broker = HBrokerPrxHelper.checkedCast(obj);
		try {
			HMWResponse resp = broker.deleteByQuery(HMWUtil.toHMWQuery(q));
			return HMWUtil.createUpdateResponse(resp);
		} catch (HMWConnException e) {
			throw new HConnException(e.getMessage());
		}
	}


	@Override
	public String upload(String srcFile, String uuid) {
		String taskID = "upload-" + UUID.randomUUID().toString();
		Map<String, String> item = new HashMap<String, String>();
		item.put("src", srcFile);
		item.put("dest", uuid);
		item.put("taskID", taskID);
		QUEUE.add(item);
		return taskID;
	}


	@Override
	public String download(String uuid, String destFile) {
		String taskID = "download-" + UUID.randomUUID().toString();
		Map<String, String> item = new HashMap<String, String>();
		item.put("src", uuid);
		item.put("dest", destFile);
		item.put("taskID", taskID);
		QUEUE.add(item);
		return taskID;
	}

	@Override
	public StatusResponse getStatus(String taskID) {
		if (taskInQueue(taskID)) {
			return StatusResponse.WAITING;
		}

		if (ftpStatus.containsKey(taskID)) {
			return ftpStatus.get(taskID);
		}

		return StatusResponse.FINISHED;
	}

	public void report(String taskID, long process) {
		ftpStatus.put(taskID, new StatusResponse(process));
	}

	private boolean taskInQueue(String taskID) {
		for (Iterator it = QUEUE.iterator(); it.hasNext(); ) {
			Map item = (Map) it.next();
			if (item.containsKey("taskID")) {
				if (taskID.equalsIgnoreCase((String) item.get("taskID")))
					return true;
			}
		}
		return false;
	}

	@Override
	public String getSerialID(boolean includeDate) {
		StringBuffer buf = new StringBuffer();
		if (includeDate) {
			buf.append(sdf.format(new Date())).append("/");
		}
		buf.append(UUID.randomUUID().toString());
		return buf.toString();
	}

	public Map getConfiguration() {
		HCriteria crit = new HCriteria();
		HQuery q = HQueryFactory.newQuery("__sys_configuration", crit);
		QueryResponse resp = null;
		try {
			resp = query(q);
		} catch (HConnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HDocumentList list = resp.getResults();
		if (list != null) {
			HDocument doc = list.get(0);
			if (doc != null) {
				return doc.getProps();
			}
		}
		return new HashMap();
	}

	@Override
	public SchemaMetadata getSchemaMetadata(String schemaName) {
		HCriteria crit = new HCriteria();
		crit.addEqualTo("schemaName", schemaName);
		HQuery q = HQueryFactory.newQuery("__sys_schema", crit);
		QueryResponse resp = null;
		try {
			resp = query(q);
		} catch (HConnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HDocumentList list = resp.getResults();
		if (list != null) {
			HDocument doc = list.get(0);
			if (doc != null) {
				return new SchemaMetadata(doc.getProps());
			}
		}
		return null;
	}

	@Override
	public void initFtpClient() {
		startFTPRunner();
	}
}
