package org.iipg.hurricane.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import org.iipg.hurricane.MixedRecord;

public class MixedOutputFile implements MixedFileConstant {

	private DataOutputStream out;

	public MixedOutputFile(OutputStream out) throws IOException {
		this.out = new DataOutputStream(out);
	}

	public OutputStream getOutputStream() {
		return this.out;
	}

	private void init() throws IOException {
		out.write(MAGIC);
		out.write(VERSION);
	}

	private int write(MixedRecord record) throws IOException {
		byte[] buf = record.toByteArray();
		out.write(buf);
		return buf.length;
	}

	public int add(MixedRecord record) throws IOException {
		init();
		return write(record);
	}

	public void close() throws IOException {
		out.close();
	}

	/**
	 * Unit Test Case
	 * 
	 * @param args
	 *            [0] input folder, includes files to package
	 * @param args
	 *            [1] output bigfile name
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		/*
		 * MixedOutputFile out = new MixedOutputFile(new
		 * FileOutputStream(args[1])); File f = new File(args[0]); File[] list =
		 * f.listFiles(); for (int i=0; i<list.length; i++) { if
		 * (list[i].isFile()) { MixedRecord record = new MixedRecord();
		 * record.setBinaryData(readFromFile(list[i]));
		 * record.put(MixedRecord.FILENAME, list[i].getName()); out.add(record);
		 * } } out.close();
		 */
	}

	@SuppressWarnings("unused")
	private static byte[] readFromFile(File f) {
		try {
			FileInputStream in = new FileInputStream(f);
			byte[] buf = (byte[]) Array.newInstance(byte.class, in.available());
			in.read(buf);
			return buf;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
}
