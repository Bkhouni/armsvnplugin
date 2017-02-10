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


public class SVNMultiCommit extends JQLSVNDB {


	private final ActiveObjects ao;

	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNMultiCommit( ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}
	@Override
	public String getFunctionName() {
		return "svnMultiCommit";
	}

	private static final Logger log = LoggerFactory
			.getLogger(SVNMultiCommit.class);

	public List<QueryLiteral> getValues(
			QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {

		List<QueryLiteral> res = new ArrayList<QueryLiteral>();

		if (validate(queryCreationContext, operand, terminalClause)
				.hasAnyErrors()) {
			return res;
		}

		int minCommits = getOptionalInteger(operand, 0);
		int limit = getOptionalInteger(operand, 1);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getIssuesWithAMinimalAmountOfCommits(
				minCommits, limit, queryCreationContext.isSecurityOverriden(),
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
		return nicerLine(1, "MIN COMMITS COUNT", true, "Integer >=2")
				+ nicerLine(2, "LIMIT", false, "Integer >=1");
	}

	@Override
	public MessageSet validate(ApplicationUser searcher,
			FunctionOperand operand, TerminalClause terminalClause) {
		MessageSet messageSet = super.validateMimalParameters(operand);

		if (messageSet.hasAnyErrors()) {
			return messageSet;
		}

		// #0 commits amount
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 0,
				2, false).hasAnyErrors()) {
			return messageSet;
		}

		// #2 limit
		return validateIntegerParameterEqualsOrGreaterThan(messageSet, operand,
				1, 1, true);

	}

	//VALIANTYS - DONE
	protected List<String> getIssuesWithAMinimalAmountOfCommits(int minCommits,
			int limit, boolean overrideSecurity, String username) {

		Query query;
		List<Key> keys;
	  /*
		Connection conn = null;
		PreparedStatement ps = null;


		try {
			conn = getConnection();
			String sql = "SELECT PROJECT, ISSUE, count(distinct REVISION) as COMMITS FROM KEYS WHERE";

			sql = overrideSecurity(sql, overrideSecurity);

			sql += " GROUP BY PROJECT, ISSUE HAVING COMMITS >= ?";          */

			query = Query.select("PROJECT, ISSUE").group("PROJECT, ISSUE HAVING COUNT(DISTINCT REVISION_ID) >= " + minCommits);

			if (limit > 0) {
				/*sql = limitAndSort(sql);*/
				query = limitAndSort(query, limit);
			}

			keys = newArrayList(this.ao.find(Key.class, query));
			keys = overrideSecurity(username, keys, overrideSecurity);

			/*ps = conn.prepareStatement(sql);
			int i = 0;
			if (!overrideSecurity) {
				ps.setString(++i, username);
			}

			ps.setLong(++i, minCommits);
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
		}
*/
		List<String> res = new ArrayList<String>();
		loadIssueKeys(res, keys);
		return res;
	}

	protected Query  limitAndSort(Query query, int limit) {
		/*sql = "SELECT f.project, f.issue, r.repoId, r.revision, r.timestamp AS time FROM ("
				+ sql
				+ ") AS f, keys AS k, revisions AS r where k.project=f.project AND k.issue=f.issue and r.repoId=k.repoId AND r.revision=k.revision ORDER BY timestamp DESC";

		sql = "SELECT DISTINCT  project, issue FROM(" + sql + ") LIMIT ?";*/

		query = Query.select().alias(Key.class,"k").alias(Revision.class, "r").join(Revision.class, "r.REPO_ID=k.REPO_ID AND r.ID = k.REVISION_ID").order("r.RTIMESTAMP DESC").limit(limit);

		return query;
	}
}