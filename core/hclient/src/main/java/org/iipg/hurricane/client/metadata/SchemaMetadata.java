package org.iipg.hurricane.client.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaMetadata {
	
	private String name = "";
	private String desc = "";
	private Map<String, FieldMetadata> fields = new HashMap<String, FieldMetadata>();

	public String getName() { return this.name; };
	public String getDesc() { return this.desc; };
	
	public SchemaMetadata(Map<String, Object> props) {
		init(props);
	}

	public FieldMetadata getField(String fieldName) {
		return fields.get(fieldName);
	}
	
	private void init(Map<String, Object> props) {
		this.name = (String) props.get("name");
		this.desc = (String) props.get("desc");
		List fieldList = (List) props.get("fields");
		for (Object item : fieldList) {
			FieldMetadata fmd = new FieldMetadata((String) item);
			fields.put(fmd.getName(), fmd);			
		}
	}
	public Collection<FieldMetadata> getFields() {
		return fields.values();
	}

}
