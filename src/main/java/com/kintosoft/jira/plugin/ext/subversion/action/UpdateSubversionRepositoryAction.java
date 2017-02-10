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
import org.polarion.svnwebclient.configuration.WebConfigurationProvider;

import java.util.Enumeration;
import java.util.Properties;


public class UpdateSubversionRepositoryAction extends
		AddSubversionRepositoryAction {
	private long repoId = -1;


//	@Autowired
	public UpdateSubversionRepositoryAction(
			ALMMultipleSubversionRepositoryManager multipleRepoManager, ActiveObjects ao)
			throws Exception {
		super(multipleRepoManager, ao);
	}

	public String doDefault() {
		if (ERROR.equals(super.doDefault()))
			return ERROR;

		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		if (repoId == -1) {
			addErrorMessage(getText("subversion.repository.id.missing"));
			return ERROR;
		}

		// Retrieve the cvs repository
		final SubversionManager repository = getMultipleRepoManager()
				.getRepository(repoId);
		if (repository == null) {
			addErrorMessage(getText("subversion.repository.does.not.exist",
					Long.toString(repoId)));
			return ERROR;
		}
		/*
		 * this.setDisplayName(repository.getDisplayName());
		 * this.setRoot(repository.getRoot()); if
		 * (repository.getViewLinkFormat() != null) {
		 * this.setWebLinkType(repository.getViewLinkFormat().getType());
		 * this.setChangesetFormat(repository.getViewLinkFormat()
		 * .getChangesetFormat());
		 * this.setViewFormat(repository.getViewLinkFormat().getViewFormat());
		 * this.setFileAddedFormat(repository.getViewLinkFormat()
		 * .getFileAddedFormat());
		 * this.setFileDeletedFormat(repository.getViewLinkFormat()
		 * .getFileDeletedFormat());
		 * this.setFileModifiedFormat(repository.getViewLinkFormat()
		 * .getFileModifiedFormat());
		 * this.setFileReplacedFormat(repository.getViewLinkFormat()
		 * .getFileReplacedFormat()); }
		 * this.setUsername(repository.getUsername());
		 * this.setPassword(repository.getPassword());
		 * this.setPrivateKeyFile(repository.getPrivateKeyFile());
		 * this.setRevisionCacheSize(new Integer(repository
		 * .getRevisioningCacheSize())); this.setRevisionIndexing(new
		 * Boolean(repository.isRevisionIndexing()));
		 */
		return INPUT;
	}

	public String doExecute() {
		if (!hasPermissions()) {
			addErrorMessage(getText("subversion.admin.privilege.required"));
			return ERROR;
		}

		if (repoId == -1) {
			return getRedirect("ALMViewSubversionRepositories.jspa");
		}

		try {
			SWCUtils.setAO(ao, getMultipleRepoManager());
			SWCUtils.updateRepository(props);
		} catch (Exception e) {
			addErrorMessage(e.getMessage());
			addErrorMessage(getText("admin.errors.occured.when.updating"));
			return ERROR;
		}

		SubversionManager subversionManager = getMultipleRepoManager()
				.getRepository(repoId);
		if (!subversionManager.isActive()) {
			addErrorMessage(subversionManager.getInactiveMessage());
			addErrorMessage(getText("admin.errors.occured.when.updating"));
			return ERROR;
		}
		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	public long getRepoId() {
		return Long.parseLong(props
				.getProperty(WebConfigurationProvider.REPO_ID));
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
		props.setProperty(WebConfigurationProvider.REPO_ID,
				Long.toString(repoId));
	}

	public void setRepoIdForUpdate(long repoId) {
		this.repoId = repoId;
		try {
			this.props.clear();

			Properties original = SWCUtils.getRepositoryProperties(repoId);
			Enumeration keys = original.propertyNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				Object value = original.get(key);
				props.put(key, value == null ? null : value.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}
}
