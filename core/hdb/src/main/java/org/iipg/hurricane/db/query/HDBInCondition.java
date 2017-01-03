package org.iipg.hurricane.db.query;

import java.util.List;

public class HDBInCondition extends HDBCondition {

	private List items = null;
	
	public HDBInCondition(String fieldName, Object fieldValue) {
		super(fieldName, "");
		items = (List) fieldValue;
	}

	@Override
	public String getConditionType() {
		return HDBInCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			if (items != null && items.size() > 0) {
				StringBuffer buf = new StringBuffer();
				buf.append(getFieldName()).append(" in (");
				for (int i=0; i<items.size(); i++) {
					buf.append("'" + items.get(i) + "'").append(",");
				}
				int pos = buf.lastIndexOf(",");
				if (pos > 0) {
					buf.replace(pos, pos + 1, ")");
				}
				return buf.toString();
			}
		}
		return "";
	}

}
