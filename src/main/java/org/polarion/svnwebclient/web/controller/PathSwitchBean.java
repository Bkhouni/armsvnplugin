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
import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.*;
import org.polarion.svnwebclient.web.model.data.PathSwitchContent;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class PathSwitchBean extends AbstractBean {
	protected long revision;
	protected long headRevision;
	protected DataDirectoryElement directoryElement;
	protected String prefix;
	protected ILinkProvider linkProvider;

	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;
	protected IDataProvider dataProvider;

	public PathSwitchBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();

		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());

		if (PathSwitch.TRUNK.equals(this.requestHandler.getType())) {
			String trunkUrl = this.requestHandler.getUrlPrefix()
					+ "/"
					+ ConfigurationProvider.getInstance(dataProvider.getId())
							.getTrunkName();
			String url = trunkUrl + "/" + this.requestHandler.getUrlSuffix();

			int resourceType = dataProvider.checkUrl(url, this.revision);
			if (resourceType == IDataProvider.NOT_EXIST) {
				String redirectPage = trunkUrl;
				int resType = dataProvider.checkUrl(trunkUrl, this.revision);
				if (resType == IDataProvider.NOT_EXIST) {
					redirectPage = this.requestHandler.getUrlPrefix();
				}
				UrlGenerator urlGenerator = UrlGeneratorFactory
						.getUrlGenerator(
								this.linkProvider.getDirectoryContentLink(),
								this.requestHandler.getLocation());
				urlGenerator.addParameter(RequestParameters.URL,
						UrlUtil.encode(redirectPage));
				if (this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator
							.addParameter(RequestParameters.CREV, Long
									.toString(this.requestHandler
											.getCurrentRevision()));
				}
				if (this.isMultiSelectionUrl) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (this.isSingleRevision) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}

				this.redirect(urlGenerator.getUrl());
				return false;
			} else {
				UrlGenerator urlGenerator;
				if (resourceType == IDataProvider.DIRECTORY) {
					urlGenerator = UrlGeneratorFactory.getUrlGenerator(
							this.linkProvider.getDirectoryContentLink(),
							this.requestHandler.getLocation());
				} else {
					urlGenerator = UrlGeneratorFactory.getUrlGenerator(
							this.linkProvider.getFileContentLink(),
							this.requestHandler.getLocation());
				}
				urlGenerator.addParameter(RequestParameters.URL,
						UrlUtil.encode(url));
				if (this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator
							.addParameter(RequestParameters.CREV, Long
									.toString(this.requestHandler
											.getCurrentRevision()));
				}
				if (this.isMultiSelectionUrl) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (this.isSingleRevision) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}
				this.redirect(urlGenerator.getUrl());
				return false;
			}
		} else {
			this.prefix = this.requestHandler.getUrlPrefix() + "/";
			if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
				this.prefix += ConfigurationProvider.getInstance(
						dataProvider.getId()).getBranchesName();
			} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
				this.prefix += ConfigurationProvider.getInstance(
						dataProvider.getId()).getTagsName();
			}

			int resourceType = dataProvider
					.checkUrl(this.prefix, this.revision);
			if (resourceType == IDataProvider.NOT_EXIST) {
				this.directoryElement = null;
			} else {
				this.directoryElement = dataProvider.getDirectory(this.prefix,
						this.revision);
			}
		}

		return true;
	}

	protected void redirect(String url) throws SVNWebClientException {
		try {
			this.state.getResponse().sendRedirect(url);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	public PathSwitchContent getSwitchContent() {
		if (this.directoryElement == null) {
			return null;
		} else {
			return new PathSwitchContent(this.directoryElement,
					this.requestHandler, this.prefix,
					this.requestHandler.getUrlSuffix());
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNull(RequestParameters.PREFIX);
				this.checkNotNull(RequestParameters.SUFFIX);
				this.checkNotNullOrEmpty(RequestParameters.TYPE);
			}
		};
	}

	public String getName() {
		String ret = null;
		if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
			ret = "branch";
		} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
			ret = "tag";
		}
		return ret;
	}

	public String getPluralName() {
		String ret = null;
		if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
			ret = "branches";
		} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
			ret = "tags";
		}
		return ret;
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), this.linkProvider,
				this.isSingleRevision, this.isMultiSelectionUrl);
	}

	public String getCancelUrl() throws ConfigurationException {
		List navigationPath = this.getNavigation().getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		String returnUrl = lastElement.getUrl();
		int index = returnUrl.indexOf("?");
		if (index == -1) {
			returnUrl += "?";
		}
		if (this.linkProvider.isPickerMode()) {
			returnUrl += "&" + RequestParameters.CONTENT_MODE_TYPE + "="
					+ LinkProviderFactory.PICKER_CONTENT_MODE_VALUE;

			if (this.isMultiSelectionUrl) {
				returnUrl += "&" + RequestParameters.MULTI_URL_SELECTION;
			}
			if (this.isSingleRevision) {
				returnUrl += "&" + RequestParameters.SINGLE_REVISION;
			}
		}

		return returnUrl;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
