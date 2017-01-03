package org.iipg.hurricane.service.cmd;

import javax.servlet.http.HttpServletRequest;

import org.iipg.hurricane.service.HCommand;


public class ListCmd extends HCommand {
	private final static String NAME = "list";
	
	public ListCmd(String schemaName) {
		super(schemaName);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void run(HttpServletRequest req) {
		// TODO Auto-generated method stub
		
	}

}
