package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.metadata.HDBDiHandler;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

public class HDBSysBroker implements HDBBroker {

	private static HurricaneConfiguration conf = HurricaneConfigurationFactory.getInstance();
	
	private static String SYS_CONFIGURATION = "__sys_configuration";
	private static String SYS_SCHEMA = "__sys_schema";
	
	private String schemaName = "";
	
	public HDBSysBroker(String schemaName) {
		this.schemaName = schemaName;
	}

	@Override
	public HDBResponse query(HQuery hQuery) throws HQueryException {
		if (SYS_CONFIGURATION.equals(schemaName)) {
			return getSysConf();
		} else if (SYS_SCHEMA.equals(schemaName)) {
			try {
				return getSchemaMetadata(hQuery.getSolrQuery());
			} catch (Exception e) {
				e.printStackTrace();
				throw new HQueryException(e.getMessage());
			}
		}
		return new HDBResponse();
	}
	
	private HDBResponse getSysConf() {
		List<HDBRecord> list = new ArrayList<HDBRecord>();
		HDBRecord doc = new HDBRecord();
		Map props = conf.getProps();
		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
			String key = (String) it.next();
			Object value = props.get(key);
			doc.put(key, value);
		}
		list.add(doc);
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(1);
		resp.setDataType("resultset");
		resp.setData(list);
		resp.setConsumeTime(0);
		
		return resp;
	}
	
	private HDBResponse getSchemaMetadata(HDBQuery hQuery) throws ParserConfigurationException, SAXException, IOException {
		String schemaName = "";
		for (HDBCondition cond : hQuery.getWhereClauses()) {
			if ("schemaName".equals(cond.getFieldName())) {
				schemaName = (String) cond.getFieldValue();
				break;
			}
		}
		SchemaParser parser = new SchemaParser(schemaName);
		
		List<HDBRecord> list = new ArrayList<HDBRecord>();
		HDBRecord doc = new HDBRecord();

		doc.put("name", parser.getName());
		doc.put("desc", parser.getDesc());
		for (Field item : parser.getFields()) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("name", item.getName());
				obj.put("type", item.getType());
				obj.put("mode", item.getMode());
				obj.put("flag", item.getFlag());
				doc.put("fields", obj.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		list.add(doc);
		
		HDBResponse resp = new HDBResponse();
		resp.setTotalCount(1);
		resp.setDataType("resultset");
		resp.setData(list);
		resp.setConsumeTime(0);
		
		return resp;
	}

	@Override
	public int store(HDocument record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HDBResponse getBinary(HDBQuery hQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(HDocument record) {
		return 0;		
	}
	

	@Override
	public int updateByQuery(HDocument record, HQuery hQuery) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteByQuery(HQuery hQuery) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(HDocument record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reloadSchema(String schemaName) throws HurricaneException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HDBResponse reportQuery(HReportQuery hQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResponse listHandlers(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResponse createHandler(String type, String dihName,
			String dihType, String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResponse startHandler(String handlerType, String dihName,
			Map params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResponse getHandlerStatus(String string, String dihName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResponse createHandlers(String string, List<HDBDiHandler> handlers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int subscribe(HQuery query, String tb) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean optimize() {
		// TODO Auto-generated method stub
		return false;
	}


}
