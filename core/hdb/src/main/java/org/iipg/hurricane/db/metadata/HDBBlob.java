package org.iipg.hurricane.db.metadata;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.iipg.hurricane.util.MD5Util;

public class HDBBlob extends HDBBaseObject {
	private Object uuid = null;
	private byte[] blob = null;
	private Map<String, Map<String, byte[]>> extBlob = new HashMap<String, Map<String, byte[]>>();
	
	public HDBBlob(Object uuid){
		this.uuid = uuid;
	}

	public HDBBlob() {
		// TODO Auto-generated constructor stub
	}
	
	public void setUuid(Object uuid) {
		this.uuid = uuid;
	}
	
	public Object getUuid() { return this.uuid; }
	
	public void setBlob(byte[] blob){
		this.blob = blob;
		if (this.uuid == null) {
			this.uuid = UUID.randomUUID().toString();
		}
	}
	public byte[] getBlob(){
		return this.blob;
	}
	
	public OutputStream openStream(String key){
		return null;
		
	}
	public InputStream getStream (String key){
		return null;
		
	}

	public void addExt(String key, Map<String, byte[]> binary) {
		extBlob.put(key, binary);		
	}
	
	public Iterator<String> getExtIterator() {
		return extBlob.keySet().iterator();
	}

	public Map<String, byte[]> getExtBlob(String key) {
		return extBlob.get(key);
	}
}
