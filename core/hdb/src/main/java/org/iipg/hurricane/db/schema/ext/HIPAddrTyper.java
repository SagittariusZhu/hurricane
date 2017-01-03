package org.iipg.hurricane.db.schema.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;

import com.google.common.net.InetAddresses;

public class HIPAddrTyper  extends HTyper {

	public HIPAddrTyper(Field ori) {
		super(ori);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Field getDefaultField() {
		return createField(getOriField().getName(), "int", getOriField().getFlag());
	}

	@Override
	public Field[] getFields() {
		List<Field> list = new ArrayList<Field>();
		list.add(createField(getOriField().getName(), "int", getOriField().getFlag()));
		return list.toArray(new Field[0]);
	}

	@Override
	public Object run(Object value) {
		if (getOriField().isMultiValued()) {
			List ret = new ArrayList();
			List list = (List) value;
			for (Object item : list) {
				Map<String, Object> itemMap = new HashMap<String, Object>();
				itemMap.put(getOriField().getName(), convertFromIP((String) item));
				ret.add(itemMap);
			}
			return ret;
		} else {
			Map<String, Object> ret = new HashMap<String, Object>();
			ret.put(getOriField().getName(), convertFromIP((String) value));
			return ret;
		}
	}

	@Override
	public void convert(HDBRecord record) {
		String oriName = getOriField().getName();
		if (getOriField().isMultiValued()) {
			List values = (List) record.get(oriName);
			List newValues = new ArrayList();
			for (int i=0; i<values.size(); i++) {
				int value = (Integer) values.get(i);
				newValues.add(convertToIP(value));
			}
			record.put(oriName, newValues);
		} else {
			int value = (Integer) record.get(oriName);
			String newValue = convertToIP(value);
			record.put(oriName, newValue);
		}
	}

	@Override
	public String getQueryValue(String value) {
		return convertFromIP(value) + "";
	}
	
	private int convertFromIP(String ip) {
		int value = InetAddresses.coerceToInteger(InetAddresses.forString(ip));
		return value;
	}
	
	private String convertToIP(int value) {
		String ipAddr = InetAddresses.fromInteger(value).getHostAddress();
		return ipAddr;
	}

}
