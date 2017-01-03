package org.iipg.hurricane.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.iipg.hurricane.MixedRecord;

public class MixedFileSpliter {

	private MixedInputFile bif = null;

	public int getCurrPos() {
		if (bif != null) {
			return bif.getPos();
		}
		return 0;
	}

	public MixedFileSpliter(InputStream in) {
		try {
			bif = new MixedInputFile(in);
		} catch (MixedFileFormatException e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
	}

	public byte[] getNext() throws IOException, MixedFileFormatException {
		if (bif.hasNext()) {
			MixedRecord record = bif.next();
			return record.toByteArrayRead();
		}
		return new byte[0];
	}

	public void close() {
		if (bif != null)
			try {
				bif.close();
			} catch (IOException e) {
				// TODO �Զ���� catch ��
				e.printStackTrace();
			}
	}

	private static void printRaw(MixedRecord record) {
		System.out.println(new String(record.getBinaryData()));
	}

	private static void printProps(MixedRecord record) {
		for (Iterator it = record.getKeySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = record.getValue(key);
			System.out.println(key + " : " + value);
		}
	}

	/**
	 * Test Main
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		int count = 0;
		long startTime = System.currentTimeMillis();
		String fName = args[0];
		File f = new File(fName);
		if (!f.exists()) {
			System.out.println("input file not exist!");
			return;
		}

		/*
		 * String outDir = args[1] + File.separator + f.getName(); File outPath
		 * = new File(outDir); if (outPath.exists()) { outPath.delete(); //
		 * System.out.println("output path already exist!"); // return; }
		 * outPath.mkdir();
		 */

		try {
			MixedFileSpliter spliter = new MixedFileSpliter(
					new FileInputStream(f));
			int currPos = spliter.getCurrPos();
			for (byte[] it = spliter.getNext(); it.length > 0; it = spliter
					.getNext()) {
				MixedRecord record = MixedRecord.load(it);
				if (record.getValue(MixedFileConstant.DATA_TYPE) == null) {
					System.out.println("DATA_TYPE IS NULL!");
					printProps(record);
				}
				/*
				 * String DATA_TYPE=record.getValue("DATA_TYPE"); String subName
				 * = outDir + File.separator +
				 * record.getValue(HtmlProperty.ARTICLE_TITLE)+"."+DATA_TYPE;
				 * System.out.println(subName + " - " + currPos + ":" +
				 * it.length); saveToFile(subName, record.getBinaryData());
				 */

				// if(DATA_TYPE.equalsIgnoreCase("DATA_HTTP")||DATA_TYPE.equalsIgnoreCase("DATA_IM")||DATA_TYPE.equalsIgnoreCase("DATA_SMS")){
				// System.out.println(subName + " - " + currPos + ":" +
				// it.length);
				// printProps(record);
				// }
				// printRaw(record);

				currPos = spliter.getCurrPos();
				count++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Count: " + count);
		System.out.println("Use " + (System.currentTimeMillis() - startTime)
				+ " ms.");
	}

	private static void saveToFile(String fName, byte[] content) {
		File f = new File(fName);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(f);
			fOut.write(content);
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
