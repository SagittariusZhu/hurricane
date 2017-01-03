package org.iipg.hurricane.solr;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.iipg.hurricane.HInsertException;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBEqualCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.db.util.HDBUtil;
import org.iipg.hurricane.solr.util.SolrUtil;
import org.xml.sax.SAXException;


public class SolrAdapter extends HAdapter {
	public static final Log LOG = LogFactory.getLog(SolrAdapter.class);

	public SolrAdapter(HurricaneConfiguration conf, String dbName) throws ParserConfigurationException, SAXException, IOException {
		super(conf, dbName);
	}

	@Override
	public HDBResultSet query(HDBQuery con) throws HQueryException {
		LOG.debug("select from " + getDbName() + " where ( " + con.getWhereClauses2() + " )");

		Field uuidField = this.getSchemaParser().getUniqueKey();

		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());

		HDBResultSet rset = null;

		try {
			rset = solrClient.query(con, uuidField);

			for (int i=0; i<rset.getCount(); i++) {

				boolean needFS = false;
				HDBRecord record = (HDBRecord) rset.getItem(i);
				for (String fieldName : con.getSelectFields()) {
					if (!record.containsField(fieldName)) continue;
					Object value = record.get(fieldName);
					if (value == null) continue;

					Field field = getSchemaParser().getField(fieldName);

					if (field == null) {
						LOG.warn("Field " + fieldName + " not found in schema, ignore it.");
						continue;
					}

					if (HTyper.isExtType(field.getType())) {
						try {
							HTyper typer = HTyper.getTyper(field);
							typer.convert(record);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}  

					//				if (field.isDate()) {
					//					Object newValue = null;
					//					if (field.isMultiValued()) {
					//						newValue = new ArrayList();
					//						for (Date item : (List<Date>) value) {
					//							Date newItem = SolrUtil.correctDate(item, false);
					//							((List)newValue).add(newItem);
					//						}						
					//					} else {
					//						newValue = SolrUtil.correctDate((Date) value, false);
					//					}
					//					record.put(fieldName, newValue);
					//				} else if (field.isBlob()) {
					//					//needFS = true;
					//				}
				}

				if (needFS) {
					//				String uuid = (String) record.get(uuidField.getName());
					//				HAdapter fsAdapter = HurricaneQuery.getFSAdapter();
					//				try {
					//					HDBBlob blob = (HDBBlob) fsAdapter.queryByID(uuid, null);
					//					record.setBinary(blob.getBlob());
					//				} catch (Exception e) {
					//					// TODO Auto-generated catch block
					//					e.printStackTrace();
					//				}
				}
			}
		} finally {
			if (solrClient != null)
				solrClient.close();
		}

		return rset;
	}

	@Override
	public HDBBaseObject queryByID(Object uuid, List<String> fields) throws Exception {
		Field uuidField = this.getSchemaParser().getUniqueKey();

		HDBQuery q = new SolrQuery();
		q.setSchema(getSchemaParser());
		q.setSchemaName(getDbName());

		for (String field : fields) {
			q.addSelectField(field);
		}
		q.addWhereClause(new HDBEqualCondition(uuidField.getName(), uuid));
		q.setWhereClauses2(null);
		HDBResultSet rset = query(q);
		if (rset != null && rset.getCount() > 0)
			return rset.getItem(0);
		return null;
	}

	@Override
	public int store(HDBBaseObject object) throws Exception {
		LOG.debug("store into " + getDbName() + " value ( " + object + " )");

		if (!(object instanceof HDBRecord)) {
			String msg = "store invalid type object!";
			LOG.warn("Solr Insert: " + msg);
			throw new HInsertException(msg);
		}

		if (existRecord((HDBRecord) object)) {
			String msg = "Duplication record with same unique id";
			LOG.warn("Solr Insert: " + msg);
			throw new HInsertException(msg);
		}

		ExDocumentBuilder builder = new ExDocumentBuilder(getSchemaParser());
		builder.parse((HDBRecord)object);
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		try {
			solrClient.add(builder.getDocument());
		} finally {
			if (solrClient != null)
				solrClient.close();
		}
		return 1;
	}

	@Override
	public int store(List<HDBBaseObject> list) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteByID(String uuid) throws Exception {
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		try {
			solrClient.deleteById(uuid);
		} finally {
			if (solrClient != null)
				solrClient.close();
		}
		return 1;
	}

	@Override
	public int deleteByIDs(List<String> list) throws Exception {
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		try {
			solrClient.deleteByIds(list);
		} finally {
			if (solrClient != null)
				solrClient.close();
		}
		return list.size();
	}

	@Override
	public int delete(HDBQuery con) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAutoCommit() {
		boolean autoCommit = false;
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		if (solrClient != null) {
			autoCommit = solrClient.isAutoCommit();
			solrClient.close();
		}

		return autoCommit;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) {
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		if (solrClient != null) {
			solrClient.setAutoCommit(autoCommit);
			solrClient.close();
		}
	}
	
