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

import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.resource.Actions;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class RevisionDetails {
	protected DataRevision revision;
	protected long headRevision;
	protected IRevisionDecorator revisionDecorator;
	protected State state;
	protected Comparator comparator;
	protected AbstractRequestHandler requestHandler;

	public class Element {
		protected DataRevision.ChangedElement element;

		public Element(DataRevision.ChangedElement element) {
			this.element = element;
		}

		public String getName() {
			return HtmlUtil.encode(this.element.getPath());
		}

		public String getCopyPath() {
			String path = this.element.getCopyPath();
			if (path != null) {
				if (path.startsWith("/") && path.length() > 1) {
					path = path.substring(1);
				}
				return HtmlUtil.encode(path);
			} else {
				return "";
			}
		}

		public String getCopyUrl() {
			StringBuffer result = new StringBuffer(Links.REVISION);
			result.append("?url=").append(this.getCopyPath());
			result.append("&rev=").append(this.getCopyRevision());
			return result.toString();
		}

		public String getCopyRevision() {
			long revision = element.getCopyRevision();
			return (revision == -1 ? "" : Long.toString(revision));
		}

		public String getImage() {
			if (this.element.getType() == DataRevision.TYPE_ADDED) {
				return Images.RESOURCE_ADDED;
			} else if (this.element.getType() == DataRevision.TYPE_DELETED) {
				return Images.RESOURCE_DELETED;
			} else if (this.element.getType() == DataRevision.TYPE_MODIFIED) {
				return Images.RESOURCE_MODIFIED;
			} else {
				return Images.RESOURCE_ADDED;
			}
		}

		public String getUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.CHANGED_RESOURCE,
					RevisionDetails.this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.element.getPath()));
			if (RevisionDetails.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(RevisionDetails.this.requestHandler
								.getCurrentRevision()));
			}
			urlGenerator.addParameter(RequestParameters.REV,
					Long.toString(RevisionDetails.this.revision.getRevision()));
			if (this.element.getType() == DataRevision.TYPE_ADDED) {
				urlGenerator
						.addParameter(RequestParameters.ACTION, Actions.ADD);
			} else if (this.element.getType() == DataRevision.TYPE_DELETED) {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.DELETE);
			} else if (this.element.getType() == DataRevision.TYPE_MODIFIED) {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.MODIFY);
			} else {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.REPLACE);
			}
			return urlGenerator.getUrl();
		}
	}

	public RevisionDetails(DataRevision revision, long headRevision,
			IRevisionDecorator revisionDecorator, State state,
			AbstractRequestHandler requestHandler) {
		this.revision = revision;
		this.headRevision = headRevision;
		this.revisionDecorator = revisionDecorator;
		this.state = state;
		this.requestHandler = requestHandler;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.revision.getRevision());
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();// TODO
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public String getRevision() {
		return Long.toString(this.revision.getRevision());
	}

	public String getDate() {
		return DateFormatter.format(this.revision.getDate());
	}

	public String getAge() {
		return DateFormatter.format(this.revision.getDate(),
				DateFormatter.RELATIVE);
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.revision.getAuthor());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.revision.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.revision.getComment());
	}

	public String getComment() {
		return HtmlUtil.encode(this.revision.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.revision.getComment());
	}

	public String getChangedElementsCount() {
		return Integer.toString(this.revision.getChangedElements().size());
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.revision.getRevision();
	}

	public boolean isRevisionDecorated() {
		return this.revisionDecorator.isRevisionDecorated(this.getRevision(),
				this.state.getRequest());
	}

	public String getDecorationTitle() {
		return this.revisionDecorator.getSectionTitle();
	}

	public String getDecorationContent() {
		return this.revisionDecorator.getSectionContent(this.getRevision(),
				this.state.getRequest());
	}

	public List getChangedElements() {
		List ret = new ArrayList();
		List changedElements = this.revision.getChangedElements();
		Collections.sort(changedElements, this.comparator);
		for (Iterator i = changedElements.iterator(); i.hasNext();) {
			ret.add(new Element((DataRevision.ChangedElement) i.next()));
		}
		return ret;
	}
}
