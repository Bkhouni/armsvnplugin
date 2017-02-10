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

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class UrlUtil {

	public static String append(String parent, String child) {
		StringBuffer ret = new StringBuffer();
		ret.append(UrlUtil.trim(parent));
		String data = UrlUtil.trim(child);
		if (data.startsWith("/") && (data.length() > 1)) {
			data = data.substring(1);
		}
		if (data.length() > 0) {
			ret.append("/");
			ret.append(data);
		}
		return ret.toString();
	}

	public static String trim(String url) {
		String ret = url.trim();
		if (ret.endsWith("/")) {
			ret = ret.substring(0, url.length() - 1);
		}
		return ret;
	}

	public static String encode(String url) {
		StringBuffer res = new StringBuffer();
		try {
			if (url.indexOf(" ") != -1) {
				String[] strs = url.split(" ");
				for (int i = 0; i < strs.length; i++) {
					String str = strs[i];
					res.append(URLEncoder.encode(str, "UTF-8"));
					if (i < strs.length - 1) {
						res.append("%20");
					}
				}
			} else {
				res.append(URLEncoder.encode(url, "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(UrlUtil.class).error(e, e);
		}
		return res.toString();
	}

	public static String decode(String url) {
		String ret = null;
		try {
			ret = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(UrlUtil.class).error(e, e);
		}
		return ret;
	}

	public static List getPathChain(String path) {
		List ret = new ArrayList();
		if ((path != null) && (path.length() > 0)) {
			String pathUrl = "";
			String[] pathElements = path.split("/");
			for (int i = 0; i < pathElements.length; i++) {
				if (i == 0) {
					pathUrl = pathElements[i];
				} else {
					pathUrl += "/" + pathElements[i];
				}
				ret.add(pathUrl);
			}
		}
		return ret;
	}

	public static String getLastPathElement(String path) {
		int index = path.lastIndexOf("/");
		if (index == -1) {
			return path;
		} else {
			if (index < (path.length() - 1)) {
				return path.substring(index + 1);
			} else {
				return "";
			}
		}
	}

	public static String getPreviousFullPath(String path) {
		int index = path.lastIndexOf("/");
		if (index == -1) {
			return "";
		} else {
			return path.substring(0, index);
		}
	}

	public static String getNextLevelFullPath(String path) {
		int index = path.indexOf("/");
		if (index == -1) {
			return "";
		} else {
			if (index == (path.length() - 1)) {
				return "";
			} else {
				return path.substring(index + 1);
			}
		}
	}

	public static String getFilenameAttribute(String fileName,
			HttpServletRequest request) {
		String ret = "filename";

		String encodedFileName = Encoder.encodeUTF8ASCII(fileName);
		if (fileName.equals(encodedFileName)) {
			if (fileName.indexOf(" ") == -1) {
				ret += "=" + fileName;
			} else {
				ret += "=\"" + fileName + "\"";
			}
		} else {
			encodedFileName = encodedFileName.replaceAll(" ", "%20");
			String userAgent = request.getHeader("User-Agent");
			if (userAgent == null) {
				userAgent = "";
			} else {
				userAgent = userAgent.toLowerCase();
			}
			if ((userAgent.indexOf("msie 6.0") != -1)
					|| (userAgent.indexOf("msie 7.0") != -1)
					|| (userAgent.indexOf("msie 8.0") != -1)) {
				ret += "=" + encodedFileName;
			} else {
				ret += "*=\"UTF-8''" + encodedFileName + "\"";
			}
		}
		return ret;
	}
}
