package org.iipg.hurricane.db.schema;

import java.util.Collection;

public class Field {
	private String name = "";
	private String desc = "";
	private String type = "string";
	private boolean indexed = true;
	private boolean stored = true;
	private String language = "cn";
	private boolean multiValued = false;
	private boolean required = false;
	private String mode = "ro";
	private boolean unique = false;
	private boolean log = false;
	private boolean mask = false;
	private boolean combine = false;
	private boolean dedup = false;
	private boolean signature = false;
	private int length = 100;

	public String getName() { return this.name; }
	public String getDesc() { return this.desc; }
	public String getType() { return this.type; }
	public boolean isIndexed() { return this.indexed; }
	public boolean isStored() { return this.stored; }
	public String getLanguage() { return this.language; }
	public boolean isMultiValued() { return this.multiValued; }
	public boolean isRequired() { return this.required; }
	public String getMode() { return this.mode; }
	public boolean isUnique() {	return this.unique; }
	public boolean isLog() { return this.log; }
	public boolean isMask() { return this.mask; }
	public boolean isDedup() { return this.dedup; }
	public boolean isSignature() { return this.signature; }
	public boolean isCombine() { return this.combine; }
	public int getLength() { return this.length;	}

	public void setName(String name) { this.name = name; }
	public void setDesc(String desc) { this.desc = desc; }
	public void setType(String type) { this.type = type; }
	public void setIndexed(boolean indexed) { this.indexed = indexed; }
	public void setStored(boolean stored) {	this.stored = stored; }
	public void setLanguage(String language) { this.language = language; }
	public void setMultiValued(boolean multiValued) { this.multiValued = multiValued; }
	public void setRequired(boolean required) { this.required = required; }
	public void setMode(String mode) { this.mode = mode; }
	public void setUnique(boolean unique) { this.unique = unique; }
	public void setLog(boolean log) { this.log = log; }
	public void setMask(boolean mask) { this.mask = mask; }
	public void setDedup(boolean dedup) { this.dedup = dedup; }
	public void setSignature(boolean signature) { this.signature = signature; }
	public void setCombine(boolean combine) { this.combine = combine; }
	public void setLength(int length) { this.length = length; }

	// conventional function
	public boolean isBlob() { return "hblob".equalsIgnoreCase(type); }
	public boolean isDate() { return "date".equalsIgnoreCase(type); }
	
	//ISURCMLKDG
	public String getFlag() {
		StringBuffer flag = new StringBuffer();
		flag.append(isIndexed() ? "I" : "-")
			.append(isStored() ? "S" : "-")
			.append(isUnique() ? "U" : "-")
			.append(isRequired() ? "R" : "-")
			.append(isCombine() ? "C" : "-")
			.append(isMultiValued() ? "M" : "-")
			.append(isLog() ? "L" : "-")
			.append(isMask() ? "K" : "-")
			.append(isDedup() ? "D" : "-")
			.append(isSignature() ? "G" : "-");
		return flag.toString();
	}
	
	public void setFlag(String flag) {
		int idx = 0;
		setIndexed(flag.substring(idx++, idx).equals("I"));
		setStored(flag.substring(idx++, idx).equals("S"));
		setUnique(flag.substring(idx++, idx).equals("U"));
		setRequired(flag.substring(idx++, idx).equals("R"));
		setCombine(flag.substring(idx++, idx).equals("C"));
		setMultiValued(flag.substring(idx++, idx).equals("M"));
		setLog(flag.substring(idx++, idx).equals("L"));
		setMask(flag.substring(idx++, idx).equals("K"));
		setDedup(flag.substring(idx++, idx).equals("D"));
		setSignature(flag.substring(idx++, idx).equals("G"));
	}

}
