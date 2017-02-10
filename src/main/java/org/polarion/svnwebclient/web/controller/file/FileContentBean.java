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
package org.polarion.svnwebclient.web.controller.file;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataFileElement;
import org.polarion.svnwebclient.decorations.BaseViewProvider;
import org.polarion.svnwebclient.decorations.IAlternativeViewProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.util.contentencoding.ContentEncodingHelper;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Button;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.file.FileContent;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileContentBean extends AbstractBean {
	public static final String FILE_CONTENT = "filecontent";

	protected long headRevision;
	protected long revision;
	protected long line;
	protected String url;
	protected DataFileElement fileElement;
	protected IDataProvider dataProvider;

	public FileContentBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.line = this.requestHandler.getLine();
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.revision);
		}
		this.fileElement = (DataFileElement) dataProvider.getInfo(this.url,
				this.revision);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNullOrEmpty(RequestParameters.URL);
			}
		};
	}

	public FileContent getFileContent() {
		return new FileContent(this.fileElement, this.requestHandler,
				this.headRevision, this.url);
	}

	public Map getViews() {
		Map ret = new HashMap();

		BaseViewProvider baseViewProvider = new BaseViewProvider(
				this.requestHandler.getLocation());
		String[] baseViews = baseViewProvider.getAvailableAlternativeViews(
				this.url, this.revision);
		if (baseViews != null) {
			for (int i = 0; i < baseViews.length; i++) {
				ret.put(baseViews[i], baseViewProvider
						.getAlternativeViewContentUrl(this.url, this.revision,
								line, baseViews[i]));
			}
		}

		IAlternativeViewProvider alternativeViewProvider = ConfigurationProvider
				.getAlternativeViewProvider();
		String[] alternativeViews = alternativeViewProvider
				.getAvailableAlternativeViews(this.url, this.revision);
		if (alternativeViews != null) {
			for (int i = 0; i < alternativeViews.length; i++) {
				ret.put(alternativeViews[i], alternativeViewProvider
						.getAlternativeViewContentUrl(this.url, this.revision,
								line, alternativeViews[i]));
			}
		}

		return ret;
	}

	public String getSelectedView() {
		String ret = this.requestHandler.getView();
		if (ret == null) {
			ret = BaseViewProvider.CONTENT;
		}
		return ret;
	}

	public String getSelectedViewUrl() {
		return (String) this.getViews().get(this.getSelectedView());
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), this.url,
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), true);
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

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.COMMIT_GRAPH,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		urlGenerator.addParameter(RequestParameters.PEGREV,
				Long.toString(startRevision));
		ret.add(new Button(urlGenerator.getUrl(), Images.COMMIT_GRAPH,
				"Commit graph"));

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.STATS_ITEM,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		urlGenerator.addParameter(RequestParameters.PEGREV,
				Long.toString(startRevision));
		ret.add(new Button(urlGenerator.getUrl(), Images.STATISTICS,
				"Statistics"));

		if (this.requestHandler.getCurrentRevision() == -1) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_UPDATE, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.UPDATE, "Commit"));
		}

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.FILE_DOWNLOAD,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.ATTACHMENT,
				RequestParameters.VALUE_TRUE);
		ret.add(new Button(urlGenerator.getUrl(), Images.DOWNLOAD, "Download"));

		return ret;
	}

	public Collection getCharacterEncodings() throws ConfigurationException {
		return ContentEncodingHelper
				.getCharacterEncodings(dataProvider.getId());
	}

	public boolean isSelectedCharacterEncoding(String encoding)
			throws ConfigurationException {
		return ContentEncodingHelper.isSelectedCharacterEncoding(
				dataProvider.getId(), this.state, encoding);
	}
}
