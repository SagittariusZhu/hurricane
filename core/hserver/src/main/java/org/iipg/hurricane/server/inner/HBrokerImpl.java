package org.iipg.hurricane.server.inner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.db.HDBBroker;
import org.iipg.hurricane.db.HDocument;
import org.iipg.hurricane.db.HDBFactory;
import org.iipg.hurricane.db.HQuery;
import org.iipg.hurricane.db.HReportQuery;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.util.HDBUtil;
import org.iipg.hurricane.model.HMWBlobHolder;
import org.iipg.hurricane.model.HMWConnException;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWDocumentSeqHolder;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.model.HMWReportQuery;
import org.iipg.hurricane.model.HMWResponse;
import org.iipg.hurricane.service._HBrokerDisp;
import org.iipg.hurricane.util.HMWResponseUtil;
import org.xml.sax.SAXException;

import Ice.Current;

public class HBrokerImpl extends _HBrokerDisp {
	
	public static final Log LOG = LogFactory.getLog(HBrokerImpl.class);

	@Override
	public HMWResponse add(HMWDocument doc, Current __current)
			throws HMWConnException {
		LOG.info("receive insert request: " + doc.schema);
		long start = System.currentTimeMillis();
		HDocument hDoc = null;
		try {
			hDoc = HDocument.parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e.getMessage(), 302);
		}
		//Global.QUEUE.add(record);
		
		String schemaName = doc.schema;
		if (schemaName == null || schemaName.length() == 0) {
			throw new HMWConnException("Please set schema name first.", 301);
		}
		HDBBroker broker = null;
		HMWResponse resp = null;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			broker.store(hDoc);
			resp = HMWResponseUtil.createSuccessResponse(1, (int) (System.currentTimeMillis() - start));
			return resp;
		} catch (Exception e) {
			LOG.warn("ADD: " + e.getMessage());
			throw new HMWConnException(e.getMessage(), 302);
		}
	}

	@Override
	public HMWResponse addBatch(HMWDocument[] docs, Current __current)
			throws HMWConnException {
		int count = 0;
		long start = System.currentTimeMillis();
		HMWResponse resp = null;
		for (int i=0; i<docs.length; i++) {
			//HDocument record = HMWUtil.toHDocument(docs[i]);
			//Global.QUEUE.add(record);
			add(docs[i], __current);
			count ++;
		}
		resp = HMWResponseUtil.createSuccessResponse(count, (int) (System.currentTimeMillis() - start));
		return resp;
	}

	@Override
	public HMWResponse query(HMWQuery q, Current __current)	throws HMWConnException {		
		LOG.info("receive query request: " + q.schema);
		
		String schemaName = q.schema;
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HQuery query = HQuery.parse(q);

			HDBResponse resp = broker.query(query);
			return HMWResponseUtil.createResponse(resp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}

	@Override
	public HMWResponse getBinary(HMWQuery q, Current __current) throws HMWConnException {
		LOG.info("receive query request: " + q);
		
		String schemaName = q.schema;
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HDBQuery query = HDBUtil.parseSimpleQuery(q);
			HDBResponse resp = broker.getBinary(query);
			
			return HMWResponseUtil.createResponse(resp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}

	@Override
	public HMWResponse update(HMWDocument doc, Current __current)
			throws HMWConnException {
		LOG.info("receive update request: " + doc.schema);
		long start = System.currentTimeMillis();
		HDocument hDoc = null;
		try {
			hDoc = HDocument.parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e.getMessage(), 302);
		}
		
		String schemaName = doc.schema;
		if (schemaName == null || schemaName.length() == 0) {
			throw new HMWConnException("Please set schema name first.", 301);
		}
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			broker.update(hDoc);
			return HMWResponseUtil.createSuccessResponse(1, (int) (System.currentTimeMillis() - start));
		} catch (Exception e) {
			LOG.warn("UPDATE: " + e.getMessage());
			throw new HMWConnException(e.getMessage(), 302);
		}
	}

	@Override
	public HMWResponse updateBatch(HMWDocument[] docs, Current __current)
			throws HMWConnException {
		int count = 0;
		long start = System.currentTimeMillis();
		HMWResponse resp = null;
		for (int i=0; i<docs.length; i++) {
			//HDocument record = HMWUtil.toHDocument(docs[i]);
			//Global.QUEUE.add(record);
			update(docs[i], __current);
			count ++;
		}
		resp = HMWResponseUtil.createSuccessResponse(count, (int) (System.currentTimeMillis() - start));
		return resp;
	}

	@Override
	public HMWResponse updateByQuery(HMWDocument doc, HMWQuery q, Current __current)
			throws HMWConnException {
		LOG.info("receive updateByQuery request: " + q.schema);
		long start = System.currentTimeMillis();

		HDocument hDoc = null;
		try {
			hDoc = HDocument.parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e.getMessage(), 302);
		}
		
		String schemaName = q.schema;
		if (schemaName == null || schemaName.length() == 0) {
			throw new HMWConnException("Please set schema name first.", 301);
		}

		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HQuery query = HQuery.parse(q);
			int val = broker.updateByQuery(hDoc, query);
			return HMWResponseUtil.createSuccessResponse(val, (int) (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}

	@Override
	public HMWResponse delete(HMWDocument doc, Current __current) throws HMWConnException {
		LOG.info("receive delete request: " + doc.schema);
		long start = System.currentTimeMillis();
		HDocument hDoc = null;
		try {
			hDoc = HDocument.parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e.getMessage(), 302);
		}
		
		String schemaName = doc.schema;
		if (schemaName == null || schemaName.length() == 0) {
			throw new HMWConnException("Please set schema name first.", 301);
		}
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			int val = broker.delete(hDoc);
			return HMWResponseUtil.createSuccessResponse(val, (int) (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}
	

	@Override
	public HMWResponse deleteBatch(HMWDocument[] docs, Current __current)
			throws HMWConnException {
		int count = 0;
		long start = System.currentTimeMillis();
		HMWResponse resp = null;
		for (int i=0; i<docs.length; i++) {
			//HDocument record = HMWUtil.toHDocument(docs[i]);
			//Global.QUEUE.add(record);
			delete(docs[i], __current);
			count ++;
		}
		resp = HMWResponseUtil.createSuccessResponse(count, (int) (System.currentTimeMillis() - start));
		return resp;
	}

	@Override
	public HMWResponse deleteByQuery(HMWQuery q, Current __current)	throws HMWConnException {
		LOG.info("receive delete request: " + q.schema);
		long start = System.currentTimeMillis();
		String schemaName = q.schema;
		if (schemaName == null || schemaName.length() == 0) {
			throw new HMWConnException("Please set schema name first.", 301);
		}
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HQuery query = HQuery.parse(q);
			int val = broker.deleteByQuery(query);
			return HMWResponseUtil.createSuccessResponse(val, (int) (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}

	@Override
	public HMWResponse reportQuery(HMWReportQuery q, Current __current) throws HMWConnException {
		LOG.info("receive report query request: " + q.schema);
		String schemaName = q.schema;
		HDBBroker broker;
		try {
			broker = HDBFactory.getHDBBroker(schemaName);
			HReportQuery query = HReportQuery.parse(q);
			HDBResponse resp = broker.reportQuery(query);
			
			return HMWResponseUtil.createResponse(resp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HMWConnException(e);
		}
	}

}
