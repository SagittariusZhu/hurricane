package org.iipg.web.cmd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ServletUtil {

	public static JSONObject getResponseHeader(ResponseStatus value) {
		JSONObject header = new JSONObject();
		try {
			header.put("status", value.status);
			header.put("message", value.message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return header;
	}

	public static String getContentText(HttpServletRequest req) {
		String contentType = req.getContentType();
		int dataLength = req.getContentLength();

		try {
			DataInputStream dataStream =
					new DataInputStream(req.getInputStream());

			byte[] datas = null;

			if (dataLength < 0) {//chunked
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[3500];
				int bytes = 0;
				while ((bytes = dataStream.read(buf)) > 0) {
					bout.write(buf, 0, bytes);
				}
				datas = bout.toByteArray();
			} else {
				datas = new byte[dataLength];
				int totalBytes = 0;
				while (totalBytes < dataLength) {
					int bytes = dataStream.read(datas, totalBytes, dataLength);
					totalBytes += bytes;
				}
			}

			String encode = getEncodeFromContentType(contentType);
			String reqBody = new String(datas, encode);

			return reqBody;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static final Pattern patternForCharset = Pattern.compile("charset\\s*=\\s*['\"]*([^\\s;'\"]*)", Pattern.CASE_INSENSITIVE);
	private static String getEncodeFromContentType(String contentType) {
		Matcher matcher = patternForCharset.matcher(contentType);
		if (matcher.find()) {
			String charset = matcher.group(1);
			if (Charset.isSupported(charset)) {
				return charset;
			}
		}
		return "ISO-8859-1";
	}

	private static String webHomePath = "";
	public static void setHome(String realPath) {
		webHomePath = realPath;
	}
	
	public static String getHome() {
		return webHomePath;
	}
}
