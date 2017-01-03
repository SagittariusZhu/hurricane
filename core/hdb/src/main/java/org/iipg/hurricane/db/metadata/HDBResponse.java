package org.iipg.hurricane.db.metadata;

public class HDBResponse {
	//搜索总耗时
	private int consumeTime;
	// 总记录数
	private long totalCount;
	//当前返回记录数
	private int currentCount;

	private String dataType = "";
	
	// 返回结果
	private Object data = null;

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

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public String getDataType() {
		return this.dataType;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	public Object getData() {
		return this.data;
	}
}
