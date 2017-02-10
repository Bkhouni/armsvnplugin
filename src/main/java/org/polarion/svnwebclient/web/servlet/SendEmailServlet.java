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
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.MailDelivery;
import org.polarion.svnwebclient.web.controller.ReviewBean;
import org.polarion.svnwebclient.web.resource.Links;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 6388822684170646448L;

	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String DESCRIPTION = "description";
	public static final String STACKTRACE = "stacktrace";
	protected static final String LETTER_CONTENT = "letter";
	public static final String PRESSED_BUTTON = "review";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String pressedButton = request
				.getParameter(SendEmailServlet.PRESSED_BUTTON);
		if ("Back".equals(pressedButton)) {
			SWCPluginHttpRequestWrapper.forward(request, response, Links.ERROR);
			return;
		} else {
			ReviewBean reviewBean = new ReviewBean();
			reviewBean.execute(request, response);
			String message = reviewBean.getLetterContent();

			String reportId = request.getParameter("reportId");
			reportId = HtmlUtil.encode(reportId);
			request.getSession().removeAttribute("reportId");

			if (MailDelivery.sendEmail(message, reportId)) {
				response.sendRedirect(Links.DIRECTORY_CONTENT);
			} else {
				SWCPluginHttpRequestWrapper.forward(request, response,
						Links.REVIEW);
			}
		}
	}
}
