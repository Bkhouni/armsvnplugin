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

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public interface RequestParameters {
	String URL = "url";
	String LOCATION = "location";
	String CREV = "crev";
	String PEGREV = "pegrev";
	String REV = "rev";
	String REVCOUNT = "revcount";
	String VIEW = "view";
	String ATTACHMENT = "attachment";
	String STARTREV = "startrev";
	String ENDREV = "endrev";
	String PREFIX = "prefix";
	String SUFFIX = "suffix";
	String TYPE = "type";
	String SORT_ORDER = "sortorder";
	String SORT_FIELD = "sortfield";
	String ACTION = "action";
	String LINE = "line";

	String RETRY_AGAIN = "retryagain";
	String ERROR_MESSAGE = "errormessage";

	String USERNAME = "username";
	String PASSWORD = "password";
	String VALUE_TRUE = "true";
	String VALUE_FALSE = "false";

	String START_REVISION = "startrevision";
	String END_REVISION = "endrevision";
	String REV_COUNT = "revcount";
	String HIDE_POLARION_COMMIT = "hidepolarioncommit";

	// picker
	String CONTENT_MODE_TYPE = "contentmodetype";
	String SINGLE_REVISION = "singlerevision";
	String MULTI_URL_SELECTION = "multiurlselection";

	String CHARACTER_ENCODING = "encoding";

	// session params
	String DEFAULT_CHARACTER_ENCODING = "defaultCharacterEncoding";

	// pdf viewer
	String CONTEXT_PATH = "contextPath";
}
