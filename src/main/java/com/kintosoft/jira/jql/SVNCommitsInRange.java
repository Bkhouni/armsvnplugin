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


public class SVNCommitsInRange extends JQLSVNDB {
	private static final Logger log = LoggerFactory
			.getLogger(SVNCommitsInRange.class);

	private final ActiveObjects ao;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNCommitsInRange( ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}

	@Override
	protected String usage() {
		return nicerLine(1, "REPOSITORY", true, "Integer >= 1")
				+ nicerLine(2, "START REVISION", false, "Integer >= 0")
				+ nicerLine(3, "END REVISION", false, "Integer >= 0")
				+ nicerLine(4, "LIMIT", false, "Integer >= 1");
	}

	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		MessageSet messageSet = validateMimalParameters(operand);

		if (messageSet.hasAnyErrors()) {
			return messageSet;
		}

		// #0 repoId (required)
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 0,
				1, false).hasAnyErrors()) {
			return messageSet;
		}

		// #1 start revision
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 1,
				0, true).hasAnyErrors()) {
			return messageSet;
		}

		// #2 start revision
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 2,
				0, true).hasAnyErrors()) {
			return messageSet;
		}

		// #3 limit
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 3,
				1, true).hasAnyErrors()) {
			return messageSet;
		}

		return messageSet;
	}

	@Override
	public String getFunctionName() {
		return "svnCommitNumberRange";
	}

	@Override
	public boolean isList() {
		return true;
	}

	public List<QueryLiteral> getValues(
			QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {
		List<QueryLiteral> res = new ArrayList<QueryLiteral>();

		if (validate(queryCreationContext, operand, terminalClause)
				.hasAnyErrors()) {
			return res;
		}

		// it's not optional beacause 1 parameter is required
		int repoId = getOptionalInteger(operand, 0);

		int start = getOptionalInteger(operand, 1);
		int end = getOptionalInteger(operand, 2);
		if (end == 0) {
			end = Integer.MAX_VALUE;
		}
		int limit = getOptionalInteger(operand, 3);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getIssuesInRange(repoId, start, end, limit,
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

	protected List<String> getIssuesInRange(long repoId, int start, int end,
			int limit, boolean overrideSecurity, String username) {

		List<Key> keys;
		Query query;

		/*Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			String sql;*/

			if (limit == 0) {

				query = Query.select().join(Revision.class, "k.REVISION_ID = r.ID")
						.alias(Revision.class,"r")
						.alias(Key.class, "k")
						.distinct().where("r.REPO_ID = ? AND r.ID BETWEEN ? AND ?", repoId, start, end);

				/*sql = "SELECT DISTINCT PROJECT, ISSUE FROM KEYS WHERE repoId=? AND revision BETWEEN ? AND ?";
				sql += " AND";
				sql = overrideSecurity(sql, overrideSecurity);*/
			} else {

				query = Query.select().join(Revision.class, "k.REVISION_ID = r.ID")
						.alias(Revision.class,"r")
						.alias(Key.class, "k")
						.distinct().where("r.REPO_ID = ? AND r.ID BETWEEN ? AND ?", repoId, start, end)
						.order("r.ID DESC NULLS LAST")
						.limit(limit);

				/*sql = "SELECT DISTINCT PROJECT, ISSUE, REVISION FROM KEYS WHERE repoId=? AND revision BETWEEN ? AND ?";
				sql += " AND";
				sql = overrideSecurity(sql, overrideSecurity);
				sql += "ORDER BY REVISION DESC NULLS LAST";
				sql = "select distinct project, issue from (" + sql
						+ ") LIMIT ?";*/
			}

			keys = newArrayList(this.ao.find(Key.class, query));

			/*ps = conn.prepareStatement(sql);
			int i = 0;
			ps.setLong(++i, repoId);
			ps.setLong(++i, start);
			ps.setLong(++i, end);

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