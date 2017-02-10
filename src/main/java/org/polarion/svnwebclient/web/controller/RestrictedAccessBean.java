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
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class RestrictedAccessBean extends AbstractBean {
	protected String url;
	protected String location;
	protected ILinkProvider linkProvider;
	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;
	protected IDataProvider dataProvider;

	public RestrictedAccessBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.url = this.requestHandler.getUrl();
		this.location = this.requestHandler.getLocation();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
			}
		};
	}

	public String getFullRootPageUrl() throws ConfigurationException {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.CLEAN_EXTRA_SESSION_ATTRIBUTE,
				this.requestHandler.getLocation());
		if (ConfigurationProvider.getInstance(dataProvider.getId())
				.isMultiRepositoryMode()) {
			urlGenerator
					.addParameter(RequestParameters.LOCATION, this.location);
		}
		if (this.linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}
		return urlGenerator.getUrl();
	}

	public String getPageLocation() {
		return this.location;
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getPageUrl() {
		return this.url;
	}

	public String getRestrictLoginUrl() throws ConfigurationException {
		StringBuffer res = new StringBuffer();
		res.append(Links.RESTRICT_LOGIN);
		if (ConfigurationProvider.getInstance(dataProvider.getId())
				.isMultiRepositoryMode()) {
			res.append("?").append(RequestParameters.LOCATION).append("=")
					.append(this.location).append("&")
					.append(RequestParameters.URL).append("=");
		} else {
			res.append("?").append(RequestParameters.URL).append("=");
		}
		res.append(this.url);
		if (this.isMultiSelectionUrl) {
			res.append("&").append(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.isSingleRevision) {
			res.append("&").append(RequestParameters.SINGLE_REVISION);
		}
		return res.toString();
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
