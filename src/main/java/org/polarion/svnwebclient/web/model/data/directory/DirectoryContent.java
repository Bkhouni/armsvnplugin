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
package org.polarion.svnwebclient.web.model.data.directory;

import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryContent extends BaseDirectoryContent {

	public DirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		super(directoryElement, requestHandler, revisionDecorator);
	}

	public class Element extends BaseDirectoryContent.BaseElement {

		public Element(DataRepositoryElement repositoryElement) {
			super(repositoryElement);
		}

		public String getRevisionUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION, requestHandler.getLocation());
			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV,
						Long.toString(requestHandler.getCurrentRevision()));
			}
			urlGenerator
					.addParameter(RequestParameters.REV, this.getRevision());
			return urlGenerator.getUrl();
		}

		public String getCommitGraphUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.COMMIT_GRAPH, requestHandler.getLocation());
			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV,
						Long.toString(requestHandler.getCurrentRevision()));
			}
			return urlGenerator.getUrl();
		}

		public String getStatisticsUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.STATS_ITEM, requestHandler.getLocation());
			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV,
						Long.toString(requestHandler.getCurrentRevision()));
			}
			return urlGenerator.getUrl();
		}

		public String getRevisionListUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());

			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			long startRevision = -1;
			if (requestHandler.getCurrentRevision() != -1) {
				startRevision = requestHandler.getCurrentRevision();
				urlGenerator.addParameter(RequestParameters.CREV,
						Long.toString(requestHandler.getCurrentRevision()));
			} else {
				startRevision = headRevision;
			}
			urlGenerator.addParameter(RequestParameters.START_REVISION,
					Long.toString(startRevision));
			return urlGenerator.getUrl();
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator;
			if (this.repositoryElement.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT, requestHandler.getLocation());
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT, requestHandler.getLocation());
			}

			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV,
						Long.toString(requestHandler.getCurrentRevision()));
			}
			return urlGenerator.getUrl();
		}
	}

	public List getChilds() {
		List ret = new ArrayList();
		List childElements = this.directoryElement.getChildElements();
		Collections.sort(childElements, this.comparator);
		for (Iterator i = childElements.iterator(); i.hasNext();) {
			ret.add(new Element((DataRepositoryElement) i.next()));
		}
		return ret;
	}
}
