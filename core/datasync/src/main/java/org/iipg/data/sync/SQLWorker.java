package org.iipg.data.sync;

import java.io.IOException;

import org.iipg.data.sync.conf.WorkerProp;
import org.iipg.data.sync.util.StoreUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class SQLWorker extends Worker {

	public SQLWorker(WorkerProp props) {
		super(props);
	}

	@Override
	public void run() {
		try {
			// getRecords
			SQLGetter getter = new SQLGetter(getProps().getSourceDS());

			boolean hasNext = true;
			int start = 0;
			int rows = 1000;
			while (hasNext) {
				String limits = String.format("LIMIT %d, %d", start, rows);
				JSONArray values = getter.get(getProps().getQStr() + " " + limits);
				System.out.println("Get from " + getProps().getSource() + " using " + limits);
				
				start += values.length();
				if (values.length() < rows) hasNext = false;
				
				// for each record do
//				for (int i=0; i<values.length(); i++) {
//					JSONObject obj = values.getJSONObject(i);
//					String info = String.format("No.%s: %s", i+1, obj);
//					System.out.println(info);
//				}

				// store data to remote server
				try {
					int count = StoreUtil.store(getProps(), values);
					System.out.println("store " + count + " records successful!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			super.end();
		}
	}
}
