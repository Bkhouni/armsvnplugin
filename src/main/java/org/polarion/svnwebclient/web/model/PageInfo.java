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

import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PageInfo {

	protected String page;
	protected String path;
	protected List parameters = new ArrayList();

	public PageInfo() {
	}

	public void init(String servletPath, String queryString) {
		if (servletPath != null) {
			if (servletPath.startsWith("/")) {
				this.page = servletPath.substring(1);
			} else {
				this.page = servletPath;
			}
		}

		if (queryString != null && !"".equals(queryString)) {
			String[] params = queryString.split("&");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				this.processParameter(param);
			}
		}

		if (this.path == null) {
			this.path = "";
		}
	}

	protected void processParameter(String parameter) {
		String match = RequestParameters.URL + "=";
		if (parameter.startsWith(match) && match.length() < parameter.length()) {
			this.path = UrlUtil.decode(parameter.substring(match.length()));
		} else {
			this.parameters.add(UrlUtil.decode(parameter));
		}
	}

	public String getPage() {
		return page;
	}

	public String getParameters() {
		if (!this.parameters.isEmpty()) {
			StringBuffer res = new StringBuffer();

			Iterator iter = this.parameters.iterator();
			while (iter.hasNext()) {
				String param = (String) iter.next();
				res.append(param);
				if (iter.hasNext()) {
					res.append("&");
				}
			}
			return res.toString();
		} else {
			return null;
		}
	}

	public String getPath() {
		return path;
	}

	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("Page: ").append(this.getPage());
		res.append(", path: ").append(this.getPath());
		res.append(", parameters: ").append(this.getParameters());
		return res.toString();
	}

	public static void main(String[] s) {
		String servletPath = "directoryContent.jsp";
		String queryString = "rev=21&url=webclient&crev=123";

		PageInfo info = new PageInfo();
		info.init(servletPath, queryString);
		System.out.println(info);

	}

}
