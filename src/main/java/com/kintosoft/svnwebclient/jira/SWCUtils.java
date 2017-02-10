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

package com.kintosoft.svnwebclient.jira;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraKeyUtils;
import com.kintosoft.jira.permissions.ViewVersionControlPermission;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManagerImpl;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManagerImpl;
import com.kintosoft.svnwebclient.db.DBReposMrg;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import com.kintosoft.svnwebclient.graph.model.jira.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.configuration.WebConfigurationProvider;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.javasvn.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class SWCUtils {



	private final static Logger log = LoggerFactory.getLogger(SWCUtils.class);
	public static ActiveObjects ao;
	public static  ALMMultipleSubversionRepositoryManager almsrm;

	public SWCUtils(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		SWCUtils.ao = ao;
		SWCUtils.almsrm = almsrm;

	}

	public static void setAO(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){
		SWCUtils.ao = ao;
		SWCUtils.almsrm = almsrm;
		DBReposMrg.setAO(ao);
	}


	public static Properties loadProperties() throws Exception {
		InputStream is = SWCUtils.class
				.getResourceAsStream("/svnwebclient.properties");
		if (is != null) {
			Properties svnWebClientProps = new Properties();

			svnWebClientProps.load(is);
			is.close();
			return svnWebClientProps;

		} else {
			throw new Exception(
					"The svnwebclient.properties inputstream is null");
		}

	}

	public static ConfigurationProvider getConfigurationProvider(String root)
			throws SQLException {
		return webConf2confProvider(DBReposMrg.getRepositoryConfiguration(root));
	}

	public static ConfigurationProvider getConfigurationProvider(long repoId)
			throws SQLException {
		return webConf2confProvider(DBReposMrg
				.getRepositoryConfiguration(repoId));
	}

	public static ConfigurationProvider getRepository(long repoId)
			throws SQLException {
		return webConf2confProvider(DBReposMrg
				.getRepositoryConfiguration(repoId));
	}

	public static List<ConfigurationProvider> getRepositories()
			throws Exception {
		List<ConfigurationProvider> repos = new ArrayList<ConfigurationProvider>();
		for (WebConfigurationProvider webConf : DBReposMrg
				.getRepositoriesConfiguration()) {
			repos.add(webConf2confProvider(webConf));
		}

		Collections.sort(repos, new Comparator<ConfigurationProvider>() {
			public int compare(ConfigurationProvider left,
					ConfigurationProvider right) {
				return StringUtils.defaultString(
						left.getDisplayName().toLowerCase()).compareTo(
						StringUtils.defaultString(right.getDisplayName())
								.toLowerCase());
			}
		});
		return repos;
	}

	private static ConfigurationProvider webConf2confProvider(
			WebConfigurationProvider webConf) {
		if (webConf == null) {
			return null;
		}
		return new ConfigurationProvider(webConf);
	}

	public static TreeMap<SubversionManager, Status> importSVNRepositoriesFromJIRA()
			throws SQLException {
		Map<SubversionManager, Status> result = new HashMap<SubversionManager, Status>();

		Map<Long, SubversionManager> jiraRepos = ALMMultipleSubversionRepositoryManagerImpl
				.loadSvnManagers();

		/*
		 * jiraRepos = new HashMap<Long, SubversionManager>(); jiraRepos.put(0L,
		 * test("aaab")); jiraRepos.put(1L, test("bbab")); jiraRepos.put(2L,
		 * test("ccab")); jiraRepos.put(3L, test("ddab")); jiraRepos.put(4L,
		 * test("eeab"));
		 */

		for (SubversionManager jiraRepo : jiraRepos.values()) {
			String rootUrl = jiraRepo.getRoot();
			if (rootUrl.endsWith("/")) {
				rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
			}
			WebConfigurationProvider conf = DBReposMrg
					.getRepositoryConfiguration(rootUrl);
			if (conf != null) {
				result.put(jiraRepo, new Status(
						"Ignored because it is already exists", true));
				continue;
			}
			try {
				Properties jiraProps = jira2swc(jiraRepo);
				String password = jiraProps
						.getProperty(WebConfigurationProvider.PASSWORD);
				if (TextUtils.stringSet(password)) {
					try {
						password = SubversionManagerImpl
								.decryptPassword(password);
						jiraProps.setProperty(
								WebConfigurationProvider.PASSWORD, password);
					} catch (IOException e) {
						log.warn(e.getMessage());
					}
				}
				createRepository(jiraProps);
				result.put(jiraRepo, new Status("Imported", false));
			} catch (ConfigurationException e) {
				result.put(jiraRepo, new Status(e.getMessage() + " "
						+ (e.field == null ? "" : " field: " + e.field), true));
			}
		}

		TreeMap<SubversionManager, Status> sorted = new TreeMap<SubversionManager, Status>(
				result);

		return sorted;
	}

	public static PropertySet swc2jira(ConfigurationProvider confProvider) {
		MemoryPropertySet props = new MemoryPropertySet();
		props.init(null, null);
		props.setLong(
				ALMMultipleSubversionRepositoryManager.SVN_LOG_MESSAGE_CACHE_SIZE_KEY,
				0);
		String password;
		if (TextUtils.stringSet(confProvider.getProtocolKeyFile())) {
			props.setString(
					ALMMultipleSubversionRepositoryManager.SVN_PRIVATE_KEY_FILE,
					confProvider.getProtocolKeyFile());
			password = confProvider.getProtocolPassPhrase();
		} else {
			password = confProvider.getPassword();
			props.setString(
					ALMMultipleSubversionRepositoryManager.SVN_PRIVATE_KEY_FILE,
					null);
		}
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_PASSWORD_KEY,
				password);
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_REPOSITORY_NAME,
				confProvider.getDisplayName());
		props.setLong(
				ALMMultipleSubversionRepositoryManager.SVN_REVISION_CACHE_SIZE_KEY,
				0);
		props.setString(ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY,
				confProvider.getRootUrl());
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_USERNAME_KEY,
				confProvider.getUsername());
		props.setInt(
				ALMMultipleSubversionRepositoryManager.SVN_SSH_PORT_NUMBER,
				confProvider.getProtocolPortNumber());

		setSVNLinks(props);

		return props;
	}

	public static Properties jira2swc(SubversionManager jiraRepo) {
		Properties props = getDefaultProperties();
		setSVNLinks(props);

		String password = jiraRepo.getPassword();
		if (password == null) {
			password = "";
		}

		props.setProperty(WebConfigurationProvider.PASSWORD, password);
		props.setProperty(WebConfigurationProvider.PROTOCOL_PASS_PHRASE,
				password);

		if (jiraRepo.getPrivateKeyFile() != null)
			props.setProperty(WebConfigurationProvider.PROTOCOL_KEY_FILE,
					jiraRepo.getPrivateKeyFile());
		props.setProperty(WebConfigurationProvider.DISPLAY_NAME,
				jiraRepo.getDisplayName());
		String rootUrl = jiraRepo.getRoot();
		if (rootUrl.endsWith("/")) {
			rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
		}
		props.setProperty(WebConfigurationProvider.ROOT_URL, rootUrl);

		String username = jiraRepo.getUsername();
		if (username == null) {
			username = "";
		}
		props.setProperty(WebConfigurationProvider.USERNAME, username);

		return props;
	}

	private static void setSVNLinks(PropertySet props) {
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_TYPE,
				"svnwebclient");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_PATH_KEY,
				"foo");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_REPLACED,
				"/secure/SWCTopMenuAction!default.jspa?jsp=changedResource&repoId=${repoId}&location=/&url=${path}&rev=${rev}&action=replace");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_MODIFIED,
				"/secure/SWCTopMenuAction!default.jspa?jsp=changedResource&repoId=${repoId}&location=/&url=${path}&rev=${rev}&action=modify");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_DELETED,
				"/secure/SWCTopMenuAction!default.jspa?jsp=changedResource&repoId=${repoId}&location=/&url=${path}&rev=${rev}&action=delete");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_ADDED,
				"/secure/SWCTopMenuAction!default.jspa?jsp=changedResource&repoId=${repoId}&location=/&url=${path}&rev=${rev}&action=add");
		props.setString(
				ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_CHANGESET,
				"/secure/SWCTopMenuAction!default.jspa?jsp=revisionDetails&repoId=${repoId}&location=/&rev=${rev}");
	}

	private static void setSVNLinks(Properties props) {
		MemoryPropertySet linkProps = new MemoryPropertySet();
		linkProps.init(null, null);
		setSVNLinks(linkProps);
		for (Object key : linkProps.getKeys()) {
			props.put(key, linkProps.getString((String) key));
		}
	}

	public static SubversionManager createRepository(Properties props)
			throws ConfigurationException {
		log.debug("Creating the SubversionManager from the properties: "
				+ props);
		WebConfigurationProvider webConf = new WebConfigurationProvider();
		webConf.setParameters(trimProperties(props));
		ConfigurationProvider conf = webConf2confProvider(webConf);
		conf.checkConfiguration();
		try {
			DBReposMrg.createRepository(webConf);
		} catch (SQLException e) {
			throw new ConfigurationException("", e.getMessage());
		}
		PropertySet ps = swc2jira(conf);
		log.debug("Creating a MultipleSubversionRepositoryManagerImpl instance by transforming the SVN Web client properties int the Atlassian's properties format:"
				+ ps);
		return almsrm.createRepository(conf.getRepoId(), ps);
	}

	private static Properties trimProperties(Properties original) {
		Properties trim = new Properties();
		Enumeration keys = original.propertyNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Object value = original.get(key);
			if (value != null && value instanceof String) {
				value = value.toString().trim();
			}
			trim.put(key, value);
		}

		return trim;
	}

	public static void updateRepository(Properties props)
			throws ConfigurationException, SQLException {
		WebConfigurationProvider webConf = new WebConfigurationProvider();
		webConf.setParameters(trimProperties(props));
		ConfigurationProvider conf = webConf2confProvider(webConf);
		conf.checkConfiguration();
		DBReposMrg.setAO(ao);
		DBReposMrg.updateRepository(webConf);

		almsrm.updateRepository(conf.getRepoId(), swc2jira(conf));
		try {
			DataProvider.terminate(conf.getRootUrl());
			ALMMultipleSubversionRepositoryManagerImpl.updateIndexStatic();
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	synchronized public static void deleteRepository(long id)
			throws SQLException {
		String rootUrl = null;

		log.debug("Deleteing the repository...");
		rootUrl = SWCUtils.getRepository(id).getRootUrl();
		if (rootUrl != null) {
			try {
				DataProvider.terminate(rootUrl);
			} catch (DataProviderException e) {
				log.warn(e.getMessage());
			}
		} else {
			log.warn("Trying to TERMINATE an exisinting repository ID=" + id
					+ " but the root url is NULL");
		}
		// Stop indexing
		almsrm.getIndexer()
				.terminate(id);
		// Delete repor from db
		if (DBReposMrg.deleteRepository(id, ao)) {
			log.debug("------------- Removing the repository...");
			// Remove SVN Manager
			almsrm.removeRepository(id);
		}
	}

	public static long hash(String string) {
		long h = 1125899906842597L; // prime
		int len = string.length();

		for (int i = 0; i < len; i++) {
			h = 31 * h + string.charAt(i);
		}
		return h;
	}

	public static Properties getDefaultProperties() {
		Properties props = new Properties();
		props.setProperty(WebConfigurationProvider.BASIC_AUTH, "false");
		props.setProperty(WebConfigurationProvider.BASIC_REALM,
				"Subversion Web Client");
		props.setProperty(
				WebConfigurationProvider.BINARY_MIME_TYPES,
				"application/octet-stream, application/zip, application/x-visio, application/vnd.ms-excel, application/x-tar,application/x-shockwave-flash, application/rtf, application/postscript, application/vnd.ms-powerpoint,application/pdf, application/java-archive, application/x-gzip, application/msword, application/postscript,image/tiff, image/svg+xml, image/x-photoshop, image/png, image/jpeg, image/gif, image/bmp,video/mpeg, audio/x-mpeg, video/x-msvideo");
		props.setProperty(WebConfigurationProvider.BRANCHES_NAME, "branches");
		props.setProperty(WebConfigurationProvider.CACHE_PAGE_SIZE, "100");
		props.setProperty(
				WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT, "-1");
		props.setProperty(
				WebConfigurationProvider.CHARACTER_ENCODINGS,
				"windows-1250,windows-1251,windows-1252,windows-1253,windows-1254,windows-1255,windows-1256,windows-1257,windows-1258,windows-31j,UTF-8,UTF-16LE,UTF-16BE,UTF-16,ISO-8859-1,ISO-8859-13,ISO-8859-15,ISO-8859-2,ISO-8859-3,ISO-8859-4,ISO-8859-5,ISO-8859-6,ISO-8859-7,ISO-8859-8,ISO-8859-9,US-ASCII,KOI8-R");
		props.setProperty(WebConfigurationProvider.DEFAULT_ENCODING, "UTF-8");
		props.setProperty(WebConfigurationProvider.FORCED_HTTP_AUTH, "false");
		props.setProperty(WebConfigurationProvider.PATH_AUTODETECT, "true");
		props.setProperty(WebConfigurationProvider.PROTOCOL_PORT_NUMBER, "22");
		props.setProperty(WebConfigurationProvider.PROXY_PORT_NUMBER, "8080");
		props.setProperty(WebConfigurationProvider.PROXY_SUPPORTED, "false");
		props.setProperty(WebConfigurationProvider.SHOW_STACK_TRACE, "false");
		props.setProperty(WebConfigurationProvider.SVN_CONNECTIONS_COUNT, "20");
		props.setProperty(WebConfigurationProvider.TAGS_NAME, "tags");
		props.setProperty(
				WebConfigurationProvider.TEXT_MIME_TYPES,
				"text/plain, application/xslt+xml, application/xml, application/xhtml+xml, text/javascript, text/html, text/css,application/x-javascript, application/bat");
		props.setProperty(WebConfigurationProvider.TRUNK_NAME, "trunk");
		props.setProperty(WebConfigurationProvider.VERSIONS_COUNT, "20");
		return props;
	}

	public static ProtocolsConfiguration buildProtocolFromConfiguration(
			ConfigurationProvider conf) {
		if (conf == null) {
			return null;
		}
		ProtocolsConfiguration protocolsConf = new ProtocolsConfiguration();
		protocolsConf.setProtocolKeyFile(conf.getProtocolKeyFile());
		protocolsConf.setProtocolPassPhrase(conf.getProtocolPassPhrase());
		protocolsConf.setProtocolPortNumber(conf.getProtocolPortNumber());
		protocolsConf.setProtocolType(conf.getProtocolType());
		protocolsConf.setProxy(conf.getProxy());
		return protocolsConf;
	}

	public static Properties getRepositoryProperties(long repoId)
			throws SQLException {
		WebConfigurationProvider conf = DBReposMrg
				.getRepositoryConfiguration(repoId);
		Properties props = new Properties();
		for (Entry<String, String> param : conf.getParameters().entrySet()) {
			props.put(param.getKey(), param.getValue());
		}
		return props;
	}

	private static void put(Properties webProps, String key, Object value) {
		if (value != null) {
			webProps.put(key, value);
		}
	}

	public static class Status {

		public boolean isError;
		public String msg;

		public Status(String msg, boolean isError) {
			this.msg = msg;
			this.isError = isError;
		}

		public boolean getIsError() {
			return isError;
		}

		public String getMessage() {
			return msg;
		}
	}

	public static String validateLicense() {
		return ALMMultipleSubversionRepositoryManagerImpl.validateLicense();
	}

	public static boolean validateUserPrivilegesForSWC() {
		return ViewVersionControlPermission
				.hasUserViewVersionControlPermissionsOnAnyProject(getJIRAUser());
	}

	public static ApplicationUser getJIRAUser() {
		return ComponentAccessor.getJiraAuthenticationContext()
				.getLoggedInUser();
	}

	public static boolean requireTrackerSession() {
		try {
			PluginConnectionPool pcp = null;
			if(ao != null)
				pcp = new PluginConnectionPool(ao);
			return pcp.getRequireTrackerSession();

		} catch (SQLException e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public static String getJiraBase() {
		return ComponentAccessor.getApplicationProperties().getString(
				APKeys.JIRA_BASEURL);
	}

	public static String replaceIssueHtmlLinks(String text) {
		String jiraBase = getJiraBase();

		IssueService issueService = ComponentAccessor.getIssueService();

		List<String> keyNums = JiraKeyUtils.getIssueKeysFromString(text);
		IssueService.IssueResult res;
		for (String keyNum : keyNums) {
			res = issueService.getIssue(getJIRAUser(), keyNum);
			if (!res.isValid()) {
				continue;
			}
			MutableIssue issue = res.getIssue();
			String iconUrl = jiraBase + issue.getIssueTypeObject().getIconUrl();

			String img = "<img src='" + iconUrl + "' class='svn-issue'/>";
			String url = jiraBase + "/browse/" + keyNum;
			String a = anchor("javascript:void(0);", "navigateTo('" + url
					+ "'); return false;", issue.getSummary(), img + "&nbsp;"
					+ keyNum);
			String div = " <div class='svn-issue'>" + a + "</div> ";
			text = text.replaceAll(keyNum, div);
		}

		return text;
	}

	public static Issue getIssue(String keyNum) {
		String jiraBase = getJiraBase();

		IssueService issueService = ComponentAccessor.getIssueService();

		IssueResult res = issueService.getIssue(getJIRAUser(), keyNum);
		if (!res.isValid()) {
			return null;
		}

		MutableIssue jiraIssue = res.getIssue();

		return new Issue(jiraIssue, jiraBase);
	}

	public static String anchor(String href, String javascript, String title,
			String text) {
		return "<a href='" + href + "' onclick=\"" + javascript + "\" title='"
				+ title + "'>" + text + "</a>";
	}
}
