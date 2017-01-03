package org.iipg.hurricane.hdfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.ha.HAServiceTarget;
import org.apache.hadoop.io.IOUtils;
import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.util.MD5Util;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDFSSchema {

	private static Logger LOG = LoggerFactory.getLogger(HDFSSchema.class);

	private static Configuration hdfsConf = new Configuration();

	public static Configuration getConf() {
		return hdfsConf;
	}
	
	public static int getRpcTimeoutForChecks() {
		int rpcTimeoutForChecks = -1;
		if (hdfsConf != null) {
			rpcTimeoutForChecks = hdfsConf.getInt(
					CommonConfigurationKeys.HA_FC_CLI_CHECK_TIMEOUT_KEY,
					CommonConfigurationKeys.HA_FC_CLI_CHECK_TIMEOUT_DEFAULT);
		}
		return rpcTimeoutForChecks;
	}
	
	public static HAServiceTarget resolveTarget(String nnId) {
		return new NNHAServiceTarget(hdfsConf, null, nnId);
	}
	
	public static String getNameServices() {
		try {
			FileSystem hfs = FileSystem.get(hdfsConf);
			FsStatus status = hfs.getStatus();
			String name = hfs.getCanonicalServiceName();
			return name;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] readFully(String schemaName, String uuid, 
			int startPos, int bufLen) throws HurricaneException {
		try {
			FileSystem hfs = FileSystem.get(hdfsConf);
			Path target = buildPath(schemaName, uuid);
			LOG.info(target.toString());
			FSDataInputStream in = hfs.open(target);

			if (in == null || in.available() < startPos + bufLen) {
				LOG.warn("Input parameter is error! " + target.toString());
				throw new IOException("Input parameter is error! " + target.toString());
			}
			//ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = (byte[]) Array.newInstance(byte.class, bufLen);
			in.seek(startPos);
			IOUtils.readFully(in, buf, 0, buf.length);
			in.close();
			hfs.close();
			return buf;
		} catch (Exception fne) {
			LOG.warn(schemaName + " : " + uuid + " has no blob in HDFS.");
			throw new HurricaneException("uuid " + uuid + " has no blob in HDFS.");
		}
	}

	public static boolean exist(String schemaName, String uniqueID) throws IOException {
		FileSystem hfs = FileSystem.get(hdfsConf);
		Path target = buildPath(schemaName, uniqueID);
		return hfs.exists(target);
	}
	
	public static byte[] readFully(String schemaName, String uuid) throws Exception {
		FileSystem hfs = FileSystem.get(hdfsConf);
		Path target = buildPath(schemaName, uuid);
		FSDataInputStream in = hfs.open(target);
		int bufLen = in.available();
		//ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = (byte[]) Array.newInstance(byte.class, bufLen);
		in.seek(0);
		IOUtils.readFully(in, buf, 0, buf.length);
		in.close();
		hfs.close();
		return buf;
	}

	public static int write(String schemaName, String uuid, byte[] data) throws HurricaneException {
		try {
			if (data != null && data.length > 0) {
				FileSystem hfs = FileSystem.get(hdfsConf);
				Path target = buildPath(schemaName, uuid);
				FSDataOutputStream outputStream = null;

				outputStream = hfs.create(target);
				outputStream.write(data, 0, data.length);
				outputStream.close();

				hfs.close();
				return data.length;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e.getMessage());
		}
	}

	public static void delete(String dbName, String uuid) {
		LOG.info("Delete HDFS data for " + dbName + " : " + uuid);
		try {
			FileSystem hfs = FileSystem.get(hdfsConf);
			Path target = buildPath(dbName, uuid);
			hfs.delete(target, true);
			deleteRecursive(dbName, target.getParent(), hfs);
			hfs.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e.getMessage());
		}		
	}

	public static boolean clearSchema(String schemaName) throws HurricaneException {
		LOG.info("Clear HDFS schema.");
		try {
			FileSystem hfs = FileSystem.get(hdfsConf);
			boolean ret = hfs.delete(new Path("/blob/" + schemaName), true);
			hfs.close();
			return ret;
		} catch(Exception e) {
			e.printStackTrace();
			throw new HurricaneException(e.getMessage());
		}
	}

	private static Path buildPath(String schemaName, String uuid) {
		String md5Str = MD5Util.toMD5String(uuid);
		Path dfs = new Path("/blob/" + schemaName + "/" + md5Str.substring(0, 4) + "/" + md5Str.substring(5, 9) + "/" + uuid);
		return dfs;
	}


	private static void deleteRecursive(String dbName, Path target, FileSystem hfs) throws FileNotFoundException, IOException {
		Path stopPath = new Path("/blob/" + dbName);
		if (target.toString().equals(stopPath.toString())) {
			return;
		}
		if (hfs.isDirectory(target)) {
			FileStatus[] status = hfs.listStatus(target);
			if (status.length > 0) return;
			hfs.delete(target, true);
			deleteRecursive(dbName, target.getParent(), hfs);
		}
	}

	public static JSONObject getInfo() {
		int kb = 1024;
		JSONObject props = new JSONObject();
		try {
			FileSystem hfs = FileSystem.get(hdfsConf);
			FsStatus status = hfs.getStatus();
			props.put("CanonicalServiceName", hfs.getCanonicalServiceName());
			props.put("capacity", status.getCapacity() / kb);
			props.put("used", status.getUsed() / kb);
//			props.put("remaining", status.getRemaining() / kb);
		} catch (Exception ignore) {}
		return props;
	}

	public static void copy(String srcName, String uid, String destName) throws Exception {
		byte[] buf = readFully(srcName, uid);
		if (buf != null)
			write(destName, uid, buf);
	}



}
