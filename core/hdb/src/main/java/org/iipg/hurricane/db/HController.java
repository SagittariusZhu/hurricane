package org.iipg.hurricane.db;

import java.util.List;
import java.util.Map;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.json.JSONObject;

public abstract class HController {
	
	private HurricaneConfiguration conf = null;
	
	public HController(HurricaneConfiguration conf) {
		this.conf = conf;
	}
	
	public HurricaneConfiguration getConf() {
		return this.conf;
	}
	
	public abstract boolean createSchema(SchemaParser parser);

	public abstract boolean deleteSchema(String schemaName, boolean fullDelete);

	public abstract boolean refreshSchema(SchemaParser parser);

	public abstract String getName();

	public abstract JSONObject getInfo();

	public abstract HDBResultSet listHandlers(String coreName, String type);

	public abstract boolean createHandler(String schemaName, String dihName, String dihType, String content);

	public abstract boolean startHandler(String schemaName, String dihName, Map params);

	public abstract HDBResultSet getHandlerStatus(String schemaName, String dihName);

	public abstract boolean createHandlers(String schemaName, List<HDBDiHandler> handlers);

	public abstract Map<String, ?> getProps();

	public abstract JSONObject getSchemaStatus(String coreName);
}
