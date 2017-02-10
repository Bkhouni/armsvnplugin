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
import com.kintosoft.jira.utils.Keys;
import com.kintosoft.svnwebclient.graph.entities.ao.AppConfig;
import com.kintosoft.svnwebclient.graph.entities.managers.ApplicationConfigurationManager;
import org.apache.log4j.LogManager;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;


public class PluginConnectionPool {
	private static final org.apache.log4j.Logger log = LogManager.getLogger("atlassian.plugin");

	private static final int DEFAULT_CONNECTION_TIMEOUT = 10;


	private static PluginConnectionPool me;

	private JdbcConnectionPool pool;

	private String indexPath;

	private String jdbcUrl;

	private String clientId;



	public static ActiveObjects activeObjects;

	public static ActiveObjects getActiveObjects() {
		return activeObjects;
	}

	public static void setActiveObjects(ActiveObjects activeObjects) {
		PluginConnectionPool.activeObjects = activeObjects;
	}

	public PluginConnectionPool(ActiveObjects activeObjects) {
		me = this;
        PluginConnectionPool.activeObjects = activeObjects;
	}

	public void initialize(String path, String clientId, boolean mvcc) {

		indexPath = path;
		this.clientId = clientId;

		/*initialize(mvcc);*/
	}

	public void initialize(boolean mvcc) {
		jdbcUrl = "jdbc:h2:file:";
		jdbcUrl += indexPath;
		jdbcUrl += ";MVCC=" + Boolean.toString(mvcc);
		// jdbcUrl += ";DB_CLOSE_ON_EXIT=TRUE";

		log.info("Connection pool is going to be initialized: " + jdbcUrl);
		JdbcDataSource h2Datasource = new JdbcDataSource();
		h2Datasource.setURL(jdbcUrl);
		pool = JdbcConnectionPool.create(h2Datasource);

		log.info("Connection pool has been initialized.");

		Connection conn = null;
		try {
			conn = getConnection(null);
			pool.setLoginTimeout(DEFAULT_CONNECTION_TIMEOUT);
		} catch (Exception e) {
			log.error("Problem during database pool initialization", e);
		} finally {
			closeConnection(conn);
		}
	}

	public static int getDBConnectionPoolSize() throws SQLException {
		return Integer.parseInt(getProperty(Keys.db.poolsize));
	}

	public static boolean getShareConnections() throws SQLException {
		return Boolean.parseBoolean(getProperty(Keys.db.shareconnections));
	}

	public  static  boolean getRequireTrackerSession() throws SQLException {
		return Boolean.parseBoolean(getProperty(Keys.db.requiresession));
	}

	public void setDBConnectionPoolSize(int value, ActiveObjects ao) throws SQLException {
		setProperty(Keys.db.poolsize, Integer.toString(value));
	}

	public void setShareConnections(boolean value, ActiveObjects ao) throws SQLException {
		setProperty(Keys.db.shareconnections, Boolean.toString(value));
	}

	public void setRequireTrackerSession(boolean value, ActiveObjects ao) throws SQLException {
		setProperty(Keys.db.requiresession, Boolean.toString(value));
	}

	/*public static PluginConnectionPool getInstance() {
		if (me == null) {
			new PluginConnectionPool();
		}
		return me;
	}*/

	public void shutdown() {
		try {
			Connection con = pool.getConnection();
			Statement st = con.createStatement();
			String shutdownCmd = "SHUTDOWN"
					+ (getCompactOnClose() ? " COMPACT" : "");
			log.info("Closing the database... " + shutdownCmd);
			st.execute(shutdownCmd);
			con.close();
			log.info("Closing the connection pool... ");
			pool.dispose();
			log.info("The database is closed. Active connections: "
					+ pool.getActiveConnections());
			pool = null;
		} catch (SQLException e) {
			log.warn("Problem during pool shutdown", e);
		}
	}

	public static void closeConnection(Connection conn) {
		if (conn == null) {
			return;
		}
		try {
			conn.rollback();
			me.setSchema(conn, null);
			conn.close();
		} catch (SQLException e) {
			log.warn(e.getMessage());
		}
	}

