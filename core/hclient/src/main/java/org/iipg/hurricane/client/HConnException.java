package org.iipg.hurricane.client;

public class HConnException extends Exception {

	private int errorCode = 0;
	
	public HConnException(String msg) {
		super(msg);
	}

	public HConnException(String reason, int errorCode) {
		super(reason);
		this.errorCode = errorCode;
	}
	
	public int getCode() {
		return this.errorCode;
	}

}
