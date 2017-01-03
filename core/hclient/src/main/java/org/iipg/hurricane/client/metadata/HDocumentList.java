package org.iipg.hurricane.client.metadata;

import java.util.ArrayList;
import java.util.List;

public class HDocumentList {
	
	private List<HDocument> list = new ArrayList<HDocument>();

	public int size() {
		return list.size();
	}

	public HDocument get(int i) {
		return list.get(i);
	}
	
	public void add(HDocument doc) {
		list.add(doc);		
	}

}
