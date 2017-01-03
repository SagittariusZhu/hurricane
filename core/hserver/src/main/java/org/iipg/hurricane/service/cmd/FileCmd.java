package org.iipg.hurricane.service.cmd;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.FileUtil;
import org.iipg.hurricane.util.ServletUtil;
import org.iipg.hurricane.util.XmlUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCmd extends HCommand {

	private static Logger log = LoggerFactory.getLogger(FileCmd.class);

	public FileCmd(String schemaName) {
		super(schemaName);
	}

	@Override
	public void run(HttpServletRequest req) {
		String path = ConvertionUtil.getSimpleStringWithNull(req.getParameter("path"));		
		String encode = ConvertionUtil.getSimpleStringWithNull(req.getParameter("encode"));		
		String fullPath = "/hurricane/configs/" + getName() + path;
		//String fullPath = "/hurricane/schema/" + getName() + ".xml";
		ZkOperator zkOper = new ZkOperator();   

		try {   
			Properties props = FileUtil.readPropertiesFile("zk.properties");
			String zkServerAddress = props.getProperty("zk.host.endpoints");
			zkOper.setHosts(zkServerAddress);

			if (zkOper.isExist(fullPath)) {
				String content = new String(zkOper.getData(fullPath), encode);

				if (fullPath.endsWith(".xml")) {
					content = content.replaceAll("\n", "");
					content = XmlUtil.prettyPrint(content);

					this.setHeader(KEY_CONTENT_TYPE, "text/xml;charset=utf-8");
					this.setRawOutput(content);
				} else if (fullPath.endsWith(".json")) {
					this.setHeader(KEY_CONTENT_TYPE, "application/json;charset=utf-8");
					this.setRawOutput(content);				
				}
				success = true;		
			} else {
				this.header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
				header.put("message", "File [" + path + "] not exist.");
			}
		} catch (Exception e) {   
			e.printStackTrace();   
		}  finally {
			zkOper.close();
		}
	}


}
