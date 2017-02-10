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

package com.kintosoft.svnwebclient.graph.model.jira;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.convert.BeanConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DataTransferObject(converter = BeanConverter.class)
public class Issue {

	private static final int DESCRIPTION_MAX_LENGHT = 140;

	private String keyNum;

	private String iconUrl;

	private String typeId;

	private String typeName;

	private String statusId;

	private String statusName;

	private String priorityId;

	private String priorityName;

	private String summary = "";

	private String assigneeName = "";

	private String assigneeDisplayName = "";

	private String iconUrlPriority;

	private String iconUrlStatus;

	private boolean resolved;

	private boolean isSubTask;

	private List<String> versionsFixed = new ArrayList<String>();

	private List<String> versionsAffected = new ArrayList<String>();

	private String reporter = "";

	private Date created;

	private Date updated;

	private String description = "";

	private Date dueDate;

	private String jiraBaseUrl;

	@RemoteMethod
	public String getJiraBaseUrl() {
		return jiraBaseUrl;
	}

	@RemoteMethod
	public void setJiraBaseUrl(String jiraBaseUrl) {
		this.jiraBaseUrl = jiraBaseUrl;
	}

	@RemoteMethod
	public String getKeyNum() {
		return keyNum;
	}

	@RemoteMethod
	public void setKeyNum(String keyNum) {
		this.keyNum = keyNum;
	}

	@RemoteMethod
	public String getIconUrl() {
		return iconUrl;
	}

	@RemoteMethod
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	@RemoteMethod
	public String getTypeId() {
		return typeId;
	}

	@RemoteMethod
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	@RemoteMethod
	public String getTypeName() {
		return typeName;
	}

	@RemoteMethod
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@RemoteMethod
	public String getStatusId() {
		return statusId;
	}

	@RemoteMethod
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	@RemoteMethod
	public String getStatusName() {
		return statusName;
	}

	@RemoteMethod
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@RemoteMethod
	public String getPriorityId() {
		return priorityId;
	}

	@RemoteMethod
	public void setPriorityId(String priorityId) {
		this.priorityId = priorityId;
	}

	@RemoteMethod
	public String getPriorityName() {
		return priorityName;
	}

	@RemoteMethod
	public void setPriorityName(String priorityName) {
		this.priorityName = priorityName;
	}

	@RemoteMethod
	public String getSummary() {
		return summary;
	}

	@RemoteMethod
	public void setSummary(String summary) {
		this.summary = summary;
	}

	@RemoteMethod
	public String getAssigneeName() {
		return assigneeName;
	}

	@RemoteMethod
	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}

	@RemoteMethod
	public String getAssigneeDisplayName() {
		return assigneeDisplayName;
	}

	@RemoteMethod
	public void setAssigneeDisplayName(String assigneeDisplayName) {
		this.assigneeDisplayName = assigneeDisplayName;
	}

	@RemoteMethod
	public String getIconUrlPriority() {
		return iconUrlPriority;
	}

	@RemoteMethod
	public void setIconUrlPriority(String iconUrlPriority) {
		this.iconUrlPriority = iconUrlPriority;
	}

	@RemoteMethod
	public String getIconUrlStatus() {
		return iconUrlStatus;
	}

	@RemoteMethod
	public void setIconUrlStatus(String iconUrlStatus) {
		this.iconUrlStatus = iconUrlStatus;
	}

	@RemoteMethod
	public boolean isResolved() {
		return resolved;
	}

	@RemoteMethod
	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	@RemoteMethod
	public boolean isSubTask() {
		return isSubTask;
	}

	@RemoteMethod
	public void setSubTask(boolean isSubTask) {
		this.isSubTask = isSubTask;
	}

	@RemoteMethod
	public List<String> getVersionsFixed() {
		return versionsFixed;
	}

	@RemoteMethod
	public void setVersionsFixed(List<String> versionsFixed) {
		this.versionsFixed = versionsFixed;
	}

	@RemoteMethod
	public List<String> getVersionsAffected() {
		return versionsAffected;
	}

	@RemoteMethod
	public void setVersionsAffected(List<String> versionsAffected) {
		this.versionsAffected = versionsAffected;
	}

	@RemoteMethod
	public String getReporter() {
		return reporter;
	}

	@RemoteMethod
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	@RemoteMethod
	public Date getCreated() {
		return created;
	}

	@RemoteMethod
	public void setCreated(Date created) {
		this.created = created;
	}

	@RemoteMethod
	public Date getUpdated() {
		return updated;
	}

	@RemoteMethod
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@RemoteMethod
	public String getDescription() {
		return description;
	}

	@RemoteMethod
	public void setDescription(String description) {
		this.description = description;
	}

	@RemoteMethod
	public Date getDueDate() {
		return dueDate;
	}

	@RemoteMethod
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Issue(com.atlassian.jira.issue.Issue jiraIssue, String jiraBaseUrl) {

		this.keyNum = jiraIssue.getKey();

		this.jiraBaseUrl = jiraBaseUrl;

		dueDate = jiraIssue.getDueDate();

		if (jiraIssue.getIssueTypeObject() != null) {
			this.typeId = jiraIssue.getIssueTypeObject().getId();
			this.typeName = jiraIssue.getIssueTypeObject().getName();
			this.iconUrl = jiraIssue.getIssueTypeObject().getIconUrl();
		}

		if (jiraIssue.getStatusObject() != null) {
			this.statusId = jiraIssue.getStatusObject().getId();
			this.statusName = jiraIssue.getStatusObject().getName();
			this.iconUrlStatus = jiraIssue.getStatusObject().getIconUrl();
		}

		if (jiraIssue.getPriorityObject() != null) {
			this.priorityId = jiraIssue.getPriorityObject().getId();
			this.priorityName = jiraIssue.getPriorityObject().getName();
			this.iconUrlPriority = jiraIssue.getPriorityObject().getIconUrl();
		}

		for (Version v : jiraIssue.getAffectedVersions()) {
			this.versionsAffected.add(v.getName());
		}

		for (Version v : jiraIssue.getFixVersions()) {
			this.versionsFixed.add(v.getName());
		}

		this.summary = jiraIssue.getSummary();

		this.resolved = (jiraIssue.getResolutionId() != null);

		this.isSubTask = jiraIssue.isSubTask();

		ApplicationUser user = jiraIssue.getAssignee();
		this.assigneeDisplayName = (user == null ? null : user.getDisplayName());

		user = jiraIssue.getAssigneeUser();
		this.assigneeName = (user == null ? null : user.getName());

		user = jiraIssue.getReporter();
		this.reporter = (user == null ? null : user.getDisplayName());

		this.created = jiraIssue.getCreated();
		this.updated = jiraIssue.getUpdated();
		this.description = jiraIssue.getDescription();

		if (this.description != null) {
			if (this.description.length() > DESCRIPTION_MAX_LENGHT) {
				this.description = (this.description.substring(0, 140) + "...");
			}
			this.description = this.description.replaceAll("\r", "")
					.replaceAll("\n", " ");
		}
	}
}
