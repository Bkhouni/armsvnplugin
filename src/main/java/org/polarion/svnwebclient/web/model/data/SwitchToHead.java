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

import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.RequestParameters;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class SwitchToHead {
	protected String originalUrl;
	protected long revision;
	protected long headRevision;
	protected String urlInRevision;
	protected String urlInHead;
	protected boolean redirectToDirectory;
	protected String location;

	public SwitchToHead(String location, String originalUrl,
			String urlInRevision, String urlInHead, boolean redirectToDirectory) {
		this.location = location;
		this.originalUrl = originalUrl;
		this.urlInRevision = urlInRevision;
		this.urlInHead = urlInHead;
		this.redirectToDirectory = redirectToDirectory;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public void setHeadRevision(long headRevision) {
		this.headRevision = headRevision;
	}

	public String getOriginalUrl() {
		return HtmlUtil.encode(this.originalUrl);
	}

	public String getRevision() {
		return Long.toString(this.revision);
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.revision);
	}

	public String getHeadRevision() {
		return Long.toString(this.headRevision);
	}

	public String getUrlInRevision() {
		return HtmlUtil.encode(this.urlInRevision);
	}

	public String getUrlInHead() {
		return HtmlUtil.encode(this.urlInHead);
	}

	public String getUrl() {
		String ret = "";
		if (this.urlInHead != null) {
			UrlGenerator urlGenerator;
			if (this.redirectToDirectory) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT, this.location);
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT, this.location);
			}

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(this.urlInHead));
			ret = urlGenerator.getUrl();
		}
		return ret;
	}
}
