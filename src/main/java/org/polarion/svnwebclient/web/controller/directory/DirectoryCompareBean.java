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
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataDirectoryCompareItem;
import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.RevisionDetails;
import org.polarion.svnwebclient.web.model.data.directory.DirectoryCompare;
import org.polarion.svnwebclient.web.model.sort.DirectoryCompareSortManager;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryCompareBean extends AbstractBean {
	protected long headRevision;
	protected long revision;
	protected List compareItems;
	protected DataRevision startRevision;
	protected DataRevision endRevision;
	protected DirectoryCompareSortManager sortManager;
	protected IDataProvider dataProvider;

	public DirectoryCompareBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		if (this.requestHandler.getPegRevision() != -1) {
			this.revision = this.requestHandler.getPegRevision();
		} else if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		} else {
			this.revision = this.headRevision;
		}

		this.sortManager = new DirectoryCompareSortManager();

		if (this.requestHandler.getStartRevision() != -1) {
			this.startRevision = dataProvider
					.getRevisionInfo(this.requestHandler.getStartRevision());
		}
		if (this.requestHandler.getEndRevision() != -1) {
			this.endRevision = dataProvider.getRevisionInfo(this.requestHandler
					.getEndRevision());
		}

		if ((this.requestHandler.getStartRevision() != -1)
				&& (this.requestHandler.getEndRevision() != -1)) {
			String location = dataProvider.getLocation(
					this.requestHandler.getUrl(), this.revision,
					this.requestHandler.getStartRevision());
			this.compareItems = dataProvider.compareDirectoryRevisions(
					location, this.requestHandler.getStartRevision(),
					this.requestHandler.getEndRevision());
		} else {
			if (this.requestHandler.getStartRevision() == -1) {
				this.compareItems = this.getDirectoryContent(dataProvider,
						this.requestHandler.getUrl(),
						this.requestHandler.getEndRevision(), true);
			} else if (this.requestHandler.getEndRevision() == -1) {
				this.compareItems = this.getDirectoryContent(dataProvider,
						this.requestHandler.getUrl(),
						this.requestHandler.getStartRevision(), false);
			}
		}

		return true;
	}

	protected List getDirectoryContent(IDataProvider dataProvider, String url,
			long revision, boolean added) throws DataProviderException {
		List ret = new ArrayList();
		DataDirectoryElement directory = dataProvider.getDirectory(url,
				revision, true);
		for (Iterator i = directory.getChildElements().iterator(); i.hasNext();) {
			DataRepositoryElement element = (DataRepositoryElement) i.next();
			DataDirectoryCompareItem compareItem = null;
			if (added) {
				compareItem = new DataDirectoryCompareItem(element.getName(),
						DataDirectoryCompareItem.OPERATION_ADD);
				compareItem.setDirectory(element.isDirectory());
				compareItem.setOldRevision(-1);
				compareItem.setNewRevision(revision);
			} else {
				compareItem = new DataDirectoryCompareItem(element.getName(),
						DataDirectoryCompareItem.OPERATION_DELETE);
				compareItem.setDirectory(element.isDirectory());
				compareItem.setOldRevision(revision);
				compareItem.setNewRevision(-1);
			}
			ret.add(compareItem);
		}
		return ret;
	}

	public RevisionDetails getStartRevisionInfo() {
		if (this.startRevision == null) {
			return null;
		}
		return new RevisionDetails(this.startRevision, this.headRevision, null,
				null, null);
	}

	public RevisionDetails getEndRevisionInfo() {
		if (this.endRevision == null) {
			return null;
		}
		return new RevisionDetails(this.endRevision, this.headRevision, null,
				null, null);
	}

	public String getStartRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV,
				Long.toString(this.requestHandler.getStartRevision()));
		return urlGenerator.getUrl();
	}

	public String getEndRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV,
				Long.toString(this.requestHandler.getEndRevision()));
		return urlGenerator.getUrl();
	}

	public DirectoryCompare getCompareResult() {
		DirectoryCompare ret = new DirectoryCompare(this.compareItems,
				this.requestHandler, this.requestHandler.getUrl(),
				this.requestHandler.getStartRevision(),
				this.requestHandler.getEndRevision());
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.STARTREV);
				this.checkLong(RequestParameters.ENDREV);
			}
		};
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), true);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
