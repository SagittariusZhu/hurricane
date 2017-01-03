package org.iipg.hurricane.hdfs.jmx;

import java.util.ArrayList;
import java.util.List;

import org.iipg.hurricane.jmx.client.JMXRequest;
import org.iipg.hurricane.jmx.client.JMXRequestHandler;
import org.iipg.hurricane.jmx.client.JMXResponse;
import org.iipg.hurricane.jmx.client.JMXResponseExtractor;
import org.json.simple.JSONObject;

public class HadoopJMXRequest extends JMXRequest {

	private String params = null;
	
	public HadoopJMXRequest(String params) {
		super(null);
		this.params = params;
	}

	@Override
	public List<String> getRequestParts() {
		List<String> parts = new ArrayList<String>();
		parts.add(this.params);
		return parts;
	}

	@Override
	public <R extends JMXResponse<? extends JMXRequest>> R createResponse(
			JSONObject pResponse) {
		return (R) new HadoopJMXResponse(this, pResponse);
	}

	@Override
	public JMXRequestHandler getRequestHandler(String pServerURL) {
		return new HadoopJMXRequestHandler(pServerURL, getTargetConfig());
	}

	@Override
	public JMXResponseExtractor getResponseExtractor() {
		return new HadoopJMXResponseExtractor();
	}
	
	public static class NameNodeInfo extends HadoopJMXRequest {
		
		public NameNodeInfo() {
			super("Hadoop:service=NameNode,name=NameNodeInfo");
		}
		
	};

	public static class NameNodeStatus extends HadoopJMXRequest {
		
		public NameNodeStatus() {
			super("Hadoop:service=NameNode,name=NameNodeStatus");
		}
		
	};
}
