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
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class Utils {
	/**
	 * Returns start revision of page to which passed revision stored
	 * 
	 * @param revision
	 *            Revision number
	 * @param pageSize
	 *            Cache page size
	 * @return Start revision of page to which passed revision stored
	 */
	public static long getPageStartRevision(long revision, long pageSize) {
		return revision - (revision % pageSize);
	}

	/**
	 * Returns end revision of page to which passed revision stored
	 * 
	 * @param revision
	 *            Revision number
	 * @param pageSize
	 *            Cache page size
	 * @return End revision of page to which passed revision stored
	 */
	public static long getPageEndRevision(long revision, long pageSize) {
		return Utils.getPageStartRevision(revision, pageSize) + pageSize - 1;
	}

	public static String getRootUrl(long repoId, String url, String name,
			String password) throws Exception {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();

		SVNRepository repository = null;
		String res = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL
					.parseURIEncoded(url));
			ConfigurationProvider confProvider = SWCUtils
					.getConfigurationProvider(repoId);
			ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(name, password, SWCUtils
							.buildProtocolFromConfiguration(confProvider));
			repository.setAuthenticationManager(authManager);
			SVNURL root = repository.getRepositoryRoot(true);
			res = root.toString();
			repository.closeSession();
		} catch (SVNException e) {
			throw new CommentsCacheException(e);
		}
		return res;
	}
}
