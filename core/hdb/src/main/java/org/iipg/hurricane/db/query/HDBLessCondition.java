package org.iipg.hurricane.db.query;

public class HDBLessCondition extends HDBCondition {

	public HDBLessCondition(String fieldName, Object fieldValue) {
		super(fieldName, fieldValue);
	}

	@Override
	public String getConditionType() {
		return HDBLessCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "<'" + getFieldValue() + "'";
		} else if ("date".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "<'" + getFieldValue() + "'";
		}

		return getFieldName() + "<" + getFieldValue();
	}

}
