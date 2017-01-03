package org.iipg.data.sync;

import org.iipg.data.sync.conf.WorkerProp;

public class WorkerFactory {

	public static Worker newInstance(WorkerProp prop) {
		return new SQLWorker(prop);
	}

}
