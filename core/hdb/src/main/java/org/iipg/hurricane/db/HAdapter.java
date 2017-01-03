package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.xml.sax.SAXException;

public abstract class HAdapter {
	
	private HurricaneConfiguration conf = null;
	private SchemaParser schema = null;
	private String dbName = "";
	
	public HAdapter(HurricaneConfiguration conf, String dbName) throws ParserConfigurationException, SAXException, IOException {
		this.conf = conf;
		this.dbName = dbName;
		this.schema = new SchemaParser(dbName);
	}
	
	public HurricaneConfiguration getConf() {
		return this.conf;
	}
	
	public SchemaParser getSchemaParser() {
		return this.schema;
	}
	
	public String getDbName() {
		return this.dbName;
	}
	
	// build Query instance
	public abstract HDBQuery createQuery();
	public abstract HDBUpdateQuery createUpdateQuery();
	public abstract HDBReportQuery createReportQuery();

	// query
	public abstract HDBResultSet query(HDBQuery q) throws HQueryException;	
	public abstract HDBBaseObject queryByID(Object uuid, List<String> fields) throws Exception;
	
	public abstract HDBResultSet reportQuery(HDBReportQuery roQuery) throws HQueryException;

	public abstract void fillResultSet(HDBResultSet rset, List<String> fields);

	// insert
	public abstract int store(HDBBaseObject object) throws Exception;
	
	public abstract int store(List<HDBBaseObject> list) throws Exception;

	// update
	public abstract int update(HDBBaseObject object) throws Exception;
	
	public abstract int update(HDBUpdateQuery q) throws Exception;

	// delete
	public abstract int deleteByID(String uuid) throws Exception;
	
	public abstract int deleteByIDs(List<String> list) throws Exception;
	
	public abstract int delete(HDBQuery q) throws Exception;
	
	// manage
	public abstract boolean isAutoCommit();
	
	public abstract void setAutoCommit(boolean autoCommit);
	
	public abstract void optimize();
	
	public abstract void close();

	// replication
	public abstract HDBResultSet copyToByQuery(HAdapter adapter, HDBQuery hQuery) throws Exception;

	public abstract int copyToByList(HAdapter adapter, List<String> list) throws Exception;

}
