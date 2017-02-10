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

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.opensymphony.util.TextUtils;


public class UpdateDatabaseServerAction extends JiraWebActionSupport {


	private final ALMMultipleSubversionRepositoryManager manager;

	private String password;


	public UpdateDatabaseServerAction(
			ALMMultipleSubversionRepositoryManager manager) {
		this.manager = manager;
	}

	public boolean hasPermissions() {
		return hasPermission(Permissions.ADMINISTER);
	}

	public String doDefault() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}
		return INPUT;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*public String getPoolUrl() {
		return PluginConnectionPool.getInstance().getPoolUrl();
	}*/

	public void doValidation() {
		if (!TextUtils.stringSet(getPassword())) {
			addError(
					"password",
					getText("subversion.db.errors.you.must.specify.a.valid.password"));
		}

	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		try {
			/*PluginConnectionPool.getInstance().setConsolePassword(password);*/
		} catch (Exception e) {
			manager.getIndexer().error = e.getMessage();
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}
}
