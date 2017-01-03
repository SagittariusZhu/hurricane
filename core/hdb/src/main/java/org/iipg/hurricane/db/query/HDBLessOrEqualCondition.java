package org.iipg.hurricane.db.query;

public class HDBLessOrEqualCondition extends HDBCondition {

	public HDBLessOrEqualCondition(String fieldName, Object fieldValue) {
		super(fieldName, fieldValue);
	}

	@Override
	public String getConditionType() {
		return HDBLessOrEqualCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "<='" + getFieldValue() + "'";
		} else if ("date".equalsIgnoreCase(fieldType)) {
			return getFieldName() + "<='" + getFieldValue() + "'";
		}

		return getFieldName() + "<=" + getFieldValue();
	}

}
