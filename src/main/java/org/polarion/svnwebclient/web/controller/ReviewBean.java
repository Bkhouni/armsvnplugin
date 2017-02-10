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

import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.web.servlet.SendEmailServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReviewBean {
	protected String letterContent;

	public boolean execute(HttpServletRequest request,
			HttpServletResponse response) {
		String version = ConfigurationProvider.getProductVersion();
		this.letterContent = this.formReport(
				request.getParameter(SendEmailServlet.DESCRIPTION),
				request.getParameter(SendEmailServlet.EMAIL),
				request.getParameter(SendEmailServlet.NAME),
				request.getParameter(SendEmailServlet.STACKTRACE), version,
				request.getParameter("reportId"));
		return true;
	}

	public String getEncodedLetterContent() {
		String res = this.letterContent.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;",
				"\t");
		return res;
	}

	public String getLetterContent() {
		return this.letterContent;
	}

	protected String formReport(String userComment, String email, String name,
			String stackTrace, String version, String reportId) {
		version = HtmlUtil.encode(version);
		reportId = HtmlUtil.encode(reportId);

		String msgPlusTrace = "";
		if (stackTrace == null) {
			stackTrace = "<i>[empty]</i>";
		} else {
			stackTrace = HtmlUtil.encodeWithSpace(stackTrace);
			stackTrace = stackTrace.replaceAll("\r", "<br>");
			String[] mas = stackTrace.split("\n");

			for (int i = 0; i < mas.length; i++) {
				msgPlusTrace += mas[i];
			}
		}

		if (userComment != null) {
			userComment = HtmlUtil.encodeWithSpace(userComment);
			userComment = userComment.replaceAll("\r", "<br>");
		}
		userComment = (userComment != null && userComment.length() > 0) ? userComment
				: "<i>[empty]</i>";

		String jvmProps = System.getProperties().toString();
		jvmProps = HtmlUtil.encode(jvmProps);
		jvmProps = jvmProps.replace('\n', ' ');

		msgPlusTrace += "<br><br><b>JVM Properties:</b><br>" + jvmProps
				+ "<br><br>";

		if (email != null) {
			email = email.trim();
			email = HtmlUtil.encode(email);
		}
		if (name != null) {
			name = name.trim();
			name = HtmlUtil.encode(name);
		}
		String author = (name != null ? name : "")
				+ (email != null && email.length() > 0 ? " &lt;" + email
						+ "&gt;" : "");
		author = author.length() > 0 ? author : "<i>[not specified]</i>";

		String messageBody = "<b>Report ID:" + reportId + "</b>"
				+ "<b><br><br>Version:</b> " + version
				+ "<br><br><b>From:</b> " + author + "<br><br>"
				+ "<b>User comment:</b><br>" + userComment
				+ "<br><br><b>Stack trace</b><br>" + msgPlusTrace;
		return messageBody;
	}
}
