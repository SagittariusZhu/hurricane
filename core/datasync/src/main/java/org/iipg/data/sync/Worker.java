package org.iipg.data.sync;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.iipg.data.sync.conf.WorkerProp;

public abstract class Worker extends Thread {
	
	private CyclicBarrier barrier;  
    private int threadID;  
	
    private WorkerProp props = null;
	
	public Worker(WorkerProp props) {
		this.props = props;
	}
	
	protected WorkerProp getProps() {
		return this.props;
	}
	
	public abstract void run();

	public void setBarrier(CyclicBarrier barrier, int threadID) {
		this.barrier = barrier;   
        this.threadID = threadID;  
	}
	
	public void end() {
		try {  
            barrier.await();  
        } catch (InterruptedException e1) {  
            e1.printStackTrace();  
        } catch (BrokenBarrierException e1) {  
            e1.printStackTrace();  
        }  
	}

}
