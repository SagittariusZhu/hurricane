package org.iipg.hurricane.monitor.cmd;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.monitor.FSObject;
import org.iipg.web.cmd.ConvertionUtil;
import org.iipg.web.cmd.HAdminCommand;
import org.iipg.web.cmd.IIPGCmdException;
import org.iipg.web.cmd.ResponseStatus;
import org.iipg.web.cmd.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class FileSystemCmd extends HAdminCommand {

	@Override
	public void run(HttpServletRequest req) throws IIPGCmdException {
		response = new JSONObject();
        String command = ConvertionUtil.getSimpleStringWithNull(req.getParameter("command"));

        JSONObject spaceInfo = FSObject.getSpace();
        
        header = ServletUtil.getResponseHeader(ResponseStatus.RS_OK);
        try {
            response.put("data", spaceInfo);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

}
