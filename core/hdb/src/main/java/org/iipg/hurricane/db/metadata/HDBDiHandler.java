package org.iipg.hurricane.db.metadata;

public class HDBDiHandler {

	private String dihName = "";
	private String dihType = "";
	private byte[] dihContent = null;
	
	public HDBDiHandler(String dihName, String dihType) {
		this.dihName = dihName;
		this.dihType = dihType;
	}
	
	public void setDihName(String dihName) { this.dihName = dihName; }
	public void setDihType(String dihType) { this.dihType = dihType; }
	public void setDihContent(byte[] dihContent) { this.dihContent = dihContent; }
	
	public String getDihName() { return this.dihName; }
	public String getDihType() { return this.dihType; }
	public byte[] getDihContent() { return this.dihContent; }
	
}
