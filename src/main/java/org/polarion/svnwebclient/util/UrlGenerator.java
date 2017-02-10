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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class UrlGenerator {
	protected String url;
	protected List parameters = new ArrayList();
	private String anchor = null;

	public UrlGenerator(String url) {
		this.url = url;
	}

	public void addParameter(String name, String value) {
		this.parameters.add(name + "=" + value);
	}

	public void addParameter(String name) {
		this.parameters.add(name);
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getUrl() {
		StringBuffer ret = new StringBuffer(this.url);
		if (this.parameters.size() > 0) {
			ret.append("?");
			for (Iterator i = this.parameters.iterator(); i.hasNext();) {
				String parameter = (String) i.next();
				ret.append(parameter);
				if (i.hasNext()) {
					ret.append("&");
				}
			}
		}
		if (anchor != null) {
			ret.append("#" + anchor);
		}
		return ret.toString();
	}
}
