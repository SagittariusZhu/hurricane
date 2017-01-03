package org.iipg.hurricane.service.cmd;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.HurricaneException;
import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.conf.HurricaneConfigurationFactory;
import org.iipg.hurricane.db.HController;
import org.iipg.hurricane.db.HControllerFactory;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.service.HCommand;
import org.iipg.hurricane.service.ResponseStatus;
import org.iipg.hurricane.util.ConvertionUtil;
import org.iipg.hurricane.util.ServerInfo;
import org.iipg.hurricane.util.ServletUtil;
import org.iipg.hurricane.util.ZkOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCmd extends HCommand {
	private static final String NAME = "system";
	private static Logger log = LoggerFactory.getLogger(SystemCmd.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private JSONObject info = null;

	public SystemCmd(String schemaName) {
		super(schemaName);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public JSONObject getOutputJSON() {		
		return info;
	}

	@Override
	public void run(HttpServletRequest req) {
		info = new JSONObject();

		try {
			Map<String, ?> props = conf.getProps();
			for (String key : props.keySet()) {
				if (key.startsWith("controller.")) {
					HController controller = HControllerFactory.getInstance(conf.get(key));
					info.put(controller.getName(), controller.getInfo());
				}
			}	
			info.put(ServerInfo.getName(), ServerInfo.getVersion());
			info.put("start", sdf.format(ServerInfo.getStartTime()));
			info.put("jvmversion", ServerInfo.getJVMVersion());
			info.put("configs", ServerInfo.getProperties());
			info.put("system", ServerInfo.getRuntimeInfo());
			info.put("jvminfo", ServerInfo.getJVMInfo());
			info.put("ftpserverInfo", ServerInfo.getFtpServerInfo());
			info.put("zooInfo", ServerInfo.getZooKeeperInfo());
		} catch (Exception e) {
			e.printStackTrace();
			header = ServletUtil.getResponseHeader(ResponseStatus.RS_INTERNAL_ERROR);
			try {
				info.put("exception", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		success = true;
	}

}
