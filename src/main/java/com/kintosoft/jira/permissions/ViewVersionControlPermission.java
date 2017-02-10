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

package com.kintosoft.jira.permissions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.Map;

public class ViewVersionControlPermission extends AbstractJiraCondition {

	@Override
	public boolean shouldDisplay(Map args) {
		ApplicationUser user = (ApplicationUser) args.get("user");
		return hasUserViewVersionControlPermissionsOnAnyProject(user);
	}

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper arg1) {
		return hasUserViewVersionControlPermissionsOnAnyProject(user);
	}

	public static boolean hasUserViewVersionControlPermissionsOnAnyProject(
			ApplicationUser user) {
		if (user == null) {
			return false;
		}

		PermissionManager permissionManager = ComponentAccessor
				.getPermissionManager();
		Collection<Project> projects = permissionManager.getProjects(
				Permissions.BROWSE, user);
		return !projects.isEmpty();
	}

}
