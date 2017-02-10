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
package org.polarion.svnwebclient.web.model.data;

import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.model.LinkProviderFactory;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class PathSwitchContent {
	protected DataDirectoryElement directoryElement;
	protected String prefix;
	protected String suffix;
	protected AbstractRequestHandler requestHandler;
	protected boolean isPickerMode;

	public class Element {
		protected DataRepositoryElement repositoryElement;

		public Element(DataRepositoryElement repositoryElement) {
			this.repositoryElement = repositoryElement;
		}

		public String getImage() {
			return Images.DIRECTORY;
		}

		public String getName() {
			return HtmlUtil.encode(this.repositoryElement.getName());
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.CONTENT,
					PathSwitchContent.this.requestHandler.getLocation());

			String url = PathSwitchContent.this.prefix;
			url += "/";
			url += this.repositoryElement.getName();
			url += "/";
			url += PathSwitchContent.this.suffix;

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(url));
			if (PathSwitchContent.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(PathSwitchContent.this.requestHandler
								.getCurrentRevision()));
			}
			if (isPickerMode) {
				urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
						LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
				if (requestHandler.isMultiUrlSelection()) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (requestHandler.isSingleRevisionMode()) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}
			}
			return urlGenerator.getUrl();
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.repositoryElement.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.repositoryElement
					.getRevision()));
		}

		public String getDate() {
			return DateFormatter.format(this.repositoryElement.getDate());
		}

		public String getAge() {
			return DateFormatter.format(this.repositoryElement.getDate(),
					DateFormatter.RELATIVE);
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.repositoryElement.getAuthor());
		}

		public String getFirstLine() {
			return CommentUtil
					.getFirstLine(this.repositoryElement.getComment());
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.repositoryElement.getComment());
		}

		public String getComment() {
			return HtmlUtil.encode(this.repositoryElement.getComment());
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.repositoryElement.getComment());
		}
	}

	public PathSwitchContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler, String prefix, String suffix) {
		this.directoryElement = directoryElement;
		this.requestHandler = requestHandler;
		this.prefix = prefix;
		this.suffix = suffix;
		this.isPickerMode = LinkProviderFactory.getLinkProvider(
				this.requestHandler.getContentMode()).isPickerMode();
	}

	public List getChilds() {
		List ret = new ArrayList();
		List childElements = this.directoryElement.getChildElements();
		for (Iterator i = childElements.iterator(); i.hasNext();) {
			DataRepositoryElement element = (DataRepositoryElement) i.next();
			if (element.isDirectory()) {
				ret.add(new Element(element));
			}
		}
		return ret;
	}
}