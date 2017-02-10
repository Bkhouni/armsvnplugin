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
package org.polarion.svnwebclient.web.support;

import org.apache.log4j.Logger;

public class MailSettingsProvider {

	protected String emailFrom;
	protected String emailTo;
	protected String emailHost;
	protected String emailPort;
	protected String emailProject;

	protected boolean isCorrectlyInitialized;

	protected static MailSettingsProvider instance;

	protected MailSettingsProvider(String emailFrom, String emailTo,
			String emailHost, String emailPort, String emailProject) {
		super();
		this.emailFrom = emailFrom;
		this.emailTo = emailTo;
		this.emailHost = emailHost;
		this.emailPort = emailPort;
		this.emailProject = emailProject;

		if (this.emailFrom != null && this.emailTo != null
				&& this.emailPort != null && this.emailHost != null
				&& this.emailProject != null) {
			this.isCorrectlyInitialized = true;
		} else {
			this.isCorrectlyInitialized = false;
			Logger log = Logger.getLogger(MailSettingsProvider.class);
			log.info("Email setings isn't correctly initialized. "
					+ "EmailFrom: " + this.emailFrom + ", emailTo: " + emailTo
					+ ", emailHost: " + emailHost + "emailPort: " + emailPort
					+ "emailProject: " + emailProject);
		}
	}

	public static MailSettingsProvider getInstance() {
		return instance;
	}

	public static void init(String emailFrom, String emailTo, String emailHost,
			String emailPort, String emailProject) {
		instance = new MailSettingsProvider(emailFrom, emailTo, emailHost,
				emailPort, emailProject);
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public String getHost() {
		return emailHost;
	}

	public String getPort() {
		return emailPort;
	}

	public String getProjectName() {
		return emailProject;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public boolean isCorrectlyInitialized() {
		return this.isCorrectlyInitialized;
	}

}
