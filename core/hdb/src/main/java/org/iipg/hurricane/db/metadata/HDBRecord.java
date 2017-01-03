package org.iipg.hurricane.db.metadata;

import java.util.HashMap;
import java.util.Map;
public class HDBRecord extends HDBBaseObject {
	
	private HashMap<String,Object> information=new HashMap<String,Object>();
	private String type;
	private byte[] buff = null;
	
	public Map<String, Object> getInformation(){
		return information;
	}
	public void add(HDBRecord record) {
		// TODO Auto-generated method stub
		information.putAll(record.getInformation());
	}
	public void put(String key, Object value) {
		// TODO Auto-generated method stub
		information.put(key, value);
	}
	public Object get(String key) {
		// TODO Auto-generated method stub
		return information.get(key);
	}

	public String toString(){
		return information.toString();
	}
	public String getType() {
		// TODO Auto-generated method stub
		return this.type;
	}
	public void setType(String type) {
		// TODO Auto-generated method stub
		 this.type=type;
	}
	
	public void push(String fieldname, Object value){
		information.put(fieldname, value);
	}

	public void pushInt(String fieldname, int value){
		
	}
	public void pushString(String fieldname,String value){
		
	}

	public void pushHblob(String fieldname, HDBBlob value){
		
	}


	
	public int getInt(String fieldname){
		return 0;
		
	}
	String getString(String fieldname){
		return fieldname;
	}
	
	public void setBinary(byte[] buff) {
		this.buff = buff;
	}
	
	public byte[] getBinary() {
		return this.buff;
	}
	
	public boolean containsField(String fieldName) {
		return information.containsKey(fieldName);
	}
	public void remove(String fieldName) {
		information.remove(fieldName);
	}
}
