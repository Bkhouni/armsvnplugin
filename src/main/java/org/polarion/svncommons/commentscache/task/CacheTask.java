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

import org.polarion.svncommons.commentscache.CommentsProvider;
import org.polarion.svncommons.commentscache.Utils;
import org.polarion.svncommons.commentscache.storage.FileStorage;
import org.polarion.svncommons.commentscache.storage.MemoryStorage;
import org.polarion.svncommons.commentscache.storage.Page;
import org.polarion.svncommons.commentscache.storage.PageInfo;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class CacheTask implements Runnable {
	protected FileStorage fileStorage;
	protected MemoryStorage memoryStorage;
	protected long revision;
	protected long pageSize;
	protected String id;
	protected String userName;
	protected String password;

	public CacheTask(String id, FileStorage fileStorage,
			MemoryStorage memoryStorage, long revision, long pageSize) {
		this.id = id;
		this.fileStorage = fileStorage;
		this.memoryStorage = memoryStorage;
		this.revision = revision;
		this.pageSize = pageSize;
	}

	public void setCredentials(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getTaskId() {
		long pageStartRevision = Utils.getPageStartRevision(this.revision,
				this.pageSize);
		long pageEndRevision = Utils.getPageEndRevision(this.revision,
				this.pageSize);
		return pageStartRevision + "-" + pageEndRevision;
	}

	public void run() {
		try {
			PageInfo pageInfo = this.fileStorage.check(this.revision);
			if (pageInfo == null) {
				CommentsProvider provider = CommentsProvider.getInstance();
				provider.setCredentials(this.id, this.userName, this.password);
				Page page = provider.getPage(this.revision, this.pageSize);
				if (page != null) {
					this.fileStorage.savePage(page);
					this.memoryStorage.addPage(page);
				}
			} else {
				int checkResult = pageInfo.check(this.revision);
				if (PageInfo.SAME_PAGE == checkResult) {
					Page oldPage = this.fileStorage.loadPage(pageInfo
							.getPageName());
					CommentsProvider provider = CommentsProvider.getInstance();
					provider.setCredentials(this.id, this.userName,
							this.password);
					Page page = provider.getPage(oldPage, this.revision,
							this.pageSize);
					if (page != null) {
						this.fileStorage.deletePage(pageInfo.getPageName());
						this.fileStorage.savePage(page);
						this.memoryStorage.addPage(page);
					}
				}
			}
		} catch (Exception e) {
			// TODO process exception
		}
	}
}
