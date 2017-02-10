/*Copyright (c) "Kinto Soft Ltd"

Subversion ALM is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.*/

package com.kintosoft.jira.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SWCPluginHttpRequestWrapper extends HttpServletRequestWrapper {

	private static final String ATT_DISPATCHER = "dispatcher";

	public SWCPluginHttpRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getServletPath() {
		String servletPath = super.getServletPath();
		return servletPath;
	}

	@Override
	public String getPathInfo() {
		String pathInfo = super.getPathInfo();
		Object att = getAttribute(ATT_DISPATCHER);
		if (att != null) {
			pathInfo = att.toString() + pathInfo;
		}
		return pathInfo;
	}

	public static void forward(HttpServletRequest request,
			HttpServletResponse response, String path) throws ServletException,
			IOException {
		forward(request, response, path, new HashMap<String, Object>());
	}

	public static void forward(HttpServletRequest request,
			HttpServletResponse response, String path,
			Map<String, Object> attributes) throws ServletException,
			IOException {
		String servletPath = request.getServletPath();
		servletPath = servletPath.substring("/plugins/servlet".length(),
				servletPath.length());
		request.setAttribute(ATT_DISPATCHER, servletPath);
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}
		RequestDispatcher dispatcher = request.getRequestDispatcher(path);
		dispatcher.forward(request, response);
	}
}
