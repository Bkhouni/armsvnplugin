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

package com.kintosoft.jira.plugin.ext.subversion.agiletabpanels;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.issuetabpanels.changes.SubversionRevisionAction;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubversionAgileTabPanelContext implements ContextProvider {

	private final static Logger log = LoggerFactory
			.getLogger(SubversionAgileTabPanelContext.class);

	private final ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager;
	private final WebResourceManager wrm;


	public SubversionAgileTabPanelContext(ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager) {
		this.multipleSubversionRepositoryManager = multipleSubversionRepositoryManager;
		this.wrm = ComponentAccessor.getWebResourceManager();
	}

	public void init(Map<String, String> params) throws PluginParseException {
	}

	public Map<String, Object> getContextMap(Map<String, Object> context) {
		Map<String, Object> res = new HashMap<String, Object>();

		wrm.requireResource("com.kintosoft.jira.subversion-plus:subversion-alm-resource-js");

		Issue issue = (Issue) context.get("issue");

		List<SVNLogEntry> logEntries;
		try {
			res.put("req", context.get("req"));
			res.put("contextPath", context.get("baseurl"));
			res.put("env", "GH");
			res.put("textutils", new TextUtils());
			res.put("stringUtils", new StringUtils());
			res.put("jirakeyutils", new JiraKeyUtils());

			logEntries = multipleSubversionRepositoryManager
					.getLogEntriesByRepository(issue, 0, 10, false);
			List<SubversionRevisionAction> actions = new ArrayList<SubversionRevisionAction>();
			if (logEntries != null) {
				for (SVNLogEntry logEntry : logEntries)
					actions.add(createSubversionRevisionAction(
							logEntry.getRepoId(), logEntry));
			}
			res.put("actions", actions);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		return res;
	}

	SubversionRevisionAction createSubversionRevisionAction(long repoId,
															SVNLogEntry logEntry) {
		return new SubversionRevisionAction(multipleSubversionRepositoryManager,logEntry,
				 null, repoId);
	}

}