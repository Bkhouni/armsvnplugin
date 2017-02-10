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

import org.apache.log4j.Logger;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.AuthenticationException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.SystemInitializing;
import org.polarion.svnwebclient.web.controller.InvalidResourceBean;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public abstract class AbstractServlet extends HttpServlet {

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		AbstractRequestHandler requestHandler = this.getRequestHandler(request);
		State state = new State(request, response);

		IDataProvider dataProvider = null;
		SystemInitializing initialize = new SystemInitializing();
		initialize.setPickerFields(this.isSingleRevision(requestHandler),
				this.isMultiSelectionUrl(requestHandler));

		try {
			dataProvider = initialize.init(request, response, requestHandler);
			if (dataProvider == null) {
				return;
			} else {
				this.executeSVNOperation(dataProvider, state);
			}
		} catch (AuthenticationException e) {
			initialize.redirectToRestrictPage();
			return;
		} catch (IncorrectParameterException ie) {
			request.getSession()
					.setAttribute(InvalidResourceBean.INVALID_RESOURCE,
							ie.getExceptionInfo());
			try {
				response.sendRedirect(this.getInvalidResourceUrl(
						requestHandler, state));
			} catch (IOException e) {
			}
		} catch (SVNWebClientException e) {
			Logger.getLogger(this.getClass()).error(e, e);
			throw new ServletException(e);
		} finally {
			if (dataProvider != null) {
				try {
					dataProvider.close();
				} catch (DataProviderException e) {
				}
			}
		}
	}

	protected boolean isPickerMode(State state) {
		boolean isPickerMode = false;
		if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE
				.equals(state.getRequest().getParameter(
						RequestParameters.CONTENT_MODE_TYPE))) {
			isPickerMode = true;
		}
		return isPickerMode;
	}

	protected String getInvalidResourceUrl(
			AbstractRequestHandler requestHandler, State state) {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.INVALID_RESOURCE, requestHandler.getLocation());
		if (this.isPickerMode(state)) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl(requestHandler)) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision(requestHandler)) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}

		return urlGenerator.getUrl();
	}

	protected boolean isMultiSelectionUrl(AbstractRequestHandler requestHandler) {
		return requestHandler.isMultiUrlSelection();
	}

	protected boolean isSingleRevision(AbstractRequestHandler requestHandler) {
		return requestHandler.isSingleRevisionMode();
	}

	protected abstract void executeSVNOperation(IDataProvider dataProvider,
			State state) throws SVNWebClientException;

	protected abstract AbstractRequestHandler getRequestHandler(
			HttpServletRequest request);
}
