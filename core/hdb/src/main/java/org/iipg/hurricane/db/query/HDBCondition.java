/**
 * 
 */
package org.iipg.hurricane.db.query;

/**
 * @author Mr.Zhu
 *
 */
public abstract class HDBCondition {
	
	private String fieldName;
	private Object fieldValue;
	
	public String getFieldName() {
		return this.fieldName;
	}

	public Object getFieldValue() {
		return this.fieldValue;
	}
	
	public HDBCondition(String fieldName, Object fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}
	
	public abstract String getConditionType();
	public abstract String toString();
	public abstract String getSQL92(String fieldType);
}
