package org.iipg.hurricane.service;

public class ResponseStatus {
	
	public int status = 0;
	public String message = "";
	
	public ResponseStatus(int status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public static final ResponseStatus RS_OK = new ResponseStatus(200, "");
	public static final ResponseStatus RS_NOT_IMPLEMENTED = new ResponseStatus(301, "Not implement");
	public static final ResponseStatus RS_INTERNAL_ERROR = new ResponseStatus(500, "Server internal error");

}
