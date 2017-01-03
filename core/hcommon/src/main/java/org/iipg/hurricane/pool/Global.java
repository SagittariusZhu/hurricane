package org.iipg.hurricane.pool;
/*
 * @(#)Global.java	1.06 2011-12-18
 *
 * Copyright (C) 2011 - Jiangnan Institute of Computing Technology. All rights reserved.
 * JN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Global {
	
	public static Queue QUEUE = new ConcurrentLinkedQueue();

}
