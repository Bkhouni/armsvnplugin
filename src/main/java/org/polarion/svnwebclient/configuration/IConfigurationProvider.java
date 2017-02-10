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

import org.polarion.svncommons.commentscache.configuration.ProxySettings;

import java.util.Set;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public interface IConfigurationProvider {
	int SVN_SSH = 1;
	int SSL = 2;
	int HTTP = 0;

	void checkConfiguration() throws ConfigurationException;

	String getUsername();

	String getPassword();

	long getSvnConnectionsCount();

	long getVersionsCount();

	boolean isPathAutodetect();

	String getTrunkName();

	String getBranchesName();

	String getTagsName();

	String getDefaultEncoding();

	boolean isShowStackTrace();

	long getCachePageSize();

	long getCachePrefetchMessagesCount();

	String getBasicRealm();

	boolean isBasicAuth();

	boolean isForcedHttpAuth();

	String getRootUrl();

	long getRepoId();

	boolean isHidePolarionCommit();

	ConfigurationError getConfigurationError();

	String getProtocolKeyFile();

	String getProtocolPassPhrase();

	int getProtocolPortNumber();

	int getProtocolType();

	ProxySettings getProxy();

	String getRepositoryLocation(String repositoryName);

	Set getCharacterEncodings();

	Set getBinaryMimeTypes();

	Set getTextMimeTypes();

	// mail settings
	String getEmailFrom();

	String getEmailTo();

	String getEmailHost();

	String getEmailPort();

	String getEmailProject();

	String getDisplayName();
}
