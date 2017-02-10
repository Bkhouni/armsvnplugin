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
import com.kintosoft.svnwebclient.db.PluginConnectionPool;


public class UpdateSVNTimeoutAction extends JiraWebActionSupport {


	private final ALMMultipleSubversionRepositoryManager manager;

	private int svnTimeoutConnection = 120000;
	private int svnTimeoutRead = 120000;

	public int getSvnTimeoutConnection() {
		return svnTimeoutConnection;
	}

	public void setSvnTimeoutConnection(int svnTimeoutConnection) {
		this.svnTimeoutConnection = svnTimeoutConnection;
	}

	public int getSvnTimeoutRead() {
		return svnTimeoutRead;
	}

	public void setSvnTimeoutRead(int svnTimeoutRead) {
		this.svnTimeoutRead = svnTimeoutRead;
	}


	public UpdateSVNTimeoutAction(ALMMultipleSubversionRepositoryManager manager) {
		this.manager = manager;
	}

	public boolean hasPermissions() {
		return hasPermission(Permissions.ADMINISTER);
	}

	public String doDefault() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}
		setSvnTimeoutConnection(PluginConnectionPool.getSVNConnectionTimeout());
		setSvnTimeoutRead(PluginConnectionPool.getSVNReadTimeout());

		return INPUT;
	}

	private void validateLimits(String fieldName, int value) {
		try {
			if (value < 1000) {
				throw new Exception(
						"The value has to be a number greater than 1000");
			}
		} catch (Exception ex) {
			addError(fieldName, getText(ex.getMessage()));
		}
	}

	public void doValidation() {
		validateLimits("svnTimeoutConnection", getSvnTimeoutConnection());
		validateLimits("svnTimeoutRead", getSvnTimeoutRead());
	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		try {
			PluginConnectionPool
					.setSVNConnectionTimeout(getSvnTimeoutConnection());
			PluginConnectionPool.setSVNReadTimeout(getSvnTimeoutRead());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}
}
