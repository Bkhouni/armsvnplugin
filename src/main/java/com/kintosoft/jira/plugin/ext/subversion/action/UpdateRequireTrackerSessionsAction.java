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

package com.kintosoft.jira.plugin.ext.subversion.action;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;

import java.sql.SQLException;


public class UpdateRequireTrackerSessionsAction extends JiraWebActionSupport {


	ActiveObjects ao;

	PluginConnectionPool pcp;


	private final ALMMultipleSubversionRepositoryManager manager;

	public String getShareConnections() {
		try {
			return Boolean.toString(pcp
					.getShareConnections());
		} catch (SQLException e) {
			log.error(e.getMessage());
			return e.getMessage();
		}
	}


	public UpdateRequireTrackerSessionsAction(ActiveObjects ao, ALMMultipleSubversionRepositoryManager manager) {
		this.manager = manager;
		this.ao = ao;
		pcp = new PluginConnectionPool(ao);
	}

	public boolean hasPermissions() {
		return hasPermission(Permissions.ADMINISTER);
	}

	public String doDefault() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}
		try {
			PluginConnectionPool cp = pcp;
			cp.setRequireTrackerSession(!cp.getRequireTrackerSession(), ao);
		} catch (Exception e) {
			log.error(e.getMessage());
			addError("requireTrackerSession", e.getMessage());
			return ERROR;
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	public void doValidation() {
	}
}
