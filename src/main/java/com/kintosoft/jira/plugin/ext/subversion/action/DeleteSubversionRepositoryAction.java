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
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeleteSubversionRepositoryAction extends SubversionActionSupport {
	private final static Logger log = LoggerFactory
			.getLogger(DeleteSubversionRepositoryAction.class);

	private long repoId;
	private SubversionManager subversionManager;



	public DeleteSubversionRepositoryAction(ALMMultipleSubversionRepositoryManager manager, ActiveObjects ao) {
		super(manager, ao);
	}

	public String getRepoId() {
		return Long.toString(repoId);
	}

	public void setRepoId(String repoId) {
		this.repoId = Long.parseLong(repoId);
	}

	public String doDefault() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		subversionManager = getMultipleRepoManager().getRepository(repoId);
		return INPUT;
	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		try {
			SWCUtils.setAO(ao, getMultipleRepoManager());
			SWCUtils.deleteRepository(repoId);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	public SubversionManager getSubversionManager() {
		return subversionManager;
	}
}
