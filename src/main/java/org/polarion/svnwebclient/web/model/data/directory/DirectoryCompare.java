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

import org.polarion.svnwebclient.data.model.DataDirectoryCompareItem;
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryCompare {
	protected List items;
	protected AbstractRequestHandler requestHandler;
	protected String url;
	protected long startRevision;
	protected long endRevision;
	protected Comparator comparator;

	public class Element {
		protected DataDirectoryCompareItem data;

		public Element(DataDirectoryCompareItem data) {
			this.data = data;
		}

		public String getImage() {
			if (this.data.isDirectory()) {
				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					return Images.DIRECTORY_ADDED;
				} else if (DataDirectoryCompareItem.OPERATION_DELETE == data
						.getOperation()) {
					return Images.DIRECTORY_DELETED;
				} else {
					return Images.DIRECTORY_MODIFIED;
				}
			} else {
				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					return Images.FILE_ADDED;
				} else if (DataDirectoryCompareItem.OPERATION_DELETE == data
						.getOperation()) {
					return Images.FILE_DELETED;
				} else {
					return Images.FILE_MODIFIED;
				}
			}
		}

		public String getName() {
			return HtmlUtil.encode(this.data.getPath());
		}

		public String getUrl() {
			if (this.data.isDirectory()) {
				return null;
			} else {
				UrlGenerator urlGenerator = UrlGeneratorFactory
						.getUrlGenerator(Links.FILE_COMPARE,
								DirectoryCompare.this.requestHandler
										.getLocation());
				String url = DirectoryCompare.this.url;
				if (url.length() != 0) {
					url += "/";
				}
				url += this.data.getPath();
				urlGenerator.addParameter(RequestParameters.URL,
						UrlUtil.encode(url));
				if (DirectoryCompare.this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator.addParameter(RequestParameters.CREV, Long
							.toString(DirectoryCompare.this.requestHandler
									.getCurrentRevision()));
				}
				if (DataDirectoryCompareItem.OPERATION_ADD != this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.STARTREV,
							Long.toString(DirectoryCompare.this.startRevision));
				}
				if (DataDirectoryCompareItem.OPERATION_DELETE != this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.ENDREV,
							Long.toString(DirectoryCompare.this.endRevision));
				}

				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.PEGREV,
							Long.toString(DirectoryCompare.this.endRevision));
				}
				if (DataDirectoryCompareItem.OPERATION_DELETE == this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.PEGREV,
							Long.toString(DirectoryCompare.this.startRevision));
				}
				return urlGenerator.getUrl();
			}
		}

		public boolean isDirectory() {
			return this.data.isDirectory();
		}
	}

	public DirectoryCompare(List items, AbstractRequestHandler requestHandler,
			String url, long startRevision, long endRevision) {
		this.items = items;
		this.requestHandler = requestHandler;
		this.url = url;
		this.startRevision = startRevision;
		this.endRevision = endRevision;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public List getElements() {
		List ret = new ArrayList();
		Collections.sort(this.items, this.comparator);
		for (Iterator i = this.items.iterator(); i.hasNext();) {
			ret.add(new Element((DataDirectoryCompareItem) i.next()));
		}
		return ret;
	}
}
