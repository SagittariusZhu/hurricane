package org.iipg.hurricane.io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.iipg.hurricane.MixedRecord;

public class MixedInputFile implements MixedFileConstant {

	private DataInputStream in;
	private boolean nextFlag = false;
	private MixedRecord current = null;
	private int curPos = 0;

	public MixedInputFile(InputStream in) throws MixedFileFormatException {
		this.in = new DataInputStream(in);
		curPos = 0;
	}

	private boolean init() {
		byte[] magic = new byte[1];
		byte[] version = new byte[1];
		try {
			in.read(magic);
			curPos += 1;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (magic[0] != MAGIC[0])
			return false;

		try {
			in.read(version);
			curPos += 1;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (version[0] != VERSION[0])
			return false;
		return true;
	}

	public InputStream getInputStream() {
		return in;
	}

	public boolean hasNext() {
		if (!nextFlag) {
			try {
				if (in.available() <= 0)
					return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			nextFlag = true;
		}
		return true;
	}

	public MixedRecord next() throws MixedFileFormatException {
		if (hasNext()) {
			current = readRecord();
			nextFlag = false;
		} else {
			current = null;
		}
		return current;
	}

	public int getPos() {
		return curPos;
	}

	public void close() throws IOException {
		in.close();
	}

	public int findMagic() {
		return 1;
	}

	private MixedRecord readRecord() throws MixedFileFormatException {
		while (true) {
			while (!init()) {
				// int offset=findMagic();
				// try {
				// in.skipBytes(offset);
				// } catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }

			}
			int checksum = 0;
			int rawLength = 0;
			try {
				checksum = in.readInt();
				curPos += 4;
				rawLength = in.readInt();
				curPos += 4;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			if (checksum == ((int) MixedRecord.VERSION[0]) << 16
					+ MixedRecord.MAGIC[0] + rawLength)
				break;
		}
		/*
		 * if(!init()){ throw new
		 * MixedFileFormatException("Not valid bigfile format."); }
		 */
		MixedRecord record = MixedRecord.load(in);
		// record.readFields(in);

		curPos += record.getRawLength();
		return record;
	}

	/**
	 * Unit Test Case
	 * 
	 * @param args
	 *            [0]: input bigfile name.
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		FileInputStream in = new FileInputStream(args[0]);
		MixedInputFile bif = new MixedInputFile(in);
		int count = 0;
		while (bif.hasNext()) {
			MixedRecord record = bif.next();

			System.out.println("DATA_TYPE:" + record.getValue(DATA_TYPE));
			printProps(record);

			count++;
		}
		System.out.println("count: " + count);
		bif.close();
	}

	private static void printProps(MixedRecord record) {
		for (Iterator it = record.getKeySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = record.getValue(key);

			System.out.println(key + " : " + value);
		}
	}
}
