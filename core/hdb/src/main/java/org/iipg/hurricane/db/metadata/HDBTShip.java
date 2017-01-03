package org.iipg.hurricane.db.metadata;

public class HDBTShip extends HDBBaseObject {
	private String type;
	private String content_entity;

	public String getContent_entity() {
		return content_entity;
	}

	public void setContent_entity(String content_entity) {
		this.content_entity = content_entity;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
