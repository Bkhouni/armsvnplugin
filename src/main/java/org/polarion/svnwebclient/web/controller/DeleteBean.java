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
package org.polarion.svnwebclient.web.controller;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.Link;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.DeletedElementsList;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.servlet.DeleteActionServlet;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.FormParameters;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DeleteBean extends AbstractBean {
	protected static final String DELETE_FLAG = "1";
	protected static final long CURRENT_REVISION = -1;

	protected Navigation navigation;
	protected String url;
	protected DeletedElementsList deletedElements;

	public DeleteBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(),
					dataProvider.getHeadRevision());
		}
		this.navigation = new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(), DeleteBean.CURRENT_REVISION,
				false);

		String[] flags = this.state.getRequest().getParameterValues(
				FormParameters.FLAGS);
		String[] types = this.state.getRequest().getParameterValues(
				FormParameters.TYPES);
		String[] names = this.state.getRequest().getParameterValues(
				FormParameters.NAMES);
		String[] revisions = this.state.getRequest().getParameterValues(
				FormParameters.REVISIONS);
		String[] sizes = this.state.getRequest().getParameterValues(
				FormParameters.SIZES);
		String[] dates = this.state.getRequest().getParameterValues(
				FormParameters.DATES);
		String[] ages = this.state.getRequest().getParameterValues(
				FormParameters.AGES);
		String[] authors = this.state.getRequest().getParameterValues(
				FormParameters.AUTHORS);
		String[] comments = this.state.getRequest().getParameterValues(
				FormParameters.COMMENTS);

		this.deletedElements = new DeletedElementsList();
		List elements = new ArrayList();
		List deletedElementsNames = new ArrayList();
		for (int i = 0; i < flags.length; i++) {
			if (DeleteBean.DELETE_FLAG.equals(flags[i])) {
				DeletedElementsList.Element element = new DeletedElementsList.Element();
				element.setType(types[i]);
				element.setName(names[i]);
				element.setRevision(revisions[i]);
				element.setSize(sizes[i]);
				element.setDate(dates[i]);
				element.setAge(ages[i]);
				element.setAuthor(authors[i]);
				element.setComment(comments[i]);
				elements.add(element);

				deletedElementsNames.add(names[i]);
			}
		}
		this.deletedElements.setDeletedElements(elements);
		this.state
				.getRequest()
				.getSession()
				.setAttribute(DeleteActionServlet.DELETED_ELEMENTS,
						deletedElementsNames);

		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public DeletedElementsList getDeletedElements() {
		return this.deletedElements;
	}

	public String getOkUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DELETE_ACTION, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		return urlGenerator.getUrl();
	}

	public String getCancelUrl() {
		List navigationPath = this.navigation.getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public Navigation getNavigation() {
		return this.navigation;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
