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
package org.polarion.svnwebclient.web.controller.directory;

import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.Button;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.directory.DirectoryContent;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryContentBean extends BaseDirectoryContentBean {

	public DirectoryContentBean() {
	}

	protected boolean executeExtraFunctionality() {
		return true;
	}

	public DirectoryContent getDirectoryContent() {
		DirectoryContent ret = new DirectoryContent(this.directoryElement,
				this.requestHandler,
				ConfigurationProvider.getRevisionDecorator());
		ret.setState(this.state);
		ret.setHeadRevision(this.headRevision);
		ret.setUrl(this.url);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	public String getDeleteUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DELETE, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		return urlGenerator.getUrl();
	}

	public List getActions() {
		List ret = new ArrayList();
		UrlGenerator urlGenerator;

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.REVISION_LIST,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		long startRevision = -1;
		if (this.requestHandler.getCurrentRevision() != -1) {
			startRevision = this.requestHandler.getCurrentRevision();
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		} else {
			startRevision = this.headRevision;
		}

		urlGenerator.addParameter(RequestParameters.START_REVISION,
				Long.toString(startRevision));
		ret.add(new Button(urlGenerator.getUrl(), Images.REVISION_LIST,
				"Revision list"));

		if (!this.url.isEmpty() && !this.url.equals("/")) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.COMMIT_GRAPH, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.url));
			urlGenerator.addParameter(RequestParameters.PEGREV,
					Long.toString(startRevision));
			ret.add(new Button(urlGenerator.getUrl(), Images.COMMIT_GRAPH,
					"Commit graph"));

			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.STATS_ITEM, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.url));
			urlGenerator.addParameter(RequestParameters.PEGREV,
					Long.toString(startRevision));
			ret.add(new Button(urlGenerator.getUrl(), Images.STATISTICS,
					"Statistics"));
		}
		if (this.requestHandler.getCurrentRevision() == -1) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_ADD, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.ADD_DIRECTORY,
					"Add directory"));

			urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.FILE_ADD,
					this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.ADD_FILE,
					"Add file"));

			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					"javascript:checkDelete()",
					this.requestHandler.getLocation());
			ret.add(new Button(urlGenerator.getUrl(), Images.DELETE,
					"Delete selected elements"));
		}

		// download directory button
		urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DOWNLOAD_DIRECTORY, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		StringBuffer downloadAction = new StringBuffer();
		if (this.getDirectoryContent().getChilds().isEmpty()) {
			downloadAction
					.append("javascript: alert('You can not download empty directory');");
		} else {
			downloadAction.append(urlGenerator.getUrl());
		}
		ret.add(new Button(downloadAction.toString(),
				Images.DOWNLOAD_DIRECTORY, "Download current directory as ZIP"));

		return ret;
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), this.url,
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), false);
	}
}
