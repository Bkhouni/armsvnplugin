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

import java.util.TreeMap;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class Page {
	protected TreeMap data;
	protected PageInfo info;

	public Page(long startRevision, long endRevision, TreeMap data,
			long pageSize) {
		this.info = new PageInfo(startRevision, endRevision, pageSize);
		this.data = data;
	}

	public TreeMap getData() {
		return this.data;
	}

	public PageInfo getInfo() {
		return this.info;
	}

	public String getComment(long revision) {
		String ret = null;
		if ((revision < this.info.getStartRevision())
				|| (revision > this.info.getEndRevision())) {
			ret = null;
		} else {
			ret = (String) this.data.get(new Long(revision));
		}
		return ret;
	}
}
