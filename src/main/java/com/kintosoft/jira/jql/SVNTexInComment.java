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
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.svnwebclient.graph.entities.ao.Comment;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.kintosoft.svnwebclient.utils.JdbcProperties;
import net.java.ao.DatabaseProvider;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.builder.EntityManagerBuilder;
import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Echoes the the string passed in as an argument.
 */


public class SVNTexInComment extends JQLSVNDB {
	private static final Logger log = LoggerFactory
			.getLogger(SVNTexInComment.class);


	private final ActiveObjects ao;


	private final DatabaseProvider dp;


	private final ALMMultipleSubversionRepositoryManager almsrm;


	public SVNTexInComment( ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		super(ao, almsrm);
		this.ao = ao;
		this.dp = ComponentAccessor.getComponentOfType(DatabaseProvider.class);
		this.almsrm = almsrm;
		new SWCUtils(ao,almsrm);
	}

	@Override
	protected String usage() {
		return nicerLine(1, "Lucene Query", true, "Ex: Hello AND Wor*")
				+ nicerLine(2, "REPOSITORY", false, "Integer >= 0")
				+ nicerLine(3, "LIMIT", false, "Integer >= 1");
	}

	public MessageSet validate(User searcher, FunctionOperand operand,
			TerminalClause terminalClause) {
		MessageSet messageSet = validateMimalParameters(operand);
		if (messageSet.hasAnyErrors()) {
			return messageSet;
		}

		// #0 text to search
		if (validateStringParameterNotEmpty(messageSet, operand, 0)
				.hasAnyErrors()) {
			return messageSet;
		}

		// #1 repository
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

		String text = operand.getArgs().get(0);

		long repoId = getOptionalInteger(operand, 1);
		int limit = getOptionalInteger(operand, 2);

		ApplicationUser searcher = queryCreationContext.getApplicationUser();

		List<String> issueKeys = null;
		try {
			issueKeys = getIssuesMatchingText(repoId, text, limit,
                    queryCreationContext.isSecurityOverriden(), searcher.getName());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (String issueKey : issueKeys) {
			res.add(new QueryLiteral(operand, issueKey));
		}

		return res;
	}

	@Override
	public String getFunctionName() {
		return "svnComment";
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

	public List<String> getIssuesMatchingText(long repoId, String text,
			int limit, boolean overrideSecurity, String username) throws ParserConfigurationException, SAXException, IOException, ParseException, SQLException {

		Query query;
		List<Key> keys;

		//******** Instantiate SearchableEntityManager

		/*EntityManager entityManager = newEntityManager();
		EntityManagerConfiguration entityManagerConfiguration = new EntityManagerConfiguration() {
			@Override
			public boolean useWeakCache() {
				return false;
			}

			@Override
			public NameConverters getNameConverters() {
				return entityManager.getNameConverters();
			}

			@Override
			public SchemaConfiguration getSchemaConfiguration() {
				return new SchemaConfiguration() {
					@Override
					public boolean shouldManageTable(String s, boolean b) {
						return false;
					}
				};
			}

			@Override
			public EntityInfoResolverFactory getEntityInfoResolverFactory() {
				return entityManager.getEntityInfoResolverFactory();
			}
		};

		SearchableEntityManager searchableEntityManager = new SearchableEntityManager(entityManager.getProvider(), entityManagerConfiguration, new LuceneConfiguration() {
			@Override
			public Directory getIndexDirectory() {
				try {
					return new SimpleFSDirectory(new File(ComponentAccessor.getIndexPathManager().getIndexRootPath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		});*/



		String sql = "";
		if (limit == 0) {
			/*sql = "select distinct project, issue from PUBLIC.ftl_search_data(?,0,0) as ft, keys as k where ft.SCHEMA=? AND ft.TABLE='COMMENTS' AND k.repoId=ft.keys[0] and k.revision=ft.keys[1]";
			sql += " AND";
			sql = overrideSecurity(sql, overrideSecurity);
*/

			query = Query.select("PROJECT, ISSUE")
					.distinct().from(Query.select("k.PROJECT, k.ISSUE").from(Comment.class)
							.alias(Comment.class,"c").alias(Key.class,"k")
							.join(Key.class,"k.REPO_ID = c.REPO_ID AND k.REVISION_ID = c.REVISION_ID")
							.getTable());


//			Comment[] comments = searchableEntityManager.search(Comment.class, "REPO_ID = ");


			if (repoId > 0) {
				/*sql += " AND k.repoId=?";*/
				query = query.where("k.REPO_ID = ?", repoId);
			}


		} else {
			/*sql = "select project, issue, timestamp from PUBLIC.ftl_search_data(?,0,0) as ft, keys as k, revisions AS r where ft.SCHEMA=? AND ft.TABLE='COMMENTS' AND k.repoId=ft.keys[0] and k.revision=ft.keys[1] and r.repoId=k.repoId AND r.revision=k.revision";
			sql += " AND";
			sql = overrideSecurity(sql, overrideSecurity);

			sql += " ORDER BY timestamp DESC NULLS LAST";

			sql = "SELECT distinct project, issue from (" + sql + ") LIMIT ?";*/

			query = Query.select("PROJECT, ISSUE")
					.distinct().from(Query.select("k.ISSUE, k.PROJECT, r.RTIMESTAMP").from(Comment.class)
							.alias(Comment.class,"c").alias(Key.class,"k").alias(Revision.class,"r")
							.join(Key.class,"k.REPO_ID = c.REPO_ID AND k.REVISION_ID = c.REVISION_ID")
							.join(Revision.class, "r.REPO_ID = k.REPO_ID AND r.ID = k.REVISION_ID")
							.getTable()).order("r.RTIMESTAMP DESC NULLS LAST");

			if (repoId > 0) {
				/*sql += " AND k.repoId=?";*/
				query = query.where("k.REPO_ID = ?", repoId);
			}

		}

		keys = newArrayList(this.ao.find(Key.class,query));
		keys = overrideSecurity(username, keys, overrideSecurity);

		/*Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			int i = 0;

			ps.setString(++i, text);

			ps.setString(++i, PluginConnectionPool.getSchemaName());

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


	private EntityManager newEntityManager() throws IOException, SAXException, ParserConfigurationException {
		JdbcProperties jdbcProperties = null;
		JiraHome jiraHome = ComponentAccessor.getComponentOfType(JiraHome.class);
		String dbconfigPath = jiraHome.getHomePath() + File.separator + "dbconfig.xml";
		File dbconfigFile = new File(dbconfigPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document document = builder.parse(dbconfigFile);
		NodeList jdbcDataSources = document.getElementsByTagName("jdbc-datasource");
		if(jdbcDataSources != null && jdbcDataSources.getLength() > 0) {
			Element jdbcDataSource = (Element)jdbcDataSources.item(0);
			String url = jdbcDataSource.getElementsByTagName("url").item(0).getTextContent();
			String userName = jdbcDataSource.getElementsByTagName("username").item(0).getTextContent();
			String password = jdbcDataSource.getElementsByTagName("password").item(0).getTextContent();

			jdbcProperties = new JdbcProperties(url,userName,password);
		}


		EntityManager entityManager = EntityManagerBuilder
				.url(jdbcProperties.url) // the JDBC url for database connection
				.username(jdbcProperties.username)
				.password(jdbcProperties.passord)
				.auto() // configuring the connection pool, auto detects connection pools on the classpath
				.build();

		return entityManager;

	}





}