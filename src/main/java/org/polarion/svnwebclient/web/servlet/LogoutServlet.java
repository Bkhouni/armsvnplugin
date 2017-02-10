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
package org.polarion.svnwebclient.web.servlet;

import com.kintosoft.jira.servlet.SWCPluginHttpRequestWrapper;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.polarion.svnwebclient.authorization.impl.CredentialsManager;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.web.SystemInitializing;
import org.polarion.svnwebclient.web.resource.Links;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = -4086979556233654490L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		logout(request, response);

		SWCPluginHttpRequestWrapper.forward(request, response,
				Links.DIRECTORY_CONTENT);
	}

	public static void logout(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		HttpSession session = request.getSession();
		Long repoId = (Long) session.getAttribute(SystemInitializing.REPOID);

		session.setAttribute(SystemInitializing.REPOID, null);
		session.setAttribute(CredentialsManager.CREDENTIALS, null);
		try {
			if (repoId != null && ConfigurationProvider.isMultiRepositoryMode()) {
				ConfigurationProvider confProvider = SWCUtils
						.getConfigurationProvider(repoId);

				org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool
						.getInstance(confProvider.getRootUrl()).shutdown();
				org.polarion.svncommons.commentscache.SVNRepositoryPool
						.getInstance(confProvider.getRootUrl()).shutdown();

			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
