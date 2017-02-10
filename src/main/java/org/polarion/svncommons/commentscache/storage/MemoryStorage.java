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
package org.polarion.svncommons.commentscache.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class MemoryStorage {
	protected static final int PAGE_COUNT = 5;
	protected static final float LOAD_FACTOR = 0.75f;

	protected LinkedHashMap data = new LinkedHashMap(MemoryStorage.PAGE_COUNT,
			MemoryStorage.LOAD_FACTOR, true) {
		private static final long serialVersionUID = -8255608399794567674L;

		protected boolean removeEldestEntry(Map.Entry eldest) {
			return this.size() > MemoryStorage.PAGE_COUNT;
		}
	};

	public synchronized void addPage(Page page) {
		this.data.put(page.getInfo(), page);
	}

	public synchronized void removePage(Page page) {
		this.data.remove(page.getInfo());
	}

	public synchronized String getComment(long revision) {
		Set entries = this.data.entrySet();
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			if (((PageInfo) entry.getKey()).check(revision) == PageInfo.CONTAINS) {
				return ((Page) entry.getValue()).getComment(revision);
			}
		}
		return null;
	}
}
