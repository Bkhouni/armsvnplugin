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

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class PageInfo {
	public static final int WRONG_PAGE = 0;
	public static final int CONTAINS = 1;
	public static final int SAME_PAGE = 2;

	public static final String SPLITTER = "-";

	protected long startRevision;
	protected long endRevision;
	protected long pageSize;

	public PageInfo(long startRevision, long endRevision, long pageSize) {
		this.startRevision = startRevision;
		this.endRevision = endRevision;
		this.pageSize = pageSize;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}

        return (this.startRevision == ((PageInfo) obj).startRevision)
                && (this.endRevision == ((PageInfo) obj).endRevision);
	}

	public int hashCode() {
		int ret = 7;
		ret = (31 * ret)
				+ ((int) (this.startRevision ^ (this.startRevision >>> 32)));
		ret = (31 * ret)
				+ ((int) (this.endRevision ^ (this.endRevision >>> 32)));
		return ret;
	}

	public long getStartRevision() {
		return this.startRevision;
	}

	public long getEndRevision() {
		return this.endRevision;
	}

	public String getPageName() {
		return this.startRevision + PageInfo.SPLITTER + this.endRevision;
	}

	public int check(long revision) {
		if (revision < this.startRevision) {
			return PageInfo.WRONG_PAGE;
		} else if (revision <= this.endRevision) {
			return PageInfo.CONTAINS;
		} else if (revision < (this.startRevision + this.pageSize)) {
			return PageInfo.SAME_PAGE;
		} else {
			return PageInfo.WRONG_PAGE;
		}
	}

	public static long getPageStartRevision(String name) {
		long ret = -1;
		int index = name.indexOf(PageInfo.SPLITTER);
		if (index != -1) {
			String value = name.substring(0, index);
			try {
				ret = Long.parseLong(value);
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public static long getPageEndRevision(String name) {
		long ret = -1;
		int index = name.lastIndexOf(PageInfo.SPLITTER);
		if (index != -1) {
			String value = name.substring(index + PageInfo.SPLITTER.length());
			try {
				ret = Long.parseLong(value);
			} catch (Exception e) {
			}
		}
		return ret;
	}
}
