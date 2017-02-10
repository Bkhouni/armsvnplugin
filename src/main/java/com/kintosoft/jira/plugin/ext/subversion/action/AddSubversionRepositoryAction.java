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

package com.kintosoft.jira.plugin.ext.subversion.action;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.configuration.WebConfigurationProvider;
import org.polarion.svnwebclient.data.javasvn.DataProvider;

import java.util.Properties;


public class AddSubversionRepositoryAction extends SubversionActionSupport {
	protected Properties props;

	private boolean pathAutodetect;
	private boolean showStackTrace;
	private boolean basicAuth;
	private boolean forcedHttpAuth;
	private boolean proxySupported;



//	@Autowired
	public AddSubversionRepositoryAction(
			 ALMMultipleSubversionRepositoryManager manager,  ActiveObjects ao) throws Exception {
		super(manager, ao);
		props = SWCUtils.getDefaultProperties();

	}

	public void doValidation() {
		validateRepositoryParameters();
	}

	public String getDisplayName() {
		return props.getProperty(WebConfigurationProvider.DISPLAY_NAME);
	}

	public void setDisplayName(String value) {
		props.setProperty(WebConfigurationProvider.DISPLAY_NAME, value);
	}

	public String doExecute() {
		if (!hasPermissions()) {
			return PERMISSION_VIOLATION_RESULT;
		}

		SubversionManager subversionManager = null;
		try {
			SWCUtils.setAO(ao, getMultipleRepoManager());
			subversionManager = SWCUtils.createRepository(props);
			if (subversionManager == null) {
				throw new Exception(
						"The Subversionmmanager cannot be created from the properties");
			}
		} catch (Exception e) {
			addErrorMessage(e.getMessage());
			return ERROR;
		}

		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	// This is public for testing purposes
	public void validateRepositoryParameters() {

		props.setProperty(WebConfigurationProvider.PATH_AUTODETECT,
				Boolean.toString(pathAutodetect));
		props.setProperty(WebConfigurationProvider.BASIC_AUTH,
				Boolean.toString(basicAuth));
		props.setProperty(WebConfigurationProvider.FORCED_HTTP_AUTH,
				Boolean.toString(forcedHttpAuth));
		props.setProperty(WebConfigurationProvider.SHOW_STACK_TRACE,
				Boolean.toString(showStackTrace));
		props.setProperty(WebConfigurationProvider.PROXY_SUPPORTED,
				Boolean.toString(proxySupported));

		WebConfigurationProvider webConfProvider = new WebConfigurationProvider();
		webConfProvider.setParameters(props);
		try {
			ConfigurationProvider confProvider = new ConfigurationProvider(
					webConfProvider);
//			log.debug("The input properties (with the format expected by the SVN Web Client) are going to be validated: "+ props + " ...");
			confProvider.checkConfiguration();
//			log.debug("...the input properties have been validated");
//			log.debug("Getting the root from the input url...");
			props.setProperty(WebConfigurationProvider.ROOT_URL,
					DataProvider.getRoot(props));
//			log.debug("...the root url has been retrieved from the input url");
		} catch (ConfigurationException e) {
//			log.warn(e);
			addError("foo", "<b>" + e.field + "</b> " + e.getMessage());
		} catch (Exception e) {
//			log.warn(e);
			addError("Repository Root Url",
					"Repository Root Url " + e.getMessage());
		}
	}

	public String getRoot() {
		return props.getProperty(WebConfigurationProvider.ROOT_URL);
	}

	public void setRoot(String value) {
		props.setProperty(WebConfigurationProvider.ROOT_URL, value);
	}

	public long getSvnConnectionsCount() {
		return Long.parseLong(props
				.getProperty(WebConfigurationProvider.SVN_CONNECTIONS_COUNT));
	}

	public void setSvnConnectionsCount(long value) {
		props.setProperty(WebConfigurationProvider.SVN_CONNECTIONS_COUNT,
				Long.toString(value));
	}

	public long getVersionsCount() {
		return Long.parseLong(props
				.getProperty(WebConfigurationProvider.VERSIONS_COUNT));
	}

	public void setVersionsCount(long value) {
		props.setProperty(WebConfigurationProvider.VERSIONS_COUNT,
				Long.toString(value));
	}

	public boolean getPathAutodetect() {
		return Boolean.parseBoolean(props
				.getProperty(WebConfigurationProvider.PATH_AUTODETECT));
	}

	public void setPathAutodetect(boolean value) {
		pathAutodetect = true;
	}

	public String getTrunkName() {
		return props.getProperty(WebConfigurationProvider.TRUNK_NAME);
	}

	public void setTrunkName(String value) {
		props.setProperty(WebConfigurationProvider.TRUNK_NAME, value);
	}

	public String getBranchesName() {
		return props.getProperty(WebConfigurationProvider.BRANCHES_NAME);
	}

	public void setBranchesName(String value) {
		props.setProperty(WebConfigurationProvider.BRANCHES_NAME, value);
	}

	public String getTagsName() {
		return props.getProperty(WebConfigurationProvider.TAGS_NAME);
	}

	public void setTagsName(String value) {
		props.setProperty(WebConfigurationProvider.TAGS_NAME, value);
	}

	public String getDefaultEncoding() {
		return props.getProperty(WebConfigurationProvider.DEFAULT_ENCODING);
	}

	public void setDefaultEncoding(String value) {
		props.setProperty(WebConfigurationProvider.DEFAULT_ENCODING, value);
	}

	public boolean getShowStackTrace() {
		return Boolean.parseBoolean(props
				.getProperty(WebConfigurationProvider.SHOW_STACK_TRACE));
	}

	public void setShowStackTrace(boolean value) {
		showStackTrace = value;
	}

	public long getCachePageSize() {
		return Long.parseLong(props
				.getProperty(WebConfigurationProvider.CACHE_PAGE_SIZE));
	}

	public void setCachePageSize(long value) {
		props.setProperty(WebConfigurationProvider.CACHE_PAGE_SIZE,
				Long.toString(value));
	}

	public long getCachePrefetchMessagesCount() {
		return Long
				.parseLong(props
						.getProperty(WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT));
	}

	public void setCachePrefetchMessagesCount(long value) {
		props.setProperty(
				WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT,
				Long.toString(value));
	}

	public String getBasicRealm() {
		return props.getProperty(WebConfigurationProvider.BASIC_REALM);
	}

	public void setBasicRealm(String value) {
		props.setProperty(WebConfigurationProvider.BASIC_REALM, value);
	}

	public boolean getBasicAuth() {
		return Boolean.parseBoolean(props
				.getProperty(WebConfigurationProvider.BASIC_AUTH));
	}

	public void setBasicAuth(boolean value) {
		basicAuth = value;
	}

	public boolean getForcedHttpAuth() {
		return Boolean.parseBoolean(props
				.getProperty(WebConfigurationProvider.FORCED_HTTP_AUTH));
	}

	public void setForcedHttpAuth(boolean value) {
		forcedHttpAuth = value;
	}

	public String getProtocolKeyFile() {
		return props.getProperty(WebConfigurationProvider.PROTOCOL_KEY_FILE);
	}

	public void setProtocolKeyFile(String value) {
		props.setProperty(WebConfigurationProvider.PROTOCOL_KEY_FILE, value);
	}

	public String getProtocolPassPhrase() {
		return props.getProperty(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
	}

	public void setProtocolPassPhrase(String value) {
		props.setProperty(WebConfigurationProvider.PROTOCOL_PASS_PHRASE, value);
	}

	public int getProtocolPortNumber() {
		return Integer.parseInt(props
				.getProperty(WebConfigurationProvider.PROTOCOL_PORT_NUMBER));
	}

	public void setProtocolPortNumber(int value) {
		props.setProperty(WebConfigurationProvider.PROTOCOL_PORT_NUMBER,
				Integer.toString(value));
	}

	public String getProxyHost() {
		return props.getProperty(WebConfigurationProvider.PROXY_HOST);
	}

	public void setProxyHost(String value) {
		props.setProperty(WebConfigurationProvider.PROXY_HOST, value);
	}

	public boolean getProxySupported() {
		return Boolean.parseBoolean(props
				.getProperty(WebConfigurationProvider.PROXY_SUPPORTED));
	}

	public void setProxySupported(boolean value) {
		proxySupported = value;
	}

	public String getProxyPassword() {
		return props.getProperty(WebConfigurationProvider.PROXY_PASSWORD);
	}

	public void setProxyPassword(String value) {
		props.setProperty(WebConfigurationProvider.PROXY_PASSWORD, value);
	}

	public int getProxyPort() {
		return Integer.parseInt(props
				.getProperty(WebConfigurationProvider.PROXY_PORT_NUMBER));
	}

	public void setProxyPort(int value) {
		props.setProperty(WebConfigurationProvider.PROXY_PORT_NUMBER,
				Integer.toString(value));
	}

	public String getProxyUserName() {
		return props.getProperty(WebConfigurationProvider.PROXY_USER_NAME);
	}

	public void setProxyUserName(String value) {
		props.setProperty(WebConfigurationProvider.PROXY_USER_NAME, value);
	}

	public String getCharacterEncodings() {
		return props.getProperty(WebConfigurationProvider.CHARACTER_ENCODINGS);
	}

	public void setCharacterEncodings(String value) {
		props.setProperty(WebConfigurationProvider.CHARACTER_ENCODINGS, value);
	}

	public String getBinaryMimeTypes() {
		return props.getProperty(WebConfigurationProvider.BINARY_MIME_TYPES);
	}

	public void setBinaryMimeTypes(String value) {
		props.setProperty(WebConfigurationProvider.BINARY_MIME_TYPES, value);

	}

	public String getTextMimeTypes() {
		return props.getProperty(WebConfigurationProvider.TEXT_MIME_TYPES);
	}

	public void setTextMimeTypes(String value) {
		props.setProperty(WebConfigurationProvider.TEXT_MIME_TYPES, value);
	}

	public String getUsername() {
		return props.getProperty(WebConfigurationProvider.USERNAME);
	}

	public void setUsername(String value) {
		props.setProperty(WebConfigurationProvider.USERNAME, value);
	}

	public String getPassword() {
		return props.getProperty(WebConfigurationProvider.PASSWORD);
	}

	public void setPassword(String value) {
		props.setProperty(WebConfigurationProvider.PASSWORD, value);
	}
}
