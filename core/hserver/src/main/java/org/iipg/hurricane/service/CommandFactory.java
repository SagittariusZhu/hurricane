package org.iipg.hurricane.service;

import org.iipg.hurricane.service.cmd.*;

public class CommandFactory {

	public static HCommand getCommand(String cmdName, String schemaName) {
		if ("select".equals(cmdName)) {
			return new SelectCmd(schemaName);
		} else if ("addDoc".equals(cmdName)){
			return new AddDocCmd(schemaName);
		} else if ("update".equals(cmdName)){
			return new UpdateCmd(schemaName);
		} else if ("delete".equals(cmdName)){
			return new DeleteCmd(schemaName);
		} else if ("huke".equals(cmdName)){
			return new HukeCmd(schemaName);
		} else if ("dih".equals(cmdName)){
			return new DihCmd(schemaName);
		} else if ("dsync".equals(cmdName)){
			return new DSyncCmd(schemaName);
		} else if ("dataSource".equals(cmdName)){
			return new DSourceCmd(schemaName);
		} else if ("subscribe".equals(cmdName)){
			return new SubscribeCmd(schemaName);
		} else if ("file".equals(cmdName)){
			return new FileCmd(schemaName);
		} else if ("optimize".equals(cmdName)){
			return new OptimizeCmd(schemaName);
		} else {
			return new DefaultCmd(cmdName);
		}
	}

	public static HCommand getAdminCommand(String cmdName) {
		if ("cores".equals(cmdName)) {
			return new CoresCmd("");
		} else if ("system".equals(cmdName)){
			return new SystemCmd("");
		} else if ("properties".equals(cmdName)){
			return new PropertiesCmd("");
		} else {
			return new DefaultCmd(cmdName);
		}
	}
}
