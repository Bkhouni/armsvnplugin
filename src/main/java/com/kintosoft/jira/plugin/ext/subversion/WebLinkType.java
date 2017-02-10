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

package com.kintosoft.jira.plugin.ext.subversion;

public class WebLinkType extends Object {
	private final String key;
	private final String name;
	private final ViewLinkFormat viewLinkFormat;

	public WebLinkType(String key, String name, String viewFormat,
			String changeset, String fileAdded, String fileModified,
			String fileReplaced, String fileDeleted) {
		this.key = key;
		this.name = name;
		viewLinkFormat = new ViewLinkFormat(key, changeset, fileAdded,
				fileModified, fileReplaced, fileDeleted, viewFormat);
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getChangesetFormat() {
		return viewLinkFormat.getChangesetFormat();
	}

	public String getFileAddedFormat() {
		return viewLinkFormat.getFileAddedFormat();
	}

	public String getViewFormat() {
		return viewLinkFormat.getViewFormat();
	}

	public String getFileModifiedFormat() {
		return viewLinkFormat.getFileModifiedFormat();
	}

	public String getFileReplacedFormat() {
		return viewLinkFormat.getFileReplacedFormat();
	}

	public String getFileDeletedFormat() {
		return viewLinkFormat.getFileDeletedFormat();
	}
}
