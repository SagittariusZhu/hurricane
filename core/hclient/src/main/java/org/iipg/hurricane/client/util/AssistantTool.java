/**
 * 
 */
package org.iipg.hurricane.client.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.iipg.hurricane.client.metadata.HDocument;
import org.iipg.hurricane.client.metadata.HDocumentList;

/**
 * @author lixiaojing
 *
 */
public class AssistantTool {
	private static Random radom = new Random(Integer.MAX_VALUE);

	public static String getRadomString(int length){
		int count = Math.abs(radom.nextInt());
		String str = Integer.toString(count);
		int distance = length - str.length();
		if (distance > 0) {
			for (int j = 0; j < distance; j++) {
				str = "0" + str;
			}
		} else {
			str = str.substring(Math.abs(distance));
		}
//		System.out.println(str);
		return str;
	}

	public static void printResultSet(HDocumentList hDocs) {
		System.out.println("Got " + hDocs.size() + " rows.");
		for (int i=0; i<hDocs.size(); i++) {
			System.out.println("--------------");
			HDocument doc = hDocs.get(i);
			Map props = doc.getProps();
			for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
				String fieldName = (String) it.next();
				System.out.println(formatLeftS(fieldName, 14) + " :\t" + props.get(fieldName));
			}
			byte[] buf = doc.getBinary();
			if (buf != null && buf.length > 0) {
				System.out.println(formatLeftS("BLOB", 14) + " :\t" + buf.length + " bytes");
			}
		}
	}
	
	public static String formatLeftS(String str, int min_length) {
        String format = "%-" + (min_length < 1 ? 1 : min_length) + "s";
        return String.format(format, str);
    }

}
