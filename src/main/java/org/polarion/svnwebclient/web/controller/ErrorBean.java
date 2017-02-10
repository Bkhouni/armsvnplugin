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

import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.StringId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorBean {

	protected String stacktrace;
	protected String message;
	protected String reportId;

	protected String description;
	protected String name;
	protected String email;

	public boolean execute(HttpServletRequest request,
			HttpServletResponse response) {
		Throwable exception = (Throwable) request
				.getAttribute("javax.servlet.error.exception");

		if (exception != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			exception.printStackTrace(printWriter);

			this.stacktrace = stringWriter.toString();
			this.message = exception.getMessage();
			this.reportId = StringId.generateRandom(5);
		} else {
			this.stacktrace = request.getParameter("stacktrace") == null ? ""
					: request.getParameter("stacktrace");
			this.message = request.getParameter("message") == null ? ""
					: request.getParameter("message");
			this.reportId = request.getParameter("reportId");
		}
		this.stacktrace = HtmlUtil.encodeWithSpace(this.stacktrace);
		this.message = HtmlUtil.encodeWithSpace(this.message);

		this.description = request.getParameter("description") == null ? ""
				: request.getParameter("description");
		this.description = HtmlUtil.encode(this.description);
		this.name = request.getParameter("name") == null ? "" : request
				.getParameter("name");
		this.email = request.getParameter("email") == null ? "" : request
				.getParameter("email");

		return true;
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public String getMessage() {
		return message;
	}

	public String getReportId() {
		return reportId;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
}
