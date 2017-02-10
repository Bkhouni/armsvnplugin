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

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import org.polarion.svncommons.commentscache.storage.FileStorage;
import org.polarion.svncommons.commentscache.storage.MemoryStorage;
import org.polarion.svncommons.commentscache.storage.Page;
import org.polarion.svncommons.commentscache.storage.PageInfo;
import org.polarion.svncommons.commentscache.task.CacheTask;
import org.polarion.svncommons.commentscache.task.CacheTaskExecutor;
import org.polarion.svncommons.commentscache.task.RequestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.io.ISVNSession;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class CommentsCache implements ISVNSession {
	private static final Logger logger = LoggerFactory
			.getLogger(CommentsCache.class);
	private static Map instances = Collections.synchronizedMap(new HashMap());
	protected String id;
	protected String username;
	protected String password;
	protected String repository;

	protected static final int CACHE_THREADS = 2;
	protected static final int REQUEST_THREADS = 5;
	protected static final int ITEMS_COUNT = 100;
	protected static final float LOAD_FACTOR = 0.75f;

	protected CommentsCacheConfig config;
	protected FileStorage fileStorage;
	protected MemoryStorage memoryStorage;
	protected ExecutorService cacheExecutor;
	protected ExecutorService requestExecutor;
	protected boolean isPrefetched;

	protected static boolean isForcedHttpAuth = false;

	protected Map cache = Collections.synchronizedMap(new LinkedHashMap(
			CommentsCache.ITEMS_COUNT, CommentsCache.LOAD_FACTOR, true) {
		private static final long serialVersionUID = -3923561948621030157L;

		protected boolean removeEldestEntry(Map.Entry eldest) {
			return this.size() > CommentsCache.ITEMS_COUNT;
		}
	});

	private CommentsCache() {
	}

	public static synchronized void init(CommentsCacheConfig config, String id,
			String repository) throws CommentsCacheException {
		try {
			SVNRepositoryPool.init(id, CommentsCache.CACHE_THREADS
					+ CommentsCache.REQUEST_THREADS, repository);
			if (instances.get(id) != null) {
				return;
			}

			logger.info("Initializing comments cache. " + "Id: " + id + ", "
					+ "repository: " + repository + ", " + "url: "
					+ config.getUrl() + ", " + "cache directory: "
					+ config.getPath() + ", " + "cache page size: "
					+ config.getPageSize());

			CommentsCache instance = new CommentsCache();
			instance.id = id;
			instance.repository = repository;
			instance.config = config;
			instance.fileStorage = new FileStorage(id,
					instance.config.getPath(), instance.config.getPageSize());
			instance.memoryStorage = new MemoryStorage();
			instance.cacheExecutor = new CacheTaskExecutor(
					CommentsCache.CACHE_THREADS);
			instance.requestExecutor = Executors
					.newFixedThreadPool(CommentsCache.REQUEST_THREADS);
			instances.put(id, instance);
		} catch (Exception e) {
			throw new CommentsCacheException(e);
		}
	}

	public static synchronized CommentsCache getInstance(String id,
			String username, String password) throws CommentsCacheException {
		CommentsCache instance = (CommentsCache) CommentsCache.instances
				.get(id);
		if (instance == null) {
			throw new CommentsCacheException(CommentsCache.class.getName()
					+ " must be initialized before first usage");
		}
		instance.username = username;
		instance.password = password;
		return instance;
	}

	public String getComment(long revision) throws CommentsCacheException {
		try {
			String ret = this.getCachedComment(revision);
			if (ret == null) {
				CacheTask cacheTask = new CacheTask(this.id, this.fileStorage,
						this.memoryStorage, revision, this.config.getPageSize());
				cacheTask.setCredentials(this.username, this.password);
				this.cacheExecutor.execute(cacheTask);
				ret = (String) this.requestExecutor.submit(
						new RequestTask(this.id, this.username, this.password,
								revision)).get();
				this.cache.put(new Long(revision), ret);
			}

			return ret == null ? "" : ret;
		} catch (Exception e) {
			throw new CommentsCacheException(e);
		}
	}

	protected String getCachedComment(long revision)
			throws CommentsCacheException {
		try {
			String ret = (String) this.cache.get(new Long(revision));
			if (ret == null) {
				ret = this.memoryStorage.getComment(revision);
				if (ret == null) {
					PageInfo pageInfo = this.fileStorage.check(revision);
					if (pageInfo != null) {
						int checkResult = pageInfo.check(revision);
						if (PageInfo.CONTAINS == checkResult) {
							Page page = this.fileStorage.loadPage(pageInfo
									.getPageName());
							this.memoryStorage.addPage(page);
							ret = page.getComment(revision);
							if (ret != null) {
								this.cache.put(new Long(revision), ret);
							}
						}
					}
				}
			}
			return ret;
		} catch (Exception e) {
			throw new CommentsCacheException(e);
		}
	}

	public void prefetch(long quantity) throws CommentsCacheException {
		if (this.isPrefetched) {
			return;
		}
		try {
			SVNRepository repos = SVNRepositoryPool.getInstance(id)
					.getRepository(this.username, this.password);
			long headRevision = CommentsProvider.getInstance()
					.updateHeadRevision(repos);

			long prefetchQuantity = quantity;
			if (quantity == -1) {
				prefetchQuantity = headRevision;
			}

			long endRevision = headRevision - prefetchQuantity;
			if (endRevision < 0) {
				endRevision = 0;
			}
			long endPageRevision = Utils.getPageStartRevision(endRevision,
					this.config.getPageSize());
			long actualRevision = Utils.getPageStartRevision(headRevision,
					this.config.getPageSize());

			while (actualRevision >= endPageRevision) {
				CacheTask cacheTask = new CacheTask(this.id, this.fileStorage,
						this.memoryStorage, actualRevision,
						this.config.getPageSize());
				cacheTask.setCredentials(this.username, this.password);
				this.cacheExecutor.execute(cacheTask);
				actualRevision -= this.config.getPageSize();
			}
		} catch (Exception e) {
			throw new CommentsCacheException(e);
		}
		this.isPrefetched = true;
	}

	synchronized public static void shutdown(String id) {
		CommentsCache cache = (CommentsCache) instances.get(id);
		if (cache == null) {
			return;
		}
		try {
			cache.cacheExecutor.shutdownNow();
			cache.requestExecutor.shutdownNow();
		} catch (Exception e) {
		}
		instances.remove(id);
	}

	synchronized public static void shutdown() {
		Iterator it = instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String id = (String) entry.getKey();
			shutdown(id);
		}

	}

	public boolean keepConnection(SVNRepository repository) {
		return true;
	}

	public void saveCommitMessage(SVNRepository repository, long revision,
			String comment) {
		// this.cache.put(new Long(revision), comment);
	}

	public String getCommitMessage(SVNRepository repository, long revision) {
		try {
			return this.getCachedComment(revision);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean hasCommitMessage(SVNRepository repository, long revision) {
		// try {
		// return (this.getCachedComment(revision) != null);
		// } catch (Exception e) {
		// return false;
		// }
		return false;
	}

	// TODO: rework this ugly code. Rework approach to work with connections and
	// settings
	public static void setForcedHttpAuth(boolean isForcedHttpAuth) {
		CommentsCache.isForcedHttpAuth = isForcedHttpAuth;
	}

	public static boolean isForcedHttpAuth() {
		return CommentsCache.isForcedHttpAuth;
	}
}
