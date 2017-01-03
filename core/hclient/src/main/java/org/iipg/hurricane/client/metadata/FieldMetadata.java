package org.iipg.hurricane.client.metadata;

import java.util.Map;

import org.iipg.hurricane.client.util.JSONUtil;

public class FieldMetadata {
	
	private static int INDEXED_POS = 0;
	private static int STORED_POS = 1;
	private static int UNIQUE_POS = 2;
	private static int REQUIRED_POS = 3;
	private static int COMBINE_POS = 4;
	private static int MULTI_POS = 5;
	private static int LOG_POS = 6;
	private static int MASK_POS = 7;
	private static int DEDUP_POS = 8;
	private static int SIGNATURE_POS = 9;
	
	private String name = "";
	private String type = "";
	private String flag = "";
	private String mode = "";
	
	public String getName() { return this.name; }
	public String getType() { return this.type; }
	public String getMode() { return this.mode; }
	public String getFlag() { return this.flag;	}
	
	public FieldMetadata(String propStr) {
		Map props = JSONUtil.toMap(propStr);
		this.name = (String) props.get("name");
		this.type = (String) props.get("type");
		this.flag = (String) props.get("flag");
		this.mode = (String) props.get("mode");
	}

	public boolean isIndexed() {
		return !("-".equals(this.flag.substring(INDEXED_POS, INDEXED_POS + 1)));
	}
	public boolean isStored() {
		return !("-".equals(this.flag.substring(STORED_POS, STORED_POS + 1)));
	}
	public boolean isUnique() {
		return !("-".equals(this.flag.substring(UNIQUE_POS, UNIQUE_POS + 1)));
	}
	public boolean isRequired() {
		return !("-".equals(this.flag.substring(REQUIRED_POS, REQUIRED_POS + 1)));
	}
	public boolean isCombine() {
		return !("-".equals(this.flag.substring(COMBINE_POS, COMBINE_POS + 1)));
	}
	public boolean isMultiValued() {
		return !("-".equals(this.flag.substring(MULTI_POS, MULTI_POS + 1)));
	}
	public boolean isLog() {
		return !("-".equals(this.flag.substring(LOG_POS, LOG_POS + 1)));
	}
	public boolean isMask() {
		return !("-".equals(this.flag.substring(MASK_POS, MASK_POS + 1)));
	}
	public boolean isDedup() {
		return !("-".equals(this.flag.substring(DEDUP_POS, DEDUP_POS + 1)));
	}
	public boolean isSignature() {
		return !("-".equals(this.flag.substring(SIGNATURE_POS, SIGNATURE_POS + 1)));
	}
	
	public boolean isBlob() {
		return "hblob".equals(this.type);
	}

}
