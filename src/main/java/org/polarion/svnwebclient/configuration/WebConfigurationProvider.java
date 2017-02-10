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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class WebConfigurationProvider {
	public static final String DISPLAY_NAME = "DisplayName";
	public static final String BASIC_AUTH = "BasicAuth";
	public static final String BASIC_REALM = "BasicRealm";
	public static final String FORCED_HTTP_AUTH = "ForcedHttpAuth";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String SVN_CONNECTIONS_COUNT = "SvnConnectionsCount";
	public static final String VERSIONS_COUNT = "VersionsCount";
	public static final String PATH_AUTODETECT = "PathAutodetect";
	public static final String TRUNK_NAME = "TrunkName";
	public static final String BRANCHES_NAME = "BranchesName";
	public static final String TAGS_NAME = "TagsName";
	public static final String DEFAULT_ENCODING = "DefaultEncoding";
	public static final String SHOW_STACK_TRACE = "ShowStackTrace";
	public static final String CACHE_PAGE_SIZE = "CachePageSize";
	public static final String CACHE_PREFETCH_MESSAGES_COUNT = "CachePrefetchMessagesCount";

	public static final String ROOT_URL = "RootUrl";

	public static final String PROTOCOL_KEY_FILE = "ProtocolKeyFile";
	public static final String PROTOCOL_PASS_PHRASE = "ProtocolPassPhrase";
	public static final String PROTOCOL_PORT_NUMBER = "ProtocolPortNumber";
	// proxy
	public static final String PROXY_SUPPORTED = "ProxySupported";
	public static final String PROXY_HOST = "ProxyHost";
	public static final String PROXY_PORT_NUMBER = "ProxyPortNumber";
	public static final String PROXY_USER_NAME = "ProxyUserName";
	public static final String PROXY_PASSWORD = "ProxyPassword";

	public static final String CHARACTER_ENCODINGS = "CharacterEncodings";

	// mime-types
	public static final String BINARY_MIME_TYPES = "BinaryMimeTypes";
	public static final String TEXT_MIME_TYPES = "TextMimeTypes";

	// picker
	public static final String PICKER_MULTI_URL = "PickerMultiUrl";

	// email settings
	public static final String EMAIL_FROM = "EmailFrom";
	public static final String EMAIL_TO = "EmailTo";
	public static final String EMAIL_PROJECT_NAME = "EmailProjectName";
	public static final String EMAIL_HOST = "EmailHost";
	public static final String EMAIL_PORT = "EmailPort";

	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";

	public static final String REPO_ID = "repoId";

	protected Map<String, String> parameters = new HashMap<String, String>();

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setParameters(Properties properties) {
		this.parameters = new HashMap<String, String>();

		for (Entry<Object, Object> prop : properties.entrySet()) {
			parameters.put((String) prop.getKey(), (String) prop.getValue());
		}
	}

	public Map<String, String> getParameters() {
		return this.parameters;
	}

	public void setParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	public String getParameter(String name) {
		return this.parameters.get(name);

	}
}
