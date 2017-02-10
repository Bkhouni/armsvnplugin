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

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.svnwebclient.graph.entities.ao.RepoConfig;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.managers.RepositoryConfigurationManager;
import com.kintosoft.svnwebclient.graph.entities.managers.RepositoryManager;
import org.polarion.svnwebclient.configuration.WebConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.collect.Lists.newArrayList;

public class DBReposMrg {

	private static Logger log = LoggerFactory.getLogger(DBReposMrg.class);

	public static final String STATUS_CREATED = "created";

	public static final List<String> ignoreParameters;

	public static ActiveObjects ao;


	static {
		ignoreParameters = new ArrayList<String>();
		ignoreParameters.add(WebConfigurationProvider.ROOT_URL);
		ignoreParameters.add(WebConfigurationProvider.DISPLAY_NAME);
		ignoreParameters.add(WebConfigurationProvider.REPO_ID);
		ignoreParameters.add(WebConfigurationProvider.SHOW_STACK_TRACE);

	}

	public static void setAO(ActiveObjects ao){
		DBReposMrg.ao = ao;
	}


	public static void createRepository(WebConfigurationProvider conf)
			throws SQLException {
		String root = conf.getParameter(WebConfigurationProvider.ROOT_URL);
		String name = conf.getParameter(WebConfigurationProvider.DISPLAY_NAME);
		long repoId = -1;

	/*	Connection conn = null;
		try {
			conn = getConnection();*/

			repoId = createRepositoryInTrans(root, name);

			addRepositoryConfigurationInTrans(repoId, conf);

			conf.getParameters().put(WebConfigurationProvider.REPO_ID,
					Long.toString(repoId));
/*
			conn.commit();
		} finally {
			PluginConnectionPool.closeConnection(conn);
		}*/
	}

	private static long createRepositoryInTrans(
			String rootUrl, String name) throws SQLException {

		RepositoryManager repositoryManager = new RepositoryManager(ao);
		repositoryManager.addRepository(rootUrl, name);

		/*PreparedStatement ps = null;
		try {
			String sql = "insert into REPOSITORIES(URL, NAME) VALUES(?,?)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, rootUrl);
			ps.setString(2, name);
			ps.execute();*/

		long repoId = getRepositoryIdFromNameInTrans(name);
		if (repoId == -1) {
			throw new SQLException("The repository " + name
					+ " should exist");
		}
			return repoId;
		/*} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
	}

	private static void addRepositoryConfigurationInTrans(
			long repoId, WebConfigurationProvider conf) throws SQLException {

		RepositoryConfigurationManager repositoryConfigurationManager = new RepositoryConfigurationManager(ao);


		/*String sql = "insert into REPOSITORIES_CONFIGURATION(REPOID,KEY,VALUE) VALUES(?,?,?)";

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);*/

			for (Entry<String, String> prop : conf.getParameters().entrySet()) {
				if (ignoreParameters.contains(prop.getKey())) {
					continue;
				}

				RepoConfig repo = repositoryConfigurationManager.addRepositoryConfiguration(repoId, prop.getKey(),prop.getValue());

				/*ps.setLong(1, repoId);
				ps.setString(2, prop.getKey());
				String value = prop.getValue();
				value = value == null ? "" : value;
				ps.setString(3, prop.getValue());
				log.debug(sql + " : (" + prop.getKey() + "," + prop.getValue()
						+ ")");
				ps.execute();*/
			}
		/*} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
	}

	private static long getRepositoryIdFromtUrlInTrans(
			String rootUrl) throws SQLException {

		RepositoryManager repositoryManager = new RepositoryManager(ao);
		Repository repo = repositoryManager.getRepositoryByURL(rootUrl);

		return repo.getID();

		/*PreparedStatement ps = null;
		try {
			String sql = "select id from REPOSITORIES where url=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, rootUrl);
			ResultSet rs = ps.executeQuery();
			long res = -1L;
			if (rs.next()) {
				res = rs.getLong(1);
			}
			rs.close();
			return res;
		} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
	}

	private static long getRepositoryIdFromNameInTrans(
			String name) throws SQLException {

		RepositoryManager repositoryManager = new RepositoryManager(ao);
		Repository repo = repositoryManager.getRepositoryByName(name);

		long res = -1L;
		if(repo != null)
			res = repo.getID();

		/*PreparedStatement ps = null;
		try {
			String sql = "select id from REPOSITORIES where name=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			long res = -1L;
			if (rs.next()) {
				res = rs.getLong(1);
			}
			rs.close();
			return res;
		} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
		return repo.getID();
	}

	public static void updateRepository(WebConfigurationProvider conf)
			throws SQLException {
		long repoId = Long.parseLong(conf
				.getParameter(WebConfigurationProvider.REPO_ID));
		String rootUrl = conf.getParameter(WebConfigurationProvider.ROOT_URL);
		String name = conf.getParameter(WebConfigurationProvider.DISPLAY_NAME);

		/*Connection conn = null;
		try {
			conn = getConnection();*/

		updateRepositoryInTrans(repoId, rootUrl, name);
		updateRepositoryConfigurationInTrans(repoId, conf);
			/*conn.commit();
		} finally {
			PluginConnectionPool.closeConnection(conn);
		}*/
	}

