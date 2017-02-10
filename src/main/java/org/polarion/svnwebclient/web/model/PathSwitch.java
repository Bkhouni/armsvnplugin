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
package org.polarion.svnwebclient.web.model;

import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class PathSwitch {
	public static final String TRUNK = "Trunk";
	public static final String BRANCHES = "Branches";
	public static final String TAGS = "Tags";

	protected String urlPrefix;
	protected String urlSuffix;
	protected String selected;
	protected long currentRevision;
	protected String url;
	protected String location;
	protected boolean isPickerMode;
	protected boolean isSingleRevision;
	protected boolean isMultiSelectionUrl;

	public static class Element {
		protected String name;
		protected String url;
		protected boolean selected;

		public Element(String name, String url, boolean selected) {
			this.name = name;
			this.url = url;
			this.selected = selected;
		}

		public String getName() {
			return this.name;
		}

		public String getUrl() {
			return this.url;
		}

		public boolean isSelected() {
			return this.selected;
		}
	}

	public PathSwitch(String location, String urlPrefix, String urlSuffix,
			String selected, String url, long currentRevision) {
		this(location, urlPrefix, urlSuffix, selected, url, currentRevision,
				false, false, false);
	}

	public PathSwitch(String location, String urlPrefix, String urlSuffix,
			String selected, String url, long currentRevision,
			boolean isPickerMode, boolean isSingleRevision,
			boolean isMultipleSelectionUrl) {
		this.location = location;
		this.urlPrefix = urlPrefix;
		this.urlSuffix = urlSuffix;
		this.selected = selected;
		this.currentRevision = currentRevision;
		this.url = url;
		this.isPickerMode = isPickerMode;
		this.isSingleRevision = isSingleRevision;
		this.isMultiSelectionUrl = isMultipleSelectionUrl;

	}

	public List getElements() {
		List ret = new ArrayList();
		ret.add(new Element(PathSwitch.TRUNK, this
				.generateUrl(PathSwitch.TRUNK), PathSwitch.TRUNK
				.equals(this.selected)));
		ret.add(new Element(PathSwitch.BRANCHES, this
				.generateUrl(PathSwitch.BRANCHES), PathSwitch.BRANCHES
				.equals(this.selected)));
		ret.add(new Element(PathSwitch.TAGS, this.generateUrl(PathSwitch.TAGS),
				PathSwitch.TAGS.equals(this.selected)));
		return ret;
	}

	protected String generateUrl(String type) {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.PATH_SWITCH, this.location);
		urlGenerator.addParameter(RequestParameters.PREFIX,
				UrlUtil.encode(this.urlPrefix));
		urlGenerator.addParameter(RequestParameters.SUFFIX,
				UrlUtil.encode(this.urlSuffix));
		urlGenerator.addParameter(RequestParameters.TYPE, type);
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		if (this.isPickerMode) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}
		if (this.currentRevision != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.currentRevision));
		}
		return urlGenerator.getUrl();
	}
}
