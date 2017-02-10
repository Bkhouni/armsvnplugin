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

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

public class DBConsoleAuthorizationFilter implements Filter {

	final private static Logger log = LoggerFactory.getLogger(Filter.class);

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext()
				.getLoggedInUser();

		if (user == null) {
			log.warn("Anonymous user is trying to access to the database web console.");
			return;
		}

		PermissionManager permissionManager = ComponentAccessor
				.getPermissionManager();

		if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)) {
			log.warn("A no adminstrator user is trying to access to the database web console: "
					+ user.getName());
			return;
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {

	}

}
