/**
 * 
 */
package org.iipg.hurricane.db.query;

/**
 * @author zhuhy
 *
 */
public class HDBSpanCondition extends HDBCondition{

	private Object lowerValue;
	private Object upperValue;
	
	private boolean includeLower;
	private boolean includeUpper;
	
	public HDBSpanCondition(String name, Object lower, Object upper, boolean includeLower, boolean includeUpper){
		super(name, "");
		this.lowerValue = lower;
		this.upperValue = upper;
		this.includeLower = includeLower;
		this.includeUpper = includeUpper;
	}

	public Object getLowerValue() {
		return lowerValue;
	}

	public Object getUpperValue() {		
		return upperValue;
	}
	
	public boolean isIncludeLower() {
		return this.includeLower;
	}
	
	public boolean isIncludeUpper() {
		return this.includeUpper;
	}
	
	@Override
	public String getConditionType() {
		return HDBSpanCondition.class.getName();
	}

	/**
	 * @author hhn
	 */
	@Override
	public String toString() {
		return null;
	}

	@Override
	public String getSQL92(String fieldType) {		
		if ("date".equalsIgnoreCase(fieldType)) {
			String lowerValue = "";
			String upperValue = "";
			if (isIncludeLower()) {
				lowerValue = getLowerValue() + " 00:00:00";
			} else {
				lowerValue = getLowerValue() + " 23:59:59";				
			}
			if (isIncludeUpper()) {
				upperValue = getUpperValue() + " 23:59:59";
			} else {
				upperValue = getUpperValue() + " 00:00:00";				
			}
			return getFieldName() + " between '" + lowerValue + "' and '" + upperValue + "'";
		} else if ("string".equalsIgnoreCase(fieldType)) {
			return getFieldName() + " between '" + getLowerValue() + "' and '" + getUpperValue() + "'";
		}
		return "";
	}
}
