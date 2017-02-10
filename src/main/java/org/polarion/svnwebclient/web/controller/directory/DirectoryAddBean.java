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

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Link;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryAddBean extends AbstractBean {
	protected static final long CURRENT_REVISION = -1;

	protected Navigation navigation;
	protected String url;
	protected String id;

	public DirectoryAddBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(),
					dataProvider.getHeadRevision());
		}
		this.navigation = new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				DirectoryAddBean.CURRENT_REVISION, false);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public String getOkUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DIRECTORY_ADD_ACTION, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		return urlGenerator.getUrl();
	}

	public String getCancelUrl() {
		List navigationPath = this.navigation.getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public Navigation getNavigation() {
		return this.navigation;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
