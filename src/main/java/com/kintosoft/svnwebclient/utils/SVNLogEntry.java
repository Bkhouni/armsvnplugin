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

package com.kintosoft.svnwebclient.utils;

import com.atlassian.jira.util.JiraKeyUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class SVNLogEntry extends org.tmatesoft.svn.core.SVNLogEntry {

	private long repoId;

	public long getRepoId() {
		return repoId;
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	public SVNLogEntry(long repoId, Map changedPaths, long revision,
			String author, Date date, String message) {
		super(changedPaths, revision, author, date, message);
		setRepoId(repoId);
	}

	/**
	 * Converts all lower case JIRA issue keys to upper case so that they can be
	 * correctly rendered in the Velocity macro, makelinkedhtml.
	 * 
	 * @param logMessageToBeRewritten
	 *            The SVN log message to be rewritten.
	 * @return The rewritten SVN log message.
	 * @see <a href="http://jira.atlassian.com/browse/SVN-93">SVN-93</a>
	 */
	protected String rewriteLogMessage(final String logMessageToBeRewritten) {
		String logMessage = logMessageToBeRewritten;
		final String logMessageUpperCase = StringUtils.upperCase(logMessage);
		final Set<String> issueKeys = new HashSet<String>(
				getIssueKeysFromCommitMessage(logMessageUpperCase));

		for (String issueKey : issueKeys)
			logMessage = logMessage.replaceAll("(?ium)" + issueKey, issueKey);

		return logMessage;
	}

	List<String> getIssueKeysFromCommitMessage(String logMessageUpperCase) {
		return JiraKeyUtils.getIssueKeysFromString(logMessageUpperCase);
	}
}
