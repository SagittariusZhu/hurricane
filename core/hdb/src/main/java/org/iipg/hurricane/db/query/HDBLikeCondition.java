package org.iipg.hurricane.db.query;

public class HDBLikeCondition extends HDBCondition {

	public HDBLikeCondition(String fieldName, Object fieldValue) {
		super(fieldName, fieldValue);
	}

	@Override
	public String getConditionType() {
		return HDBLikeCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + " like '" + getFieldValue() + "'";
		}
		
		return "";
	}

}
