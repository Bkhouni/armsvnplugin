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
package org.polarion.svnwebclient.web.controller;

import org.apache.log4j.Logger;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.AuthenticationException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.SystemInitializing;
import org.polarion.svnwebclient.web.controller.directory.PickerDirectoryContentBean;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.model.PageInfo;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.servlet.LogoutServlet;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public abstract class AbstractBean implements IBean {
	protected State state;
	protected AbstractRequestHandler requestHandler;

	public boolean execute(HttpServletRequest request,
			HttpServletResponse response) throws SVNWebClientException {
		this.state = new State(request, response);
		this.requestHandler = this.getRequestHandler(request);
		IDataProvider dataProvider = null;
		SystemInitializing initialize = new SystemInitializing();
		if (this.isPickerInstance()) {
			initialize.setIsPickerInstance();
			initialize.setPickerFields(this.isSingleRevision(),
					this.isMultiSelectionUrl());
		}

		try {
			dataProvider = initialize.init(request, response,
					this.requestHandler);
			if (dataProvider == null) {
				return false;
			} else {
				return this.executeSVNOperation(dataProvider);
			}
		} catch (AuthenticationException e) {
			initialize.redirectToRestrictPage();
			return false;
		} catch (IncorrectParameterException ie) {
			request.getSession()
					.setAttribute(InvalidResourceBean.INVALID_RESOURCE,
							ie.getExceptionInfo());
			try {
				response.sendRedirect(this.getInvalidResourceUrl());
			} catch (IOException e) {
			}
			return false;
		} catch (SVNWebClientException e) {
			Logger.getLogger(this.getClass()).error(e, e);
			try {
				LogoutServlet.logout(request, response);
				response.sendRedirect(getErrorUrl(e.getMessage()));
			} catch (Exception e1) {
			}
			return false;
		} finally {
			if (dataProvider != null) {
				try {
					dataProvider.close();
				} catch (DataProviderException e) {
				}
			}
		}
	}

	public PageInfo getCurrentPageInfo() {
		String servletPath = (String) this.state.getRequest().getAttribute(
				"javax.servlet.forward.servlet_path");
		if (servletPath == null) {
			servletPath = this.state.getRequest().getServletPath();
		}

		String queryString = (String) this.state.getRequest().getAttribute(
				"javax.servlet.forward.query_string");
		if (queryString == null) {
			queryString = this.state.getRequest().getQueryString();
		}

		PageInfo pageInfo = new PageInfo();
		pageInfo.init(servletPath, queryString);
		return pageInfo;
	}

	protected boolean isPickerMode() {
		boolean isPickerMode = false;
		if (this.isPickerInstance()) {
			isPickerMode = true;
		} else if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE
				.equals(this.state.getRequest().getParameter(
						RequestParameters.CONTENT_MODE_TYPE))) {
			isPickerMode = true;
		}
		return isPickerMode;
	}

	protected boolean isPickerInstance() {
		boolean isPickerInstance = false;
		if (this instanceof PickerDirectoryContentBean) {
			isPickerInstance = true;
		}
		return isPickerInstance;
	}

	protected String getInvalidResourceUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.INVALID_RESOURCE, this.requestHandler.getLocation());
		if (this.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isSingleRevision()) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			if (this.isMultiSelectionUrl()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
		}
		return urlGenerator.getUrl();
	}

	protected String getErrorUrl(String description) {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.ERROR, this.requestHandler.getLocation());

		urlGenerator.addParameter("errormessage", "The page cannot be loaded");
		urlGenerator.addParameter("description", description);

		return urlGenerator.getUrl();
	}

	protected boolean isMultiSelectionUrl() {
		return this.requestHandler.isMultiUrlSelection();
	}

	protected boolean isSingleRevision() {
		return this.requestHandler.isSingleRevisionMode();
	}

	protected abstract boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException;

	protected abstract AbstractRequestHandler getRequestHandler(
			HttpServletRequest request);
}
