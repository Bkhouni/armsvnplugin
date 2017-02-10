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

import com.kintosoft.jira.servlet.SWCPluginHttpRequestWrapper;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.opensymphony.util.TextUtils;
import org.jfree.util.Log;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.authorization.UserCredentials;
import org.polarion.svnwebclient.authorization.impl.CredentialsManager;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.javasvn.DataProvider;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.servlet.LogoutServlet;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SystemInitializing {
	public static final String ORIGINAL_URL = "originalUrl";
	public static final String REPOID = "repoId";
	protected State state;
	protected boolean isPickerInstance = false;
	protected boolean isSingleRevision;
	protected boolean isMultiSelectionUrl;

	public IDataProvider init(HttpServletRequest request,
			HttpServletResponse response, AbstractRequestHandler requestHandler)
			throws SVNWebClientException {

		String licError = SWCUtils.validateLicense();
		if (licError != null) {
			throw new SVNWebClientException(licError);
		}

		if (SWCUtils.requireTrackerSession()) {
			if (SWCUtils.getJIRAUser() == null) {
				throw new SVNWebClientException(
						"Please, log in JIRA prior to browse on Subversion.");
			} else {
				if (!SWCUtils.validateUserPrivilegesForSWC()) {
					// if user is not null (logged in JIRA), then it must have
					// the required JIRA privilege
					throw new SVNWebClientException(
							"You have not enough JIRA privileges to browse on Subversion.\n"
									+ "Please, contact your administrator and ask for the 'View Version Control', 'View Issue Source Tab' or 'View Development Tools' permission accordingly to your JIRA version.");

				}
			}
		}

		Long repoId = null;

		Long requestId = null;
		String paramRequestId = request.getParameter(SystemInitializing.REPOID);
		if (TextUtils.stringSet(paramRequestId)) {
			requestId = Long.parseLong(paramRequestId);
		}
		Long sessionId = (Long) request.getSession().getAttribute(
				SystemInitializing.REPOID);

		if (requestId != null) {
			if (sessionId != null) {
				if (requestId.equals(sessionId)) {
					repoId = sessionId;
				} else {
					try {
						LogoutServlet.logout(request, response);
					} catch (ServletException e) {
						Log.warn(e);
					}
					repoId = requestId;
				}
			} else {
				repoId = requestId;
			}
		} else {
			if (sessionId != null) {
				repoId = sessionId;
			} else {
				try {
					SWCPluginHttpRequestWrapper.forward(request, response,
							Links.LOGIN);
				} catch (Exception e) {
					Log.warn(e);
				}
				return null;
			}
		}

		ConfigurationProvider confProvider = ConfigurationProvider
				.getInstance(repoId);

		if (confProvider == null) {
			// Rare case when the active repository is removed but it still
			// lives in session!,
			try {
				SWCPluginHttpRequestWrapper.forward(request, response,
						Links.LOGIN);
			} catch (Exception e) {
				Log.warn(e);
			}
			return null;
		}

		if (confProvider.getConfigurationError().isError()) {
			throw new SVNWebClientException(confProvider
					.getConfigurationError().getException());
		}

		this.state = new State(request, response);
		CredentialsManager credentialsManager = new CredentialsManager();
		UserCredentials userCredentials = null;

		userCredentials = credentialsManager.getUserCredentials(confProvider,
				request, response);
		if (userCredentials == null) {
			return null;
		}

		String url = this.getRepositoryLocation(confProvider, requestHandler);

		String rootUrl;
		try {
			rootUrl = DataProvider.getID(confProvider, url,
					userCredentials.getUsername(),
					userCredentials.getPassword());
			if (ConfigurationProvider.isMultiRepositoryMode()) {
				// Should use configuration rather than user credentials????????
				DataProvider.startup(userCredentials.getUsername(),
						userCredentials.getPassword(), rootUrl, url);
			}
		} catch (Exception e) {
			throw new SVNWebClientException(e);

		}
		request.getSession().setAttribute(SystemInitializing.REPOID, repoId);
		this.checkRestrictedAccess();

		IDataProvider dataProvider = new DataProvider();
		dataProvider.setRelativeLocation(rootUrl,
				requestHandler.getRepositoryName());
		dataProvider.connect(userCredentials, rootUrl, url);
		return dataProvider;
	}

	protected void checkRestrictedAccess() {
		String originalUrl = (String) this.state.getRequest().getSession()
				.getAttribute(SystemInitializing.ORIGINAL_URL);
		if (originalUrl != null) {
			String currentUri = this.state.getRequest().getServletPath()
					.substring(1);
			if (!originalUrl.equals(currentUri)) {
				AttributeStorage.getInstance().cleanSession(
						this.state.getRequest().getSession());
			}
		}
	}

	public void redirectToRestrictPage() {
		String query = this.state.getRequest().getQueryString();
		String uri = this.state.getRequest().getServletPath();
		this.state
				.getRequest()
				.getSession()
				.setAttribute(SystemInitializing.ORIGINAL_URL, uri.substring(1));
		try {
			String restrictUrl = Links.RESTRICTED_ACCESS + "?" + query;
			if (this.isPickerInstance) {
				restrictUrl += "&" + RequestParameters.CONTENT_MODE_TYPE + "="
						+ LinkProviderFactory.PICKER_CONTENT_MODE_VALUE;
			}
			this.state.getResponse().sendRedirect(restrictUrl);
		} catch (IOException e) {
		}
	}

	protected String getRepositoryLocation(ConfigurationProvider confProvider,
			AbstractRequestHandler requestHandler) {
		return confProvider.getRepositoryLocation(requestHandler
				.getRepositoryName());
	}

	public void setIsPickerInstance() {
		this.isPickerInstance = true;
	}

	public void setPickerFields(boolean isSingleRevision,
			boolean isMultiSelectionUrl) {
		this.isSingleRevision = isSingleRevision;
		this.isMultiSelectionUrl = isMultiSelectionUrl;
	}
}
