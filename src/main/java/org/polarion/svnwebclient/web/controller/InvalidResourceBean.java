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

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class InvalidResourceBean extends AbstractBean {
	public static final String INVALID_RESOURCE = "invalidResource";
	protected IncorrectParameterException.ExceptionInfo info;
	protected ILinkProvider linkRpovider;

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.info = (IncorrectParameterException.ExceptionInfo) this.state
				.getRequest().getSession()
				.getAttribute(InvalidResourceBean.INVALID_RESOURCE);
		this.state.getRequest().getSession()
				.removeAttribute(InvalidResourceBean.INVALID_RESOURCE);
		this.linkRpovider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		return true;
	}

	public String getMessage() {
		if (info != null) {
			return this.info.getMessage();
		} else {
			return "";
		}
	}

	public String getDescription() {
		if (this.info != null) {
			return this.info.getDescription();
		} else {
			return "";
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getActionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				this.linkRpovider.getDirectoryContentLink(),
				this.requestHandler.getLocation());
		if (this.requestHandler.isMultiUrlSelection()) {
			urlGenerator.addParameter(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.requestHandler.isSingleRevisionMode()) {
			urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
		}

		return urlGenerator.getUrl();
	}

	public boolean isPickerMode() {
		return this.linkRpovider.isPickerMode();
	}
}
