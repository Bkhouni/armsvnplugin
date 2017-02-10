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

package com.kintosoft.jira.actions;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.projecttabpanels.SubversionProjectRevisionAction;
import com.kintosoft.jira.utils.Utils;
import com.kintosoft.svnwebclient.graph.db.StatsService;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


public class SWCTopMenuAction extends JiraWebActionSupport {


	private WebResourceManager wrm;


	private final ActiveObjects ao;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SWCTopMenuAction(ALMMultipleSubversionRepositoryManager almsrm, ActiveObjects ao) {
		this.wrm = ComponentAccessor.getWebResourceManager();
		this.almsrm = almsrm;
		this.ao = ao;
	}

	private List<SubversionProjectRevisionAction> commits;
	private String title;

	@Override
	public String doDefault() throws Exception {
		return SUCCESS;
	}

	public String getRepoId() {
		return ActionContext.getRequest().getParameter("repoId");
	}

	public String getJsp() {
		String jsp = ActionContext.getRequest().getParameter("jsp");
		if (jsp == null) {
			jsp = "directoryContent";
		}
		return jsp + ".jsp";
	}

	public String getQueryString() {
		return ActionContext.getRequest().getQueryString();
	}

	public String doFilterCommitsByAuthor() {
		wrm.requireResource("com.kintosoft.jira.subversion-plus:subversion-alm-resource-js");

		String author = ActionContext.getRequest().getParameter("author");
		String filterId = ActionContext.getRequest().getParameter("filterId");

		title = "The commits of the '" + author + "' Subversion user to the '"
				+ Utils.getFilerName(filterId) + "' filter";

		try {
			StatsService ss = new StatsService(ao);
			List<SVNLogEntry> logEntries = StatsService
					.getUserCommitsForJIRAFilter(author,
							Long.parseLong(filterId));
			commits = new ArrayList<SubversionProjectRevisionAction>();
			for (SVNLogEntry logEntry : logEntries) {
				commits.add(new SubversionProjectRevisionAction(almsrm,logEntry,
						 null, logEntry.getRepoId()));
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return "filter-commits";

	}

	public String doFilterCommitsByDate() {
		wrm.requireResource("com.kintosoft.jira.subversion-plus:subversion-alm-resource-js");

		String date = ActionContext.getRequest().getParameter("day");
		String filterId = ActionContext.getRequest().getParameter("filterId");

		title = "The commits made in " + date + " included in the '"
				+ Utils.getFilerName(filterId) + "' filter";

		try {
			StatsService ss = new StatsService(ao);
			List<SVNLogEntry> logEntries = StatsService
					.getDateCommitsForJIRAFilter(date, Long.parseLong(filterId));
			commits = new ArrayList<SubversionProjectRevisionAction>();
			for (SVNLogEntry logEntry : logEntries) {
				commits.add(new SubversionProjectRevisionAction(almsrm,logEntry,
						 null, logEntry.getRepoId()));
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return "filter-commits";

	}

	public List<SubversionProjectRevisionAction> getCommits() {
		return commits;
	}

	public HttpServletRequest getRequest() {
		return ActionContext.getRequest();
	}

	public String getTitle() {
		return title;
	}
}
