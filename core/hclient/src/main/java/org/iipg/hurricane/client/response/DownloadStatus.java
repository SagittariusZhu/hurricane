package org.iipg.hurricane.client.response;

public class DownloadStatus {

	private int code = 0;
	
	public DownloadStatus(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public static final DownloadStatus Remote_File_Noexist = new DownloadStatus(100);
	public static final DownloadStatus Local_Bigger_Remote = new DownloadStatus(101);
	public static final DownloadStatus Download_New_Failed = new DownloadStatus(102);
	public static final DownloadStatus Download_From_Break_Failed = new DownloadStatus(103);
	
	public static final DownloadStatus Download_New_Success = new DownloadStatus(200);
	public static final DownloadStatus Download_From_Break_Success = new DownloadStatus(201);

}
