package org.iipg.hurricane.client.util;

import java.util.Map;

import org.iipg.hurricane.client.metadata.HCriteria;
import org.iipg.hurricane.client.metadata.HDocument;
import org.iipg.hurricane.client.metadata.HDocumentList;
import org.iipg.hurricane.client.metadata.HQuery;
import org.iipg.hurricane.client.metadata.HReportQuery;
import org.iipg.hurricane.client.response.QueryResponse;
import org.iipg.hurricane.client.response.UpdateResponse;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.model.HMWReportQuery;
import org.iipg.hurricane.model.HMWResponse;

public class HMWUtil {

	public static HDocument toHDocument(HMWDocument doc) {
		HDocument hDoc = new HDocument();
		hDoc.setSchema(doc.schema);
		hDoc.setFormatProps(doc.dict);
		hDoc.setBinary(doc.blob);

		return hDoc;
	}

	public static HMWDocument toHMWDocument(HDocument hDoc) {
		HMWDocument doc = new HMWDocument();
		doc.blob = hDoc.getBinary();
		doc.schema = hDoc.getSchema();
		doc.dict = hDoc.getFormatProps();

		return doc;
	}

	public static HMWQuery toHMWQuery(HQuery q) {
		HMWQuery hmwQuery = new HMWQuery();
		hmwQuery.schema = q.getSchema();
		hmwQuery.selectFields = q.getSelectFieldsStr();
		hmwQuery.qStr = q.getQStr();
		hmwQuery.orderByStr = q.getOrderFieldsStr();
		hmwQuery.highlightFields = q.getHighlightFieldsStr();
		hmwQuery.highlightMode = q.getHighlightMode();
		hmwQuery.rowStart = q.getRowStart();
		hmwQuery.rowCount = q.getRowCount();
		hmwQuery.uuid = null;
		hmwQuery.item = null;
		return hmwQuery;
	}

	public static HMWQuery toSimpleHMWQuery(String schemaName, Object uuid, String itemID) {
		HMWQuery hmwQuery = new HMWQuery();
		hmwQuery.schema = schemaName;
		hmwQuery.rowStart = 0;
		hmwQuery.rowCount = 1;
		hmwQuery.uuid = uuid + "";
		hmwQuery.item = itemID;
		hmwQuery.qStr = null;
		return hmwQuery;
	}

	public static HMWReportQuery toHMWReportQuery(HReportQuery q) {
		HMWReportQuery hmwQuery = new HMWReportQuery();
		hmwQuery.schema = q.getSchema();
		hmwQuery.qStr = q.getQStr();
		hmwQuery.groupByField = q.getGroupByField();
		hmwQuery.havingClause = q.getHavingClause();
		hmwQuery.rowStart = q.getRowStart();
		hmwQuery.rowCount = q.getRowCount();
		return hmwQuery;
	}

	public static UpdateResponse createUpdateResponse(HMWResponse resp) {
		UpdateResponse uResp = new UpdateResponse();
		uResp.setRows(resp.totalCount);
		return uResp;
	}

	public static QueryResponse createQueryResponse(HMWResponse resp) {
		QueryResponse qResp = new QueryResponse(resp.totalCount);
		if (resp.dataType.equals("blob")) {
			byte[] blob = resp.blob;
			HDocumentList docs = new HDocumentList();
			HDocument doc = new HDocument();
			doc.setBinary(blob);
			docs.add(doc);
			qResp.setResults(docs);
		} else if (resp.dataType.equals("resultset")) {
			HMWDocument[] list = resp.data;
			HDocumentList docs = new HDocumentList();
			for (int i=0; i<list.length; i++) {
				HDocument doc = HMWUtil.toHDocument(list[i]);
				docs.add(doc);
			}
			qResp.setResults(docs);
		}
		return qResp;
	}
}
