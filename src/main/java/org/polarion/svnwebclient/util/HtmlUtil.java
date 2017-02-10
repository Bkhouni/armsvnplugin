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
package org.polarion.svnwebclient.util;

import com.kintosoft.svnwebclient.jira.SWCUtils;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class HtmlUtil {
	public static String encodeWithSpace(String inStr) {
		return HtmlUtil.encode(inStr, true, true);
	}

	public static String encode(String inStr) {
		return HtmlUtil.encode(inStr, false, true);
	}

	public static String encode(String inStr, boolean issues) {
		return HtmlUtil.encode(inStr, false, issues);
	}

	protected static String encode(String inStr, boolean changeSpace,
			boolean issues) {
		if (inStr == null || inStr.length() == 0) {
			return "";
		}
		StringBuffer retStr = new StringBuffer();
		int i = 0;
		while (i < inStr.length()) {
			if (inStr.charAt(i) == '&') {
				retStr.append("&amp;");
			} else if (inStr.charAt(i) == '<') {
				retStr.append("&lt;");
			} else if (inStr.charAt(i) == '>') {
				retStr.append("&gt;");
			} else if (inStr.charAt(i) == '\"') {
				retStr.append("&quot;");
			} else if (inStr.charAt(i) == ' ') {
				retStr.append("&nbsp;");
			} else if (changeSpace && inStr.charAt(i) == '\t') {
				retStr.append("&nbsp;&nbsp;&nbsp;&nbsp;");
			} else
				retStr.append(inStr.charAt(i));
			i++;
		}
		if (issues) {
			return SWCUtils.replaceIssueHtmlLinks(retStr.toString());
		} else {
			return retStr.toString();
		}
	}
}
