package org.iipg.hurricane.db.schema;

public class Relation {
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	private String name;
	private String type;
	private String from;
	private String to;
	private String field;

}
