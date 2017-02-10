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
package org.polarion.svnwebclient.decorations;

import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.RequestParameters;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class BaseViewProvider implements IAlternativeViewProvider {
	public static final String CONTENT = "Content";
	public static final String ANNOTATE = "Annotate";
	protected String location;

	public BaseViewProvider(String location) {
		this.location = location;
	}

	public String[] getAvailableAlternativeViews(String resourceUrl,
			long revision) {
		String[] ret = new String[2];
		ret[0] = BaseViewProvider.CONTENT;
		ret[1] = BaseViewProvider.ANNOTATE;
		return ret;
	}

	public String getAlternativeViewContentUrl(String resourceUrl,
			long revision, long line, String viewName) {
		String ret = null;
		if (BaseViewProvider.CONTENT.equals(viewName)) {
			String targetView = Links.FILE_DATA;

			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					targetView, this.location);
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(resourceUrl));
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(revision));
			if (line > 0) {
				urlGenerator.setAnchor(Long.toString(line));
			}
			ret = urlGenerator.getUrl();
		} else if (BaseViewProvider.ANNOTATE.equals(viewName)) {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_ANNOTATION, this.location);
			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(resourceUrl));
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(revision));
			ret = urlGenerator.getUrl();
		}
		return ret;
	}
}
