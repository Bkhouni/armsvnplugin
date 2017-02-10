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

package com.kintosoft.jira.plugin.ext.subversion.linkrenderer;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 * Used when the user does not specify any web links for Perforce - just return
 * String values, no links.
 */
public class NullLinkRenderer implements SubversionLinkRenderer {
	public String getRevisionLink(String contextPath, long repoId,
			SVNLogEntry revision) {
		return Long.toString(revision.getRevision());
	}

	public String getChangePathLink(String contextPath, long repoId,
			SVNLogEntry revision, SVNLogEntryPath logEntryPath) {
		return logEntryPath.getPath();
	}

	public String getCopySrcLink(String contextPath, long repoId,
			SVNLogEntry revision, SVNLogEntryPath logEntryPath) {
		return logEntryPath.getCopyPath() + " #"
				+ logEntryPath.getCopyRevision();
	}
}