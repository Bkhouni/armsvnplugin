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
package org.polarion.svnwebclient.authorization.impl;

import com.kintosoft.jira.servlet.SWCPluginHttpRequestWrapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.authorization.ICredentialsManager;
import org.polarion.svnwebclient.authorization.UserCredentials;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.javasvn.DataProvider;
import org.polarion.svnwebclient.web.SystemInitializing;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.servlet.LogoutServlet;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.tmatesoft.svn.core.SVNException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.StringTokenizer;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class CredentialsManager implements ICredentialsManager {
	public static final String CREDENTIALS = "credentials";
	private ConfigurationProvider confProvider;

	public UserCredentials getUserCredentials(
			ConfigurationProvider confProvider, HttpServletRequest request,
			HttpServletResponse response) throws SVNWebClientException {
		UserCredentials credentials = null;
		try {
			credentials = (UserCredentials) request.getSession().getAttribute(
					CredentialsManager.CREDENTIALS);
		} catch (Exception e) {
			Log.warn(e);
			try {
				LogoutServlet.logout(request, response);
			} catch (ServletException e1) {
			}
			return null;
		}
		this.confProvider = confProvider;
		String username = null;
		String password = null;
		AbstractRequestHandler requestHandler = this.getRequestHandler(request);

		if (credentials == null) {
			// BasicAuth mode
			if (confProvider.isBasicAuth()
					&& this.isBasicAuthentication(request)) {
				UserCredentials basicCredentials = this
						.getBasicAuthenticationCredentials(request);
				username = basicCredentials.getUsername();
				password = basicCredentials.getPassword();
			} else {
				username = requestHandler.getUsername();
				password = requestHandler.getPassword();
			}

			// download manager calls
			if (this.isBasicAuthentication(request)) {
				UserCredentials basicCredentials = this
						.getBasicAuthenticationCredentials(request);
				if (basicCredentials != null) {
					username = basicCredentials.getUsername();
					password = basicCredentials.getPassword();
				}
			}

			credentials = new UserCredentials(username, password);

			String url = this.getRepositoryLocation(requestHandler);
			try {
				DataProvider.verify(confProvider, url,
						credentials.getUsername(), credentials.getPassword());
				request.getSession().setAttribute(
						CredentialsManager.CREDENTIALS, credentials);
				if ("".equals(password)
						&& request.getSession().getAttribute(
								ICredentialsManager.IS_LOGGED_IN) != null) {
					this.forceCredentialsRequest(request, response);
					return null;
				} else {
					request.getSession().setAttribute(
							ICredentialsManager.IS_LOGGED_IN, "exist");
				}
			} catch (SVNException se) {
				Logger.getLogger(this.getClass()).debug(
						"It's not allowed to enter, your credentials:\t"
								+ "username: " + credentials.getUsername()
								+ ", " + "url: " + url);
				try {
					SWCPluginHttpRequestWrapper.forward(request, response,
							Links.LOGIN);
				} catch (Exception e) {
					Log.warn(e);
				}
				return null;
			}
		}

		Logger.getLogger(this.getClass()).debug(
				"Credentials: \nUsername: " + credentials.getUsername());
		return credentials;
	}

	protected boolean isBasicAuthentication(HttpServletRequest request) {
		String auth = request.getHeader("Authorization");
        return auth != null && !auth.equals("")
                && auth.toLowerCase().startsWith("basic");
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected String getRepositoryLocation(AbstractRequestHandler requestHandler) {
		String res = "";
		if (!ConfigurationProvider.isMultiRepositoryMode()) {
			res = confProvider.getRepositoryUrl();
		} else {
			res += confProvider.getParentRepositoryDirectory() + "/"
					+ requestHandler.getRepositoryName();
		}
		return res;
	}

	protected UserCredentials getBasicAuthenticationCredentials(
			HttpServletRequest request) {
		UserCredentials res = null;
		String auth = request.getHeader("Authorization");
		auth = auth.substring(auth.lastIndexOf(" ") + 1, auth.length());
		String authStr = new String(Base64.decodeBase64(auth.getBytes()));
		StringTokenizer stringtokenizer = new StringTokenizer(authStr, ":");
		if (stringtokenizer.hasMoreTokens()) {
			res = new UserCredentials();
			res.setUsername(stringtokenizer.nextToken().toLowerCase());
		}
		if (stringtokenizer.hasMoreTokens()) {
			res.setPassword(stringtokenizer.nextToken());
		}
		return res;
	}

	protected void forceCredentialsRequest(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().setAttribute(CredentialsManager.CREDENTIALS, null);
		request.getSession().setAttribute(SystemInitializing.REPOID, null);
		try {
			if (confProvider.isBasicAuth()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setHeader("WWW-Authenticate", "BASIC realm=\""
						+ confProvider.getBasicRealm() + "\"");
				response.sendError(401);
			} else {
				RequestDispatcher dispatcher = request
						.getRequestDispatcher(Links.LOGIN);
				dispatcher.forward(request, response);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e, e);
		}
	}

}
