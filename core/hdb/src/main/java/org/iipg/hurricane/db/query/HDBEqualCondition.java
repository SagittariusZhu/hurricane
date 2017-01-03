package org.iipg.hurricane.db.query;

public class HDBEqualCondition extends HDBCondition {
	
	public HDBEqualCondition(String fieldName, Object uuid) {
		super(fieldName, uuid);
	}

	@Override
	public String getConditionType() {
		return HDBEqualCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "='" + getFieldValue() + "'";
		} else if ("date".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "='" + getFieldValue() + "'";
		}

		return getFieldName() + "=" + getFieldValue();
	}

}
