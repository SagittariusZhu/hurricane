package org.iipg.hurricane;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.iipg.hurricane.io.MixedFileConstant;

public class MixedRecord implements MixedFileConstant {
	/**
	 * raw length in InputStream
	 */
	private int rawLength;
	/**
	 * inner byte array, store file content
	 */
	private byte[] buf;
	/**
	 * properties for file
	 */
	private Map props = new HashMap();

	/**
	 * Constructor
	 */
	public MixedRecord() {
		this.buf = new byte[0];// lxj modify
	}

	/**
	 * Constructor, store buf as file content
	 * 
	 * @param buf
	 */
	public MixedRecord(byte[] buf) {
		this(buf, new HashMap());
	}

	/**
	 * Constructor
	 * 
	 * @param buf
	 * @param props
	 */
	public MixedRecord(byte[] buf, Map props) {
		this.buf = (byte[]) Array.newInstance(byte.class, buf.length);
		System.arraycopy(buf, 0, this.buf, 0, buf.length);
		this.props.putAll(props);
	}

	public int getRawLength() {
		return rawLength;
	}

	public void setRawLength(int length) {
		this.rawLength = length;
	}

	public byte[] getBinaryData() {
		return this.buf;
	}

	public void setBinaryData(byte[] data) {
		this.buf = data;
	}

	public void putAll(Map map) {
		this.props.putAll(map);
	}

	public Map getProps() {
		return this.props;
	}

	public void putValue(String key, String value) {
		this.props.put(key, value);
	}

	public String getValue(String key) {
		return (String) this.props.get(key);
	}

	public Set getKeySet() {
		return this.props.keySet();
	}

	public boolean containsKey(String key) {
		return this.props.containsKey(key);
	}

	public int getBufLength() {
		return (buf == null) ? 0 : this.buf.length;
	}

	/**
	 * Convert record to raw byte array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		int buflen = 0;
		if (this.buf == null || this.buf.length == 0) // 2011-10-10, added by
		// xuhj.
		{
			buflen = 0;
			out.writeInt(0);
			out.writeInt(0);
		} else // 2011-10-10, added by xuhj.
		{
			buflen = this.buf.length;
			out.writeInt(buflen);
			out.writeInt(buflen);
			out.write(buf);
		}

		String propsStr = buildStrFromMap(props);
		byte[] propsBuf = propsStr.getBytes(DEFAULT_CHARSET);
		// byte[] propsBuf = buildByteFromMap(props);

		int propslen = propsBuf.length;
		out.writeInt(propslen);
		out.write(propsBuf);

		// System.out.println("buflen: " + buflen + "; propslen: " + propslen);
		return bout.toByteArray();
	}

	public int toByteArrayEx(DataOutputStream outs, int offset)
			throws IOException {
		int mylen = offset;
		int CheckSum = ((int) MixedRecord.VERSION[0]) << 16
				+ MixedRecord.MAGIC[0] + getBufLength();
		outs.write(MixedRecord.MAGIC);
		outs.write(MixedRecord.VERSION);
		outs.writeInt(CheckSum);
		int buflen = 0;
		if (this.buf == null || this.buf.length == 0) // 2011-10-10, added by
		// xuhj.
		{
			outs.writeInt(0);
			outs.writeInt(0);
		}// 2011-10-10, added by xuhj.
		else // 2011-10-10, added by xuhj.
		{
			buflen = this.buf.length;
			outs.writeInt(buflen);
			outs.writeInt(buflen);
			outs.write(buf);
		}
		mylen += 14 + buflen;

		byte[] propsBuf = null;
		int propslen = 0;
		/*
		 * String propsStr = buildStrFromMap(props); if (propsStr != null) {
		 * propsBuf = propsStr.getBytes(DEFAULT_CHARSET); propslen =
		 * propsBuf.length; outs.writeInt(propslen); outs.write(propsBuf); }
		 * else outs.writeInt(propslen);
		 */
		propsBuf = buildByteFromMap(props);
		propslen = propsBuf.length;
		outs.writeInt(propslen);
		outs.write(propsBuf);

		mylen += 4 + propslen;

