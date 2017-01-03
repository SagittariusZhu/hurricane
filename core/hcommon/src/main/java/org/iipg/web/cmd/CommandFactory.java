package org.iipg.web.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandFactory {

	private static Logger LOG = LoggerFactory.getLogger(CommandFactory.class);
	
	private static String cmdPrefix = "org.iipg.web.cmd";
	
	public static void setCmdPrefix(String prefix) {
		cmdPrefix = prefix;
	}
	
	public static String getCmdPrefix() {
		return cmdPrefix;
	}
	
	public static HCommand getCommand(String cmdName, String cmdPara) {
		Class cmdClass = getCommandClassByName(cmdName);
		if (cmdClass != null) {
			HCommand cmd = (HCommand) ClassUtils.newInstance(cmdClass, new Class[]{String.class}, new Object[]{cmdPara});
			return cmd;
		}
		return new DefaultCmd(cmdName, cmdPara);
	}

	public static HAdminCommand getAdminCommand(String cmdName) {
		Class cmdClass = getCommandClassByName(cmdName);
		if (cmdClass != null) {
			HAdminCommand cmd = (HAdminCommand) ClassUtils.newInstance(cmdClass);
			return cmd;
		}
		return new DefaultAdminCmd(cmdName);
	}

	private static Class getCommandClassByName(String cmdName) {
		String className = cmdPrefix + "." + StringUtils.toUpperCaseFirstOne(cmdName) + "Cmd";
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			LOG.warn("Command " + cmdName + " class not found! className=" + className + "");
		}
		return null;
	}

}
