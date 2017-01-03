package org.iipg.hurricane.hdfs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.query.HDBReportQuery;
import org.iipg.hurricane.db.query.HDBUpdateQuery;
import org.iipg.hurricane.util.MD5Util;
import org.xml.sax.SAXException;

public class HDFSAdapter extends HAdapter {
	public static final Log LOG = LogFactory.getLog(HDFSAdapter.class);

	public HDFSAdapter(HurricaneConfiguration conf, String dbName) throws ParserConfigurationException, SAXException, IOException {
		super(conf, dbName);
	}
	
	@Override
	public HDBQuery createQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResultSet query(HDBQuery con) throws HQueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBBaseObject queryByID(Object uuid, List<String> fields) throws Exception {
		try {
			HDBBlob blob = new HDBBlob(uuid);
			String uniqueID = getUniqueID(blob.getUuid());

			int startPos = 0;
			int bufLen = -1;

			if (fields != null) {
				String item = fields.get(0);
				if (item != null && item.indexOf(":") >= 0) {
					String[] arr = item.split(":");
					startPos = Integer.parseInt(arr[0]);
					bufLen = Integer.parseInt(arr[1]);
					byte[] buf = HDFSSchema.readFully(getDbName(), uniqueID, startPos, bufLen);
					blob.setBlob(buf);
				} else {
					byte[] buf = HDFSSchema.readFully(getDbName(), uniqueID);
					blob.setBlob(buf);
				}
			}
			return blob;
		} catch (Exception e) {
			LOG.warn(getDbName() + " : " + uuid + " has no blob in HDFS.");
			throw new HurricaneException("uuid " + uuid + " has no blob in HDFS.");
		}
	}

	@Override
	public void fillResultSet(HDBResultSet rset, List<String> fields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int store(HDBBaseObject object) throws Exception {
		if (!(object instanceof HDBBlob)) {
			return 0;
		}
		HDBBlob blob = (HDBBlob) object;
		String uniqueID = getUniqueID(blob.getUuid());
		byte[] buff = blob.getBlob();

		if (buff != null && buff.length > 0)
			HDFSSchema.write(this.getDbName(), uniqueID, buff);
		
		for (Iterator<String> it = blob.getExtIterator(); it.hasNext(); ) {
			String key = it.next();
			Map<String, byte[]> blobMap = blob.getExtBlob(key);
			for (Iterator<String> itSub = blobMap.keySet().iterator(); itSub.hasNext(); ) {
				String subUUID = itSub.next();
				byte[] subBuff = blobMap.get(subUUID);
				if (subBuff != null && subBuff.length > 0)
					HDFSSchema.write(this.getDbName(), subUUID, subBuff);
			}
		}
		return 1;
	}

	private String getUniqueID(Object uuid) {
		return MD5Util.toMD5String("" + uuid);
	}

	@Override
	public int store(List<HDBBaseObject> list) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteByID(String uuid) throws Exception {
		HDFSSchema.delete(this.getDbName(), uuid);
		return 0;
	}

	@Override
	public int deleteByIDs(List<String> list) throws Exception {
		for (String uuid : list) {
			deleteByID(uuid);
		}
		return list.size();
	}

	@Override
	public int delete(HDBQuery con) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAutoCommit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
	}

	@Override
	public HDBUpdateQuery createUpdateQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(HDBBaseObject object) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(HDBUpdateQuery q) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HDBReportQuery createReportQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResultSet reportQuery(HDBReportQuery roQuery)
			throws HQueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDBResultSet copyToByQuery(HAdapter adapter, HDBQuery hQuery)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int copyToByList(HAdapter adapter, List<String> list)
			throws Exception {
		for (String id : list) {
			String uniqueID = getUniqueID(id);
			if (HDFSSchema.exist(this.getDbName(), uniqueID))
				HDFSSchema.copy(this.getDbName(), uniqueID, adapter.getDbName());
		}
		return list.size();
	}

	@Override
	public void optimize() {
		// TODO Auto-generated method stub
		
	}

}
