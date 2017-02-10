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

import com.kintosoft.jira.servlet.SWCPluginHttpRequestWrapper;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.AuthenticationException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.data.model.DataChangedElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.web.AttributeStorage;
import org.polarion.svnwebclient.web.controller.ChangeConfirmation;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.ChangeResult;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.FormParameters;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryAddActionServlet extends AbstractServlet {
	protected static final long CURRENT_REVISION = -1;

	private static final long serialVersionUID = -7706208408425844239L;

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
		AbstractRequestHandler requestHandler = this.getRequestHandler(state
				.getRequest());

		String name = state.getRequest().getParameter(
				FormParameters.DIRECTORY_NAME);
		String comment = state.getRequest()
				.getParameter(FormParameters.COMMENT);
		if (name == null) {
			AttributeStorage handler = AttributeStorage.getInstance();
			HttpSession session = state.getRequest().getSession();
			name = (String) handler.getParameter(session,
					FormParameters.DIRECTORY_NAME);
			comment = (String) handler.getParameter(session,
					FormParameters.COMMENT);
		} else {
			AttributeStorage handler = AttributeStorage.getInstance();
			HttpSession session = state.getRequest().getSession();
			handler.addParameter(session, FormParameters.DIRECTORY_NAME, name);
			handler.addParameter(session, FormParameters.COMMENT, comment);
		}

		long revision = dataProvider.getHeadRevision();
		DataRepositoryElement dataRepositoryElement = null;

		String directoryUrl = requestHandler.getUrl();
		if (!directoryUrl.endsWith("/")) {
			directoryUrl += "/";
		}
		directoryUrl += name;

		ChangeResult changeResult = null;
		Navigation navigation = new Navigation(dataProvider.getId(),
				requestHandler.getUrl(), requestHandler.getLocation(),
				DirectoryAddActionServlet.CURRENT_REVISION, false);

		try {
			// if there's no path, handle IncorrectParameterException
			dataRepositoryElement = dataProvider
					.getInfo(directoryUrl, revision);

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(dataRepositoryElement.getAuthor());
			element.setComment(dataRepositoryElement.getComment());
			element.setDate(dataRepositoryElement.getDate());
			element.setDirectory(true);
			element.setName(dataRepositoryElement.getName());
			element.setRevision(dataRepositoryElement.getRevision());
			elements.add(element);
			String message = "Directory " + name
					+ " already exists in specified location";
			changeResult = new ChangeResult(false, message, elements,
					navigation);
		} catch (IncorrectParameterException ie) {
			DataChangedElement changedElement = dataProvider.createDirectory(
					requestHandler.getUrl(), name, comment);
			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(changedElement.getAuthor());
			element.setComment(changedElement.getComment());
			element.setDate(changedElement.getDate());
			element.setDirectory(true);
			element.setName(changedElement.getName());
			element.setRevision(changedElement.getRevision());
			elements.add(element);
			String message = "Directory " + name + " was succesfully added";
			changeResult = new ChangeResult(true, message, elements, navigation);
		} catch (AuthenticationException ae) {
			throw ae;
		} catch (DataProviderException e) {
			AttributeStorage.getInstance().cleanSession(
					state.getRequest().getSession());
			throw e;
		}

		AttributeStorage.getInstance().cleanSession(
				state.getRequest().getSession());
		state.getRequest().getSession()
				.setAttribute(ChangeConfirmation.CHANGE_RESULT, changeResult);
		try {
			SWCPluginHttpRequestWrapper.forward(state.getRequest(),
					state.getResponse(), Links.CHANGE_CONFIRMATION);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
