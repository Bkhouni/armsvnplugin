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

package com.kintosoft.svnwebclient.db;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.kintosoft.svnwebclient.graph.model.jira.KeyModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFunctions {

	private final static Logger log = LoggerFactory
			.getLogger(DatabaseFunctions.class);

	final private static UserManager userManager;
	final private static IssueManager issueManager;
	final private static PermissionManager permissionManager;

	static {
		userManager = ComponentAccessor.getUserManager();
		issueManager = ComponentAccessor.getIssueManager();
		permissionManager = ComponentAccessor.getPermissionManager();
	}

	public static boolean hasVersionControlPermisions(String username,
			String project, long issueNum) {

		return hasPermisions(Permissions.BROWSE, username,
				project, issueNum);
	}

	public static boolean hasBrowsePermisions(String username, String project,
			long issueNum) {

		return hasPermisions(Permissions.BROWSE, username, project, issueNum);
	}

	public static boolean isIssue(String project, long issueNum) {
		String issueKey = project + "-" + Long.toString(issueNum);
		return issueManager.getIssueObject(issueKey) != null;
	}

	public static boolean hasPermisions(int permission, String username,
			String project, long issueNum) {
		String issueKey = project + "-" + Long.toString(issueNum);
		ApplicationUser user = userManager.getUser(username);
		if (user == null) {
			log.warn("Subversion plugin no user found:" + username);
			return false;
		}
		Issue issue = issueManager.getIssueObject(StringUtils
				.upperCase(issueKey));
		if (issue == null) {
			return false;
		}
		return permissionManager.hasPermission(permission, issue, user);
	}

	public static String issueKey(String project, long issue) {
		return project + "-" + Long.toString(issue);
	}

	public static List<KeyModel> getJIRAFilterIssues(long filterId)
			throws SQLException {

		/*SimpleResultSet rs = new SimpleResultSet();
		rs.addColumn("PROJECT", Types.VARCHAR, 10, 0);
		rs.addColumn("ISSUE", Types.BIGINT, 10, 0);
		String url = conn.getMetaData().getURL();
*/

		List<KeyModel> keys = new ArrayList<>();
		KeyModel keyObj = null;

		/*if (url.equals("jdbc:columnlist:connection")) {
			return rs;
		}

		if (filterId <= 1) {
			return rs;
		}
*/

		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext()
				.getLoggedInUser();

		SearchRequestManager searchRequestManager = ComponentAccessor
				.getComponent(SearchRequestManager.class);

		SearchRequest searchRequest = searchRequestManager
				.getSearchRequestById(user, filterId);

		SearchProvider searchProvider = ComponentAccessor
				.getComponent(SearchProvider.class);

		try {
			List<Issue> issues = searchProvider.search(
					searchRequest.getQuery(), user,
					PagerFilter.getUnlimitedFilter()).getIssues();
			String keyNum;
			String key;
			long num;
			int i = -1;
			for (Issue issue : issues) {
				keyNum = issue.getKey();
				i = keyNum.indexOf("-");
				key = keyNum.substring(0, i);
				num = Long.parseLong(keyNum.substring(i + 1, keyNum.length()));

				keyObj.setProject(key);
				keyObj.setIssue(key);
				/*rs.addRow(key, num);*/

				keys.add(keyObj);
			}
		} catch (SearchException e) {
			log.warn(e.getMessage());
		}

		return keys;
	}
}
