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

package com.kintosoft.jira.plugin.ext.subversion.projecttabpanels;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.issuetabpanels.changes.SubversionRevisionAction;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import org.ofbiz.core.util.UtilMisc;

import java.util.Map;

/**
 * One item in the 'Subversion Commits' project tab.
 * 
 * This class extends {@link SubversionRevisionAction} (basically, there is no
 * issue to group by here, and we need to use a ProjectTabPanelModuleDescriptor
 * in stead of an IssueTabPanelModuleDescriptor)
 */


public class SubversionProjectRevisionAction extends SubversionRevisionAction {
	protected final ProjectTabPanelModuleDescriptor projectDescriptor;


	public SubversionProjectRevisionAction(ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager,
			SVNLogEntry logEntry,
			ProjectTabPanelModuleDescriptor descriptor, long repoId) {
		super(multipleSubversionRepositoryManager, logEntry, null, repoId);
		this.projectDescriptor = descriptor;
	}

	public String getHtml(JiraWebActionSupport webAction) {
		Map params = UtilMisc.toMap("webAction", webAction, "action", this);
		return descriptor.getHtml("view", params);
	}
}
