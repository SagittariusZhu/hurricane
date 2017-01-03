/**
 * 
 */
package org.iipg.hurricane.db.query;

/**
 * @author lixiaojing
 *
 */
public class HDBMultiClause {
	public static enum LogicRelation {
		AND		{ @Override public String toString() { return "+"; } },
		OR		{ @Override public String toString() { return " "; } },
		NOT 	{ @Override public String toString() { return "-"; } };
	}
	
	public static enum ConditionType{
		TERM,
		SPAN,
		MULTI;
	}
	
	public static enum FieldType{
		INT,
		DATE;
	}
	
	private HDBCondition condition;
	private LogicRelation relation;
	
	public HDBMultiClause(HDBCondition condition, LogicRelation relation){
		this.condition = condition;
		this.relation = relation;
	}

	public HDBCondition getCondition() {
		return condition;
	}

	public void setCondition(HDBCondition condition) {
		this.condition = condition;
	}

	public LogicRelation getRelation() {
		return relation;
	}

	public void setRelation(LogicRelation relation) {
		this.relation = relation;
	}

}
