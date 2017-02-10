/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved. 
 * Email: community@polarion.org
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 (the "License"). You may not use 
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */
package org.polarion.svncommons.commentscache.task;

import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class CacheTaskExecutor extends ThreadPoolExecutor {
	protected Set tasks = Collections.synchronizedSet(new HashSet());

	public CacheTaskExecutor(int threads) {
		super(threads, threads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue());
	}

	public void execute(Runnable command) {
		if (command instanceof CacheTask) {
			// prevents of posting similar tasks during caching
			CacheTask task = (CacheTask) command;

			boolean contains = false;
			synchronized (this.tasks) {
				contains = this.tasks.contains(task.getTaskId());
				if (!contains) {
					this.tasks.add(task.getTaskId());
				}
			}

			if (!contains) {
				super.execute(task);
			}
		} else {
			super.execute(command);
		}
	}

	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (r instanceof CacheTask) {
			// prevents of posting similar tasks during caching
			CacheTask task = (CacheTask) r;
			this.tasks.remove(task.getTaskId());
		}
	}
}