		return mylen;
	}

	/**
	 * Convert record to raw byte array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArrayRead() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		int buflen = 0;
		if (this.buf == null || this.buf.length == 0) // 2011-10-10, added by
		// xuhj.
		{
			buflen = 0;
			out.writeInt(0);
			// out.writeInt(0); //lxj modify
		} else // 2011-10-10, added by xuhj.
		{
			buflen = this.buf.length;
			out.writeInt(buflen);
			out.write(buf);
		}

		// String propsStr = buildStrFromMap(props);
		// byte[] propsBuf = propsStr.getBytes(DEFAULT_CHARSET);
		byte[] propsBuf = buildByteFromMap(props);

		int propslen = propsBuf.length;
		out.writeInt(propslen);
		out.write(propsBuf);

		// System.out.println("buflen: " + buflen + "; propslen: " + propslen);
		return bout.toByteArray();
	}

	private String buildStrFromMap(Map props) {
		StringBuffer buf = new StringBuffer();
		for (Iterator it = props.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			buf.append(key).append(":").append(props.get(key)).append("~.~.");
		}
		return buf.toString();
	}

	// lxj modify
	private byte[] buildByteFromMap(Map props) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		StringBuffer buf = new StringBuffer();
		for (Iterator it = props.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) props.get(key);
			byte[] tmp;
			try {
				tmp = (key + ":" + value).getBytes(DEFAULT_CHARSET);
				int len = tmp.length;
				os.writeInt(len);
				os.write(tmp);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return bos.toByteArray();
	}

	private static Map convertBinaryToMap(byte[] buf) {
		Map props = new HashMap();
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf));

		int prolen;
		try {
			prolen = in.readInt();
		} catch (IOException e1) {
			prolen = 0;
			e1.printStackTrace();
			return props;
		}
		while (prolen > 0) {
			byte[] probuf = (byte[]) Array.newInstance(byte.class, prolen);
			try {
				in.read(probuf);
			} catch (IOException e2) {
				e2.printStackTrace();
				continue;
			}

			String propsStr = "";
			try {
				propsStr = new String(probuf, DEFAULT_CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if (propsStr.contains(":")) {
				int index = propsStr.indexOf(":");
				String name = propsStr.substring(0, index);
				String value = propsStr.substring(index + 1);
				props.put(name, value);
			}

			try {
				prolen = in.readInt();
			} catch (IOException e1) {
				prolen = 0;
				break;
			}
		}
		return props;
	}

	private static Map convertBinaryToMap1(byte[] buf) {
		Map props = new HashMap();
		String propsStr = "";
		try {
			propsStr = new String(buf, DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// String[] pairs = propsStr.split("\n");
		String[] pairs = propsStr.split("~.~.");
		// String[] pairs = propsStr.split("~$~.");
		for (int i = 0; i < pairs.length; i++) {
			String[] pair = pairs[i].split(":");
			if (pair.length == 2) {
				props.put(pair[0], pair[1]);
			}
		}
		return props;
	}

	/**
	 * static method, load record from byte array.
	 * 
	 * @param raw
	 * @return
	 */
	public static MixedRecord load(byte[] raw) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));
		return load(in);
	}

	/**
	 * Static method, load Record from DataInputStream
	 * 
	 * @param in
	 * @return
	 */
	public static MixedRecord load(DataInputStream in) {
		MixedRecord record = new MixedRecord();
		try {
			int buflen = in.readInt();
			if (buflen > 0) {
				byte[] buf = (byte[]) Array.newInstance(byte.class, buflen);
				in.read(buf);
				record.setBinaryData(buf);
			}

			int propslen = in.readInt();

			byte[] propsBuf = (byte[]) Array.newInstance(byte.class, propslen);
			in.read(propsBuf);
			record.putAll(convertBinaryToMap(propsBuf));

			// set raw length in InputStream
			// 8 = int + int
			record.setRawLength(4 + buflen + 4 + propslen);
			return record;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		int buflen = this.buf.length;
		out.writeInt(buflen);
		if (buflen > 0) {
			out.write(buf);
		}
		byte[] propsBuf = null;
		int propslen = 0;
		propsBuf = buildByteFromMap(props);
		propslen = propsBuf.length;
		out.writeInt(propslen);
		out.write(propsBuf);

	}

	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		int buflen = in.readInt();
		if (buflen > 0) {
			byte[] buf = (byte[]) Array.newInstance(byte.class, buflen);
			in.readFully(buf);
			this.setBinaryData(buf);
		}
		int propslen = in.readInt();
		if (propslen > 0) {
			byte[] propsBuf = (byte[]) Array.newInstance(byte.class, propslen);
			in.readFully(propsBuf);
			this.putAll(convertBinaryToMap(propsBuf));
		}
	}

}
