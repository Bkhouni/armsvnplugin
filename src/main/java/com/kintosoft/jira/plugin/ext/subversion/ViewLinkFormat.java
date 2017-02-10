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

/**
 * Struct holding the linkformat.* link format parameters for a repository.
 * 
 * @author Chenggong Lu
 */
public class ViewLinkFormat {
	private String type;
	private String changesetFormat;
	private String fileAddedFormat;
	private String viewFormat;
	private String fileModifiedFormat;
	private String fileReplacedFormat;
	private String fileDeletedFormat;

	public ViewLinkFormat(String type, String changesetFormat,
			String fileAddedFormat, String fileModifiedFormat,
			String fileReplacedFormat, String fileDeletedFormat,
			String pathLinkFormat) {
		this.setType(type);
		this.setViewFormat(pathLinkFormat);
		this.setFileAddedFormat(fileAddedFormat);
		this.setFileModifiedFormat(fileModifiedFormat);
		this.setFileReplacedFormat(fileReplacedFormat);
		this.setFileDeletedFormat(fileDeletedFormat);
		this.setChangesetFormat(changesetFormat);
	}

	public void fillFormatFromOther(ViewLinkFormat other) {
		if (other != null) {
			if (this.getType() == null) {
				this.setType(other.getType());
			}
			if (this.getViewFormat() == null) {
				this.setViewFormat(other.viewFormat);
			}
			if (this.getFileAddedFormat() == null) {
				this.setFileAddedFormat(other.fileAddedFormat);
			}
			if (this.getFileModifiedFormat() == null) {
				this.setFileModifiedFormat(other.fileModifiedFormat);
			}
			if (this.getFileReplacedFormat() == null) {
				this.setFileReplacedFormat(other.fileReplacedFormat);
			}
			if (this.getFileDeletedFormat() == null) {
				this.setFileDeletedFormat(other.fileDeletedFormat);
			}
			if (this.getChangesetFormat() == null) {
				this.setChangesetFormat(other.changesetFormat);
			}
		}

	}

	public String toString() {
		return "pathLink: " + getViewFormat() + " addedFormat: "
				+ getFileAddedFormat() + " modifiedFormat: "
				+ getFileModifiedFormat() + " replacedFormat: "
				+ getFileReplacedFormat() + " deletedFormat: "
				+ getFileDeletedFormat();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChangesetFormat() {
		return changesetFormat;
	}

	public void setChangesetFormat(String changesetFormat) {
		this.changesetFormat = changesetFormat;
	}

	public String getFileAddedFormat() {
		return fileAddedFormat;
	}

	public void setFileAddedFormat(String fileAddedFormat) {
		this.fileAddedFormat = fileAddedFormat;
	}

	public String getViewFormat() {
		return viewFormat;
	}

	public void setViewFormat(String viewFormat) {
		this.viewFormat = viewFormat;
	}

	public String getFileModifiedFormat() {
		return fileModifiedFormat;
	}

	public void setFileModifiedFormat(String fileModifiedFormat) {
		this.fileModifiedFormat = fileModifiedFormat;
	}

	public String getFileReplacedFormat() {
		return fileReplacedFormat;
	}

	public void setFileReplacedFormat(String fileReplacedFormat) {
		this.fileReplacedFormat = fileReplacedFormat;
	}

	public String getFileDeletedFormat() {
		return fileDeletedFormat;
	}

	public void setFileDeletedFormat(String fileDeletedFormat) {
		this.fileDeletedFormat = fileDeletedFormat;
	}
}
