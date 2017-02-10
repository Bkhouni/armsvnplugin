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
package org.polarion.svnwebclient.web.servlet;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.web.controller.RevisionListBean;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RevisionModeChangeServlet extends AbstractServlet {
	private static final long serialVersionUID = 1758390832915586758L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider, State state)
			throws SVNWebClientException {
		HttpSession session = state.getRequest().getSession();
		if (session
				.getAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE) == null) {
			session.setAttribute(
					RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE,
					RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE);
		} else {
			session.removeAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE);
		}
		try {
			state.getResponse().sendRedirect(
					Links.REVISION_LIST + "?"
							+ state.getRequest().getQueryString());
		} catch (IOException ie) {
			throw new SVNWebClientException(ie);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

}
