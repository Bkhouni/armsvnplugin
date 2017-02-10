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
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Echoes the the string passed in as an argument.
 */

public class SVNCommitsInDateRange extends JQLSVNDB {
	private static final Logger log = LoggerFactory
			.getLogger(SVNCommitsInDateRange.class);


	private final ActiveObjects ao;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNCommitsInDateRange(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.almsrm = almsrm;
		new SWCUtils(ao, almsrm);
	}

	@Override
	protected String usage() {
		return nicerLine(1, "REPO_ID", false, "Integer >= 0")
				+ nicerLine(2, "START DATE", false,
						"Date string with 'yyyy-MM-dd' pattern")
				+ nicerLine(3, "END DATE", false,
						"Date string with 'yyyy-MM-dd' pattern")
				+ nicerLine(4, "LIMIT", false, "Integer >= 1");
	}

	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		// It not requires minimal param presence validation
		MessageSet messageSet = new MessageSetImpl();

		// #0 repoId
		if (validateIntegerParameterEqualsOrGreaterThan(messageSet, operand, 0,
				0, true).hasAnyErrors()) {
			return messageSet;
		}

		// #1 start
		if (validateDateOptional(messageSet, operand, 1).hasAnyErrors()) {
			return messageSet;
		}

		// #2 end
		if (validateDateOptional(messageSet, operand, 2).hasAnyErrors()) {
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
		return "svnCommitDateRange";
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

		int repoId = getOptionalInteger(operand, 0);

		String start = getOptionalString(operand, 1);
		Timestamp tsStart = string2timestamp(start, false);

		String end = getOptionalString(operand, 2);
		Timestamp tsEnd = string2timestamp(end, true);

		int limit = getOptionalInteger(operand, 3);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = getIssuesInDateRange(repoId, tsStart, tsEnd,
				limit, queryCreationContext.isSecurityOverriden(),
				searcher.getName());

		for (String issueKey : issueKeys) {
			res.add(new QueryLiteral(operand, issueKey));
		}

		return res;
	}

	public int getMinimumNumberOfExpectedArguments() {
		return 0;
	}

	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	//VALIANTYS - DONE
	protected List<String> getIssuesInDateRange(long repoId, Timestamp start,
			Timestamp end, int limit, boolean overrideSecurity, String username) {


		/*Connection conn = null;
		PreparedStatement ps = null;*/

		Query query;
		List<Key> keys;
		Repository repository = this.ao.get(Repository.class, (int)repoId);
/*		try {
			conn = getConnection();
			String sql;*/

			if (limit == 0) {
				/*sql = "SELECT DISTINCT PROJECT, ISSUE FROM KEYS AS K, REVISIONS AS R WHERE K.REPOID=R.REPOID AND K.REVISION=R.REVISION AND timestamp BETWEEN ? AND ?";
				sql += " AND";
				sql = overrideSecurity(sql, overrideSecurity);*/

				query = Query.select("PROJECT,ISSUE").alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.RTIMESTAMP BETWEEN ? AND ?", start, end).distinct();

				if (repoId > 0) {
					/*sql += " AND R.REPOID=?";*/
					query = query.where("r.REPO_ID = ?",repository);
				}
			} else {
				/*sql = "SELECT PROJECT, ISSUE, TIMESTAMP FROM KEYS AS K, REVISIONS AS R WHERE K.REPOID=R.REPOID AND K.REVISION=R.REVISION AND timestamp BETWEEN ? AND ?";
				sql += " AND";
				sql = overrideSecurity(sql, overrideSecurity);*/

				query = Query.select("PROJECT,ISSUE").alias(Key.class, "k").alias(Revision.class,"r")
						.join(Revision.class,"k.REVISION_ID = r.ID")
						.where("r.RTIMESTAMP BETWEEN ? AND ?", start, end);


				if (repoId > 0) {
					/*sql += " AND R.REPOID=?";*/
					query = query.where("r.REPO_ID = ?",repository);
				}

				query.order("RTIMESTAMP DESC NULLS LAST").distinct();


				/*sql += "ORDER BY TIMESTAMP DESC NULLS LAST";
				sql = "select distinct project, issue from (" + sql
						+ ") LIMIT ?";*/
			}

			keys = newArrayList(this.ao.find(Key.class, query));
			keys = overrideSecurity(username, keys, overrideSecurity);
			if (keys.size() > limit) {
				keys.removeAll(keys.subList(keys.size() - 1, limit - 1));
			}

			/*ps = conn.prepareStatement(sql);
			int i = 0;
			ps.setRTimestamp(++i, start);
			ps.setRTimestamp(++i, end);

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
		}*/
		List<String> res = new ArrayList<String>();
			loadIssueKeys(res, keys);
			return res;
	}
}