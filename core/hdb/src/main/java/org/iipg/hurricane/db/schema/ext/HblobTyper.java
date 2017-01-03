package org.iipg.hurricane.db.schema.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;

public class HblobTyper extends HTyper {
	
	public HblobTyper(Field ori) {
		super(ori);
	}
	
	@Override
	public Field getDefaultField() {
		return createField(getOriField().getName(), "string", getOriField().getFlag());
	}
	
	public Field[] getFields() {
		List<Field> list = new ArrayList<Field>();
		list.add(createField(getOriField().getName(), "string", getOriField().getFlag()));
		return list.toArray(new Field[0]);
	}

	@Override
	public Object run(Object value) {
		if (getOriField().isMultiValued()) {
			List ret = new ArrayList();
			List list = (List) value;
			if (list == null) return ret;
			for (Object item : list) {
				Map<String, Object> itemMap = new HashMap<String, Object>();
				itemMap.put(getOriField().getName(), item);
				ret.add(itemMap);
			}
			return ret;
		} else {
			Map<String, Object> ret = new HashMap<String, Object>();
			ret.put(getOriField().getName(), value);
			return ret;
		}
	}

	@Override
	public void convert(HDBRecord record) {
		//do nothing
	}

	@Override
	public String getQueryValue(String value) {
		return null; //unsupport query
	}

}
