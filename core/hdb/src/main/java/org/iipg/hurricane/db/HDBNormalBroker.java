package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.iipg.hurricane.HDeleteException;
import org.iipg.hurricane.HInsertException;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.HUpdateException;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.metadata.*;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HDBUtil;

import org.xml.sax.SAXException;

public class HDBNormalBroker implements HDBBroker {
	public static final Log LOG = LogFactory.getLog(HDBNormalBroker.class);

	protected String schemaName;
	private SchemaParser schema;

	private HAdapter fsAdapter;
	private HAdapter solrAdapter;

	private synchronized void buildAdapters() {
		if (fsAdapter == null) {
			try {
				fsAdapter = HAdapterFactory.createFSAdapter(schemaName);
			} catch (Exception e) {
				fsAdapter = null;
				System.err.println(e.getMessage());
			}
		}
		if (solrAdapter == null) {
			try {
				solrAdapter = HAdapterFactory.createSolrAdapter(schemaName);
			} catch (Exception e) {
				solrAdapter = null;
				System.err.println(e.getMessage());
			}
		}
	}
	
	public HAdapter getSolrAdapter() {
		return this.solrAdapter;
	}
	
	public HAdapter getFSAdapter() {
		return this.fsAdapter;
	}

	public HDBNormalBroker(String schemaName) throws ParserConfigurationException, SAXException, IOException {
		this.schemaName = schemaName;
		schema = new SchemaParser(schemaName);
		buildAdapters();
	}

	/**
	 * Insert document into schema
	 * @param record
	 */
	public int store(HDocument doc) throws HInsertException {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);

		HDBRecord record = doc.getSolrObject();
		HDBBlob blob = doc.getBlobObject();

		if (record != null && solrAdapter != null) {
			try {
				solrAdapter.store(record);
			} catch (Exception e) {
				throw new HInsertException(e.getMessage());
			}
		}
		if (blob != null && fsAdapter != null) {
			try {
				fsAdapter.store(blob);
			} catch (Exception e) {
				throw new HInsertException(e.getMessage());
			}
		}
		
		return 1;
	}

	/**
	 * @param query
	 * @return
	 * @throws HQueryException 
	 */
	public HDBResponse query(HQuery query) throws HQueryException {
		HDBUtil.checkAdapter(solrAdapter);
		
		HDBResultSet rset = null;

		HDBQuery hQuery = query.getSolrQuery();
		
		if (hQuery.getWhereClauses2() != null) {
			rset = solrAdapter.query(hQuery);
		}
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(rset.getTotalCount());
		resp.setDataType("resultset");
		resp.setData(rset.getItems());
		resp.setConsumeTime(rset.getConsumeTime());
		
		return resp;
	}
	
	/**
	 * Download binary data by special condition.
	 * 	uuidField: uuid
	 *  item: bindata-id
	 *  
	 * @param query
	 * @return
	 */
	public HDBResponse getBinary(HDBQuery query) throws HQueryException {	
		HDBUtil.checkAdapter(fsAdapter);
		
		long start = System.currentTimeMillis();
		Object uuid = null;
		String item = null;
		
		List<String> items = new ArrayList<String>();
		for (HDBCondition cond : query.getWhereClauses()) {
			if (cond.getFieldName().equals("uuid")) {
				uuid = cond.getFieldValue();
			} else if (cond.getFieldName().equals("item")) {
				item = (String) cond.getFieldValue();
				items.add(item);
			}
		}

		if (uuid == null) {
			throw new HurricaneException("MUST set uid for getBinary!");
		}

		HDBBlob blob = null;
		try {
			blob = (HDBBlob) fsAdapter.queryByID(uuid, items);
		} catch (Exception e) {
			throw new HurricaneException(e.getMessage());
		}

		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(1);
		resp.setDataType("blob");
		resp.setData(blob);
		resp.setConsumeTime((int) (System.currentTimeMillis() - start));
		
		return resp;
	}

	public int update(HDocument doc) throws HUpdateException {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);
		
		HDBRecord ro = doc.getSolrObject();
		HDBBlob blob = doc.getBlobObject();

		if (ro != null && solrAdapter != null) {
			if (ro.getInformation().size() > 1)
				try {
					solrAdapter.update(ro);
				} catch (Exception e) {
					throw new HUpdateException(e.getMessage());
				}
		}
		
		if (blob != null && fsAdapter != null) {
			if (blob.getBlob().length > 0)
				throw new HUpdateException("Not support to update binary fields.");
		}
		
		return 1;
	}
	
	@Override
	public int updateByQuery(HDocument doc, HQuery query) {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);

		HDBQuery roQuery = query.getSolrQuery();

		HDBResultSet rset = null;
		
		if (roQuery.getWhereClauses2() != null) {
			roQuery.setRowCount(100);
			rset = solrAdapter.query(roQuery);
		}
				