	private static void updateRepositoryInTrans(long repoId,
			String rootUrl, String name) throws SQLException {

		RepositoryManager repositoryManager = new RepositoryManager(ao);
		repositoryManager.updateRepository(rootUrl, name, repoId);

		/*String sql = "UPDATE REPOSITORIES SET URL=?, NAME=? WHERE ID=?";

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, rootUrl);
			ps.setString(2, name);
			ps.setLong(3, repoId);
			ps.execute();
		} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
	}

	private static void updateRepositoryConfigurationInTrans(
			long repoId, WebConfigurationProvider conf) throws SQLException {

		RepositoryConfigurationManager repositoryConfigurationManager = new RepositoryConfigurationManager(ao);


		/*String sql = "update REPOSITORIES_CONFIGURATION SET VALUE=? WHERE REPOID=? AND KEY=?";

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);*/

			for (Entry<String, String> prop : conf.getParameters().entrySet()) {
				if (ignoreParameters.contains(prop.getKey())) {
					continue;
				}

				String value = prop.getValue();
				value = value == null ? "" : prop.getValue();
				repositoryConfigurationManager.updateRepositoryConfigurationValue(repoId, prop.getKey(), value);
			}
				/*ps.setString(1, prop.getValue());
				ps.setLong(2, repoId);
				ps.setString(3, prop.getKey());
				ps.execute();
			}
		} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/

	}

	public static WebConfigurationProvider getRepositoryConfiguration(
			long repoId) throws SQLException {

		/*Connection conn = null;
		try {
			conn = getConnection();*/

			return getRepositoryConfigurationInTrans(repoId);
		/*} finally {
			PluginConnectionPool.closeConnection(conn);
		}*/
	}

	public static WebConfigurationProvider getRepositoryConfigurationInTrans(
			long repoId) throws SQLException {
		/*String sql = "select * from REPOSITORIES_CONFIGURATION where REPOID=?";*/

		RepositoryConfigurationManager repositoryConfigurationManager = new RepositoryConfigurationManager(ao);
		List<RepoConfig> repoConfigs = repositoryConfigurationManager.getRepositoryConfigurationByRepoId((int)repoId);

		RepositoryManager repositoryManager = new RepositoryManager(ao);


				WebConfigurationProvider conf = new WebConfigurationProvider();
		conf.setParameters(new HashMap<String, String>());



		for(RepoConfig repoConfig : repoConfigs) {
			String key = repoConfig.getKey();
			String value = repoConfig.getValue();
			conf.getParameters().put(key, value);
		}




		/*PreparedStatement ps = null;
		try {

			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String key = rs.getString("KEY");
				String value = rs.getString("VALUE");
				conf.getParameters().put(key, value);
			}

			rs.close();

			ps.close();

			sql = "select * from REPOSITORIES where ID=?";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);*/

			Repository repo = ao.get(Repository.class, (int)repoId);

			/*rs = ps.executeQuery();*/

			if (repo == null) {
				throw new SQLException(
						"incoherent data. This should not happen. Please, contact at your Administrator");
			}

			String name = repo.getName();
			String url = repo.getUrl();

			/*rs.close();*/

			conf.getParameters().put(WebConfigurationProvider.ROOT_URL, url);
			conf.getParameters().put(WebConfigurationProvider.DISPLAY_NAME,
					name);
			conf.getParameters().put(WebConfigurationProvider.REPO_ID,
					Long.toString(repoId));

			return conf;
		/*} finally {
			PluginConnectionPool.closeStatement(ps);
		}*/
	}

	public static WebConfigurationProvider getRepositoryConfiguration(
			String rootUrl) throws SQLException {
		/*Connection conn = null;
		try {
			conn = getConnection();*/

			long repoId = getRepositoryIdFromtUrlInTrans(rootUrl);

			return getRepositoryConfigurationInTrans(repoId);
		/*} finally {
			PluginConnectionPool.closeConnection(conn);
		}*/
	}

	public static List<WebConfigurationProvider> getRepositoriesConfiguration()
			throws SQLException {
		/*String sql = "select * from REPOSITORIES";*/

		List<Repository> repositories = newArrayList(ao.find(Repository.class));


		List<WebConfigurationProvider> res = new ArrayList<WebConfigurationProvider>();

		for(Repository repository : repositories) {
			long repoId = repository.getID();
			WebConfigurationProvider conf = getRepositoryConfigurationInTrans(
					repoId);
			res.add(conf);
		}

		/*Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();

			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				long repoId = rs.getLong("ID");
				WebConfigurationProvider conf = getRepositoryConfigurationInTrans(
						conn, repoId);
				res.add(conf);
			}
			rs.close();
			return res;

		} catch (SQLException ex) {
			log.warn(ex.getMessage());
			return null;
		} finally {
			PluginConnectionPool.closeStatement(ps);
			PluginConnectionPool.closeConnection(conn);
		}*/

		return res;
	}

	private static boolean runPreparedStatementForRepoId(Connection conn,
			String sql, long repoId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			return ps.execute();
		} finally {
			PluginConnectionPool.closeStatement(ps);
		}
	}

	synchronized public static boolean deleteRepository(long repoId, ActiveObjects activeObjects) {
		RepositoryManager repositoryManager = new RepositoryManager(activeObjects);
		repositoryManager.deleteRepository(repoId);

		return true;

	}
}
