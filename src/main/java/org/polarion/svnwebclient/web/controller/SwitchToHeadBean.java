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
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.SwitchToHead;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class SwitchToHeadBean extends AbstractBean {
	protected long headRevision;
	protected String originalUrl;
	protected String urlInRevision;
	protected String urlInHead;
	protected boolean redirectToDirectory;
	protected DataRepositoryElement repositoryElement;
	protected ILinkProvider linkProvider;
	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;
	protected IDataProvider dataProvider;

	public SwitchToHeadBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.urlInHead = "";
		this.urlInRevision = "";

		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();

		this.headRevision = dataProvider.getHeadRevision();
		this.originalUrl = this.requestHandler.getUrl();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());

		this.repositoryElement = dataProvider.getInfo(this.urlInRevision,
				this.requestHandler.getPegRevision());

		List pathChain = UrlUtil.getPathChain(this.originalUrl);
		boolean found = false;
		for (int i = pathChain.size() - 1; i >= 0; i--) {
			try {
				this.urlInRevision = (String) pathChain.get(i);
				this.urlInHead = dataProvider
						.getLocation(this.urlInRevision,
								this.requestHandler.getPegRevision(),
								this.headRevision);
				found = true;
				break;
			} catch (DataProviderException e) {
				this.urlInHead = null;
				this.urlInRevision = null;
			}
		}

		if (this.originalUrl.equals(this.urlInRevision)) {
			DataRepositoryElement repositoryElement = dataProvider.getInfo(
					this.urlInHead, this.headRevision);
			if (repositoryElement.isDirectory()) {
				this.redirect(this.linkProvider.getDirectoryContentLink(),
						this.urlInHead);
			} else {
				this.redirect(this.linkProvider.getFileContentLink(),
						this.urlInHead);
			}
			return false;
		}

		if (found) {
			DataRepositoryElement repositoryElement = dataProvider.getInfo(
					this.urlInHead, this.headRevision);
            this.redirectToDirectory = repositoryElement.isDirectory();
		}
		return true;
	}

	protected void redirect(String base, String url)
			throws SVNWebClientException {
		try {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					base, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(url));
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new AbstractRequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.PEGREV);
			}
		};

	}

	public SwitchToHead getSwitchToHead() {
		SwitchToHead switchToHead = new SwitchToHead(
				this.requestHandler.getLocation(), this.originalUrl,
				this.urlInRevision, this.urlInHead, this.redirectToDirectory);
		switchToHead.setHeadRevision(this.headRevision);
		switchToHead.setRevision(this.requestHandler.getPegRevision());
		return switchToHead;
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), this.originalUrl,
				this.requestHandler.getLocation(),
				this.requestHandler.getPegRevision(),
				!this.repositoryElement.isDirectory(), this.linkProvider,
				this.isSingleRevision, this.isMultiSelectionUrl);
	}

	public List getActions() {
		return null;
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
