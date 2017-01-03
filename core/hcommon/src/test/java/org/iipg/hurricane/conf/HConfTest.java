package org.iipg.hurricane.conf;

import org.junit.Test;

public class HConfTest {

	@Test
	public void run() {
		HurricaneConfiguration conf = new HurricaneConfiguration();
		System.out.println(conf.get("adapter.fs"));
	}
}