	private String getSchemaNameFromClientId(String clientId) {
		clientId = clientId.replaceAll("-", "_");
		return "CLIENT_" + clientId;
	}

	private Connection getConnection(String clientId) throws SQLException {
		if (pool == null) {
			throw new SQLException("The pool has not been initialized.");
		}
		Connection conn = pool.getConnection();
		conn.rollback();
		conn.setAutoCommit(false);
		setSchema(conn, clientId);
		return conn;
	}

	/*public static Connection getConnection() throws SQLException {
		if (getInstance().clientId == null) {
			throw new SQLException("The clientId has not been initialized.");
		}
		return getInstance().getConnection(getInstance().clientId);
	}*/

	private void setSchema(Connection conn, String clientId)
			throws SQLException {
		String schemaName = "PUBLIC";
		if (clientId != null) {
			schemaName = getSchemaNameFromClientId(clientId);
		}
		executeSQL(conn, "SET SCHEMA " + schemaName);
	}

	private static boolean executeSQL(Connection conn, String sql)
			throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			return st.execute(sql);
		} finally {
			closeStatement(st);
		}
	}

	public static void closeStatement(Statement st) {
		if (st == null) {
			return;
		}
		try {
			st.close();
		} catch (SQLException e) {
			log.warn(e.getMessage());
		}
	}

	public void deleteSchemaInTrans(String clientId) throws SQLException {
		Connection conn = null;
		try {
			conn = getConnection(null);
			executeSQL(conn, "DROP SCHEMA IF EXISTS "
					+ getSchemaNameFromClientId(clientId));
		} finally {
			closeConnection(conn);
		}
	}

	public static void createSchema() throws SQLException, IOException {

		if (getPropertySorroundingExceptions(Keys.db.schedule) == null) {
			setPropertySorroundingExceptions(Keys.db.schedule, "3600");
		}
		if (getPropertySorroundingExceptions(Keys.db.accepted) == null) {
			setPropertySorroundingExceptions(Keys.db.accepted, "false");
		}
		if (getPropertySorroundingExceptions(Keys.db.poolsize) == null) {
			setPropertySorroundingExceptions(Keys.db.poolsize, "100");
		}

		// backward compatibility and initialization
		if (getPropertySorroundingExceptions(Keys.db.svntimeoutconnection) == null) {
			setPropertySorroundingExceptions(Keys.db.svntimeoutconnection, "120000");
			setPropertySorroundingExceptions(Keys.db.svntimeoutread, "120000");
		}

		if (getPropertySorroundingExceptions(Keys.db.compactonclose) == null) {
			setPropertySorroundingExceptions(Keys.db.compactonclose, "false");
		}

		if (getPropertySorroundingExceptions(Keys.db.maxindexingthreads) == null) {
			setPropertySorroundingExceptions(Keys.db.maxindexingthreads, "3");
		}



	}

	public void dropSchema(String clientId) throws SQLException, IOException {

		Connection c = null;
		try {
			c = getConnection(null);
			String schemName = getSchemaNameFromClientId(clientId);
			executeSQL(c, "drop schema if exists " + schemName);
			c.commit();
		} finally {
			closeConnection(c);
		}
	}

	public void deleteSchema(String clientId) {
		if (clientId == null) {
			return;
		}

	}

	private void executeScript(String clientId, InputStream is)
			throws SQLException, IOException {

		Connection conn = null;
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is);
			conn = getConnection(clientId);
			RunScript.execute(conn, isr);
			conn.commit();
		} finally {
			if (isr != null)
				isr.close();
			closeConnection(conn);
		}
	}

	public void setConsolePassword(String password) throws SQLException {

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection(null);
			ps = conn
					.prepareStatement("create user if not exists h2admin password ? admin");
			ps.setString(1, UUID.randomUUID().toString());
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("alter user h2admin set password ?");
			ps.setString(1, password);
			ps.execute();
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(conn);
		}
	}

	public String getPoolUrl() {
		return jdbcUrl;
	}

	public String getIndexPath() {
		int i = indexPath.lastIndexOf(File.separator);
		if (i == -1) {
			return "<Unknown>";
		}
		return indexPath.substring(0, i);
	}

	private String getSchemaname() {
		return getSchemaNameFromClientId(clientId);
	}

	public static String getSchemaName() {
		/*return getInstance().getSchemaname();*/
		return null;
	}

	public static String getProperty(String key) {
		/*Connection conn = null;
		try {*/

			/*conn = getConnection();*/
			return getPropertyInTrans(key);
		/*} finally {
			closeConnection(conn);
		}*/

	}

	protected static String getPropertyInTrans(String key)
			 {

        ApplicationConfigurationManager applicationConfigurationManager = new ApplicationConfigurationManager(activeObjects);
        AppConfig appconf = applicationConfigurationManager.getApplicationConfigurationByKey(key);
	    String value = null;
        if(appconf != null)
            value = appconf.getValue();
        return value;

	}

	public static void setProperty(String key, String value)
			throws SQLException {
		Properties props = new Properties();
		props.put(key, value);
		setProperties(props);
	}

	public static void setProperties(Properties props) throws SQLException {

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            setPropertyInTrans(key, value);
        }

		/*Connection conn = null;
		PreparedStatement psInsert = null;
		PreparedStatement psUpdate = null;

		try {
			conn = getConnection();
			psInsert = conn
					.prepareStatement("insert into APPLICATION_CONFIGURATION(value, key) values(?,?)");
			psUpdate = conn
					.prepareStatement("update APPLICATION_CONFIGURATION set value=? where key=?");

			for (String key : props.stringPropertyNames()) {
				String value = props.getProperty(key);
				setPropertyInTrans(conn, psInsert, psUpdate, key, value);
			}
			conn.commit();
		} finally {
			closeStatement(psInsert);
			closeStatement(psUpdate);
			closeConnection(conn);
		}*/
	}

	protected static void setPropertyInTrans(String key, String value) throws SQLException {

	    ApplicationConfigurationManager applicationConfigurationManager = new ApplicationConfigurationManager(activeObjects);
        applicationConfigurationManager.addApplicationConfiguration(key,value);
		/*String oldValue = getPropertyInTrans(key);*/

		/*PreparedStatement ps;
		if (oldValue == null) {
			ps = psInsert;
		} else {
			ps = psUpdate;
		}
		ps.setString(1, value);
		ps.setString(2, key);
		ps.execute();*/
	}

	public static String getPropertySorroundingExceptions(String key) {

		return getProperty(key);

	}

	public static void setPropertySorroundingExceptions(String key, String value) {
		try {
			setProperty(key, value);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
	}

	public static int getSVNConnectionTimeout() {
		return Integer
				.parseInt(getPropertySorroundingExceptions(Keys.db.svntimeoutconnection));
	}

	public static void setSVNConnectionTimeout(int value) {
		setPropertySorroundingExceptions(Keys.db.svntimeoutconnection,
				Integer.toString(value));
	}

	public static int getSVNReadTimeout() {
		return Integer
				.parseInt(getPropertySorroundingExceptions(Keys.db.svntimeoutread));
	}

	public static int getMaxIndexingThreads() throws NumberFormatException,
			SQLException {
		return Integer.parseInt(getProperty(Keys.db.maxindexingthreads));
	}

	public static void setSVNReadTimeout(int value) {
		setPropertySorroundingExceptions(Keys.db.svntimeoutread,
				Integer.toString(value));
	}

	public static boolean getCompactOnClose() {
		return Boolean
				.parseBoolean(getPropertySorroundingExceptions(Keys.db.compactonclose));
	}

	public static void setCompactOnClose(boolean value) {
		setPropertySorroundingExceptions(Keys.db.compactonclose,
				Boolean.toString(value));
	}

	public boolean schemaExists(String name) throws SQLException {
		String sql = "SELECT * FROM INFORMATION_SCHEMA.SCHEMATA where SCHEMA_NAME ='"
				+ name + "'";
		Connection conn = null;
		Statement st = null;
		try {
			conn = getConnection(null);
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			boolean exists = rs.next();
			rs.close();
			return exists;
		} finally {
			closeStatement(st);
			closeConnection(conn);
		}

	}
}
