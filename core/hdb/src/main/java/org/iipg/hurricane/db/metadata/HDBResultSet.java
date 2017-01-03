package org.iipg.hurricane.db.metadata;

import java.util.ArrayList;
import java.util.List;

public class HDBResultSet{
	//搜索总耗时
	private int consumeTime;
	// 总记录数
	private long totalCount;
	//当前返回记录数
	private int currentCount;

	// 返回结果集
	private List<HDBBaseObject> items = new ArrayList<HDBBaseObject>();

	public int getConsumeTime() {
		return consumeTime;
	}

	public void setConsumeTime(int consumeTime) {
		this.consumeTime = consumeTime;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long l) {
		this.totalCount = l;
	}

	public int getCount() {
		return currentCount;
	}

	public void setCount(int currentPageSize) {
		this.currentCount = currentPageSize;
	}

	public List<HDBBaseObject> getItems() {
		return items;
	}

	public void setItems(List<HDBBaseObject> items) {
		this.items = items;
	}
	
	public void addItem(HDBBaseObject item){
		this.items.add(item);
	}
	
	public HDBBaseObject getItem(int index){
		return this.items.get(index);
	}
}
