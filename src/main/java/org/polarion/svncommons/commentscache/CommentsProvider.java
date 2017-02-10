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
package org.polarion.svncommons.commentscache;

import org.polarion.svncommons.commentscache.storage.Page;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class CommentsProvider {
	protected static CommentsProvider instance;
	protected String userName;
	protected String password;
	protected String id;
	protected long headRevision = -1;

	protected static class SVNLogEntryHandler implements ISVNLogEntryHandler {
		protected TreeMap data = new TreeMap();

		public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
			long revision = logEntry.getRevision();
			String message = logEntry.getMessage();
			if (message == null) {
				message = "";
			}
			this.data.put(new Long(revision), message);
		}

		public TreeMap getData() {
			return this.data;
		}
	}

	private CommentsProvider() {
	}

	public static synchronized CommentsProvider getInstance() {
		if (CommentsProvider.instance == null) {
			CommentsProvider.instance = new CommentsProvider();
		}
		return CommentsProvider.instance;
	}

	public void setCredentials(String id, String userName, String password) {
		this.id = id;
		this.userName = userName;
		this.password = password;
	}

	public Page getPage(long revision, long pageSize) throws Exception {
		SVNRepository repository = null;
		try {
			repository = SVNRepositoryPool.getInstance(this.id).getRepository(
					this.userName, this.password);
			long pageStartRevision = Utils.getPageStartRevision(revision,
					pageSize);
			long pageEndRevision = Utils.getPageEndRevision(revision, pageSize);

			if (pageEndRevision > this.headRevision) {
				long latestRevision = this.updateHeadRevision(repository);
				if (pageEndRevision > latestRevision) {
					pageEndRevision = latestRevision;
				}
			}

			// SVNLogEntryHandler logHandler = new SVNLogEntryHandler();
			// repository.log(new String[] { "/" }, pageStartRevision,
			// pageEndRevision, false, false, logHandler);
			// TreeMap data = logHandler.getData();
			// return new Page(pageStartRevision, pageEndRevision, data,
			// pageSize);

			TreeMap data = this.getRevisionsMap(repository, pageStartRevision,
					pageEndRevision);
			if (data.size() == 0) {
				return null;
			} else {
				return new Page(pageStartRevision, pageEndRevision, data,
						pageSize);
			}
		} finally {
			if (repository != null) {
				SVNRepositoryPool.getInstance(this.id).releaseRepository(
						repository);
			}
		}
	}

	public Page getPage(Page page, long revision, long pageSize)
			throws Exception {
		SVNRepository repository = null;
		try {
			repository = SVNRepositoryPool.getInstance(this.id).getRepository(
					this.userName, this.password);

			long pageStartRevision = page.getInfo().getEndRevision() + 1;
			long pageEndRevision = Utils.getPageEndRevision(revision, pageSize);

			if (pageEndRevision > this.headRevision) {
				long latestRevision = this.updateHeadRevision(repository);
				if (pageEndRevision > latestRevision) {
					pageEndRevision = latestRevision;
				}
			}

			// SVNLogEntryHandler logHandler = new SVNLogEntryHandler();
			// repository.log(new String[] { "/" }, pageStartRevision,
			// pageEndRevision, false, false, logHandler);
			// TreeMap data = logHandler.getData();
			// data.putAll(page.getData());
			// return new Page(page.getInfo().getStartRevision(),
			// pageEndRevision, data, pageSize);

			TreeMap data = this.getRevisionsMap(repository, pageStartRevision,
					pageEndRevision);
			if (data.size() == 0) {
				return null;
			} else {
				data.putAll(page.getData());
				return new Page(page.getInfo().getStartRevision(),
						pageEndRevision, data, pageSize);
			}
		} finally {
			if (repository != null) {
				SVNRepositoryPool.getInstance(this.id).releaseRepository(
						repository);
			}
		}
	}

	public String getLogMessage(long revision) throws Exception {
		SVNRepository repository = null;
		try {
			repository = SVNRepositoryPool.getInstance(this.id).getRepository(
					this.userName, this.password);

			// SVNLogEntryHandler logHandler = new SVNLogEntryHandler();
			// repository.log(new String[] { "/" }, revision, revision, false,
			// false, logHandler);
			// TreeMap data = logHandler.getData();
			// return (String) data.get(data.firstKey());

			TreeMap revisionMap = this.getRevisionsMap(repository, revision,
					revision);
			return (String) revisionMap.get(new Long(revision));
		} finally {
			if (repository != null) {
				SVNRepositoryPool.getInstance(this.id).releaseRepository(
						repository);
			}
		}
	}

	public synchronized long updateHeadRevision(SVNRepository repository)
			throws SVNException {
		this.headRevision = repository.getLatestRevision();
		return this.headRevision;
	}

	protected TreeMap getRevisionsMap(SVNRepository repository,
			long pageStartRevision, long pageEndRevision) throws Exception {
		Collection colect = repository.log(new String[] { "" }, null,
				pageStartRevision, pageEndRevision, false, false);
		TreeMap res = new TreeMap();
		long expectedCount = pageEndRevision - pageStartRevision + 1;

		long counter = 0;
		for (Iterator entries = colect.iterator(); entries.hasNext();) {
			SVNLogEntry logEntry = (SVNLogEntry) entries.next();
			if (logEntry.getRevision() == 0) {
				counter++;
				continue;
			}
			String message = logEntry.getMessage();
			if (message == null) {
				return new TreeMap();
			} else {
				res.put(new Long(logEntry.getRevision()), message);
				counter++;
			}
		}
		if (counter == expectedCount) {
			return res;
		} else {
			return new TreeMap();
		}
	}
}
