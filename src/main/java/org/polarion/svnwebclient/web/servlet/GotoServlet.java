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
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GotoServlet extends AbstractServlet {
	private static final long serialVersionUID = -4637591501688567428L;

	protected static final String FILEPATH_PARAM = "filepath";
	protected static final String SET_REVISION_PARAM = "setRevision";
	protected static final String INPUT_REVISION_PARAM = "inputRevision";

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

		HttpServletRequest request = state.getRequest();
		String path = request.getParameter(GotoServlet.FILEPATH_PARAM);
		String setRevision = request
				.getParameter(GotoServlet.SET_REVISION_PARAM);
		long revision = -1;
		if ("HEAD".equals(setRevision)) {
			revision = -1;
		} else {
			try {
				revision = Long.parseLong(request
						.getParameter(GotoServlet.INPUT_REVISION_PARAM));
			} catch (Exception e) {
			}
		}

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.CONTENT, requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(path));
		urlGenerator.addParameter(RequestParameters.CREV,
				Long.toString(revision));

		ILinkProvider linkProvider = LinkProviderFactory
				.getLinkProvider(requestHandler.getContentMode());
		if (linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
            if (requestHandler.isMultiUrlSelection()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
		}
		try {
			state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (IOException ie) {
			throw new SVNWebClientException(ie);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
