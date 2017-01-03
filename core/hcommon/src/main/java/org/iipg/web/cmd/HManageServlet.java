package org.iipg.web.cmd;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;


public class HManageServlet extends HttpServlet {

	public static final String SERVLET_NAME = "HManagerServlet";
	
	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		this.doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
		long startTime = System.currentTimeMillis();
		
		String cmdStr = req.getRequestURI().substring(1);
		String[] cmdArr = cmdStr.split("/");
		String cmdName = "";
		String schemaName = "";
		boolean adminMode = false;
		if (cmdArr.length < 2)
			cmdName = cmdArr[0];
		else {
			cmdName = cmdArr[cmdArr.length - 1];
			if ("admin".equals(cmdArr[cmdArr.length - 2])) {
				adminMode = true;
			} else {
				adminMode = false;
				schemaName = cmdArr[cmdArr.length - 2];
			}
		}
		
		HCommand command = null;
		if (adminMode)
			command = CommandFactory.getAdminCommand(cmdName);
		else
			command = CommandFactory.getCommand(cmdName, schemaName);
			
		try {
			command.run(req);
		} catch (IIPGCmdException he) {
			he.printStackTrace();
			res.setContentType("text/plain;charset=utf-8");
	        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        res.getWriter().println(he.getMessage());	
		}
		
		if ("json".equals(req.getParameter("wt"))) {
			res.setContentType("application/json;charset=utf-8");			
	        res.setStatus(HttpServletResponse.SC_OK);
	        JSONObject header = command.getResponseHeader();
	        try {
				header.put("QTime", System.currentTimeMillis() - startTime);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject respObj = new JSONObject();
			try {
				respObj.put("responseHeader", header);
				respObj.put("data", command.getOutputJSON());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        res.getWriter().println(respObj.toString());
		} else {
			res.setContentType("text/plain;charset=utf-8");
	        res.setStatus(HttpServletResponse.SC_OK);
	        res.getWriter().println(command.getOutputText());			
		}
	}
}
