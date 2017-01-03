/**
 * 
 */
package org.iipg.hurricane.db.query;

import java.util.ArrayList;

/**
 * @author lixiaojing
 *
 */
public class HDBMultiCondition extends HDBCondition{
	
	public HDBMultiCondition(String fieldName, String fieldValue) {
		super(fieldName, fieldValue);
	}

	public static class TooManyClauses extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public TooManyClauses() {
			super("maxClauseCount is set to " + maxClauseCount);
		}
	}

	private static int maxClauseCount = 1024;

	private ArrayList<HDBMultiClause> conditions = new ArrayList<HDBMultiClause>();	
	
	public void add(HDBCondition condition, HDBMultiClause.LogicRelation relation){
		add(new HDBMultiClause(condition,relation));
	}
	
	/* @author hhn 
	*  @date 2013-09-21
	*  
	*/
	public void addAndClause(HDBCondition condition){
		add(new HDBMultiClause(condition,HDBMultiClause.LogicRelation.AND));
	}
	public void addOrClause(HDBCondition condition){
		add(new HDBMultiClause(condition,HDBMultiClause.LogicRelation.OR));
	}
	public void addNotClause(HDBCondition condition){
		add(new HDBMultiClause(condition,HDBMultiClause.LogicRelation.NOT));
	}

	public void add(HDBMultiClause clause){
	    if (conditions.size() >= maxClauseCount)
	        throw new TooManyClauses();

		conditions.add(clause);
	}
	
	public HDBMultiClause.LogicRelation getHConditionRelation(int index){
		return conditions.get(index).getRelation();
	}
	
	public HDBCondition getHCondition(int index){
		return conditions.get(index).getCondition();
	}
	
	public int getClauseCount(){
		return conditions.size();
	}

	@Override
	public String getConditionType() {
		return HDBMultiCondition.class.getName();
	}

	/* @author hhn 
	*  @date 2013-09-21
	*  
	*/
	@Override
	public String toString() {
		String sql = null;
		for(int index=0; index<this.getClauseCount(); index++){
			sql = sql + this.getHConditionRelation(index) + "(" +
					(this.getHCondition(index)).toString() + ") ";
		}
		return sql;
		
	}

	@Override
	public String getSQL92(String fieldType) {
		// TODO Auto-generated method stub
		return null;
	}
}
