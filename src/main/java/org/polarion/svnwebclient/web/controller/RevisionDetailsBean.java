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
import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.RevisionDetails;
import org.polarion.svnwebclient.web.model.sort.RevisionDetailsSortManager;
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
public class RevisionDetailsBean extends AbstractBean {
	protected DataRevision dataRevision;
	protected long headRevision;
	protected RevisionDetailsSortManager sortManager;
	protected IDataProvider dataProvider;

	public RevisionDetailsBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.sortManager = new RevisionDetailsSortManager();
		this.headRevision = dataProvider.getHeadRevision();
		this.dataRevision = dataProvider.getRevisionInfo(this.requestHandler
				.getRevision());
		return true;
	}

	public RevisionDetails getRevision() throws ConfigurationException {
		RevisionDetails ret = new RevisionDetails(this.dataRevision,
				this.headRevision, ConfigurationProvider.getInstance(
						dataProvider.getId()).getRevisionDecorator(),
				this.state, this.requestHandler);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.REV);
			}
		};
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), false);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
