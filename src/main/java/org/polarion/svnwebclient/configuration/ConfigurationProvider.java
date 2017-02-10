/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved.
 * Email: community@polarion.org
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"). You may not use
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.polarion.svnwebclient.configuration;

import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.opensymphony.util.TextUtils;
import org.polarion.svncommons.commentscache.configuration.ProxySettings;
import org.polarion.svnwebclient.decorations.IAlternativeViewProvider;
import org.polarion.svnwebclient.decorations.IAuthorDecorator;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class ConfigurationProvider implements IConfigurationProvider {

	private static Logger log = LoggerFactory
			.getLogger(ConfigurationProvider.class);

	protected static ConfigurationProvider instance;

	protected static IRevisionDecorator revisionDecorator;
	protected static IAlternativeViewProvider alternativeViewProvider;
	protected static IAuthorDecorator authorDecorator;

	protected ConfigurationError error = new ConfigurationError();

	final private WebConfigurationProvider webConfProvider;

	public ConfigurationError getConfigurationError() {
		return this.error;
	}

	public ConfigurationProvider(WebConfigurationProvider webConfProvider) {
		this.webConfProvider = webConfProvider;
	}

	public void checkConfiguration() throws ConfigurationException {
		log.debug("Checking " + WebConfigurationProvider.DISPLAY_NAME + "... ");
		this.checkNotNullOrEmpty(WebConfigurationProvider.DISPLAY_NAME);
		String parentUrl = webConfProvider
				.getParameter(WebConfigurationProvider.ROOT_URL);
		String rootUrl = webConfProvider
				.getParameter(WebConfigurationProvider.ROOT_URL);

		log.debug("Checking " + WebConfigurationProvider.ROOT_URL + "... ");
		this.checkNotNullOrEmpty(WebConfigurationProvider.ROOT_URL);

		log.debug("Checking " + WebConfigurationProvider.SVN_CONNECTIONS_COUNT
				+ "... ");
		this.checkLong(WebConfigurationProvider.SVN_CONNECTIONS_COUNT);

		log.debug("Checking " + WebConfigurationProvider.VERSIONS_COUNT
				+ "... ");
		this.checkLong(WebConfigurationProvider.VERSIONS_COUNT);

		log.debug("Checking " + WebConfigurationProvider.PATH_AUTODETECT
				+ "... ");
		this.checkBoolean(WebConfigurationProvider.PATH_AUTODETECT);
		if (this.isPathAutodetect()) {
			log.debug("Checking " + WebConfigurationProvider.TRUNK_NAME
					+ "... ");
			this.checkNotNullOrEmpty(WebConfigurationProvider.TRUNK_NAME);

			log.debug("Checking " + WebConfigurationProvider.BRANCHES_NAME
					+ "... ");
			this.checkNotNullOrEmpty(WebConfigurationProvider.BRANCHES_NAME);

			log.debug("Checking " + WebConfigurationProvider.TAGS_NAME + "... ");
			this.checkNotNullOrEmpty(WebConfigurationProvider.TAGS_NAME);
		}

		log.debug("Checking " + WebConfigurationProvider.DEFAULT_ENCODING
				+ "... ");
		this.checkNotNullOrEmpty(WebConfigurationProvider.DEFAULT_ENCODING);

		log.debug("Checking " + WebConfigurationProvider.CACHE_PAGE_SIZE
				+ "... ");
		this.checkLong(WebConfigurationProvider.CACHE_PAGE_SIZE);

		log.debug("Checking "
				+ WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT
				+ "... ");
		this.checkLong(WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT);

		if (webConfProvider
				.getParameter(WebConfigurationProvider.SHOW_STACK_TRACE) != null) {
			log.debug("Checking " + WebConfigurationProvider.SHOW_STACK_TRACE
					+ "... ");
			this.checkBoolean(WebConfigurationProvider.SHOW_STACK_TRACE);
		}

		log.debug("Checking " + WebConfigurationProvider.BASIC_AUTH + "... ");
		this.checkBoolean(WebConfigurationProvider.BASIC_AUTH);
		if (this.isBasicAuth()) {
			log.debug("Checking " + WebConfigurationProvider.BASIC_REALM
					+ "... ");
			this.checkNotNullOrEmpty(WebConfigurationProvider.BASIC_REALM);
		}

		if (IConfigurationProvider.SVN_SSH == this.getProtocolType()) {
			log.debug("The protocol type is SSH");

			log.debug("Checking "
					+ WebConfigurationProvider.PROTOCOL_PORT_NUMBER + "... ");
			this.checkInt(WebConfigurationProvider.PROTOCOL_PORT_NUMBER);

			log.debug("Checking " + WebConfigurationProvider.USERNAME + "... ");
			this.checkNotNull(WebConfigurationProvider.USERNAME);
			String protocolKeyFile = this.getProtocolKeyFile();
			if (TextUtils.stringSet(protocolKeyFile)) {
				log.debug("The protocol key file is: " + protocolKeyFile);

				log.debug("Checking "
						+ WebConfigurationProvider.PROTOCOL_PASS_PHRASE
						+ "... ");
				this.checkNotNull(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);

				log.debug("Checking "
						+ WebConfigurationProvider.PROTOCOL_KEY_FILE + "... ");
				this.checkNotNullOrEmpty(WebConfigurationProvider.PROTOCOL_KEY_FILE);
			} else {
				log.debug("The protocol key file is null or empty");
				this.checkNotNull(WebConfigurationProvider.PASSWORD);
			}
		} else if (IConfigurationProvider.SSL == this.getProtocolType()) {
			log.debug("The protocol type is SSL");
			if (!TextUtils.stringSet(this.getProtocolKeyFile())) {
				log.debug("The protocol key is null or empty");

				log.debug("Checking " + WebConfigurationProvider.USERNAME
						+ "... ");
				this.checkNotNull(WebConfigurationProvider.USERNAME);

				log.debug("Checking " + WebConfigurationProvider.PASSWORD
						+ "... ");
				this.checkNotNull(WebConfigurationProvider.PASSWORD);
			}
		} else {
			log.debug("The protocol type is HTTP, SVN");
			// http, svn
			if (rootUrl != null) {
				log.debug("Checking " + WebConfigurationProvider.USERNAME
						+ "... ");
				this.checkNotNull(WebConfigurationProvider.USERNAME);

				log.debug("Checking " + WebConfigurationProvider.PASSWORD
						+ "... ");
				this.checkNotNull(WebConfigurationProvider.PASSWORD);
			}
		}
		log.debug("Checking " + WebConfigurationProvider.PROXY_SUPPORTED
				+ "... ");
		this.checkBoolean(WebConfigurationProvider.PROXY_SUPPORTED);
		if (new Boolean(
				webConfProvider
						.getParameter(WebConfigurationProvider.PROXY_SUPPORTED))
				.booleanValue() == true) {
			log.debug("Checking "
					+ WebConfigurationProvider.PROTOCOL_PORT_NUMBER + "... ");
			this.checkInt(WebConfigurationProvider.PROXY_PORT_NUMBER);

			log.debug("Checking " + WebConfigurationProvider.PROXY_HOST
					+ "... ");
			this.checkNotNullOrEmpty(WebConfigurationProvider.PROXY_HOST);
		}
	}

	public static String getProductVersion() {
		return "";
	}

	public String getEmailFrom() {
		String emailFrom = webConfProvider
				.getParameter(WebConfigurationProvider.EMAIL_FROM);
		return emailFrom;
	}

	public String getEmailTo() {
		String emailTo = webConfProvider
				.getParameter(WebConfigurationProvider.EMAIL_TO);
		return emailTo;
	}

	public String getEmailHost() {
		String emailHost = webConfProvider
				.getParameter(WebConfigurationProvider.EMAIL_HOST);
		return emailHost;
	}

	public String getEmailPort() {
		String emailPort = webConfProvider
				.getParameter(WebConfigurationProvider.EMAIL_PORT);
		return emailPort;
	}

	public String getEmailProject() {
		String emailProject = webConfProvider
				.getParameter(WebConfigurationProvider.EMAIL_PROJECT_NAME);
		return emailProject;
	}

	public static boolean isEmbedded() {
		return true;
	}

	public boolean isBasicAuth() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.BASIC_AUTH);
		value = value.trim();
        return WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value);
	}

	public boolean isForcedHttpAuth() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.FORCED_HTTP_AUTH);
		value = value.trim();
        return WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value);
	}

	public boolean isHidePolarionCommit() {
		return false;
	}

	public String getBasicRealm() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.BASIC_REALM);
		return value.trim();
	}

	public String getParentRepositoryDirectory() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.ROOT_URL);
		if (value == null) {
			return null;
		} else {
			value = value.trim();
			if (value.endsWith("/")) {
				value = value.substring(0, value.length() - 1);
			}
			return value.trim();
		}
	}

	public static boolean isMultiRepositoryMode() {
		return true;
	}

	public String getRepositoryUrl() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.ROOT_URL);
		if (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value.trim();
	}

	public String getUsername() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.USERNAME);
		return value.trim();
	}

	public String getPassword() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.PASSWORD);
		return value.trim();
	}

	public long getSvnConnectionsCount() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.SVN_CONNECTIONS_COUNT);
		return Long.parseLong(value);
	}

	public static String getTempDirectory() {
		String result = null;
		String value = null;
		if (value == null) {
			result = getOSTempDir();
			int index = result
					.lastIndexOf(System.getProperty("file.separator"));
			if (index != -1) {
				result = result.substring(0, index);
			}
		} else {
			result = value.trim();
		}
		return result;
	}

	public long getVersionsCount() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.VERSIONS_COUNT);
		return Long.parseLong(value);
	}

	public boolean isPathAutodetect() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.PATH_AUTODETECT);
		value = value.trim();
        return WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value);
	}

	public String getTrunkName() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.TRUNK_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public String getBranchesName() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.BRANCHES_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public String getTagsName() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.TAGS_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public String getDefaultEncoding() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.DEFAULT_ENCODING);
		return value.trim();
	}

	public boolean isShowStackTrace() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.SHOW_STACK_TRACE);
		value = value.trim();
        return WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value);
	}

	public static String getCacheDirectory() {
		String result = null;
		String value = null;
		if (value == null) {
			String tmpDir = getOSTempDir();
			result = tmpDir + "cache";
		} else {
			result = value.trim();
		}
		return result;
	}

	public long getCachePageSize() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.CACHE_PAGE_SIZE);
		return Long.parseLong(value);
	}

	public long getCachePrefetchMessagesCount() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT);
		return Long.parseLong(value);
	}

	public static boolean isLogout() {
		return true;
	}

	public String getProtocolKeyFile() {
		return webConfProvider
				.getParameter(WebConfigurationProvider.PROTOCOL_KEY_FILE);
	}

	public String getProtocolPassPhrase() {
		return webConfProvider
				.getParameter(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
	}

	public int getProtocolPortNumber() {
		String value = webConfProvider
				.getParameter(WebConfigurationProvider.PROTOCOL_PORT_NUMBER);
		return Integer.parseInt(value);
	}

	public ProxySettings getProxy() {
		ProxySettings proxy = new ProxySettings();
		String isSupported = webConfProvider
				.getParameter(WebConfigurationProvider.PROXY_SUPPORTED);
		proxy.setProxySupported(new Boolean(isSupported).booleanValue());
		if (new Boolean(isSupported).booleanValue() == true) {
			proxy.setHost(webConfProvider
					.getParameter(WebConfigurationProvider.PROXY_HOST));
			proxy.setUserName(webConfProvider
					.getParameter(WebConfigurationProvider.PROXY_USER_NAME));
			proxy.setPassword(webConfProvider
					.getParameter(WebConfigurationProvider.PROXY_PASSWORD));
			String port = webConfProvider
					.getParameter(WebConfigurationProvider.PROXY_PORT_NUMBER);
			if (port != null) {
				proxy.setPort(Integer.parseInt(port));
			}
		}
		return proxy;
	}

	public static IRevisionDecorator getRevisionDecorator() {
		if (revisionDecorator == null) {
			revisionDecorator = (IRevisionDecorator) instantiate("org.polarion.svnwebclient.decorations.impl.RevisionDecorator");
		}
		return revisionDecorator;
	}

	public static IAlternativeViewProvider getAlternativeViewProvider() {
		if (alternativeViewProvider == null) {
			alternativeViewProvider = (IAlternativeViewProvider) instantiate("org.polarion.svnwebclient.decorations.impl.AlternativeViewProvider");
		}
		return alternativeViewProvider;
	}

	public static IAuthorDecorator getAuthorDecorator() {
		if (authorDecorator == null) {
			authorDecorator = (IAuthorDecorator) instantiate("org.polarion.svnwebclient.decorations.impl.AuthorDecorator");
		}
		return authorDecorator;
	}

	public int getProtocolType() {
		String url = null;
		if (isMultiRepositoryMode()) {
			url = this.getParentRepositoryDirectory();
		} else {
			url = this.getRepositoryUrl();
		}

		if (url.indexOf("svn+ssh://") != -1) {
			return IConfigurationProvider.SVN_SSH;
		} else if (url.indexOf("https://") != -1) {
			return IConfigurationProvider.SSL;
		} else {
			return IConfigurationProvider.HTTP;
		}
	}

	protected void checkNotNull(String parameterName)
			throws ConfigurationException {
		String value = webConfProvider.getParameter(parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName,
					"It must be defined");
		}
	}

	protected static Object instantiate(String className) {
		try {
			Class clazz = Class.forName(className);
			return clazz.newInstance();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	protected void checkNotNullOrEmpty(String parameterName)
			throws ConfigurationException {
		String value = webConfProvider.getParameter(parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName,
					"It must be defined");
		}
		value = value.trim();
		if (value.length() == 0) {
			throw new ConfigurationException(parameterName,
					"It must be not empty string");
		}
	}

	protected void checkBoolean(String parameterName)
			throws ConfigurationException {
		String value = webConfProvider.getParameter(parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName,
					"It must be defined");
		}
		value = value.trim();
		if (!(WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value) || WebConfigurationProvider.VALUE_FALSE
				.equalsIgnoreCase(value))) {
			throw new ConfigurationException(parameterName, "Invalid value \""
					+ value + "\"" + "Only "
					+ WebConfigurationProvider.VALUE_TRUE + " and "
					+ WebConfigurationProvider.VALUE_FALSE + " are allowed");
		}
	}

	protected void checkLong(String parameterName)
			throws ConfigurationException {
		String value = webConfProvider.getParameter(parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName,
					"It must be defined");
		}

		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ConfigurationException(parameterName, "Invalid value \""
					+ value + "\". " + "It must be numeric");
		}
	}

	protected void checkInt(String parameterName) throws ConfigurationException {
		String value = webConfProvider.getParameter(parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName,
					"It must be defined");
		}

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ConfigurationException(parameterName, "Invalid value \""
					+ value + "\". " + "It must be numeric");
		}
	}

	protected static String getOSTempDir() {
		String tempdir = System.getProperty("java.io.tmpdir");
		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		return tempdir;
	}

	public String getRepositoryLocation(String repositoryName) {
		String res = "";
		if (!isMultiRepositoryMode()) {
			res = this.getRepositoryUrl();
		} else {
			res += this.getParentRepositoryDirectory() + "/" + repositoryName;
		}
		return res;
	}

	public Set getCharacterEncodings() {
		Set res = new HashSet();

		String strEncodings = webConfProvider
				.getParameter(WebConfigurationProvider.CHARACTER_ENCODINGS);
		if (strEncodings != null) {
			strEncodings = strEncodings.trim();

			String[] encodings = strEncodings.split(",");
			for (int i = 0; i < encodings.length; i++) {
				String encoding = encodings[i];
				res.add(encoding.trim());
			}
		}
		return res;
	}

	public Set getBinaryMimeTypes() {
		Set res = new HashSet();

		String strMimeTypes = webConfProvider
				.getParameter(WebConfigurationProvider.BINARY_MIME_TYPES);
		if (strMimeTypes != null) {
			strMimeTypes = strMimeTypes.trim();

			String[] mimeTypes = strMimeTypes.split(",");
			for (int i = 0; i < mimeTypes.length; i++) {
				String mimeType = mimeTypes[i];
				res.add(mimeType.trim().toLowerCase());
			}
		}
		return res;
	}

	public Set getTextMimeTypes() {
		Set res = new HashSet();

		String strMimeTypes = webConfProvider
				.getParameter(WebConfigurationProvider.TEXT_MIME_TYPES);
		if (strMimeTypes != null) {
			strMimeTypes = strMimeTypes.trim();

			String[] mimeTypes = strMimeTypes.split(",");
			for (int i = 0; i < mimeTypes.length; i++) {
				String mimeType = mimeTypes[i];
				res.add(mimeType.trim().toLowerCase());
			}
		}
		return res;
	}

	public static ConfigurationProvider getInstance(String id)
			throws ConfigurationException {
		try {
			return SWCUtils.getConfigurationProvider(id);
		} catch (Exception e) {
			throw new ConfigurationException(id, e.getMessage());
		}
	}

	public static ConfigurationProvider getInstance(long repoId)
			throws ConfigurationException {
		try {
			return SWCUtils.getConfigurationProvider(repoId);
		} catch (Exception e) {
			throw new ConfigurationException("repoId=" + repoId, e.getMessage());
		}
	}

	@Override
	public String getDisplayName() {
		return webConfProvider
				.getParameter(WebConfigurationProvider.DISPLAY_NAME);
	}

	@Override
	public String getRootUrl() {
		return webConfProvider.getParameter(WebConfigurationProvider.ROOT_URL);
	}

	@Override
	public long getRepoId() {
		return Long.parseLong(webConfProvider
				.getParameter(WebConfigurationProvider.REPO_ID));
	}
}