	@Override
	public void optimize() {
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		if (solrClient != null) {
			try {
				solrClient.optimize();
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			solrClient.close();
		}
	}

	@Override
	public void close() {
	}

	@Override
	public HDBQuery createQuery() {
		return new SolrQuery();
	}

	@Override
	public void fillResultSet(HDBResultSet rset, List<String> fields) {
		if (rset == null || rset.getCount() == 0) return;
		Field uuidField = this.getSchemaParser().getUniqueKey();

		for (int i=0; i<rset.getCount(); i++) {
			HDBRecord record = (HDBRecord) rset.getItem(i);
			String uuid = (String) record.get(uuidField.getName());
			try {
				HDBRecord anotherRecord = (HDBRecord) queryByID(uuid, fields);
				if (anotherRecord == null) {
					LOG.warn("No record for " + uuid + " in solr DB.");
					continue;
				}
				for (String field : fields) {
					record.put(field, anotherRecord.get(field));
				}
				record.setBinary(anotherRecord.getBinary());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public HDBUpdateQuery createUpdateQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(HDBBaseObject object) throws Exception {
		LOG.debug("update " + getDbName() + " value ( " + object + " )");

		SolrUpdateQuery q = new SolrUpdateQuery(getSchemaParser(), object);

		HDBRecord record = SolrUtil.buildUpdateRecord(q);
		int ret = store(record);
		return ret;
	}

	@Override
	public int update(HDBUpdateQuery q) throws Exception {
		return 0;
	}

	@Override
	public HDBReportQuery createReportQuery() {
		return new SolrReportQuery();
	}

	@Override
	public HDBResultSet reportQuery(HDBReportQuery roQuery)
			throws HQueryException {
		LOG.debug("report from " + getDbName() + " group by " + roQuery.getGroupField());
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		HDBResultSet rset = null;
		try {
			rset = solrClient.facetQuery(roQuery);
		} finally {
			if (solrClient != null)
				solrClient.close();
		}

		return rset;
	}

	private boolean existRecord(HDBRecord record) {
		Field uuidField = this.getSchemaParser().getUniqueKey();
		if (!record.containsField(uuidField.getName())) {
			String msg = "Not set unique field " + uuidField.getName();
			LOG.warn("Solr Insert: " + msg);
			throw new HInsertException(msg);
		}
		Object uuid = record.get(uuidField.getName());

		HDBQuery q = new SolrQuery();
		q.setSchema(getSchemaParser());
		q.setSchemaName(getDbName());

		q.addSelectField(uuidField.getName());

		q.addWhereClause(new HDBEqualCondition(uuidField.getName(), uuid));
		q.setWhereClauses2(null);
		HDBResultSet rset = query(q);
		if (rset != null && rset.getCount() > 0)
			return true;
		return false;
	}

	@Override
	public HDBResultSet copyToByQuery(HAdapter adapter, HDBQuery hQuery) throws Exception {
		LOG.debug("select from " + getDbName() + " where ( " + hQuery.getWhereClauses2() + " )");
		long start = System.currentTimeMillis();

		HDBResultSet rset = new HDBResultSet();
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		SolrDocumentList docs = solrClient.queryRawCollection(hQuery);
		solrClient.close();

		if (docs == null) {
			rset.setConsumeTime((int) (System.currentTimeMillis() - start));
			rset.setTotalCount(0);
			rset.setCount(0);
			return rset;
		}

		List<SolrInputDocument> addDocs = new ArrayList<SolrInputDocument>();
		Field uuidField = this.getSchemaParser().getUniqueKey();
		for (int i=0; i<docs.size(); i++) {
			SolrInputDocument doc = ClientUtils.toSolrInputDocument(docs.get(i));
			if (doc.containsKey("_version_"))
				doc.removeField("_version_");
			addDocs.add(doc);
			Object value = doc.getFieldValue(uuidField.getName());
			HDBRecord record = new HDBRecord();
			record.put(uuidField.getName(), value);
			rset.addItem(record);
		}
		((SolrAdapter) adapter).add(addDocs);

		rset.setConsumeTime((int) (System.currentTimeMillis() - start));
		rset.setTotalCount(docs.getNumFound());
		rset.setCount(docs.size());
		return rset;
	}

	private void add(List<SolrInputDocument> docs) {
		HSolrClient solrClient = new HSolrClient(getConf(), getDbName());
		try {
			solrClient.add(docs);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			solrClient.close();
		}
	}

	@Override
	public int copyToByList(HAdapter adapter, List<String> list) throws Exception {
		Field uuidField = this.getSchemaParser().getUniqueKey();
		int count = 0;
		for (String id : list) {
			HDBQuery hQuery = createQuery();
			hQuery.setSchemaName(getDbName());
			hQuery.setSchema(getSchemaParser());
			//hQuery.addSelectField("");
			Map<String, String> w1 = new HashMap<String, String>();
			w1.put("field", uuidField.getName());
			w1.put("value", id);
			w1.put("op", "eq");
			HDBCondition condition = HDBUtil.buildCondition(w1);
			hQuery.addWhereClause(condition);
			copyToByQuery(adapter, hQuery);
			count ++;
		}

		return count;
	}
}
