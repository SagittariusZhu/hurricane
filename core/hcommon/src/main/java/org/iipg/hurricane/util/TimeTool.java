/**
 * 
 */
package org.iipg.hurricane.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lixiaojing
 *
 */
public class TimeTool {

	private static List<SimpleDateFormat> dateFormatList = new ArrayList<SimpleDateFormat>();
	static {
		dateFormatList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
		dateFormatList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		dateFormatList.add(new SimpleDateFormat("yyyy-MM-dd"));
		dateFormatList.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS"));
		dateFormatList.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
		dateFormatList.add(new SimpleDateFormat("yyyy/MM/dd"));
	}
	
	public static String getCurrentTime(String pattern){
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		String time = format.format(new Date());
		return time;
	}
	
	public static Long getCurrentTime(){
		return System.currentTimeMillis();
	}
	
	public static Long convertTime(String timeStr,String pattern){
		Long time = 0L;
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date d;
		try {
			d = format.parse(timeStr);
			time = d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}
	
	public static Date parse(String timeStr) throws ParseException {
		Date d = null;
		for (SimpleDateFormat df : dateFormatList) {
			try {
				d = df.parse(timeStr);
				return d;
			} catch (ParseException e) {}
		}
		throw new ParseException("unsupport date format", 400);
	}

	public static void main(String[] args) {
		System.out.println(convertTime("2013/12/23 12:10:34:100","yyyy/MM/dd HH:mm:ss:SSS"));
		System.out.println(convertTime("2013/12/23","yyyy/MM/dd"));
	}

}
