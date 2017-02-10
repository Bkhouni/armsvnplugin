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

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.scheduler.SchedulerService;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManagerImpl;
import com.kintosoft.jira.plugin.ext.subversion.revisions.scheduling.ScheduleMgr;

import java.sql.SQLException;

public class UpdateSchedulerAction extends JiraWebActionSupport {


	private final ALMMultipleSubversionRepositoryManager manager;

	private final SchedulerService schedulerService;

	private String interval;

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}


	public UpdateSchedulerAction( ALMMultipleSubversionRepositoryManager manager, SchedulerService schedulerService) {
		this.manager = manager;
		this.schedulerService = schedulerService;
	}

	public boolean hasPermissions() {
		return hasPermission(Permissions.ADMINISTER);
	}

	public String doDefault() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}
		try {
			setInterval(Integer.toString(ScheduleMgr.getInstance(manager, schedulerService)
					.getIntervalFromDB()));
		} catch (SQLException e) {
			log.error(e.getMessage());
			addError("interval", e.getMessage());
			return ERROR;
		}
		return INPUT;
	}

	public void doValidation() {
		try {
			int seconds = Integer.parseInt(getInterval());
			if (seconds < 10) {
				addError("interval",
						"The minimum schedule interval is 10 seconds.");
			}
		} catch (NumberFormatException e) {
			addError("interval",
					getText("The value has to be a number greater than 10"));
		}
	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		try {
			ScheduleMgr.getInstance(manager, schedulerService).reschedule(Integer.parseInt(interval));
		} catch (Exception e) {
			log.error(e.getMessage());
			addError("interval", e.getMessage());
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	public String doStartIndexer() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}
		try {
			ALMMultipleSubversionRepositoryManagerImpl.updateIndexStatic();
			manager.getIndexer().error = null;
		} catch (Exception e) {
			manager.getIndexer().error = e.getMessage();
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}
}
