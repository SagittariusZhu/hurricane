package org.iipg.hurricane.db;

import java.util.List;
import java.util.Map;

import org.iipg.hurricane.HInsertException;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.db.metadata.*;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;

public interface HDBBroker {

/** ICE Interface **/
	
	public HDBResponse query(HQuery query) throws HQueryException;

	public HDBResponse reportQuery(HReportQuery query);

	public int store(HDocument doc) throws HInsertException;

	public HDBResponse getBinary(HDBQuery query);

	public int updateByQuery(HDocument doc, HQuery query);

	public int update(HDocument doc);

	public int deleteByQuery(HQuery query);

	public int delete(HDocument doc);

/** HTTP Interface **/
	
	public void reloadSchema(String schemaName) throws HurricaneException;

	public HDBResponse listHandlers(String type) throws Exception;

	public HDBResponse createHandler(String type, String dihName, String dihType, String content) throws Exception;

	public HDBResponse startHandler(String handlerType, String dihName,	Map params) throws Exception;

	public HDBResponse getHandlerStatus(String string, String dihName) throws Exception;

	public HDBResponse createHandlers(String string, List<HDBDiHandler> handlers) throws Exception;


	/**
	 * Added in 2014.12.22
	 * @param query
	 * @param tb : subscribe table name
	 * @return 
	 * @throws Exception 
	 */
	public int subscribe(HQuery query, String tb) throws Exception;

	public void close();

	public boolean optimize();

}