//TODO: loop for extend 100
//		long maxCount = rset.getTotalCount();
//		int start = 100;
		
		List<String> list = getIDListFromReslutSet(rset);
		
		HDBRecord ro = doc.getSolrObject();
		HDBBlob blob = doc.getBlobObject();

		String ukey = schema.getUniqueKey().getName();

		int roUpdateSize = 0;
		if (ro != null && ro.getInformation().size() > 0 && solrAdapter != null) {
			for (String id : list) {
				ro.put(ukey, id);
				try {
					solrAdapter.update(ro);
				} catch (Exception e) {
					throw new HUpdateException(e.getMessage());
				}
				roUpdateSize ++;
			}
		}
		
		if (blob != null && fsAdapter != null) {
			if (blob.getBlob() != null && blob.getBlob().length > 0)
				throw new HUpdateException("Not support to update binary fields.");
		}		
		
		return roUpdateSize;
	}
	
	@Override
	public int delete(HDocument doc) {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);
		
		HDBRecord ro = doc.getSolrObject();
		
		String uniqueFieldName = schema.getUniqueKey().getName();
		int ret = 0;
		try {
			String uuid = (String) ro.get(uniqueFieldName);
			if (uuid == null || uuid.length() == 0)
				return 0;
			if (solrAdapter.deleteByID(uuid) > 0) ret = 1;
			if (fsAdapter.deleteByID(uuid) > 0) ret = 1;
		} catch(Exception e) {
			e.printStackTrace();
			throw new HDeleteException(e.getMessage());
		}
		
		return ret;
	}
	
	@Override
	public int deleteByQuery(HQuery query) {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);
		
		HDBQuery roQuery = query.getSolrQuery();

		int rsetSize = 0;
		HDBResultSet rset = null;
		
		if (roQuery.getWhereClauses2() != null) {
			rset = solrAdapter.query(roQuery);
		}
		
		List<String> list = getIDListFromReslutSet(rset);
		rsetSize = list.size();
		try {
			solrAdapter.deleteByIDs(list);
			fsAdapter.deleteByIDs(list);
		} catch(Exception e) {
			e.printStackTrace();
			throw new HDeleteException(e.getMessage());
		}
		
		return rsetSize;
	}
	
	private List<String> getIDListFromReslutSet(HDBResultSet rset) {
		List<String> list = new ArrayList<String>();
		if (rset.getItems() != null) {
			for (HDBBaseObject obj : rset.getItems()) {
				if (obj instanceof HDBRecord) {
					String id = (String) ((HDBRecord) obj).get(schema.getUniqueKey().getName());
					list.add(id);
				}
			}
		}
		return list;
	}

	@Override
	public void reloadSchema(String schemaName) throws HurricaneException {
		try {
			this.schema = new SchemaParser(schemaName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e.getMessage());
		}
	}

	@Override
	public HDBResponse reportQuery(HReportQuery query) {
		HDBUtil.checkAdapter(solrAdapter);
		
		HDBResultSet rset = null;

		HDBReportQuery roQuery = query.getSolrQuery();
		
		if (roQuery.size() > 0) {
			rset = solrAdapter.reportQuery(roQuery);
		}
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(rset.getTotalCount());
		resp.setDataType("resultset");
		resp.setData(rset.getItems());
		resp.setConsumeTime(rset.getConsumeTime());
		
		return resp;
	}

	@Override
	public HDBResponse listHandlers(String type) throws Exception {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
		
		HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));
		
		HDBResultSet rset = null;
		
		rset = controller.listHandlers(this.schemaName, type);
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(rset.getTotalCount());
		resp.setDataType("resultset");
		resp.setData(rset.getItems());
		resp.setConsumeTime(rset.getConsumeTime());
		
		return resp;
	}

	@Override
	public HDBResponse createHandler(String type, String dihName,
			String dihType, String content) throws Exception {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();

		HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));

		HDBResponse resp = new HDBResponse();

		if (controller.createHandler(this.schemaName, dihName, dihType, content)) {
			resp.setCount(1);
		}

		return resp;
	}

	@Override
	public HDBResponse createHandlers(String string, List<HDBDiHandler> handlers) throws Exception {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();

		HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));

		HDBResponse resp = new HDBResponse();

		if (controller.createHandlers(this.schemaName, handlers)) {
			resp.setCount(handlers.size());
		}

		return resp;
	}
	
	@Override
	public HDBResponse startHandler(String handlerType, String dihName,
			Map params) throws Exception {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();

		HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));

		HDBResponse resp = new HDBResponse();

		if (controller.startHandler(this.schemaName, dihName, params)) {
			resp.setCount(1);
		}

		return resp;
	}

	@Override
	public HDBResponse getHandlerStatus(String handlerType, String dihName) throws Exception {
		HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();

		HController controller = HControllerFactory.getInstance(conf.get("controller.ro"));

		HDBResultSet rset = controller.getHandlerStatus(this.schemaName, dihName);
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(rset.getTotalCount());
		resp.setDataType("resultset");
		resp.setData(rset.getItems());
		resp.setConsumeTime(rset.getConsumeTime());
		
		return resp;
	}

	@Override
	public int subscribe(HQuery query, String tb) throws Exception {
		HDBUtil.checkAdapter(solrAdapter);
		HDBUtil.checkAdapter(fsAdapter);
		
		HDBResultSet rset = null;

		HDBQuery roQuery = query.getSolrQuery();
		
		HDBNormalBroker subBroker = new HDBNormalBroker(tb);
		int numRows = 0;
		if (roQuery.getWhereClauses().size() > 0) {
			rset = solrAdapter.copyToByQuery(subBroker.solrAdapter, roQuery);
			List<String> list = getIDListFromReslutSet(rset);
			fsAdapter.copyToByList(subBroker.fsAdapter, list);
			numRows = list.size();
		}
		
		return numRows;
	}

	@Override
	public void close() {
		if (fsAdapter != null) {
			fsAdapter.close();
			fsAdapter = null;
		}
		if (solrAdapter != null) {
			solrAdapter.close();
			solrAdapter = null;
		}
	}

	@Override
	public boolean optimize() {
		if (solrAdapter != null) {
			solrAdapter.optimize();
			return true;
		}
		return false;
	}
}
