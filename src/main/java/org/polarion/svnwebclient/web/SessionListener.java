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
 */
package org.polarion.svnwebclient.web;

import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.apache.log4j.Logger;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent arg0) {
	}

	public void sessionDestroyed(HttpSessionEvent arg0) {
		// only for multirepository
		HttpSession session = arg0.getSession();
		Long repoId = (Long) session.getAttribute(SystemInitializing.REPOID);
		if (repoId != null) {
			try {
				ConfigurationProvider confProvider = SWCUtils
						.getConfigurationProvider(repoId);
				org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool
						.getInstance(confProvider.getRootUrl()).shutdown();
				org.polarion.svncommons.commentscache.SVNRepositoryPool
						.getInstance(confProvider.getRootUrl()).shutdown();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e, e);
				e.printStackTrace();
			}
		}
	}
}
