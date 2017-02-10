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

package com.kintosoft.jira.plugin.ext.subversion.issuetabpanels.changes;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.SubversionConstants;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.jira.plugin.ext.subversion.linkrenderer.SubversionLinkRenderer;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.util.Date;
import java.util.Map;

/**
 * One item in the 'Subversion Commits' tab.
 */
public class SubversionRevisionAction extends AbstractIssueAction {

	public static Logger log = LoggerFactory
			.getLogger(SubversionRevisionAction.class);

	private final SVNLogEntry revision;
	private final long repoId;
	protected final IssueTabPanelModuleDescriptor descriptor;
	protected ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager;

	public SubversionRevisionAction(ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager,
			SVNLogEntry logEntry,
			IssueTabPanelModuleDescriptor descriptor, long repoId) {
		super(descriptor);
		this.multipleSubversionRepositoryManager = multipleSubversionRepositoryManager;
		this.descriptor = descriptor;
		/* SVN-93 */
		this.revision = logEntry;
		this.repoId = repoId;
	}

	protected void populateVelocityParams(Map params) {
		params.put("stringUtils", new StringUtils());
		params.put("svn", this);
	}

	public SubversionLinkRenderer getLinkRenderer() {
		SubversionManager mgr = multipleSubversionRepositoryManager
				.getRepository(repoId);
		if (mgr == null) {
			log.warn("Invalid repository Id = " + repoId);
			return null;
		}
		return mgr.getLinkRenderer();
	}

	public String getRepositoryDisplayName() {

		log.info("---------- Getting repository information to issue tab panel : " + repoId);

		SubversionManager mgr = multipleSubversionRepositoryManager
				.getRepository(repoId);
		if (mgr == null) {
			log.warn("Invalid repository Id = " + repoId);
			return "Invalid repository Id = " + repoId;
		}
		return mgr.getDisplayName();
	}

	public Date getTimePerformed() {
		if (revision.getDate() == null) {
			throw new UnsupportedOperationException(
					"no revision date for this log entry");
		}
		return revision.getDate();
	}

	public long getRepoId() {
		return repoId;
	}

	public String getUsername() {
		return revision.getAuthor();
	}

	public SVNLogEntry getRevision() {
		return revision;
	}

	public boolean isAdded(SVNLogEntryPath logEntryPath) {
		return SubversionConstants.ADDED == logEntryPath.getType();
	}

	public boolean isModified(SVNLogEntryPath logEntryPath) {
		return SubversionConstants.MODIFICATION == logEntryPath.getType();
	}

	public boolean isReplaced(SVNLogEntryPath logEntryPath) {
		return SubversionConstants.REPLACED == logEntryPath.getType();
	}

	public boolean isDeleted(SVNLogEntryPath logEntryPath) {
		return SubversionConstants.DELETED == logEntryPath.getType();
	}

	public long dec(long value) {
		return --value;
	}
}
