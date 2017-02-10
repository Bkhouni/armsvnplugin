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
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Base class for the Subversion plugins actions.
 */



public class SubversionActionSupport extends JiraWebActionSupport {


	private ALMMultipleSubversionRepositoryManager multipleRepoManager;
	private List webLinkTypes;
	protected final ActiveObjects ao;

	public SubversionActionSupport(ALMMultipleSubversionRepositoryManager multipleRepoManager, ActiveObjects ao) {
		this.multipleRepoManager = multipleRepoManager;
		this.ao = ao;

	}


	public ALMMultipleSubversionRepositoryManager getMultipleRepoManager() {
		return multipleRepoManager;
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

	public String escapeJavaScript(String javascriptUnsafeString) {
		return StringEscapeUtils.escapeJavaScript(javascriptUnsafeString);
	}
}
