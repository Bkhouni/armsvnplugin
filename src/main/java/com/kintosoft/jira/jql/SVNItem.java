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

package com.kintosoft.jira.jql;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.svnwebclient.graph.entities.ao.Action;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Echoes the the string passed in as an argument.
 */
public class SVNItem extends JQLSVNDB {


	private final ActiveObjects ao;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNItem(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}

	@Override
	public String getFunctionName() {
		return "svnItem";
	}

	@Override
	public boolean isList() {
		return true;
	}

	private static final Logger log = LoggerFactory.getLogger(SVNItem.class);

	@Override
	protected String usage() {
		return nicerLine(
				1,
				"ITEM",
				true,
				"The exact location of an item in a repository. If the path is not provided, it will search for the item name everywhere. If the item is a directory, add a '/' character at the end to include all the subdirectires. Examples: 'myfile.txt', '/foo/myfile.txt', '/foo' and '/foo/'. Case sensitive")
				+ nicerLine(
						2,
						"ACTION",
						false,
						"Case sensitive. The item action: Added, Modified, Deleted or Replaced. It must be a single character: A,M,D,R or empty to include all the actions. Case sensitive")
				+ nicerLine(3, "REPOID", false, "Integer >= 0")
				+ nicerLine(4, "LIMIT", false, "Integer >=0");
	}

	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		MessageSet messageSet = new MessageSetImpl();

		// 1 parameter is mandatory
		if (validateMimalParameters(operand).hasAnyErrors()) {
			return messageSet;
		}

		// #0 item
		if (validateStringParameterNotEmpty(messageSet, operand, 0)
				.hasAnyErrors()) {
			return messageSet;
		}

		// #1 (Optional) is the action
		if (validateActionOptional(messageSet, operand, 1).hasAnyErrors()) {
			return messageSet;
		}

		// #2 repoId
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 2,
				0, true).hasAnyErrors()) {
			return messageSet;
		}

		// #3 (Optional) is the limit
		return validateIntegerParameterEqualsOrGreaterThan(messageSet, operand,
				3, 1, true);
	}

	public List<QueryLiteral> getValues(
			QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {
		List<QueryLiteral> res = new ArrayList<QueryLiteral>();

		if (validate(queryCreationContext, operand, terminalClause)
				.hasAnyErrors()) {
			return res;
		}

		String item = operand.getArgs().get(0);

		String action = "";
		action = getOptionalString(operand, 1);
		int repoId = getOptionalInteger(operand, 2);
		int limit = getOptionalInteger(operand, 3);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getItems(repoId, item, action, limit,
				queryCreationContext.isSecurityOverriden(), searcher.getName());

		for (String issueKey : issueKeys) {
			res.add(new QueryLiteral(operand, issueKey));
		}
		return res;
	}

	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	protected List<String> getItems(long repoId, String item, String action,
			int limit, boolean overrideSecurity, String username) {

		String sql;

		Query query;
		List<Key> keys;

		String name = item;
		String path = "";
		if (item.contains("/") && !item.startsWith("/")) {
			item = "/" + item;
		}
		int pos = item.lastIndexOf("/");
		if (pos > -1) {
			path = item.substring(0, pos + 1);
			name = item.substring(pos + 1, item.length());
		}

		if (limit == 0) {
			/*sql = "select distinct k.project, k.issue from keys as k, actions as a, items as i where k.repoId=a.repoId AND k.revision=a.revision AND a.repoId=i.repoId and a.itemId=i.id";*/
			query = Query.select("k.PROJECT, k.ISSUE")
					.alias(Key.class, "k").alias(Action.class,"a").alias(Item.class,"i")
					.join(Action.class, "k.REPO_ID = a.REPO_ID AND k.REVISION_ID = a.REVISION_ID")
					.join(Item.class, "a.REPO_ID = i.REPO_ID AND a.ITEM_ID = i.ID")
					.distinct();
		} else {
			/*sql = "select k.project, k.issue, timestamp from keys as k, revisions as r, actions as a, items as i " +
					"where k.repoId=r.repoId AND k.revision=r.revision AND r.repoId=a.repoId AND r.revision=a.revision AND a.repoId=i.repoId and a.itemId=i.id";*/
			query = Query.select("k.PROJECT, k.ISSUE")
					.alias(Key.class, "k").alias(Action.class,"a").alias(Item.class,"i").alias(Revision.class, "r")
					.join(Action.class, "r.ID = a.REVISION_ID AND k.REVISION = a.REVISION")
					.join(Item.class, "a.REPO_ID = i.REPO_ID AND a.ITEM_ID = i.ID")
					.join(Revision.class,"k.REPO_ID = r.REPO_ID AND k.REVISION_ID = r.ID AND r.REPO_ID = a.REPO_ID ");

		}

		if (repoId > 0)
			query = query.where("k.REPO_ID = ? ", repoId);

		if (!name.isEmpty()) {
			query = query.where("i.NAME = ? ", name);
		}
		if (!path.isEmpty()) {
			if (name.isEmpty()) {
				query = query.where("i.PATH LIKE ? ", path + "%");
				/*sql += " AND path like ?";
				path += "%";*/
			} else {
				query = query.where("i.PATH = ? ", path);
				/*sql += " AND path=?";*/
			}
		}

		if (!action.isEmpty()) {
			query = query.where("a.ACTION = ? ", action);
			/*sql += " AND action=?";*/
		}

		/*sql += " AND";
		sql = overrideSecurity(sql, overrideSecurity);*/

		if (limit > 0) {
			query = query.order("r.RTIMESTAMP DESC NULLS LAST").limit(limit);
			/*sql += " ORDER BY timestamp DESC NULLS LAST";
			sql = "SELECT project, issue FROM (" + sql + ") LIMIT ?";*/
		}

		keys = newArrayList(this.ao.find(Key.class, query));
		keys = overrideSecurity(username, keys, overrideSecurity);


		/*Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			int i = 0;
			if (repoId > 0) {
				ps.setLong(++i, repoId);
			}
			if (!name.isEmpty()) {
				ps.setString(++i, name);
			}
			if (!path.isEmpty()) {
				ps.setString(++i, path);
			}
			if (!action.isEmpty()) {
				ps.setString(++i, action);
			}
			if (!overrideSecurity) {
				ps.setString(++i, username);
			}
			if (limit > 0) {
				ps.setInt(++i, limit);
			}
			ResultSet rs = ps.executeQuery();
			loadIssueKeys(res, rs);
			rs.close();
		} catch (SQLException ex) {
			log.error(ex.getMessage());
		} finally {
			PluginConnectionPool.closeStatement(ps);
			PluginConnectionPool.closeConnection(conn);
		}*/

		List<String> res = new ArrayList<String>();
		loadIssueKeys(res, keys);
		return res;
	}
}