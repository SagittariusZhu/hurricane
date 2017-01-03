package org.iipg.hurricane.util;

import java.util.ArrayList;
import java.util.List;

import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResponse;
import org.iipg.hurricane.db.util.HDBUtil;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWResponse;

public class HMWResponseUtil {

	public static HMWResponse createSuccessResponse(int count, int used) {
		HMWResponse resp = new HMWResponse();
		resp.errorCode = 0;
		resp.reason = "";
		resp.totalCount = count;
		resp.used = used;
		resp.data = null;
		return resp;
	}

	public static HMWResponse createResponse(HDBResponse source) {
		HMWResponse resp = new HMWResponse();
		resp.errorCode = 0;
		resp.reason = "";
		resp.totalCount = (int) source.getTotalCount();
		resp.used = source.getConsumeTime();

		List<HMWDocument> docs = new ArrayList<HMWDocument>();

		resp.dataType = source.getDataType();
		if (source.getDataType().equals("resultset")) {
			List<HDBBaseObject> hDocs = (List<HDBBaseObject>) source.getData();
			for (int i=0; i<hDocs.size(); i++) {
				HMWDocument doc = HDBUtil.toHMWDocument((HDBRecord) hDocs.get(i));
				docs.add(doc);
			}
			resp.data = docs.toArray(new HMWDocument[0]);
			resp.blob = null;
		} else if (source.getDataType().equals("blob")) {
			if (source.getTotalCount() == 1) {
				HDBBlob blob = (HDBBlob) source.getData();
				resp.blob = blob.getBlob();
			} else {
				resp.blob = new byte[0];
			}
			resp.data = null;
		}

		return resp;
	}

}
