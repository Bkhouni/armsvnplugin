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
package org.polarion.svnwebclient.web;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AttributeStorage {
	protected static AttributeStorage handler = new AttributeStorage();
	protected static String RESTRICTED_MAP = "restrictedMap";

	protected AttributeStorage() {
	}

	public void addParameter(HttpSession session, String key, Object value) {
		Map restricedMap = this.getRestrictedMap(session);
		restricedMap.put(key, value);
	}

	public Object getParameter(HttpSession session, String key) {
		Map restricedMap = this.getRestrictedMap(session);
		return restricedMap.get(key);
	}

	public static AttributeStorage getInstance() {
		return handler;
	}

	public void cleanSession(HttpSession session) {
		Map restricedMap = this.getRestrictedMap(session);
		restricedMap.clear();
		session.setAttribute(SystemInitializing.ORIGINAL_URL, null);
	}

	protected synchronized Map getRestrictedMap(HttpSession session) {
		Map restricedMap = (Map) session
				.getAttribute(AttributeStorage.RESTRICTED_MAP);
		if (restricedMap == null) {
			restricedMap = Collections.synchronizedMap(new HashMap());
			session.setAttribute(AttributeStorage.RESTRICTED_MAP, restricedMap);
		}
		return restricedMap;
	}
}
