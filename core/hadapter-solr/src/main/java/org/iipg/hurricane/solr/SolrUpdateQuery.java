package org.iipg.hurricane.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;

public class SolrUpdateQuery extends HDBUpdateQuery {

	public SolrUpdateQuery(SchemaParser schemaParser, HDBBaseObject object) {
		super(schemaParser, object);
	}

	@Override
	public String getUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Map<String, String> getUpdateOper(String fieldName) {
		Object value = updateFields.get(fieldName);
		Field field = schema.getField(fieldName);
		Map<String, String> setOper = new HashMap<String, String>();
		if (field.isMultiValued()) {
			if (value instanceof List) {
				List list = (List) value;
				for (int i=0; i<list.size(); i++) {
					setOper.put("add", "" + list.get(i));					
				}
			} else {
				setOper.put("add", "" + value);
			}
		} else
			setOper.put("set", "" + value);
		return setOper;
	}

}
