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

import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.polarion.svncommons.commentscache.authentication.SVNAuthenticationManagerFactory;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.ISVNSession;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class SVNRepositoryPool {

	private static final Logger log = LoggerFactory
			.getLogger(SVNRepositoryPool.class);

	private static Map instances = Collections.synchronizedMap(new HashMap());
	protected long size;
	protected String url;
	protected boolean initialized;
	protected String id;
	protected List repositories = new ArrayList();
	protected int activeUsersCount;

	public static synchronized void init(String id, long size, String url)
			throws CommentsCacheException {
		SVNRepositoryPool instance = (SVNRepositoryPool) instances.get(id);
		if (instance == null) {
			SVNRepositoryPool pool = null;
			try {
				pool = new SVNRepositoryPool(id, size, url);
				pool.activeUsersCount = 1;
				instances.put(id, pool);
			} catch (CommentsCacheException e) {
				throw new CommentsCacheException(e);
			}
		} else {
			instance.activeUsersCount++;
		}
	}

	public static synchronized SVNRepositoryPool getInstance(String id)
			throws CommentsCacheException {
		SVNRepositoryPool pool = (SVNRepositoryPool) instances.get(id);
		if (pool == null) {
			throw new CommentsCacheException(SVNRepositoryPool.class.getName()
					+ " must be initialized before first usage");
		}
		return pool;
	}

	private SVNRepositoryPool(String id, long size, String url)
			throws CommentsCacheException {
		this.id = id;
		this.initialized = true;
		this.url = url;
		this.size = size;

		// ISVNAuthenticationManager authManager = new
		// SVNAuthenticationManager(username, password);
		ISVNSession session = new ISVNSession() {
			public boolean keepConnection(SVNRepository arg0) {
				return true;
			}

			public void saveCommitMessage(SVNRepository arg0, long arg1,
					String arg2) {
			}

			public String getCommitMessage(SVNRepository arg0, long arg1) {
				return null;
			}

			public boolean hasCommitMessage(SVNRepository arg0, long arg1) {
				return false;
			}
		};

		try {
			for (int i = 0; i < size; i++) {
				SVNRepository repository = SVNRepositoryFactory.create(
						SVNURL.parseURIDecoded(url), session);
				// repository.setAuthenticationManager(authManager);
				this.repositories.add(repository);
			}
		} catch (SVNException e) {
			throw new CommentsCacheException(e);
		}
	}

	public synchronized SVNRepository getRepository(String username,
			String password) throws Exception {
		while (this.repositories.size() == 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}

		SVNRepository ret = (SVNRepository) this.repositories.remove(0);
		ConfigurationProvider confProvider = SWCUtils
				.getConfigurationProvider(id);
		ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
				.getSVNAuthenticationManager(username, password,
						SWCUtils.buildProtocolFromConfiguration(confProvider));
		ret.setAuthenticationManager(authManager);
		return ret;
	}

	public synchronized void releaseRepository(SVNRepository repository) {
		if (repository != null) {
			ISVNAuthenticationManager auth = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(null, null, null);
			repository.setAuthenticationManager(auth);
			this.repositories.add(repository);
			this.notifyAll();
		}
	}

	synchronized public static void terminate(String id) {
		SVNRepositoryPool pool = (SVNRepositoryPool) instances.get(id);
		if (pool == null) {
			return;
		}
		for (Iterator i = pool.repositories.iterator(); i.hasNext();) {
			SVNRepository repository = (SVNRepository) i.next();
			try {
				repository.closeSession();
			} catch (Exception ex) {
				log.warn(ex.getMessage());
			}
		}
		instances.remove(id);
	}

	synchronized public static void terminate() {
		Iterator it = instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String id = (String) entry.getKey();
			terminate(id);
		}
	}

	public void shutdown() {
		this.activeUsersCount--;
		if (this.activeUsersCount == 0) {
			for (Iterator i = this.repositories.iterator(); i.hasNext();) {
				SVNRepository repository = (SVNRepository) i.next();
				repository.closeSession();
			}
		}
	}
}
