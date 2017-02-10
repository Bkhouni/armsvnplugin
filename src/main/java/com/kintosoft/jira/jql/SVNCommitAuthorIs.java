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
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
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


public class SVNCommitAuthorIs extends JQLSVNDB {
	private static final Logger log = LoggerFactory
			.getLogger(SVNCommitAuthorIs.class);


	public static ActiveObjects ao;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNCommitAuthorIs( ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		SVNCommitAuthorIs.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}


	@Override
	protected String usage() {
		return nicerLine(1, "AUTHOR", true,
				"The Subversion username. An empty string means author is null")
				+ nicerLine(2, "REPOSITORY", false, "Integer >= 0")
				+ nicerLine(3, "LIMIT", false, "Integer >= 1");
	}

	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {

		MessageSet messageSet = validateMimalParameters(operand);

		// #0 username
		// validating that there is one param at least ensures the minimal data
		// to run the sql query
		// any value (string) is accepted. Empty represents author is null;
		if (messageSet.hasAnyErrors()) {
			return messageSet;
		}

		// #1 repoId
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 1,
				0, true).hasAnyErrors()) {
			return messageSet;
		}

		// #2 limit
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 2,
				1, true).hasAnyErrors()) {
			return messageSet;
		}

		return messageSet;

	}

	public List<QueryLiteral> getValues(
			QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {
		List<QueryLiteral> res = new ArrayList<QueryLiteral>();

		if (validate(queryCreationContext, operand, terminalClause)
				.hasAnyErrors()) {
			return res;
		}

		String author = "";
		if (operand.getArgs().size() > 0) {

			author = operand.getArgs().get(0);
		}

		long repoId = getOptionalInteger(operand, 1);
		int limit = getOptionalInteger(operand, 2);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getIssuesCommitAuthorIs(repoId, author, limit,
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

	@Override
	public String getFunctionName() {
		return "svnAuthor";
	}

	@Override
	public boolean isList() {
		return true;
	}

	//VALIANTYS - DONE
	protected List<String> getIssuesCommitAuthorIs(long repoId, String author,
			int limit, boolean overrideSecurity, String username) {

		Query query;
		List<Key> keys;

//		String sql;
		if (limit == 0) {
			if (author.isEmpty()) {
//				sql = "SELECT DISTINCT project, issue FROM keys AS k, revisions AS r WHERE k.repoId=r.repoId AND k.revision=r.revision AND r.AUTHOR IS NULL";
				/* Repository is inside the revision */

				query = Query.select("PROJECT, ISSUE").alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.AUTHOR IS NULL").distinct();

			} else {
//				sql = "SELECT DISTINCT project, issue FROM keys AS k, revisions AS r WHERE k.repoId=r.repoId AND k.revision=r.revision AND r.AUTHOR=?";

				query = Query.select().alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.AUTHOR = ?",author);
			}

			/*sql += " AND";
			sql = overrideSecurity(username,keys, overrideSecurity);*/

			if (repoId > 0) {
//				sql += " AND r.repoId=?";
				query = query.where("r.REPOID = ?",repoId);
			}
		} else {
			if (author.isEmpty()) {
//				sql = "SELECT project, issue, timestamp FROM keys AS k, revisions AS r WHERE k.repoId=r.repoId AND k.revision=r.revision AND r.AUTHOR IS NULL";

				query = Query.select().alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.AUTHOR IS NULL");

			} else {
//				sql = "SELECT project, issue, timestamp FROM keys AS k, revisions AS r WHERE k.repoId=r.repoId AND k.revision=r.revision AND r.AUTHOR=?";

				query = Query.select().alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.AUTHOR = ?",author);
			}
			/*sql += " AND";
			sql = overrideSecurity(sql, overrideSecurity);*/

			if (repoId > 0) {
//				sql += " AND r.repoId=?";
				query = query.where("r.REPOID = ?",repoId);
			}

		/*	sql += " ORDER BY timestamp DESC NULLS LAST";

			sql = "SELECT DISTINCT project, issue FROM (" + sql + ") LIMIT ?";
*/

			query = query.order("RTIMESTAMP DESC NULLS LAST");
			if(limit > 0)
				query = query.limit(limit);
		}

		keys = newArrayList(ao.find(Key.class, query));
		keys = overrideSecurity(username, keys, overrideSecurity);
		if(keys.size() > limit)
			keys.removeAll(keys.subList(limit -1, keys.size()-1));


		/*
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			int i = 0;
			if (!author.isEmpty()) {
				ps.setString(++i, author);
			}
			if (!overrideSecurity) {
				ps.setString(++i, username);
			}
			if (repoId > 0) {
				ps.setLong(++i, repoId);
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
	*/
		List<String> res = new ArrayList<String>();
		loadIssueKeys(res, keys);
		return res;
	}
}