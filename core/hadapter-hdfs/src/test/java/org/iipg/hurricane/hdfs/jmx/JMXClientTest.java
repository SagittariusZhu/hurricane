package org.iipg.hurricane.hdfs.jmx;

import org.iipg.hurricane.hdfs.jmx.response.NameNodeInfo;
import org.iipg.hurricane.jmx.client.JMXClient;
import org.iipg.hurricane.jmx.client.JMXException;
import org.junit.Test;

public class JMXClientTest {

	@Test
	public void run() {
		try {
			JMXClient client = new JMXClient("http://sr1:22070/jmx");
			HadoopJMXRequest req = new HadoopJMXRequest.NameNodeInfo();
			HadoopJMXResponse resp = client.execute(req);
			
			NameNodeInfo info = new NameNodeInfo(resp);
			
			String version = info.getVersion();
			long used = info.getUsed();
			long max = info.getCapacity();
			float usage = (float) (used * 100.0 / max);
			System.out.println("Memory usage: used: " + used + 
					" / max: " + max + " = " + usage + "%" +
					"\nVersion: " + version);
		} catch (JMXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
