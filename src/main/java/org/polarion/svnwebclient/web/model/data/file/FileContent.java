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
package org.polarion.svnwebclient.web.model.data.file;

import org.polarion.svnwebclient.data.model.DataFileElement;
import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileContent {
	protected DataFileElement fileElement;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;

	public FileContent(DataFileElement fileElement,
			AbstractRequestHandler requestHandler, long headRevision, String url) {
		this.fileElement = fileElement;
		this.requestHandler = requestHandler;
		this.headRevision = headRevision;
		this.url = url;
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.fileElement.getAuthor());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.fileElement.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.fileElement.getComment());
	}

	public String getComment() {
		return HtmlUtil.encode(this.fileElement.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.fileElement.getComment());
	}

	public String getDate() {
		return DateFormatter.format(this.fileElement.getDate());
	}

	public String getAge() {
		return DateFormatter.format(this.fileElement.getDate(),
				DateFormatter.RELATIVE);
	}

	public String getRevision() {
		return Long.toString(this.fileElement.getRevision());
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.fileElement.getRevision());
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.fileElement.getRevision();
	}

	public String getSize() {
		return NumberFormatter.format(this.fileElement.getSize());
	}
}
