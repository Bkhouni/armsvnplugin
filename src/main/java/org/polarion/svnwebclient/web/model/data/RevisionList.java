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

import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.decorations.IIconDecoration;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class RevisionList {
	protected List revisions;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;
	protected DataRepositoryElement info;
	protected IRevisionDecorator revisionDecorator;
	protected State state;

	public class Element {
		protected DataRevision revision;

		public Element(DataRevision revision) {
			this.revision = revision;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.revision.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.revision.getRevision()));
		}

		public String getRevisionUrl() {
			UrlGenerator urlGenerator;
			if (RevisionList.this.info.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT,
						RevisionList.this.requestHandler.getLocation());
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT,
						RevisionList.this.requestHandler.getLocation());
			}

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			} else {
				urlGenerator.addParameter(RequestParameters.PEGREV,
						Long.toString(RevisionList.this.headRevision));
			}
			urlGenerator.addParameter(RequestParameters.CREV,
					this.getRevision());
			return urlGenerator.getUrl();
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

		public String getComment() {
			return HtmlUtil.encode(this.revision.getComment());
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.revision.getComment());
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.revision.getComment());
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.revision.getComment());
		}

		public String getRevisionInfoUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION,
					RevisionList.this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			}
			urlGenerator
					.addParameter(RequestParameters.REV, this.getRevision());
			return urlGenerator.getUrl();
		}

		public String getDownloadUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_DOWNLOAD,
					RevisionList.this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			} else {
				urlGenerator.addParameter(RequestParameters.PEGREV,
						Long.toString(RevisionList.this.headRevision));
			}
			urlGenerator.addParameter(RequestParameters.CREV,
					this.getRevision());
			urlGenerator.addParameter(RequestParameters.ATTACHMENT,
					RequestParameters.VALUE_TRUE);
			return urlGenerator.getUrl();
		}

		public boolean isRevisionDecorated() {
			return RevisionList.this.revisionDecorator.isRevisionDecorated(
					this.getRevision(), RevisionList.this.state.getRequest());
		}

		public IIconDecoration getRevisionDecoration() {
			return RevisionList.this.revisionDecorator.getIconDecoration(
					this.getRevision(), RevisionList.this.state.getRequest());
		}

		public boolean isHeadRevision() {
			return RevisionList.this.headRevision == this.revision
					.getRevision();
		}
	}

	public RevisionList(List revisions, AbstractRequestHandler requestHandler,
			long headRevision, String url, DataRepositoryElement info,
			IRevisionDecorator revisionDecorator) {
		this.revisions = revisions;
		this.requestHandler = requestHandler;
		this.headRevision = headRevision;
		this.url = url;
		this.info = info;
		this.revisionDecorator = revisionDecorator;
	}

	public String getRevisionsCount() {
		return Integer.toString(this.revisions.size());
	}

	public String getHeadRevision() {
		return NumberFormatter.format(this.headRevision);
	}

	public String getHeadRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV,
				Long.toString(this.headRevision));
		return urlGenerator.getUrl();
	}

	public List getRevisions() {
		List ret = new ArrayList();
		for (Iterator i = this.revisions.iterator(); i.hasNext();) {
			ret.add(new Element((DataRevision) i.next()));
		}
		return ret;
	}

	public boolean isDirectory() {
		return this.info.isDirectory();
	}

	public void setState(State state) {
		this.state = state;
	}
}
