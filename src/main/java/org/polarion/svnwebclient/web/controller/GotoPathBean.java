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
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.Link;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class GotoPathBean extends AbstractBean {

	protected String path;
	protected Navigation navigation;
	protected ILinkProvider linkProvider;
	protected boolean isMultiUrlSelection;

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.path = this.requestHandler.getUrl();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		this.isMultiUrlSelection = this.requestHandler.isMultiUrlSelection();

		this.navigation = new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), linkProvider, false,
				isMultiUrlSelection);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public String getPath() {
		return this.path;
	}

	public Navigation getNavigation() {
		return this.navigation;

	}

	public List getActions() {
		return null;
	}

	public String getCancelUrl() {
		List navigationPath = this.navigation.getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public String getGotoUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.GOTO, this.requestHandler.getLocation());
		if (this.linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiUrlSelection) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
		}
		return urlGenerator.getUrl();
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
