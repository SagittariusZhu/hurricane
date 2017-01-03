package org.iipg.hurricane.mysql;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlController extends HController {
	
	private static Logger LOG = LoggerFactory.getLogger(MysqlController.class);

	public MysqlController(HurricaneConfiguration conf) {
		super(conf);
	}

	@Override
	public boolean createSchema(SchemaParser parser) {
		//create schema.xml
		MysqlSchema schema;
		try {
			schema = new MysqlSchema(getConf());
			List<Field> list = parser.getFields();
			for (Field item : list) {
				if ("rw".equalsIgnoreCase(item.getMode()) || item.isUnique()) {
					schema.addField(item);
				}
			}
			schema.setName(parser.getName());
			schema.persist();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}

	@Override
	public boolean deleteSchema(String schemaName, boolean fullDelete) {
		MysqlSchema schema;
		try {
			schema = new MysqlSchema(getConf());
			schema.dropSchema(schemaName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e);
		}
	}

	@Override
	public boolean refreshSchema(SchemaParser parser) {
		return true;
	}

	@Override
	public String getName() {
		return "mysql";
	}

	@Override
	public JSONObject getInfo() {
		JSONObject info = new JSONObject();
		return info;
	}

	@Override
	public HDBResultSet listHandlers(String coreName, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createHandler(String schemaName, String dihName,
			String dihType, String content) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startHandler(String schemaName, String dihName, Map params) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HDBResultSet getHandlerStatus(String schemaName, String dihName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createHandlers(String schemaName, List<HDBDiHandler> handlers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, ?> getProps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getSchemaStatus(String coreName) {
		// TODO Auto-generated method stub
		return null;
	}

}
