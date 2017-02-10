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


public class UpdateConnectionPoolSizeAction extends JiraWebActionSupport {

	ActiveObjects ao;

	PluginConnectionPool pcp;


	private final ALMMultipleSubversionRepositoryManager manager;

	private String size = "-1";

	public String getPoolSize() {
		return size;
	}

	public void setPoolSize(String size) {
		this.size = size;
	}


	public UpdateConnectionPoolSizeAction(ActiveObjects ao, ALMMultipleSubversionRepositoryManager manager) {
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

			setPoolSize(Integer.toString(pcp
					.getDBConnectionPoolSize()));
		} catch (Exception e) {
			log.error(e.getMessage());
			addError("poolSize", e.getMessage());
			return ERROR;
		}
		return INPUT;
	}

	public void doValidation() {
		try {
			int value = Integer.parseInt(getPoolSize());
			if (value < 10) {
				addError("poolSize", "The minimum pool size is 10 connections");
			}
		} catch (NumberFormatException e) {
			addError("poolSize",
					getText("The value has to be a number greater than 10"));
		}
	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		try {
			pcp.setDBConnectionPoolSize(
					Integer.parseInt(size), ao);
		} catch (Exception e) {
			log.error(e.getMessage());
			addError("poolSize", e.getMessage());
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}
}
