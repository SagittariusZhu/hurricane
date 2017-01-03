package org.iipg.hurricane.db.query;

import org.iipg.hurricane.db.query.HDBMultiClause.ConditionType;

public class HDBGreatCondition extends HDBCondition {

	public HDBGreatCondition(String fieldName, Object fieldValue) {
		super(fieldName, fieldValue);
	}

	@Override
	public String getConditionType() {
		return HDBGreatCondition.class.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {
		if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + ">'" + getFieldValue() + "'";
		} else if ("date".equalsIgnoreCase(fieldType)) {
			return getFieldName() + ">'" + getFieldValue() + "'";
		}

		return getFieldName() + ">" + getFieldValue();
	}

}
