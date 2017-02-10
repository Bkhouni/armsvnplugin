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

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Actions;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class ChangedResourceServlet extends AbstractServlet {
	private static final long serialVersionUID = -5619377681883035515L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider, State state)
			throws SVNWebClientException {
		AbstractRequestHandler requestHandler = this.getRequestHandler(state
				.getRequest());

		String url = requestHandler.getUrl();
		long revision = requestHandler.getRevision();
		String action = requestHandler.getAction();
		long startRevision;
		long endRevision;
		int type;

		if (Actions.ADD.equals(action) || Actions.REPLACE.equals(action)) {
			startRevision = -1;
			endRevision = revision;
			type = dataProvider.checkUrl(url, revision);
		} else if (Actions.DELETE.equals(action)) {
			endRevision = -1;
			DataRepositoryElement dataRepositoryElement = dataProvider.getInfo(
					url, revision - 1);
			startRevision = dataRepositoryElement.getRevision();
			if (dataRepositoryElement.isDirectory()) {
				type = IDataProvider.DIRECTORY;
			} else {
				type = IDataProvider.FILE;
			}
		} else {
			endRevision = revision;
			DataRepositoryElement dataRepositoryElement = dataProvider.getInfo(
					url, revision - 1);
			startRevision = dataRepositoryElement.getRevision();
			if (dataRepositoryElement.isDirectory()) {
				type = IDataProvider.DIRECTORY;
			} else {
				type = IDataProvider.FILE;
			}
		}

		UrlGenerator urlGenerator = null;
		if (type == IDataProvider.FILE) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_COMPARE, requestHandler.getLocation());
		} else if (type == IDataProvider.DIRECTORY) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_COMPARE, requestHandler.getLocation());
		}
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		urlGenerator.addParameter(RequestParameters.PEGREV,
				Long.toString(revision));
		if (requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.STARTREV,
				Long.toString(startRevision));
		urlGenerator.addParameter(RequestParameters.ENDREV,
				Long.toString(endRevision));

		this.redirect(urlGenerator.getUrl(), state);
	}

	protected void redirect(String url, State state)
			throws SVNWebClientException {
		try {
			state.getResponse().sendRedirect(url);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
