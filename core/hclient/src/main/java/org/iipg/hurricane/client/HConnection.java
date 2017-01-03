package org.iipg.hurricane.client;

import java.util.Map;

import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.response.StatusResponse;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.iipg.hurricane.client.metadata.*;

public interface HConnection {
	
	// Insert
	public UpdateResponse add(HDocument doc) throws HConnException;
	public UpdateResponse add(HDocumentList docs) throws HConnException;
	
	// Update
	public UpdateResponse update(HDocument doc) throws HConnException;
	public UpdateResponse update(HDocument doc, HQuery q) throws HConnException;
	
	// Delete
	public UpdateResponse delete(HDocument doc) throws HConnException;
	public UpdateResponse delete(HQuery q) throws HConnException;
	
	// Query
	public QueryResponse query(HQuery q) throws HConnException;
	public QueryResponse reportQuery(HReportQuery q) throws HConnException;
	
	// Download API
	public QueryResponse getBinary(String schemaName, Object uuid, String itemID) throws HConnException;
	
	// Big File support
	public void initFtpClient();
	public String upload(String srcFile, String uuid);
	public String download(String uuid, String destFile);
	public StatusResponse getStatus(String taskID);
	
	
	// Misc
	public String getSerialID(boolean includeDate);
	
	// Manage
	public void close();

	public Map getConfiguration();
	
	public SchemaMetadata getSchemaMetadata(String schemaName);

}
