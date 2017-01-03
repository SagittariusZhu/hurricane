package org.iipg.hurricane.client.response;

public class UploadStatus {
	
	private int code = 0;
	
	public UploadStatus(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public static final UploadStatus Create_Directory_Fail = new UploadStatus(100);
	public static final UploadStatus File_Exits = new UploadStatus(101);
	public static final UploadStatus Remote_Bigger_Local = new UploadStatus(102);
	public static final UploadStatus Upload_From_Break_Failed = new UploadStatus(103);
	public static final UploadStatus Delete_Remote_Faild = new UploadStatus(104);
	public static final UploadStatus Upload_New_File_Failed = new UploadStatus(105);
	
	public static final UploadStatus Create_Directory_Success = new UploadStatus(200);
	public static final UploadStatus Upload_From_Break_Success = new UploadStatus(201);
	public static final UploadStatus Upload_New_File_Success = new UploadStatus(202);

}
