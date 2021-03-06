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


public class SVNMultiRepository extends JQLSVNDB {

	private final ActiveObjects ao;

	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNMultiRepository(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}


	@Override
	public String getFunctionName() {
		return "svnMultiRepository";
	}

	private static final Logger log = LoggerFactory
			.getLogger(SVNMultiRepository.class);

	public List<QueryLiteral> getValues(
			QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {

		List<QueryLiteral> res = new ArrayList<QueryLiteral>();

		if (validate(queryCreationContext, operand, terminalClause)
				.hasAnyErrors()) {
			return res;
		}

		int minRepos = getOptionalInteger(operand, 0);
		int limit = getOptionalInteger(operand, 1);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getIssuesWithAMinimalAmountOfAuthors(minRepos,
				limit, queryCreationContext.isSecurityOverriden(),
				searcher.getName());

		for (String issueKey : issueKeys) {
			res.add(new QueryLiteral(operand, issueKey));
		}

		return res;
	}

	@Override
	public boolean isList() {
		return true;
	}

	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	@Override
	protected String usage() {
		return nicerLine(1, "MIN REPOSITORIES COUNT", true, "Integer >=2")
				+ nicerLine(2, "LIMIT", false, "Integer >=1");
	}

	@Override
	public MessageSet validate(ApplicationUser searcher,
			FunctionOperand operand, TerminalClause terminalClause) {
		MessageSet messageSet = super.validateMimalParameters(operand);

		if (messageSet.hasAnyErrors()) {
			return messageSet;
		}

		// #0 Repos count
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 0,
				2, false).hasAnyErrors()) {
			return messageSet;
		}

		// #2 limit
		return validateIntegerParameterEqualsOrGreaterThan(messageSet, operand,
				2, 1, true);

	}

	protected List<String> getIssuesWithAMinimalAmountOfAuthors(int minRepos,
			int limit, boolean overrideSecurity, String username) {

		Query query;
		List<Key> keys;

		/*Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			String sql = "SELECT DISTINCT PROJECT, ISSUE, COUNT(DISTINCT REPOID) AS REPOS FROM KEYS WHERE";

			sql = overrideSecurity(sql, overrideSecurity);

			sql += " GROUP BY PROJECT, ISSUE HAVING REPOS >= ?";

			sql = "SELECT DISTINCT PROJECT, ISSUE FROM (" + sql + ")";*/

			Query q1 = Query.select("PROJECT, ISSUE").distinct()
					.from(Key.class)
					.group("PROJECT, ISSUE HAVING COUNT(DISTINCT REPO_ID) >= " + minRepos);
			query = Query.select("PROJECT, ISSUE").distinct().from(q1.getTable());

			if (limit > 0) {
				/*sql += "LIMIT ?";*/
				query = query.limit(limit);
			}

			keys = newArrayList(this.ao.find(Key.class, query));
			keys = overrideSecurity(username, keys, overrideSecurity);

			/*ps = conn.prepareStatement(sql);
			int i = 0;

			if (!overrideSecurity) {
				ps.setString(++i, username);
			}

			ps.setLong(++i, minRepos);

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