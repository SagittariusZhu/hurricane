package org.iipg.hurricane.client.response;

public class StatusResponse {

	private long process = 0;
	
	public StatusResponse(long process) {
		this.process = process;
	}
	
	public long getProcess() {
		return this.process;
	}
	
	public static final StatusResponse WAITING = new StatusResponse(0);
	public static final StatusResponse FINISHED = new StatusResponse(-1);
}
